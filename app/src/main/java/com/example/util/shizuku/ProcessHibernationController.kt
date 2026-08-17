package com.example.util.shizuku

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ProcessHibernationController {

    /**
     * Temporarily suspends Google Play Services (GMS & Play Store) to free 350MB-600MB RAM during gaming sessions.
     */
    suspend fun suspendGooglePlayServices(isAuthorized: Boolean): List<String> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        if (!isAuthorized) return@withContext logs

        val gmsPackages = listOf("com.google.android.gms", "com.android.vending")
        for (pkg in gmsPackages) {
            AdbShellExecutor.executeCommand("am set-inactive $pkg true", isAuthorized)
            val suspendRes = AdbShellExecutor.executeCommand("pm suspend $pkg", isAuthorized)
            if (suspendRes.isSuccess) {
                logs.add("💤 Google Play ($pkg) suspendido temporalmente")
            }
        }
        logs
    }

    /**
     * Restores Google Play Services back to standard active state.
     */
    suspend fun restoreGooglePlayServices(isAuthorized: Boolean): List<String> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        if (!isAuthorized) return@withContext logs

        val gmsPackages = listOf("com.google.android.gms", "com.android.vending")
        for (pkg in gmsPackages) {
            AdbShellExecutor.executeCommand("pm unsuspend $pkg", isAuthorized)
            AdbShellExecutor.executeCommand("am set-inactive $pkg false", isAuthorized)
        }
        logs.add("✓ Servicios de Google Play reactivados con normalidad")
        logs
    }

    /**
     * Freezes/hibernates high-consumption background apps while the game is running.
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
            if (pkg != excludePackage && !pkg.contains("android") && !pkg.contains("launcher") && !pkg.contains("inputmethod")) {
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
            AdbShellExecutor.executeCommand("am set-inactive $pkg false", isAuthorized)
        }
        logs.add("✓ Procesos en segundo plano deshibernados")
        logs
    }
}
