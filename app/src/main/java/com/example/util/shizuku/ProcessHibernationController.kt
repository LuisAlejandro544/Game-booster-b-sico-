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
     * Freezes/hibernates background apps while respecting custom whitelist exceptions (e.g. Discord, Spotify)
     * and prioritizing custom blacklist targets if specified.
     * Strictly protects system, keyboard, launcher, and authentication services.
     */
    suspend fun hibernateBackgroundPackages(
        packages: List<String>,
        excludePackage: String?,
        exceptions: Set<String> = emptySet(),
        customTargets: Set<String> = emptySet(),
        isAuthorized: Boolean
    ): Pair<List<String>, List<String>> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        val hibernatedPkgs = mutableListOf<String>()
        if (!isAuthorized) return@withContext Pair(logs, hibernatedPkgs)

        val targetPool = if (customTargets.isNotEmpty()) {
            packages.filter { customTargets.contains(it) || !isProtectedPackage(it, excludePackage, exceptions) }
        } else {
            packages
        }

        var count = 0
        for (pkg in targetPool) {
            if (!isProtectedPackage(pkg, excludePackage, exceptions)) {
                AdbShellExecutor.executeCommand("am set-inactive $pkg true", isAuthorized)
                AdbShellExecutor.executeCommand("am force-stop $pkg", isAuthorized)
                hibernatedPkgs.add(pkg)
                count++
            }
        }

        if (count > 0) {
            val exceptionsNote = if (exceptions.isNotEmpty()) " (Protegidas: ${exceptions.size} apps)" else ""
            logs.add("❄️ $count apps en segundo plano hibernadas para juego continuo$exceptionsNote")
        }
        Pair(logs, hibernatedPkgs)
    }

    /**
     * Helper to verify if a package is immune to hibernation (System critical, current game, or user whitelist exception).
     */
    fun isProtectedPackage(pkg: String, currentRunningGame: String?, exceptions: Set<String>): Boolean {
        if (pkg == currentRunningGame) return true
        if (exceptions.contains(pkg)) return true

        val lower = pkg.lowercase()
        return lower.contains("android") ||
                lower.contains("launcher") ||
                lower.contains("inputmethod") ||
                lower.startsWith("com.google.") ||
                lower.startsWith("com.android.") ||
                lower.contains("login") ||
                lower.contains("account") ||
                lower.contains("auth") ||
                lower.contains("gms") ||
                lower.contains("vending")
    }

    /**
     * Toggles hibernation on/off for a single specific application in real-time from the In-Game HUD.
     * E.g. User wants to wake Discord to join a voice chat or freeze TikTok while playing.
     */
    suspend fun toggleSingleAppHibernation(
        packageName: String,
        shouldHibernate: Boolean,
        isAuthorized: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isAuthorized) return@withContext false

        if (shouldHibernate) {
            AdbShellExecutor.executeCommand("am set-inactive $packageName true", isAuthorized)
            AdbShellExecutor.executeCommand("am force-stop $packageName", isAuthorized)
            true
        } else {
            AdbShellExecutor.executeCommand("pm unsuspend $packageName", isAuthorized)
            AdbShellExecutor.executeCommand("am set-inactive $packageName false", isAuthorized)
            true
        }
    }

    /**
     * Restores hibernated apps back to normal active state.
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
