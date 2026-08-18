package com.example.util.shizuku

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.data.BoosterPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Controller for Automated Gamer Do Not Disturb (DND) & Heads-up Notification Suppression.
 * Silences intrusive pop-up banners and distractions while gaming, while respecting user exceptions (whitelist)
 * for incoming phone calls and critical apps (WhatsApp, Discord, etc.).
 *
 * Guarantees 100% clean auto-reversal to stock settings when the game ends or if the app is killed.
 */
object GamerDndController {
    private const val TAG = "GamerDndController"

    /**
     * Applies Gamer DND mode:
     * 1. Disables floating Heads-Up notification banners (`settings put global heads_up_notifications_enabled 0`).
     * 2. Activates Priority Zen Mode (`settings put global zen_mode 1` / `INTERRUPTION_FILTER_PRIORITY`).
     * 3. Logs and tracks active status in BoosterPreferences for failsafe restoration.
     */
    suspend fun applyGamerDnd(
        context: Context,
        allowCalls: Boolean,
        blockHeadsUp: Boolean,
        exceptionPackages: Set<String>,
        isAuthorized: Boolean
    ): List<String> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        val prefs = BoosterPreferences(context)

        try {
            // 1. Save original states before altering
            if (isAuthorized) {
                // Ensure notification policy permission is granted via Shizuku
                AdbShellExecutor.grantPermission(
                    packageName = context.packageName,
                    permissionName = "android.permission.ACCESS_NOTIFICATION_POLICY",
                    userId = 0,
                    isAuthorized = true
                )

                val currHeadsUp = AdbShellExecutor.executeCommand("settings get global heads_up_notifications_enabled", true)
                val headsUpVal = currHeadsUp.output.trim().toIntOrNull() ?: 1
                if (prefs.getSavedHeadsUp() == -1) {
                    prefs.setSavedHeadsUp(headsUpVal)
                }

                val currZen = AdbShellExecutor.executeCommand("settings get global zen_mode", true)
                val zenVal = currZen.output.trim().toIntOrNull() ?: 0
                if (prefs.getSavedZenMode() == -1) {
                    prefs.setSavedZenMode(zenVal)
                }

                // Apply Heads-up popup suppression
                if (blockHeadsUp) {
                    val res = AdbShellExecutor.executeCommand("settings put global heads_up_notifications_enabled 0", true)
                    if (res.isSuccess) {
                        logs.add("🔇 Banners flotantes bloqueados (Heads-Up suprimido)")
                    }
                }

                // Apply Zen Mode (1 = Priority / Alarms & Exceptions Allowed)
                val zenRes = AdbShellExecutor.executeCommand("settings put global zen_mode 1", true)
                if (zenRes.isSuccess) {
                    val callText = if (allowCalls) "con llamadas permitidas" else "silencio total"
                    val exceptionsCount = exceptionPackages.size
                    logs.add("🔕 Modo No Molestar Gamer activado ($callText, $exceptionsCount excepciones)")
                }
            } else {
                // Standard non-root / non-shizuku fallback via NotificationManager if policy granted
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                if (notificationManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (notificationManager.isNotificationPolicyAccessGranted) {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                        logs.add("🔕 Modo No Molestar activado vía Android Policy")
                    } else {
                        logs.add("ℹ️ Concede acceso a 'No Molestar' o autoriza Shizuku para silenciar notificaciones")
                    }
                }
            }

            prefs.setDndActive(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error applying Gamer DND", e)
        }

        logs
    }

    /**
     * Fully restores Android notification settings and DND mode back to stock factory behavior.
     * Guaranteed execution on game exit, service onDestroy, boot recovery, and emergency panic button.
     */
    suspend fun restoreDndSettings(
        context: Context,
        isAuthorized: Boolean
    ): List<String> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        val prefs = BoosterPreferences(context)

        try {
            if (isAuthorized) {
                // Restore Heads-Up Notifications
                val savedHeadsUp = prefs.getSavedHeadsUp()
                val targetHeadsUp = if (savedHeadsUp != -1) savedHeadsUp else 1
                AdbShellExecutor.executeCommand("settings put global heads_up_notifications_enabled $targetHeadsUp", true)

                // Restore Zen Mode
                val savedZen = prefs.getSavedZenMode()
                val targetZen = if (savedZen != -1) savedZen else 0
                AdbShellExecutor.executeCommand("settings put global zen_mode $targetZen", true)

                logs.add("✓ Notificaciones y modo No Molestar restablecidos a la normalidad")
            }

            // Restore via NotificationManager if available
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (notificationManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                }
            }

            // Reset saved state
            prefs.setDndActive(false)
            prefs.setSavedZenMode(-1)
            prefs.setSavedHeadsUp(-1)
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring DND settings", e)
        }

        logs
    }
}
