package com.example.util.shizuku

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ProcessHibernationController {

    private val GMS_PACKAGES = listOf(
        "com.google.android.gms",
        "com.android.vending",
        "com.google.android.gsf",
        "com.google.android.play.games",
        "com.google.android.gms.setup"
    )

    /**
     * Temporarily suspends Google Play Services (GMS & Play Store) to free 350MB-600MB RAM during gaming sessions.
     * Only executed if the user explicitly enabled this setting for the specific game.
     */
    suspend fun suspendGooglePlayServices(isAuthorized: Boolean): List<String> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        if (!isAuthorized) return@withContext logs

        for (pkg in GMS_PACKAGES.take(2)) {
            AdbShellExecutor.executeCommand("am set-inactive $pkg true", isAuthorized)
            val suspendRes = AdbShellExecutor.executeCommand("pm suspend $pkg", isAuthorized)
            if (suspendRes.isSuccess) {
                logs.add("💤 Google Play ($pkg) suspendido temporalmente")
            }
        }
        logs
    }

    /**
     * Restores Google Play Services back to standard active state and ensures NO packages remain suspended by shell.
     */
    suspend fun restoreGooglePlayServices(isAuthorized: Boolean): List<String> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        if (!isAuthorized) return@withContext logs

        for (pkg in GMS_PACKAGES) {
            AdbShellExecutor.executeCommand("pm unsuspend $pkg", isAuthorized)
            AdbShellExecutor.executeCommand("am set-inactive $pkg false", isAuthorized)
            AdbShellExecutor.executeCommand("pm enable $pkg", isAuthorized)
        }
        logs.add("✓ Servicios de Google Play reactivados y desuspendidos con normalidad")
        logs
    }

    /**
     * Freezes/hibernates high-consumption background apps while the game is running.
     * Strictly protects all system, launcher, Google login, and authentication packages.
     */
    suspend fun hibernateBackgroundPackages(
        packages: List<String>,
        excludePackage: String?,
        isAuthorized: Boolean
    ): List<String> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        if (!isAuthorized) return@withContext logs

        var count = 0
        for (pkg in packages) {
            val isProtected = pkg == excludePackage ||
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
                AdbShellExecutor.executeCommand("am set-inactive $pkg true", isAuthorized)
                AdbShellExecutor.executeCommand("am force-stop $pkg", isAuthorized)
                count++
            }
        }
        if (count > 0) {
            logs.add("❄️ $count aplicaciones en segundo plano hibernadas para juego continuo")
        }
        logs
    }

    /**
     * Restores hibernated apps back to normal state.
     */
    suspend fun restoreHibernatedPackages(
        packages: List<String>,
        isAuthorized: Boolean
    ): List<String> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        if (!isAuthorized) return@withContext logs

        for (pkg in packages) {
            if (!pkg.contains("android") && !pkg.contains("launcher")) {
                AdbShellExecutor.executeCommand("pm unsuspend $pkg", isAuthorized)
                AdbShellExecutor.executeCommand("am set-inactive $pkg false", isAuthorized)
            }
        }
        logs.add("✓ Procesos en segundo plano deshibernados")
        logs
    }
}
