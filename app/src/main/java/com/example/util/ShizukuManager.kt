package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.model.GraphicsDriver
import com.example.util.shizuku.AdbShellExecutor
import com.example.util.shizuku.AppProcessInspector
import com.example.util.shizuku.ElevatedBoostReport
import com.example.util.shizuku.GamerDndController
import com.example.util.shizuku.GraphicsDriverController
import com.example.util.shizuku.NetworkOptimizerController
import com.example.util.shizuku.ProcessHibernationController
import com.example.util.shizuku.ShellResult
import com.example.util.shizuku.ShizukuState
import com.example.util.shizuku.ShizukuStatus
import com.example.util.shizuku.TouchResponseController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

// Re-export Shizuku models for seamless project-wide compatibility
typealias ShizukuState = com.example.util.shizuku.ShizukuState
typealias ShizukuStatus = com.example.util.shizuku.ShizukuStatus
typealias ShellResult = com.example.util.shizuku.ShellResult
typealias ElevatedBoostReport = com.example.util.shizuku.ElevatedBoostReport

/**
 * High-level unified facade for Shizuku Binder management and elevated system commands.
 */
object ShizukuManager {
    private const val TAG = "ShizukuManager"
    const val REQUEST_CODE_SHIZUKU_PERMISSION = 7001

    private val _status = MutableStateFlow(ShizukuStatus())
    val status: StateFlow<ShizukuStatus> = _status.asStateFlow()

