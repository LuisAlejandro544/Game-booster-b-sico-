package com.example.util.shizuku

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object AdbShellExecutor {
    private const val TAG = "AdbShellExecutor"

    /**
     * Executes elevated ADB Shell commands through Shizuku without requiring root or PC.
     */
    suspend fun executeCommand(command: String, isAuthorized: Boolean): ShellResult = withContext(Dispatchers.IO) {
        if (!isAuthorized) {
            return@withContext ShellResult(
                isSuccess = false,
                output = "",
                error = "Shizuku no está autorizado"
            )
        }

        try {
            val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            newProcessMethod.isAccessible = true
            val process = newProcessMethod.invoke(null, arrayOf("sh", "-c", command), null, null) as Process

            val output = StringBuilder()
            val error = StringBuilder()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.appendLine(line)
            }
            reader.close()

            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            while (errorReader.readLine().also { line = it } != null) {
                error.appendLine(line)
            }
            errorReader.close()

            val exitCode = process.waitFor()
            ShellResult(
                isSuccess = exitCode == 0,
                output = output.toString().trim(),
                error = error.toString().trim(),
                exitCode = exitCode
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error executing shell command: $command", e)
            ShellResult(
                isSuccess = false,
                output = "",
                error = e.message ?: "Error desconocido al ejecutar comando Shizuku"
            )
        }
    }

    /**
     * Grants sensitive runtime permissions directly through elevated Shizuku ADB / Root.
     */
    suspend fun grantPermission(
        packageName: String,
        permissionName: String,
        userId: Int = 0,
        isAuthorized: Boolean
    ): Boolean {
        val result = executeCommand("pm grant --user $userId $packageName $permissionName", isAuthorized)
        return result.isSuccess
    }
}
