package com.example.model

/**
 * Proportional display resolution and density scaling profiles for gaming.
 * Applies even pixel clamping (divisible by 2) and exact proportional DPI scaling
 * to guarantee no touch miscalibration or GPU tile pitch crashes.
 */
enum class DisplayResolutionScale(
    val id: String,
    val title: String,
    val subtitle: String,
    val scaleFactor: Float,
    val tag: String,
    val performanceImpact: String
) {
    NATIVE_100(
        id = "native_100",
        title = "100% Nativo (Fábrica)",
        subtitle = "Resolución física de fábrica y densidad original del dispositivo",
        scaleFactor = 1.0f,
        tag = "100% Nativo",
        performanceImpact = "Sin modificación de visualización"
    ),
    BALANCED_85(
        id = "balanced_85",
        title = "85% Escala Balanceada",
        subtitle = "Aumento moderado de FPS con nitidez casi indistinguible",
        scaleFactor = 0.85f,
        tag = "85% Balance",
        performanceImpact = "+15% FPS • Menor carga de fragment shader en GPU"
    ),
    PERFORMANCE_75(
        id = "performance_75",
        title = "75% Rendimiento HD+",
        subtitle = "Recomendado para juegos pesados (Genshin, Warzone, PUBG, Warzone)",
        scaleFactor = 0.75f,
        tag = "75% HD+",
        performanceImpact = "+30% FPS • Gran ahorro térmico y de batería"
    ),
    ULTRA_SMOOTH_50(
        id = "ultra_smooth_50",
        title = "50% Ultra Fluidez (720p/540p)",
        subtitle = "Máxima tasa de cuadros y estabilidad para teléfonos modestos",
        scaleFactor = 0.50f,
        tag = "50% Fluidez",
        performanceImpact = "+60% FPS • Rendimiento extremo para GPU débil"
    );

    val displayName: String get() = title
}

data class DisplayScaleMetrics(
    val originalWidth: Int,
    val originalHeight: Int,
    val originalDensity: Int,
    val targetWidth: Int,
    val targetHeight: Int,
    val targetDensity: Int,
    val scale: DisplayResolutionScale
)
