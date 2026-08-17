package com.example.model

data class DeviceMetrics(
    val totalRamMb: Long = 0,
    val availableRamMb: Long = 0,
    val usedRamMb: Long = 0,
    val ramUsagePercent: Int = 0,
    val cpuUsagePercent: Int = 0,
    val cpuTempCelsius: Float = 36.5f,
    val batteryLevel: Int = 100,
    val batteryTempCelsius: Float = 32.0f,
    val batteryStatus: String = "Normal",
    val pingMs: Int = 28,
    val pingRating: PingRating = PingRating.EXCELLENT,
    val storageTotalGb: Float = 0f,
    val storageFreeGb: Float = 0f,
    val storageUsedGb: Float = 0f,
    val storageUsagePercent: Int = 0,
    val cpuCores: Int = 8,
    val deviceModel: String = "Android Device",
    val refreshRateHz: Int = 60,
    val isOptimized: Boolean = false,
    val lastOptimizedTime: String = "No optimizado aún"
)

enum class PingRating(val label: String, val colorHex: Long) {
    EXCELLENT("Excelente", 0xFF10B981),
    GOOD("Bueno", 0xFF38BDF8),
    MODERATE("Moderado", 0xFFF59E0B),
    POOR("Alto", 0xFFEF4444)
}

enum class BoostProfile(
    val title: String,
    val subtitle: String,
    val iconName: String,
    val colorHex: Long,
    val targetFpsGain: String
) {
    ULTRA_TURBO(
        title = "Ultra Turbo",
        subtitle = "Máximo rendimiento, limpieza agresiva de RAM y prioridad de red",
        iconName = "bolt",
        colorHex = 0xFF00F0FF,
        targetFpsGain = "+25% FPS"
    ),
    BALANCED(
        title = "Equilibrado",
        subtitle = "Estabilidad térmica, balance óptimo de batería y fluidez",
        iconName = "balance",
        colorHex = 0xFFA855F7,
        targetFpsGain = "+15% FPS"
    ),
    BATTERY_SAVER(
        title = "Ahorro Gamer",
        subtitle = "Menor consumo de energía para sesiones de juego prolongadas",
        iconName = "battery",
        colorHex = 0xFF10B981,
        targetFpsGain = "Max Batería"
    )
}
