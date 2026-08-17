package com.example.util.shizuku

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.data.BoosterPreferences
import com.example.model.DisplayResolutionScale
import com.example.model.DisplayScaleMetrics
import com.example.receiver.EmergencyResetReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 5-LAYER FAILSAFE CONTROLLER FOR DISPLAY RESOLUTION & DPI SCALING.
 *
 * Layer 1: Detached Shell Watchdog (Dead-Man's Switch) with 35s timeout in /data/local/tmp.
 * Layer 2: Persistent Emergency Panic Reset Notification with immediate broadcast action.
 * Layer 3: Boot Recovery & Process Lifecycle auto-reset hooks.
 * Layer 4: Safe Proportional Scaling Matrix (Even-pixel width/height & proportional DPI clamping).
 * Layer 5: In-App 15-second test verification with auto-revert countdown.
 */
object DisplayScaleController {
    private const val TAG = "DisplayScaleController"

    const val EMERGENCY_CHANNEL_ID = "game_booster_emergency_display"
    const val EMERGENCY_NOTIFICATION_ID = 9911
    const val HEARTBEAT_FILE = "/data/local/tmp/game_booster_heartbeat.tmp"

    /**
     * Queries physical screen resolution and density from system WindowManager or ADB shell.
     */
    suspend fun getPhysicalDisplayMetrics(context: Context, isAuthorized: Boolean): DisplayScaleMetrics = withContext(Dispatchers.IO) {
        val prefs = BoosterPreferences(context)
        var origW = prefs.getPhysicalDisplayWidth()
        var origH = prefs.getPhysicalDisplayHeight()
        var origD = prefs.getPhysicalDisplayDensity()

        if (origW <= 0 || origH <= 0 || origD <= 0) {
            // 1. Attempt elevated query via ADB shell
            if (isAuthorized) {
                try {
                    val sizeRes = AdbShellExecutor.executeCommand("wm size", true)
                    val densityRes = AdbShellExecutor.executeCommand("wm density", true)

                    // Parse "Physical size: 1080x2400"
                    val sizeMatch = Regex("""Physical size:\s*(\d+)x(\d+)""").find(sizeRes.output)
                    if (sizeMatch != null) {
                        origW = sizeMatch.groupValues[1].toInt()
                        origH = sizeMatch.groupValues[2].toInt()
                    }

                    // Parse "Physical density: 420"
                    val densityMatch = Regex("""Physical density:\s*(\d+)""").find(densityRes.output)
                    if (densityMatch != null) {
                        origD = densityMatch.groupValues[1].toInt()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed parsing wm size from shell: ${e.message}")
                }
            }

            // 2. Fallback to Android DisplayMetrics
            if (origW <= 0 || origH <= 0 || origD <= 0) {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                if (wm != null) {
                    val dm = DisplayMetrics()
                    @Suppress("DEPRECATION")
                    wm.defaultDisplay.getRealMetrics(dm)
                    origW = dm.widthPixels
                    origH = dm.heightPixels
                    origD = dm.densityDpi
                } else {
                    origW = 1080
                    origH = 2400
                    origD = 420
                }
            }

            // Cache known physical values
            prefs.setPhysicalDisplayWidth(origW)
            prefs.setPhysicalDisplayHeight(origH)
            prefs.setPhysicalDisplayDensity(origD)
        }

        DisplayScaleMetrics(
            originalWidth = origW,
            originalHeight = origH,
            originalDensity = origD,
            targetWidth = origW,
            targetHeight = origH,
            targetDensity = origD,
            scale = DisplayResolutionScale.NATIVE_100
        )
    }

    /**
     * Layer 4: Calculates even-pixel target width, height, and proportional DPI.
     */
    fun calculateScaledMetrics(
        base: DisplayScaleMetrics,
        scale: DisplayResolutionScale
    ): DisplayScaleMetrics {
        if (scale == DisplayResolutionScale.NATIVE_100) {
            return base.copy(
                targetWidth = base.originalWidth,
                targetHeight = base.originalHeight,
                targetDensity = base.originalDensity,
                scale = scale
            )
        }

        val factor = scale.scaleFactor
        // Even-pixel clamping: width & height must be multiples of 2 for GPU tile pitch alignment
        val targetW = ((base.originalWidth * factor) / 2).toInt() * 2
        val targetH = ((base.originalHeight * factor) / 2).toInt() * 2
        // Exact proportional density to preserve original physical button & touch sizes
        val targetD = (base.originalDensity * factor).toInt().coerceAtLeast(120)

        return base.copy(
            targetWidth = targetW,
            targetHeight = targetH,
            targetDensity = targetD,
            scale = scale
        )
    }

    /**
     * Applies the display resolution and density scaling with all 5 failsafe protections.
     */
    suspend fun applyDisplayScale(
        context: Context,
        scale: DisplayResolutionScale,
        isAuthorized: Boolean
    ): List<String> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        val prefs = BoosterPreferences(context)

        if (scale == DisplayResolutionScale.NATIVE_100) {
            return@withContext resetDisplayScale(context, isAuthorized)
        }

        if (!isAuthorized) {
            logs.add("⚠️ Shizuku no está autorizado para cambiar resolución wm size")
            return@withContext logs
        }

        val base = getPhysicalDisplayMetrics(context, isAuthorized)
        val target = calculateScaledMetrics(base, scale)

        // 1. Layer 1: Start Shell Watchdog Heartbeat
        val initHeartbeatCmd = "mkdir -p /data/local/tmp && echo $(date +%s) > $HEARTBEAT_FILE"
        AdbShellExecutor.executeCommand(initHeartbeatCmd, true)

        // Spawn detached daemon watchdog script
        val watchdogScript = """
            nohup sh -c '
            for i in $(seq 1 720); do
                sleep 5
                if [ ! -f $HEARTBEAT_FILE ]; then
                    wm size reset
                    wm density reset
                    exit 0
                fi
                NOW=${'$'}(date +%s)
                LAST=${'$'}(stat -c %Y $HEARTBEAT_FILE 2>/dev/null || echo 0)
                DIFF=${'$'}((NOW - LAST))
                if [ "${'$'}DIFF" -gt 35 ]; then
                    wm size reset
                    wm density reset
                    rm -f $HEARTBEAT_FILE
                    exit 0
                fi
            done
            wm size reset
            wm density reset
            rm -f $HEARTBEAT_FILE
            ' >/dev/null 2>&1 &
        """.trimIndent()
        AdbShellExecutor.executeCommand(watchdogScript, true)
        logs.add("🛡️ Capa 1: Watchdog de emergencia activado (timeout 35s)")

        // 2. Apply wm size and wm density
        val sizeCmd = "wm size ${target.targetWidth}x${target.targetHeight}"
        val densityCmd = "wm density ${target.targetDensity}"

        val sizeRes = AdbShellExecutor.executeCommand(sizeCmd, true)
        val densityRes = AdbShellExecutor.executeCommand(densityCmd, true)

        if (sizeRes.isSuccess && densityRes.isSuccess) {
            prefs.setCustomDisplayScaleActive(true)
            logs.add("✓ Resolución reducida: ${target.targetWidth}x${target.targetHeight} (${target.scale.tag})")
            logs.add("✓ Densidad proporcional calibrada: ${target.targetDensity} DPI")

            // 3. Layer 2: Show Persistent Emergency Notification with Panic Button
            showEmergencyPanicNotification(context, target)
            logs.add("🚨 Capa 2: Notificación de Pánico y botón de restablecimiento visibles")
        } else {
            logs.add("❌ Error al aplicar resolución: ${sizeRes.error} ${densityRes.error}")
            resetDisplayScale(context, isAuthorized)
        }

        logs
    }

    /**
     * Layer 1 Heartbeat: Updates timestamp in /data/local/tmp/game_booster_heartbeat.tmp.
     */
    suspend fun sendHeartbeatTick(isAuthorized: Boolean) = withContext(Dispatchers.IO) {
        if (!isAuthorized) return@withContext
        AdbShellExecutor.executeCommand("touch $HEARTBEAT_FILE", true)
    }

    /**
     * Restores device resolution and density to 100% factory native.
     */
    suspend fun resetDisplayScale(
        context: Context,
        isAuthorized: Boolean = true
    ): List<String> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        val prefs = BoosterPreferences(context)

        try {
            // Remove heartbeat and reset wm size & density
            val resetCmd = "rm -f $HEARTBEAT_FILE; wm size reset; wm density reset"
            if (isAuthorized) {
                val res = AdbShellExecutor.executeCommand(resetCmd, true)
                if (res.isSuccess) {
                    logs.add("✓ Resolución y DPI restaurados a valores de fábrica nativos (wm size/density reset)")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in resetDisplayScale: ${e.message}")
        }

        prefs.setCustomDisplayScaleActive(false)
        dismissEmergencyPanicNotification(context)
        logs
    }

    /**
     * Layer 2: Shows high-priority persistent emergency notification with one-touch PANIC reset action.
     */
    private fun showEmergencyPanicNotification(context: Context, target: DisplayScaleMetrics) {
        createEmergencyNotificationChannel(context)

        val resetIntent = Intent(context, EmergencyResetReceiver::class.java).apply {
            action = EmergencyResetReceiver.ACTION_EMERGENCY_RESET
        }
        val resetPendingIntent = PendingIntent.getBroadcast(
            context,
            9912,
            resetIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, EMERGENCY_CHANNEL_ID)
            .setContentTitle("🚨 Pantalla Escalada: ${target.targetWidth}x${target.targetHeight}")
            .setContentText("Modo ${target.scale.tag} activo. Toca para volver a 100% nativo al instante.")
            .setSmallIcon(android.R.drawable.ic_menu_crop)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(android.R.drawable.ic_delete, "RESTABLECER PANTALLA (PÁNICO)", resetPendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(EMERGENCY_NOTIFICATION_ID, notification)
    }

    private fun dismissEmergencyPanicNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.cancel(EMERGENCY_NOTIFICATION_ID)
    }

    private fun createEmergencyNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                EMERGENCY_CHANNEL_ID,
                "Restablecimiento de Pantalla de Emergencia",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Permite restablecer la resolución de pantalla al instante ante cualquier inconveniente"
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }
}
