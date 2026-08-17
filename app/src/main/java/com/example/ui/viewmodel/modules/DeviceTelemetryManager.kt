package com.example.ui.viewmodel.modules

import android.content.Context
import com.example.model.DeviceMetrics
import com.example.util.SystemInfoHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DeviceTelemetryManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _metrics = MutableStateFlow(SystemInfoHelper.getDeviceMetrics(context))
    val metrics: StateFlow<DeviceMetrics> = _metrics.asStateFlow()

    private var pollingJob: Job? = null

    fun startPeriodicPolling(isBoostingProvider: () -> Boolean) {
        pollingJob?.cancel()
        pollingJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                if (!isBoostingProvider()) {
                    val currentPing = _metrics.value.pingMs
                    _metrics.value = SystemInfoHelper.getDeviceMetrics(context, currentPing)
                }
                delay(3000L)
            }
        }
    }

    fun measurePing() {
        scope.launch(Dispatchers.IO) {
            val ping = SystemInfoHelper.measureRealPing()
            _metrics.value = SystemInfoHelper.getDeviceMetrics(context, ping)
        }
    }

    fun updateMetrics(newMetrics: DeviceMetrics) {
        _metrics.value = newMetrics
    }

    fun stop() {
        pollingJob?.cancel()
    }
}
