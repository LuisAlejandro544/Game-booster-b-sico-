package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.service.GameWatcherService
import com.example.util.shizuku.DisplayScaleController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Emergency BroadcastReceiver triggered by the persistent panic button in the notification shade.
 * Instantly restores 100% factory resolution and density.
 */
class EmergencyResetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == ACTION_EMERGENCY_RESET) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    DisplayScaleController.resetDisplayScale(context, true)
                } finally {
                    pendingResult.finish()
                }
            }
            Toast.makeText(context, "✅ Pantalla restablecida a valores de fábrica nativos", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val ACTION_EMERGENCY_RESET = "com.example.action.EMERGENCY_DISPLAY_RESET"
    }
}
