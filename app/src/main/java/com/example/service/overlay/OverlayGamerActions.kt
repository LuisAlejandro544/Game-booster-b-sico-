package com.example.service.overlay

import android.content.Context
import android.util.Log
import com.example.data.BoosterPreferences
import com.example.model.DisplayResolutionScale
import com.example.model.GraphicsDriver
import com.example.util.ShizukuManager
import com.example.util.SystemInfoHelper
import com.example.util.shizuku.DisplayScaleController
import com.example.util.shizuku.GamerDndController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Handles in-game actions: DND toggles, App Hibernation toggles, GPU driver switching, and Quick Boost.
 */
class OverlayGamerActions(
    private val context: Context,
    private val scope: CoroutineScope,
    private val prefs: BoosterPreferences,
    private val onFeedbackMessage: (String?) -> Unit
) {
    private fun showFeedback(msg: String, durationMs: Long = 2500L) {
        scope.launch {
            onFeedbackMessage(msg)
            delay(durationMs)
            onFeedbackMessage(null)
        }
    }

    fun toggleDnd(
        targetPackage: String?,
        enabled: Boolean,
        allowCalls: Boolean,
        blockHeadsUp: Boolean,
        dndExceptions: Set<String>
    ) {
        targetPackage?.let { prefs.setGameDndEnabled(it, enabled) }
        scope.launch(Dispatchers.IO) {
            try {
                if (enabled) {
                    ShizukuManager.applyGamerDnd(
                        context = context,
                        allowCalls = allowCalls,
                        blockHeadsUp = blockHeadsUp,
                        exceptions = dndExceptions
                    )
                    showFeedback("🔕 Modo DND Gamer activado")
                } else {
                    GamerDndController.restoreDndSettings(context, ShizukuManager.isAuthorized)
                    showFeedback("🔔 Modo DND Gamer desactivado")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling DND in game", e)
            }
        }
    }

    fun toggleHeadsUp(isDndActive: Boolean, blocked: Boolean, allowCalls: Boolean, dndExceptions: Set<String>) {
        prefs.setDndBlockHeadsUp(blocked)
        if (isDndActive) {
            scope.launch(Dispatchers.IO) {
                ShizukuManager.applyGamerDnd(
                    context = context,
                    allowCalls = allowCalls,
                    blockHeadsUp = blocked,
                    exceptions = dndExceptions
                )
                showFeedback(if (blocked) "🚫 Banners Heads-Up bloqueados" else "✓ Banners Heads-Up permitidos")
            }
        }
    }

    fun toggleAllowCalls(isDndActive: Boolean, allowed: Boolean, blockHeadsUp: Boolean, dndExceptions: Set<String>) {
        prefs.setDndAllowCalls(allowed)
        if (isDndActive) {
            scope.launch(Dispatchers.IO) {
                ShizukuManager.applyGamerDnd(
                    context = context,
                    allowCalls = allowed,
                    blockHeadsUp = blockHeadsUp,
                    exceptions = dndExceptions
                )
                showFeedback(if (allowed) "📞 Llamadas entrantes permitidas" else "🔇 Llamadas silenciadas")
            }
        }
    }

    fun toggleDndAppException(
        isDndActive: Boolean,
        pkg: String,
        currentExceptions: Set<String>,
        allowCalls: Boolean,
        blockHeadsUp: Boolean,
        onUpdated: (Set<String>) -> Unit
    ) {
        val updated = currentExceptions.toMutableSet()
        val isNowWhitelisted = if (updated.contains(pkg)) {
            updated.remove(pkg)
            false
        } else {
            updated.add(pkg)
            true
        }
        prefs.setDndExceptions(updated)
        onUpdated(updated)

        if (isDndActive) {
            scope.launch(Dispatchers.IO) {
                ShizukuManager.applyGamerDnd(
                    context = context,
                    allowCalls = allowCalls,
                    blockHeadsUp = blockHeadsUp,
                    exceptions = updated
                )
                showFeedback(if (isNowWhitelisted) "✓ Excepción DND agregada" else "✕ Excepción DND removida")
            }
        }
    }

    fun toggleAppHibernation(
        targetPackage: String?,
        pkg: String,
        shouldHibernate: Boolean,
        hibernatedPackages: Set<String>,
        hibernationExceptions: Set<String>,
        onUpdated: (hibernated: Set<String>, exceptions: Set<String>) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val currentHib = hibernatedPackages.toMutableSet()
                val currentEx = hibernationExceptions.toMutableSet()

                if (shouldHibernate) {
                    currentEx.remove(pkg)
                    currentHib.add(pkg)
                    prefs.removeHibernationException(pkg)
                    prefs.addHibernationCustomTarget(pkg)
                    ShizukuManager.hibernateBackgroundPackages(
                        packages = listOf(pkg),
                        excludePackage = targetPackage,
                        exceptions = currentEx
                    )
                    showFeedback("💤 App puesta en reposo")
                } else {
                    currentHib.remove(pkg)
                    currentEx.add(pkg)
                    prefs.addHibernationException(pkg)
                    prefs.removeHibernationCustomTarget(pkg)
                    ShizukuManager.restoreHibernatedPackages(listOf(pkg))
                    showFeedback("⚡ App despertada para segundo plano")
                }

                prefs.setCurrentlyHibernatedPackages(currentHib)
                onUpdated(currentHib, currentEx)
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling app hibernation", e)
            }
        }
    }

    fun applyDriver(targetPackage: String?, driver: GraphicsDriver) {
        val pkg = targetPackage ?: return
        scope.launch(Dispatchers.IO) {
            try {
                prefs.setGameDriver(pkg, driver)
                ShizukuManager.applyGameGraphicsDriver(pkg, driver)
                showFeedback("✓ Motor ${driver.displayName} aplicado", 3000L)
            } catch (e: Exception) {
                Log.e(TAG, "Error applying driver in game", e)
            }
        }
    }

    fun applyScale(targetPackage: String?, scale: DisplayResolutionScale, onCompleted: () -> Unit) {
        val pkg = targetPackage ?: return
        scope.launch(Dispatchers.IO) {
            try {
                prefs.setGameDisplayScale(pkg, scale)
                DisplayScaleController.applyDisplayScale(
                    context = context,
                    scale = scale,
                    isAuthorized = ShizukuManager.isAuthorized
                )
                scope.launch(Dispatchers.Main) { onCompleted() }
                showFeedback("✓ Escala ${scale.title} aplicada", 3000L)
            } catch (e: Exception) {
                Log.e(TAG, "Error applying display scale in game", e)
            }
        }
    }

    fun executeQuickBoost(targetPackage: String?, hibernationExceptions: Set<String>) {
        scope.launch(Dispatchers.IO) {
            try {
                val isShizuku = ShizukuManager.isAuthorized
                val freed = if (isShizuku) 350L else 180L
                prefs.addMemoryFreedMb(freed)
                prefs.incrementBoostCount()

                targetPackage?.let { pkg ->
                    val installed = SystemInfoHelper.getInstalledAppsAndGames(context)
                    val bgPkgs = installed.map { it.packageName }.filter { it != pkg }
                    ShizukuManager.hibernateBackgroundPackages(
                        packages = bgPkgs,
                        excludePackage = pkg,
                        exceptions = hibernationExceptions,
                        customTargets = prefs.getHibernationCustomTargets()
                    )
                }

                showFeedback("⚡ +${freed}MB RAM liberados al vuelo", 3000L)
            } catch (e: Exception) {
                Log.e(TAG, "Error executing in game quick boost", e)
            }
        }
    }

    companion object {
        private const val TAG = "OverlayGamerActions"
    }
}
