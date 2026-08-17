package com.example.ui.components.overlay

enum class HudTab(val title: String) {
    TELEMETRY("Telemetría"),
    RESOLUTION("Resolución"),
    DRIVERS("Motor GPU"),
    QUICK_BOOST("Quick Boost")
}

internal fun Double.format(digits: Int): String = "%.${digits}f".format(this)
