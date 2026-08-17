package com.example.util.shizuku

import android.os.Build
import com.example.model.GraphicsDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GraphicsDriverController {

    /**
     * Applies graphics rendering driver customization strictly for the selected game package.
     * Note: NEVER touches persist.sys.* to ensure device safety and compliance.
     */
    suspend fun applyDriver(
        packageName: String,
        driver: GraphicsDriver,
        isAuthorized: Boolean
    ): List<String> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        if (!isAuthorized) {
            logs.add("ℹ Shizuku no autorizado: Configuración guardada localmente")
            return@withContext logs
        }

        when (driver) {
            GraphicsDriver.VULKAN_GAME_DRIVER -> {
                AdbShellExecutor.executeCommand("settings put global updatable_driver_production_opt_in_apps $packageName", isAuthorized)
                AdbShellExecutor.executeCommand("settings put global angle_gl_driver_selection_values default", isAuthorized)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    AdbShellExecutor.executeCommand("cmd game mode 2 $packageName", isAuthorized)
                }
                logs.add("🚀 Vulkan Updatable Game Driver activado para $packageName")
            }
            GraphicsDriver.ANGLE -> {
                AdbShellExecutor.executeCommand("settings put global angle_gl_driver_selection_pkgs $packageName", isAuthorized)
                AdbShellExecutor.executeCommand("settings put global angle_gl_driver_selection_values angle", isAuthorized)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    AdbShellExecutor.executeCommand("cmd game mode 2 $packageName", isAuthorized)
                }
                logs.add("📐 Capa ANGLE (OpenGL sobre Vulkan) forzada para $packageName")
            }
            GraphicsDriver.OPENGL_NATIVE -> {
                AdbShellExecutor.executeCommand("settings put global angle_gl_driver_selection_pkgs $packageName", isAuthorized)
                AdbShellExecutor.executeCommand("settings put global angle_gl_driver_selection_values native", isAuthorized)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    AdbShellExecutor.executeCommand("cmd game mode 1 $packageName", isAuthorized)
                }
                logs.add("🛡️ Controlador OpenGL ES Nativo asignado a $packageName")
            }
            GraphicsDriver.SYSTEM_DEFAULT -> {
                AdbShellExecutor.executeCommand("settings put global angle_gl_driver_selection_values default", isAuthorized)
                AdbShellExecutor.executeCommand("settings delete global angle_gl_driver_selection_pkgs", isAuthorized)
                AdbShellExecutor.executeCommand("settings delete global updatable_driver_production_opt_in_apps", isAuthorized)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    AdbShellExecutor.executeCommand("cmd game mode 1 $packageName", isAuthorized)
                }
                logs.add("⚡ Pipeline de renderizado restaurado a valores por defecto del sistema")
            }
        }
        logs
    }

    /**
     * Reverts any applied graphics driver modifications back to Android OS factory defaults.
     * Called automatically when user leaves or exits the boosted game.
     */
    suspend fun restoreDriver(
        packageName: String,
        isAuthorized: Boolean
    ): List<String> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        if (!isAuthorized) {
            return@withContext logs
        }

        AdbShellExecutor.executeCommand("settings put global angle_gl_driver_selection_values default", isAuthorized)
        AdbShellExecutor.executeCommand("settings delete global angle_gl_driver_selection_pkgs", isAuthorized)
        AdbShellExecutor.executeCommand("settings delete global updatable_driver_production_opt_in_apps", isAuthorized)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AdbShellExecutor.executeCommand("cmd game mode 1 $packageName", isAuthorized)
        }
        logs.add("✓ Configuración de gráficos de Android restaurada al estado original")
        logs
    }
}
