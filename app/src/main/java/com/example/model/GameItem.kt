package com.example.model

import android.graphics.drawable.Drawable

enum class GraphicsDriver(
    val id: String,
    val title: String,
    val subtitle: String,
    val tag: String,
    val description: String
) {
    SYSTEM_DEFAULT(
        id = "default",
        title = "Por Defecto del Sistema",
        subtitle = "Controlador estándar asignado por el fabricante del dispositivo",
        tag = "Auto",
        description = "No aplica modificaciones al pipeline de renderizado de la GPU."
    ),
    VULKAN_GAME_DRIVER(
        id = "vulkan",
        title = "Vulkan Game Driver",
        subtitle = "Fuerza el driver de alto rendimiento con menor overhead de CPU",
        tag = "Vulkan",
        description = "Habilita Updatable Game Driver de producción para exprimir los núcleos de la GPU."
    ),
    ANGLE(
        id = "angle",
        title = "ANGLE (OpenGL sobre Vulkan)",
        subtitle = "Traduce llamadas OpenGL ES a Vulkan directamente",
        tag = "ANGLE",
        description = "Capa de abstracción de Google que reduce los tiempos de CPU en draw calls intensivas."
    ),
    OPENGL_NATIVE(
        id = "native",
        title = "OpenGL ES Nativo",
        subtitle = "Fuerza el controlador nativo de OpenGL ES del fabricante",
        tag = "OpenGL",
        description = "Máxima compatibilidad para emuladores y motores gráficos clásicos."
    );

    val displayName: String get() = title
}

data class GameItem(
    val id: String,
    val title: String,
    val packageName: String,
    val isBuiltIn: Boolean = false,
    val category: String = "Juego",
    val iconDrawable: Drawable? = null,
    val boostCount: Int = 0,
    val lastBoostTime: Long = 0L,
    val isCustomAdded: Boolean = false,
    val graphicsDriver: GraphicsDriver = GraphicsDriver.SYSTEM_DEFAULT,
    val hibernateGoogleServices: Boolean = false,
    val deepBackgroundHibernate: Boolean = true,
    val enableOverlayHud: Boolean = true,
    val enableDnd: Boolean = true,
    val dndAllowCalls: Boolean = true,
    val dndBlockHeadsUp: Boolean = true,
    val enableTouchBoost: Boolean = true,
    val enableWifiHighPerf: Boolean = true,
    val enableCrosshair: Boolean = false,
    val displayScale: DisplayResolutionScale = DisplayResolutionScale.NATIVE_100
)

data class BoostResult(
    val memoryFreedMb: Long,
    val previousRamUsagePercent: Int,
    val currentRamUsagePercent: Int,
    val pingBefore: Int,
    val pingAfter: Int,
    val durationMs: Long,
    val isElevatedShizuku: Boolean = false,
    val appliedDriver: GraphicsDriver = GraphicsDriver.SYSTEM_DEFAULT,
    val appliedDisplayScale: DisplayResolutionScale = DisplayResolutionScale.NATIVE_100,
    val shizukuLogs: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

