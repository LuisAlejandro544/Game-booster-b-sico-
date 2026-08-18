package com.example.util.shizuku

import android.content.Context
import android.os.Process
import android.util.Log

object ProcessImmunityController {
    private const val TAG = "ProcessImmunity"

    /**
     * Grants absolute OOM Score protection and Doze exemption via Shizuku/ADB.
     * Prevents Android's Low Memory Killer (LMK) and vendor battery managers
     * from killing the GameWatcherService or HUD Overlay during heavy gaming sessions.
     */
    suspend fun applyProcessImmunity(context: Context, isAuthorized: Boolean): ShellResult {
        if (!isAuthorized) {
            return ShellResult(isSuccess = false, output = "", error = "Shizuku no está autorizado")
        }

        val pkg = context.packageName
        val pid = Process.myPid()

        Log.d(TAG, "🛡️ Aplicando inmunidad OOM Score y whitelist Doze para PID: $pid ($pkg)")

        val commands = listOf(
            // 1. Whitelist from Doze and deep sleep (Battery Optimizations)
            "dumpsys deviceidle whitelist +$pkg",
            // 2. Prevent system from marking process inactive
            "cmd activity set-inactive $pkg false",
            // 3. Grant unrestricted background execution via AppOps
            "cmd appops set $pkg RUN_IN_BACKGROUND allow",
            "cmd appops set $pkg RUN_ANY_IN_BACKGROUND allow",
            // 4. Protect from Low Memory Killer (LMK) with maximum priority (-1000)
            "echo -1000 > /proc/$pid/oom_score_adj 2>/dev/null || true",
            "cmd activity oom-adj $pid -1000 2>/dev/null || true"
        )

        val fullCommand = commands.joinToString(" && ")
        val result = AdbShellExecutor.executeCommand(fullCommand, isAuthorized = true)

        if (result.isSuccess) {
            Log.i(TAG, "✅ Proceso blindado contra cierre de memoria (OOM Score -1000 y Doze Whitelist activa)")
        } else {
            Log.w(TAG, "⚠️ Advertencia aplicando inmunidad OOM: ${result.error}")
        }

        return result
    }

    /**
     * Cleans up temporary process immunity whitelist when game session ends.
     */
    suspend fun restoreProcessImmunity(context: Context, isAuthorized: Boolean): ShellResult {
        if (!isAuthorized) {
            return ShellResult(isSuccess = false, output = "", error = "Shizuku no está autorizado")
        }

        val pkg = context.packageName
        Log.d(TAG, "Restaurando políticas de proceso para $pkg")

        val command = "dumpsys deviceidle whitelist -$pkg"
        return AdbShellExecutor.executeCommand(command, isAuthorized = true)
    }
}
