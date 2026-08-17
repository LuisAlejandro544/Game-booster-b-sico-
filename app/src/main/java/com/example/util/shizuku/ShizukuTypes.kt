package com.example.util.shizuku

enum class ShizukuState {
    NOT_RUNNING,         // Shizuku app is not running / binder not available
    PERMISSION_NEEDED,   // Shizuku is running, but permission not granted
    AUTHORIZED           // Shizuku is running and authorized (ADB or Root)
}

data class ShizukuStatus(
    val state: ShizukuState = ShizukuState.NOT_RUNNING,
    val isRoot: Boolean = false,
    val uid: Int = -1,
    val version: Int = 0,
    val message: String = "Shizuku no detectado"
)

data class ShellResult(
    val isSuccess: Boolean,
    val output: String,
    val error: String,
    val exitCode: Int = -1
)

data class ElevatedBoostReport(
    val executed: Boolean,
    val appsKilled: Int = 0,
    val message: String = "",
    val logs: List<String> = emptyList()
)
