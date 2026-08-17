package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.BoosterPreferences
import com.example.model.DisplayResolutionScale
import com.example.model.GraphicsDriver
import com.example.util.ShizukuManager
import com.example.util.SystemInfoHelper
import com.example.util.shizuku.DisplayScaleController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameWatcherService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var monitoringJob: Job? = null

    private lateinit var prefs: BoosterPreferences

    private var targetPackage: String? = null
    private var targetGameTitle: String = "Juego"
    private var appliedDriver: GraphicsDriver = GraphicsDriver.SYSTEM_DEFAULT
    private var appliedDisplayScale: DisplayResolutionScale = DisplayResolutionScale.NATIVE_100
    private var hibernateGoogle: Boolean = false
    private var deepHibernate: Boolean = true

    private var hasBeenInForeground = false
    private var consecutiveBackgroundChecks = 0

    override fun onCreate() {
        super.onCreate()
        prefs = BoosterPreferences(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val action = intent.action
        if (action == ACTION_STOP_WATCHER) {
            Log.d(TAG, "Stop action requested from user notification")
            stopSelf()
            return START_NOT_STICKY
        }

        targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE)
        targetGameTitle = intent.getStringExtra(EXTRA_TARGET_TITLE) ?: "Juego"
        val driverName = intent.getStringExtra(EXTRA_GRAPHICS_DRIVER) ?: GraphicsDriver.SYSTEM_DEFAULT.name
        appliedDriver = try {
            GraphicsDriver.valueOf(driverName)
        } catch (_: Exception) {
            GraphicsDriver.SYSTEM_DEFAULT
        }
        val scaleName = intent.getStringExtra(EXTRA_DISPLAY_SCALE) ?: DisplayResolutionScale.NATIVE_100.name
        appliedDisplayScale = try {
            DisplayResolutionScale.valueOf(scaleName)
        } catch (_: Exception) {
            DisplayResolutionScale.NATIVE_100
        }
        hibernateGoogle = intent.getBooleanExtra(EXTRA_HIBERNATE_GOOGLE, false)
        deepHibernate = intent.getBooleanExtra(EXTRA_DEEP_HIBERNATE, true)

        if (targetPackage.isNullOrBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Start Foreground Notification
        val notification = buildForegroundNotification("🔥 Modo Turbo Gamer Activo: $targetGameTitle", "Optimizando CPU, GPU, resolución y RAM en vivo")
        startForeground(NOTIFICATION_ID, notification)

        // Save active state to prefs
        prefs.setActiveBoostedPackage(targetPackage)

        // Start background watch loop
        startGameMonitoringLoop()

        return START_STICKY
    }

    private fun startGameMonitoringLoop() {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            val pkg = targetPackage ?: return@launch

            // 1. Initial Injection: Apply GPU driver
            ShizukuManager.applyGameGraphicsDriver(pkg, appliedDriver)

            // 1.2 Apply Display Resolution & DPI Scale with 5-layer failsafe
            if (appliedDisplayScale != DisplayResolutionScale.NATIVE_100) {
                DisplayScaleController.applyDisplayScale(
                    context = this@GameWatcherService,
                    scale = appliedDisplayScale,
                    isAuthorized = ShizukuManager.isAuthorized
                )
            }

            // 1.5 Launch Floating HUD Overlay if permitted and enabled
            if (android.provider.Settings.canDrawOverlays(this@GameWatcherService) && prefs.getGameOverlayHud(pkg)) {
                GameOverlayService.start(
                    context = this@GameWatcherService,
                    packageName = pkg,
                    gameTitle = targetGameTitle,
                    driver = appliedDriver,
                    displayScale = appliedDisplayScale
                )
            }

            // 2. Hibernation: Background tasks
            if (deepHibernate) {
                val installed = SystemInfoHelper.getInstalledAppsAndGames(this@GameWatcherService)
                val bgPkgs = installed.map { it.packageName }.filter { it != pkg }
                ShizukuManager.hibernateBackgroundPackages(bgPkgs, pkg)
            }

            // 3. Hibernation: Google Play Services if requested
            if (hibernateGoogle) {
                prefs.setGoogleServicesSuspended(true)
                ShizukuManager.suspendGooglePlayServices()
            }

            // 4. Monitoring Loop with Heartbeat Watchdog ticks
            while (isActive) {
                delay(3000L)

                val isFg = ShizukuManager.isAppInForeground(pkg)
                if (isFg) {
                    hasBeenInForeground = true
                    consecutiveBackgroundChecks = 0
                    // Layer 1 Failsafe: keep shell watchdog alive while user is playing
                    DisplayScaleController.sendHeartbeatTick(ShizukuManager.isAuthorized)
                } else {
                    if (hasBeenInForeground) {
                        consecutiveBackgroundChecks++
                        // If game is not in foreground for 2 consecutive checks (~6 seconds), user switched away/exited
                        if (consecutiveBackgroundChecks >= 2) {
                            Log.d(TAG, "Game $pkg is no longer in foreground. Exiting turbo watcher and restoring system...")
                            break
                        }
                    }
                }
            }

            // Finished or user exited game -> Restore system and stop service
            restoreAllSystemSettings()
            stopSelf()
        }
    }

    private suspend fun restoreAllSystemSettings() {
        // Stop Floating In-Game HUD
        GameOverlayService.stop(this@GameWatcherService)

        // Restore Display Scale & Density (Layer 3)
        DisplayScaleController.resetDisplayScale(this@GameWatcherService, ShizukuManager.isAuthorized)

        val pkg = targetPackage
        if (pkg != null) {
            ShizukuManager.restoreGameGraphicsDriver(pkg)
        }

        if (prefs.isGoogleServicesSuspended() || hibernateGoogle) {
            ShizukuManager.restoreGooglePlayServices()
            prefs.setGoogleServicesSuspended(false)
        }

        if (deepHibernate) {
            val installed = SystemInfoHelper.getInstalledAppsAndGames(this@GameWatcherService)
            val bgPkgs = installed.map { it.packageName }
            ShizukuManager.restoreHibernatedPackages(bgPkgs)
        }

        prefs.setActiveBoostedPackage(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.launch {
            restoreAllSystemSettings()
        }
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Game Booster Turbo Sentinel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitorea la sesión de juego y restaura la configuración de Android al salir"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(title: String, text: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, GameWatcherService::class.java).apply {
            action = ACTION_STOP_WATCHER
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Detener & Restaurar", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val TAG = "GameWatcherService"
        const val CHANNEL_ID = "game_watcher_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START_WATCHER = "com.example.action.START_WATCHER"
        const val ACTION_STOP_WATCHER = "com.example.action.STOP_WATCHER"

        const val EXTRA_TARGET_PACKAGE = "extra_target_package"
        const val EXTRA_TARGET_TITLE = "extra_target_title"
        const val EXTRA_GRAPHICS_DRIVER = "extra_graphics_driver"
        const val EXTRA_DISPLAY_SCALE = "extra_display_scale"
        const val EXTRA_HIBERNATE_GOOGLE = "extra_hibernate_google"
        const val EXTRA_DEEP_HIBERNATE = "extra_deep_hibernate"

        fun start(
            context: Context,
            packageName: String,
            gameTitle: String,
            driver: GraphicsDriver,
            hibernateGoogle: Boolean,
            deepHibernate: Boolean,
            displayScale: DisplayResolutionScale = DisplayResolutionScale.NATIVE_100
        ) {
            val intent = Intent(context, GameWatcherService::class.java).apply {
                action = ACTION_START_WATCHER
                putExtra(EXTRA_TARGET_PACKAGE, packageName)
                putExtra(EXTRA_TARGET_TITLE, gameTitle)
                putExtra(EXTRA_GRAPHICS_DRIVER, driver.name)
                putExtra(EXTRA_DISPLAY_SCALE, displayScale.name)
                putExtra(EXTRA_HIBERNATE_GOOGLE, hibernateGoogle)
                putExtra(EXTRA_DEEP_HIBERNATE, deepHibernate)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, GameWatcherService::class.java).apply {
                action = ACTION_STOP_WATCHER
            }
            context.startService(intent)
        }
    }
}
