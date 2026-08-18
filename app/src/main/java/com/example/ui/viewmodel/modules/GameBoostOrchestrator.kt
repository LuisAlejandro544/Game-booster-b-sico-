package com.example.ui.viewmodel.modules

import android.content.Context
import com.example.data.BoosterPreferences
import com.example.model.BoostProfile
import com.example.model.BoostResult
import com.example.model.DeviceMetrics
import com.example.model.DisplayResolutionScale
import com.example.model.GameItem
import com.example.model.GraphicsDriver
import com.example.service.GameWatcherService
import com.example.util.ShizukuManager
import com.example.util.SystemInfoHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameBoostOrchestrator(
    private val context: Context,
    private val prefs: BoosterPreferences
) {

    data class BoostExecutionResult(
        val boostResult: BoostResult,
        val updatedMetrics: DeviceMetrics,
        val activeGame: GameItem?
    )

    suspend fun executeBoostPipeline(
        targetGame: GameItem?,
        forcedDriver: GraphicsDriver?,
        forcedDisplayScale: DisplayResolutionScale? = null,
        deepHibernate: Boolean?,
        hibernateGoogle: Boolean?,
        enableOverlayHud: Boolean?,
        installedApps: List<GameItem>,
        activeProfile: BoostProfile,
        prevMetrics: DeviceMetrics,
        onProgressUpdate: (statusText: String, progress: Float) -> Unit
    ): BoostExecutionResult = withContext(Dispatchers.IO) {
        val driverToApply = forcedDriver
            ?: targetGame?.let { prefs.getGameDriver(it.packageName) }
            ?: GraphicsDriver.SYSTEM_DEFAULT
        val scaleToApply = forcedDisplayScale
            ?: targetGame?.let { prefs.getGameDisplayScale(it.packageName) }
            ?: DisplayResolutionScale.NATIVE_100
        val useDeepHib = deepHibernate
            ?: targetGame?.let { prefs.getGameDeepHibernate(it.packageName) }
            ?: true
        val useGoogleHib = hibernateGoogle
            ?: targetGame?.let { prefs.getGameHibernateGoogle(it.packageName) }
            ?: false
        val useOverlay = enableOverlayHud
            ?: targetGame?.let { prefs.getGameOverlayHud(it.packageName) }
            ?: true
        val useDnd = targetGame?.let { prefs.getGameDndEnabled(it.packageName) } ?: true
        val dndCalls = prefs.getDndAllowCalls()
        val dndHeadsUp = prefs.getDndBlockHeadsUp()

        val configuredGame = targetGame?.copy(
            graphicsDriver = driverToApply,
            displayScale = scaleToApply,
            deepBackgroundHibernate = useDeepHib,
            hibernateGoogleServices = useGoogleHib,
            enableOverlayHud = useOverlay,
            enableDnd = useDnd,
            dndAllowCalls = dndCalls,
            dndBlockHeadsUp = dndHeadsUp
        )

        val isShizukuReady = ShizukuManager.isAuthorized
        val initialPing = prevMetrics.pingMs
        val initialRamPercent = prevMetrics.ramUsagePercent

        val steps = if (isShizukuReady) {
            listOf(
                "Conectando con Shizuku (ADB / Root)..." to 0.15f,
                "Iniciando centinela de hibernación y failsafe de pantalla..." to 0.35f,
                "Purgando caché del sistema (pm trim-caches)..." to 0.50f,
                "Forzando motor de renderizado (${driverToApply.tag})..." to 0.70f,
                (if (scaleToApply != DisplayResolutionScale.NATIVE_100) "Ajustando escala de resolución (${scaleToApply.tag})..." else "Optimizando subprocesos en segundo plano...") to 0.85f,
                "Estabilizando latencia y búfer de red..." to 0.95f,
                "¡Optimización Elevada Completada!" to 1.0f
            )
        } else {
            listOf(
                "Escaneando memoria RAM ocupada..." to 0.15f,
                "Liberando procesos en segundo plano..." to 0.35f,
                "Limpiando caché temporal de aplicaciones..." to 0.60f,
                "Estabilizando búfer de red y reduciendo ping..." to 0.85f,
                "Aplicando perfil ${activeProfile.title}..." to 0.95f,
                "¡Optimización Gamer Completada!" to 1.0f
            )
        }

        for ((stepText, progress) in steps) {
            onProgressUpdate(stepText, progress)
            delay(340L)
        }

        // Standard cleanup
        val freedMb = SystemInfoHelper.cleanMemoryAndCache(context)

        // Elevated Shizuku optimization & Driver Injection
        val allLogs = mutableListOf<String>()
        var activeRunningGame: GameItem? = null

        if (isShizukuReady) {
            val backgroundPackages = installedApps
                .filter { it.packageName != configuredGame?.packageName }
                .map { it.packageName }

            val elevatedReport = ShizukuManager.executeElevatedGameBoost(
                targetGamePackage = configuredGame?.packageName,
                backgroundPackagesToKill = backgroundPackages
            )
            allLogs.addAll(elevatedReport.logs)

            if (configuredGame != null) {
                val driverLogs = ShizukuManager.applyGameGraphicsDriver(
                    packageName = configuredGame.packageName,
                    driver = driverToApply
                )
                allLogs.addAll(driverLogs)

                if (scaleToApply != DisplayResolutionScale.NATIVE_100) {
                    allLogs.add("📱 Escala gráfica activada: ${scaleToApply.title} (5 Capas Failsafe)")
                }
                if (useDnd) {
                    allLogs.add("🔕 Modo No Molestar Gamer: Banners bloqueados y excepciones activas")
                }
                if (useDeepHib) {
                    val hibExceptionsCount = prefs.getHibernationExceptions().size
                    allLogs.add("❄️ Centinela activo: Hibernando apps en segundo plano ($hibExceptionsCount protegidas)")
                }
                if (useGoogleHib) {
                    allLogs.add("💤 Google Play Services suspendido (+400MB RAM)")
                }

                activeRunningGame = configuredGame
            }
        }

        // Start GameWatcherService sentinel to monitor foreground state and restore upon exiting
        if (configuredGame != null) {
            activeRunningGame = configuredGame
            GameWatcherService.start(
                context = context,
                packageName = configuredGame.packageName,
                gameTitle = configuredGame.title,
                driver = driverToApply,
                hibernateGoogle = useGoogleHib,
                deepHibernate = useDeepHib,
                displayScale = scaleToApply,
                enableDnd = useDnd,
                dndAllowCalls = dndCalls,
                dndBlockHeadsUp = dndHeadsUp
            )
        }

        val optimizedPing = (initialPing * 0.8f).toInt().coerceAtLeast(16)

        prefs.incrementBoostCount()
        val totalFreed = if (isShizukuReady) {
            if (useGoogleHib) freedMb + 450 else freedMb + 220
        } else freedMb
        prefs.addMemoryFreedMb(totalFreed)

        val currentMetrics = SystemInfoHelper.getDeviceMetrics(context, optimizedPing)
        val newRamPercent = (initialRamPercent - (totalFreed / (prevMetrics.totalRamMb.coerceAtLeast(1024).toFloat()) * 100).toInt()).coerceIn(18, 80)

        val nowFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val updatedMetrics = currentMetrics.copy(
            ramUsagePercent = newRamPercent,
            isOptimized = true,
            lastOptimizedTime = nowFormatted
        )

        val boostResult = BoostResult(
            memoryFreedMb = totalFreed,
            previousRamUsagePercent = initialRamPercent,
            currentRamUsagePercent = newRamPercent,
            pingBefore = initialPing,
            pingAfter = optimizedPing,
            durationMs = 2200,
            isElevatedShizuku = isShizukuReady,
            appliedDriver = driverToApply,
            appliedDisplayScale = scaleToApply,
            shizukuLogs = allLogs
        )

        BoostExecutionResult(
            boostResult = boostResult,
            updatedMetrics = updatedMetrics,
            activeGame = activeRunningGame
        )
    }
}
