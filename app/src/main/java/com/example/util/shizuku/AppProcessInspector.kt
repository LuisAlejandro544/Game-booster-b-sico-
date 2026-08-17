package com.example.util.shizuku

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppProcessInspector {

    /**
     * Checks if the specified package is currently the top/foreground activity on screen using elevated ADB.
     */
    suspend fun isAppInForeground(packageName: String, isAuthorized: Boolean): Boolean = withContext(Dispatchers.IO) {
        if (!isAuthorized) return@withContext false

        val res = AdbShellExecutor.executeCommand(
            "dumpsys activity activities | grep -E 'mResumedActivity|topResumedActivity'",
            isAuthorized
        )
        if (res.isSuccess && res.output.contains(packageName)) {
            return@withContext true
        }

        // Secondary fallback check via cmd activity
        val res2 = AdbShellExecutor.executeCommand("cmd activity get-top-activity", isAuthorized)
        if (res2.isSuccess && res2.output.contains(packageName)) {
            return@withContext true
        }

        false
    }
}