    val isAuthorized: Boolean
        get() = _status.value.state == ShizukuState.AUTHORIZED

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkAndRefreshStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _status.value = ShizukuStatus(
            state = ShizukuState.NOT_RUNNING,
            message = "Servicio Shizuku desconectado"
        )
    }

    private val permissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == REQUEST_CODE_SHIZUKU_PERMISSION) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    checkAndRefreshStatus()
                } else {
                    _status.value = ShizukuStatus(
                        state = ShizukuState.PERMISSION_NEEDED,
                        message = "Permiso denegado en Shizuku"
                    )
                }
            }
        }

    fun initialize() {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
            checkAndRefreshStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Shizuku listeners", e)
        }
    }

    fun cleanup() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up Shizuku listeners", e)
        }
    }

    fun checkAndRefreshStatus() {
        try {
            if (!Shizuku.pingBinder()) {
                _status.value = ShizukuStatus(
                    state = ShizukuState.NOT_RUNNING,
                    message = "Shizuku no está en ejecución. Inícialo por Depuración Inalámbrica o Root."
                )
                return
            }

            val hasPermission = if (Shizuku.isPreV11()) {
                false
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }

            if (hasPermission) {
                val uid = Shizuku.getUid()
                val isRoot = uid == 0
                val version = Shizuku.getVersion()
                val mode = if (isRoot) "ROOT (UID 0)" else "ADB SHELL (UID $uid)"
                _status.value = ShizukuStatus(
                    state = ShizukuState.AUTHORIZED,
                    isRoot = isRoot,
                    uid = uid,
                    version = version,
                    message = "Shizuku Conectado • Modo $mode"
                )
            } else {
                _status.value = ShizukuStatus(
                    state = ShizukuState.PERMISSION_NEEDED,
                    message = "Shizuku en ejecución • Requiere autorización"
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Shizuku ping/check failed: ${e.message}")
            _status.value = ShizukuStatus(
                state = ShizukuState.NOT_RUNNING,
                message = "Shizuku no disponible"
            )
        }
    }

    fun requestShizukuPermission() {
        try {
            if (Shizuku.pingBinder()) {
                if (!Shizuku.isPreV11()) {
                    Shizuku.requestPermission(REQUEST_CODE_SHIZUKU_PERMISSION)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request Shizuku permission", e)
        }
    }

    fun openShizukuApp(context: Context) {
        val shizukuPackage = "moe.shizuku.privileged.api"
        val launchIntent = context.packageManager.getLaunchIntentForPackage(shizukuPackage)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/download/"))
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(browserIntent)
        }
    }

    suspend fun executeShellCommand(command: String): ShellResult {
        return AdbShellExecutor.executeCommand(command, isAuthorized)
    }

    suspend fun grantRuntimePermission(packageName: String, permissionName: String, userId: Int = 0): Boolean {
        return AdbShellExecutor.grantPermission(packageName, permissionName, userId, isAuthorized)
    }

    /**
     * Elevated Game Turbo Boost via Shizuku:
     * 1. Purges system-wide app caches using `pm trim-caches`
     * 2. Force-stops background power/RAM hog processes using `am force-stop`
     * 3. Sets Android 12+ Game Mode to PERFORMANCE (`cmd game mode 2 <package>`)
     */
    suspend fun executeElevatedGameBoost(
        targetGamePackage: String?,
        backgroundPackagesToKill: List<String>
    ): ElevatedBoostReport = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        var appsKilledCount = 0

        if (!isAuthorized) {
            return@withContext ElevatedBoostReport(
                executed = false,
                message = "Shizuku inactivo: Boost estándar aplicado",
                logs = logs
            )
        }

        // 1. System cache trimming
        val trimResult = executeShellCommand("pm trim-caches 2147483647")
        if (trimResult.isSuccess) {
            logs.add("✓ Caché global del sistema purgada (pm trim-caches)")
        }

        // 2. Kill background non-system app hogs
        for (pkg in backgroundPackagesToKill.take(12)) {
            val isProtected = pkg == targetGamePackage ||
                pkg.contains("android") ||
                pkg.contains("launcher") ||
                pkg.contains("inputmethod") ||
                pkg.startsWith("com.google.") ||
                pkg.startsWith("com.android.") ||
                pkg.contains("login") ||
                pkg.contains("account") ||
                pkg.contains("auth") ||
                pkg.contains("gms") ||
                pkg.contains("vending")

            if (!isProtected) {
                val killRes = executeShellCommand("am force-stop $pkg")
                if (killRes.isSuccess) {
                    appsKilledCount++
                }
            }
        }
        if (appsKilledCount > 0) {
            logs.add("✓ $appsKilledCount apps en segundo plano cerradas con ADB")
        }

        // 3. Game mode performance trigger (Android 12+ / API 31+)
        if (targetGamePackage != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val gameModeRes = executeShellCommand("cmd game mode 2 $targetGamePackage")
            if (gameModeRes.isSuccess) {
                logs.add("✓ Modo Juego Rendimiento (Performance Mode) activado en $targetGamePackage")
            }
        }

        ElevatedBoostReport(
            executed = true,
            appsKilled = appsKilledCount,
            message = "¡Optimización ADB con Shizuku completada con éxito!",
            logs = logs
        )
    }

    suspend fun applyGameGraphicsDriver(packageName: String, driver: GraphicsDriver): List<String> {
        return GraphicsDriverController.applyDriver(packageName, driver, isAuthorized)
    }

    suspend fun restoreGameGraphicsDriver(packageName: String): List<String> {
        return GraphicsDriverController.restoreDriver(packageName, isAuthorized)
    }

    suspend fun suspendGooglePlayServices(): List<String> {
        return ProcessHibernationController.suspendGooglePlayServices(isAuthorized)
    }

    suspend fun restoreGooglePlayServices(): List<String> {
        return ProcessHibernationController.restoreGooglePlayServices(isAuthorized)
    }

    suspend fun hibernateBackgroundPackages(
        packages: List<String>,
        excludePackage: String?,
        exceptions: Set<String> = emptySet(),
        customTargets: Set<String> = emptySet()
    ): Pair<List<String>, List<String>> {
        return ProcessHibernationController.hibernateBackgroundPackages(
            packages = packages,
            excludePackage = excludePackage,
            exceptions = exceptions,
            customTargets = customTargets,
            isAuthorized = isAuthorized
        )
    }

    suspend fun toggleSingleAppHibernation(packageName: String, shouldHibernate: Boolean): Boolean {
        return ProcessHibernationController.toggleSingleAppHibernation(packageName, shouldHibernate, isAuthorized)
    }

    suspend fun restoreHibernatedPackages(packages: List<String>): List<String> {
        return ProcessHibernationController.restoreHibernatedPackages(packages, isAuthorized)
    }

    suspend fun applyGamerDnd(
        context: Context,
        allowCalls: Boolean = true,
        blockHeadsUp: Boolean = true,
        exceptions: Set<String> = emptySet()
    ): List<String> {
        return GamerDndController.applyGamerDnd(context, allowCalls, blockHeadsUp, exceptions, isAuthorized)
    }

    suspend fun restoreGamerDnd(context: Context): List<String> {
        return GamerDndController.restoreDndSettings(context, isAuthorized)
    }

    suspend fun applyTouchBoost(
        context: Context,
        pointerSpeed: Int = 7,
        forceMaxHz: Boolean = true,
        zeroAnimations: Boolean = true
    ): List<String> {
        return TouchResponseController.applyTouchBoost(context, pointerSpeed, forceMaxHz, zeroAnimations, isAuthorized)
    }

    suspend fun restoreTouchSettings(context: Context): List<String> {
        return TouchResponseController.restoreTouchSettings(context, isAuthorized)
    }

    suspend fun applyWifiHighPerf(context: Context): List<String> {
        return NetworkOptimizerController.applyWifiHighPerf(context, isAuthorized)
    }

    suspend fun restoreWifiSettings(context: Context): List<String> {
        return NetworkOptimizerController.restoreWifiSettings(context, isAuthorized)
    }

    suspend fun isAppInForeground(packageName: String): Boolean {
        return AppProcessInspector.isAppInForeground(packageName, isAuthorized)
    }
}
