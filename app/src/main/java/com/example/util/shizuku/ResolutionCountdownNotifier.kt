package com.example.util.shizuku

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.model.DisplayResolutionScale

/**
 * Handles real-time system countdown toasts during resolution and DPI test mode.
 * Operates on the Main Looper, cancels previous toasts to prevent Android Toast queuing lag,
 * and remains visible regardless of Do Not Disturb (DND) modes.
 */
object ResolutionCountdownNotifier {
    private var activeToast: Toast? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun showCountdownTick(context: Context, secondsLeft: Int, scale: DisplayResolutionScale) {
        mainHandler.post {
            try {
                activeToast?.cancel()
                val msg = "⏱️ Probando ${scale.tag}: $secondsLeft seg (Reversión auto si no confirmas)"
                val toast = Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT)
                activeToast = toast
                toast.show()
            } catch (_: Exception) {}
        }
    }

    fun showConfirmed(context: Context, scale: DisplayResolutionScale) {
        mainHandler.post {
            try {
                activeToast?.cancel()
                val toast = Toast.makeText(
                    context.applicationContext,
                    "✓ Escala ${scale.tag} confirmada",
                    Toast.LENGTH_SHORT
                )
                activeToast = toast
                toast.show()
            } catch (_: Exception) {}
        }
    }

    fun showReverted(context: Context) {
        mainHandler.post {
            try {
                activeToast?.cancel()
                val toast = Toast.makeText(
                    context.applicationContext,
                    "⏱️ Tiempo agotado: 100% Nativo restaurado",
                    Toast.LENGTH_SHORT
                )
                activeToast = toast
                toast.show()
            } catch (_: Exception) {}
        }
    }

    fun cancel() {
        mainHandler.post {
            try {
                activeToast?.cancel()
                activeToast = null
            } catch (_: Exception) {}
        }
    }
}
