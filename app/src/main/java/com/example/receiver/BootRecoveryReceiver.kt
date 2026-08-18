package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.BoosterPreferences
import com.example.util.ShizukuManager
import com.example.util.shizuku.DisplayScaleController
import com.example.util.shizuku.GamerDndController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootRecoveryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.d(TAG, "Boot or package update detected. Checking system state fail-safe...")
            val prefs = BoosterPreferences(context)

            val activePkg = prefs.getActiveBoostedPackage()
            val gmsSuspended = prefs.isGoogleServicesSuspended()
            val customScaleActive = prefs.isCustomDisplayScaleActive()
            val dndActive = prefs.isDndActive()

            if (activePkg != null || gmsSuspended || customScaleActive || dndActive) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (activePkg != null) {
                            ShizukuManager.restoreGameGraphicsDriver(activePkg)
                        }
                        if (gmsSuspended) {
                            ShizukuManager.restoreGooglePlayServices()
                            prefs.setGoogleServicesSuspended(false)
                        }
                        if (customScaleActive) {
                            DisplayScaleController.resetDisplayScale(context)
                        }
                        if (dndActive) {
                            GamerDndController.restoreDndSettings(context, ShizukuManager.isAuthorized)
                        }
                        val hibernated = prefs.getCurrentlyHibernatedPackages().toList()
                        if (hibernated.isNotEmpty()) {
                            ShizukuManager.restoreHibernatedPackages(hibernated)
                            prefs.setCurrentlyHibernatedPackages(emptySet())
                        }
                        prefs.setActiveBoostedPackage(null)
                        Log.d(TAG, "Fail-safe recovery successfully restored system settings.")
                    } catch (e: Exception) {
                        Log.e(TAG, "Fail-safe recovery error", e)
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "BootRecoveryReceiver"
    }
}
