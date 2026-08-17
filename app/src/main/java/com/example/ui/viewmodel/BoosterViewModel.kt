package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BoosterPreferences
import com.example.model.BoostProfile
import com.example.model.BoostResult
import com.example.model.DeviceMetrics
import com.example.model.DisplayResolutionScale
import com.example.model.DisplayScaleMetrics
import com.example.model.GameItem
import com.example.model.GraphicsDriver
import com.example.service.GameOverlayService
import com.example.service.GameWatcherService
import com.example.ui.viewmodel.modules.DeviceTelemetryManager
import com.example.ui.viewmodel.modules.GameBoostOrchestrator
import com.example.ui.viewmodel.modules.GameCatalogManager
import com.example.util.ShizukuManager
import com.example.util.ShizukuStatus
import com.example.util.shizuku.DisplayScaleController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BoosterViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = BoosterPreferences(application)
    private val telemetryManager = DeviceTelemetryManager(application, viewModelScope)
    private val catalogManager = GameCatalogManager(application, prefs, viewModelScope)
    private val boostOrchestrator = GameBoostOrchestrator(application, prefs)

    val shizukuStatus: StateFlow<ShizukuStatus> = ShizukuManager.status
    val metrics: StateFlow<DeviceMetrics> = telemetryManager.metrics
    val gamesList: StateFlow<List<GameItem>> = catalogManager.gamesList
    val installedApps: StateFlow<List<GameItem>> = catalogManager.installedApps

    private val _activeProfile = MutableStateFlow(prefs.getSavedProfile())
    val activeProfile: StateFlow<BoostProfile> = _activeProfile.asStateFlow()

    private val _isBoosting = MutableStateFlow(false)
    val isBoosting: StateFlow<Boolean> = _isBoosting.asStateFlow()

    private val _boostProgress = MutableStateFlow(0f)
    val boostProgress: StateFlow<Float> = _boostProgress.asStateFlow()

    private val _boostStatusText = MutableStateFlow("")
    val boostStatusText: StateFlow<String> = _boostStatusText.asStateFlow()

    private val _lastBoostResult = MutableStateFlow<BoostResult?>(null)
    val lastBoostResult: StateFlow<BoostResult?> = _lastBoostResult.asStateFlow()

    private val _showBoostDialog = MutableStateFlow(false)
    val showBoostDialog: StateFlow<Boolean> = _showBoostDialog.asStateFlow()

    private val _showAddGameSheet = MutableStateFlow(false)
    val showAddGameSheet: StateFlow<Boolean> = _showAddGameSheet.asStateFlow()

    private val _selectedGameForConfig = MutableStateFlow<GameItem?>(null)
    val selectedGameForConfig: StateFlow<GameItem?> = _selectedGameForConfig.asStateFlow()

    private val _activeRunningBoostedGame = MutableStateFlow<GameItem?>(null)
    val activeRunningBoostedGame: StateFlow<GameItem?> = _activeRunningBoostedGame.asStateFlow()

    private val _showSpeedTester = MutableStateFlow(false)
    val showSpeedTester: StateFlow<Boolean> = _showSpeedTester.asStateFlow()

    private val _pendingLaunchGame = MutableStateFlow<GameItem?>(null)
    val pendingLaunchGame: StateFlow<GameItem?> = _pendingLaunchGame.asStateFlow()

    private val _isTestingResolution = MutableStateFlow(false)
    val isTestingResolution: StateFlow<Boolean> = _isTestingResolution.asStateFlow()

    private val _testCountdownSeconds = MutableStateFlow(15)
    val testCountdownSeconds: StateFlow<Int> = _testCountdownSeconds.asStateFlow()

    private val _pendingTestScale = MutableStateFlow<DisplayResolutionScale?>(null)
    val pendingTestScale: StateFlow<DisplayResolutionScale?> = _pendingTestScale.asStateFlow()

    private var resolutionTestJob: Job? = null

    init {
        ShizukuManager.initialize()
        catalogManager.loadGames()
        telemetryManager.startPeriodicPolling(isBoostingProvider = { _isBoosting.value })
        telemetryManager.measurePing()
    }

    override fun onCleared() {
        super.onCleared()
        telemetryManager.stop()
        ShizukuManager.cleanup()
    }

    fun measurePing() {
        telemetryManager.measurePing()
    }

    fun refreshShizuku() {
        ShizukuManager.checkAndRefreshStatus()
    }

    fun requestShizukuPermission() {
        ShizukuManager.requestShizukuPermission()
    }

    fun openShizukuApp() {
        ShizukuManager.openShizukuApp(getApplication())
    }

    fun loadGames() {
        catalogManager.loadGames()
    }

    fun setProfile(profile: BoostProfile) {
        _activeProfile.value = profile
        prefs.saveProfile(profile)
    }

    fun toggleAddGameSheet(show: Boolean) {
        _showAddGameSheet.value = show
    }

    fun openGameConfig(game: GameItem) {
        val currentDriver = prefs.getGameDriver(game.packageName)
        val currentScale = prefs.getGameDisplayScale(game.packageName)
        val deepHib = prefs.getGameDeepHibernate(game.packageName)
        val hibGoogle = prefs.getGameHibernateGoogle(game.packageName)
        val overlayHud = prefs.getGameOverlayHud(game.packageName)
        _selectedGameForConfig.value = game.copy(
            graphicsDriver = currentDriver,
            displayScale = currentScale,
            deepBackgroundHibernate = deepHib,
            hibernateGoogleServices = hibGoogle,
            enableOverlayHud = overlayHud
        )
    }

    fun closeGameConfig() {
        _selectedGameForConfig.value = null
    }

    fun updateGameGraphicsDriver(game: GameItem, driver: GraphicsDriver) {
        catalogManager.updateGameDriver(game, driver)
        _selectedGameForConfig.value = _selectedGameForConfig.value?.copy(graphicsDriver = driver)
    }

    fun updateGameDisplayScale(game: GameItem, scale: DisplayResolutionScale) {
        catalogManager.updateGameDisplayScale(game, scale)
        _selectedGameForConfig.value = _selectedGameForConfig.value?.copy(displayScale = scale)
    }

    fun saveGameConfiguration(
        game: GameItem,
        driver: GraphicsDriver,
        displayScale: DisplayResolutionScale,
        deepHibernate: Boolean,
        hibernateGoogle: Boolean,
        enableOverlayHud: Boolean
    ) {
        val updated = catalogManager.saveGameConfiguration(
            game, driver, displayScale, deepHibernate, hibernateGoogle, enableOverlayHud
        )
        _selectedGameForConfig.value = updated
    }

    /**
     * Layer 5: Tests a custom resolution and density for 15 seconds with an automatic countdown and failsafe rollback.
     */
    fun startResolutionTest(scale: DisplayResolutionScale) {
        if (!ShizukuManager.isAuthorized) {
            Toast.makeText(getApplication(), "Requiere autorización Shizuku activa", Toast.LENGTH_SHORT).show()
            return
        }

        _pendingTestScale.value = scale
        _testCountdownSeconds.value = 15
        _isTestingResolution.value = true

        viewModelScope.launch {
            DisplayScaleController.applyDisplayScale(getApplication(), scale, true)

            resolutionTestJob?.cancel()
            resolutionTestJob = viewModelScope.launch {
                for (sec in 15 downTo 1) {
                    _testCountdownSeconds.value = sec
                    delay(1000L)
                }
                // Time expired without user confirmation -> automatic rollback
                cancelResolutionTest()
                Toast.makeText(getApplication(), "⏱️ Tiempo agotado. Pantalla restaurada por seguridad.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Confirms the tested resolution and saves it to the selected game configuration.
     */
    fun confirmResolutionTest(targetGame: GameItem?) {
        resolutionTestJob?.cancel()
        _isTestingResolution.value = false
        val scale = _pendingTestScale.value ?: DisplayResolutionScale.NATIVE_100
        _pendingTestScale.value = null

        if (targetGame != null) {
            updateGameDisplayScale(targetGame, scale)
            Toast.makeText(getApplication(), "✅ Escala ${scale.tag} confirmada y guardada para ${targetGame.title}", Toast.LENGTH_SHORT).show()
        }

        // Revert display back to native until game actually launches
        viewModelScope.launch {
            DisplayScaleController.resetDisplayScale(getApplication(), ShizukuManager.isAuthorized)
        }
    }

    /**
     * Cancels the resolution test immediately and restores factory display settings.
     */
    fun cancelResolutionTest() {
        resolutionTestJob?.cancel()
        _isTestingResolution.value = false
        _pendingTestScale.value = null

        viewModelScope.launch {
            DisplayScaleController.resetDisplayScale(getApplication(), ShizukuManager.isAuthorized)
        }
    }

    fun toggleSpeedTester(show: Boolean) {
        _showSpeedTester.value = show
    }

    fun dismissBoostDialog() {
        _showBoostDialog.value = false
        val game = _pendingLaunchGame.value
        _pendingLaunchGame.value = null
        if (game != null) {
            launchGameDirectly(game)
        }
    }

    fun addGameToDashboard(app: GameItem) {
        catalogManager.addGame(app)
        _showAddGameSheet.value = false
        Toast.makeText(getApplication(), "${app.title} añadido a juegos optimizados", Toast.LENGTH_SHORT).show()
    }

    fun removeGameFromDashboard(app: GameItem) {
        catalogManager.removeGame(app)
    }

    /**
     * Reverts any applied GPU render driver and restores system when the user returns to the Game Booster app.
     * Keeps Android OS graphics state and background services in their clean, stock state.
     */
    fun restoreSystemGraphicsIfActive() {
        val activeGame = _activeRunningBoostedGame.value ?: return
        viewModelScope.launch {
            GameWatcherService.stop(getApplication())
            GameOverlayService.stop(getApplication())
            ShizukuManager.restoreGameGraphicsDriver(activeGame.packageName)
            DisplayScaleController.resetDisplayScale(getApplication(), ShizukuManager.isAuthorized)
            if (prefs.isGoogleServicesSuspended()) {
                ShizukuManager.restoreGooglePlayServices()
                prefs.setGoogleServicesSuspended(false)
            }
            _activeRunningBoostedGame.value = null
            Toast.makeText(
                getApplication(),
                "Configuración de pantalla, gráficos y servicios restaurados",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun triggerBoost(
        targetGame: GameItem? = null,
        forcedDriver: GraphicsDriver? = null,
        forcedDisplayScale: DisplayResolutionScale? = null,
        deepHibernate: Boolean? = null,
        hibernateGoogle: Boolean? = null,
        enableOverlayHud: Boolean? = null
    ) {
        if (_isBoosting.value) return

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

        val configuredGame = targetGame?.copy(
            graphicsDriver = driverToApply,
            displayScale = scaleToApply,
            deepBackgroundHibernate = useDeepHib,
            hibernateGoogleServices = useGoogleHib,
            enableOverlayHud = useOverlay
        )

        // Show floating HUD immediately if overlay permission is granted
        val context = getApplication<Application>()
        if (useOverlay) {
            if (hasOverlayPermission()) {
                GameOverlayService.start(
                    context = context,
                    packageName = configuredGame?.packageName,
                    gameTitle = configuredGame?.title ?: "Juego Optimizado",
                    driver = driverToApply
                )
            } else {
                requestOverlayPermission()
                Toast.makeText(
                    context,
                    "Concede permiso de superposición para mostrar el HUD flotante",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        _selectedGameForConfig.value = null
        _pendingLaunchGame.value = configuredGame
        _isBoosting.value = true
        _showBoostDialog.value = true
        _boostProgress.value = 0f

        viewModelScope.launch {
            val executionResult = boostOrchestrator.executeBoostPipeline(
                targetGame = targetGame,
                forcedDriver = forcedDriver,
                forcedDisplayScale = forcedDisplayScale,
                deepHibernate = deepHibernate,
                hibernateGoogle = hibernateGoogle,
                enableOverlayHud = enableOverlayHud,
                installedApps = catalogManager.installedApps.value,
                activeProfile = _activeProfile.value,
                prevMetrics = telemetryManager.metrics.value,
                onProgressUpdate = { statusText, progress ->
                    _boostStatusText.value = statusText
                    _boostProgress.value = progress
                }
            )

            telemetryManager.updateMetrics(executionResult.updatedMetrics)
            _lastBoostResult.value = executionResult.boostResult
            if (executionResult.activeGame != null) {
                _activeRunningBoostedGame.value = executionResult.activeGame
            }
            _isBoosting.value = false
        }
    }

    private fun launchGameDirectly(game: GameItem) {
        val context = getApplication<Application>()
        if (hasOverlayPermission()) {
            GameOverlayService.start(
                context = context,
                packageName = game.packageName,
                gameTitle = game.title,
                driver = game.graphicsDriver
            )
        } else {
            requestOverlayPermission()
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(game.packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            Toast.makeText(context, "No se pudo abrir ${game.title}", Toast.LENGTH_SHORT).show()
        }
    }

    fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(getApplication())
        } else {
            true
        }
    }

    fun requestOverlayPermission() {
        val context = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun toggleTestOverlayHud() {
        val context = getApplication<Application>()
        if (hasOverlayPermission()) {
            val active = gamesList.value.firstOrNull()
            GameOverlayService.start(
                context = context,
                packageName = active?.packageName,
                gameTitle = active?.title ?: "Juego de Prueba",
                driver = active?.graphicsDriver ?: GraphicsDriver.SYSTEM_DEFAULT
            )
            Toast.makeText(context, "🎮 Burbuja Gamer flotante activada", Toast.LENGTH_SHORT).show()
        } else {
            requestOverlayPermission()
            Toast.makeText(context, "Concede el permiso para mostrar el HUD sobre los juegos", Toast.LENGTH_LONG).show()
        }
    }
}
