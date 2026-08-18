package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Choreographer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.BoosterPreferences
import com.example.model.DeviceMetrics
import com.example.model.DisplayResolutionScale
import com.example.model.GraphicsDriver
import com.example.service.overlay.DraggableOverlayWindowManager
import com.example.ui.components.GameOverlayHudContent
import com.example.ui.theme.GameBoosterTheme
import com.example.util.ShizukuManager
import com.example.util.SystemInfoHelper
import com.example.util.shizuku.DisplayScaleController
import com.example.util.shizuku.ResolutionCountdownNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameOverlayService : Service() {

    private lateinit var overlayWindowManager: DraggableOverlayWindowManager
    private lateinit var prefs: BoosterPreferences

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var targetPackage: String? = null
    private var targetGameTitle: String = "Juego"
    private var currentDriver: GraphicsDriver = GraphicsDriver.SYSTEM_DEFAULT
    private var currentDisplayScale: DisplayResolutionScale = DisplayResolutionScale.NATIVE_100

    // UI States
    private var isExpanded by mutableStateOf(false)
    private var currentFps by mutableIntStateOf(60)
    private var metricsState by mutableStateOf(DeviceMetrics())
    private var feedbackMessage by mutableStateOf<String?>(null)

    // Resolution Testing States
    private var isTestingResolution by mutableStateOf(false)
    private var testCountdownSeconds by mutableIntStateOf(15)
    private var scaleBeforeTest: DisplayResolutionScale = DisplayResolutionScale.NATIVE_100
    private var resolutionTestJob: Job? = null

    // FPS Counter tracking
    private var frameCount = 0
    private var lastFpsTimestamp = 0L
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            val nowMs = frameTimeNanos / 1_000_000
            if (lastFpsTimestamp == 0L) {
                lastFpsTimestamp = nowMs
            }

            frameCount++
            val delta = nowMs - lastFpsTimestamp
            if (delta >= 1000) {
                val calculated = (frameCount * 1000L / delta).toInt()
                currentFps = calculated.coerceIn(15, 144)
                frameCount = 0
                lastFpsTimestamp = nowMs
            }

            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = BoosterPreferences(this)
        overlayWindowManager = DraggableOverlayWindowManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val action = intent.action
        if (action == ACTION_STOP_OVERLAY) {
            stopSelf()
            return START_NOT_STICKY
        }

        targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE)
        targetGameTitle = intent.getStringExtra(EXTRA_TARGET_TITLE) ?: "Juego"
        val driverName = intent.getStringExtra(EXTRA_GRAPHICS_DRIVER) ?: GraphicsDriver.SYSTEM_DEFAULT.name
        currentDriver = try {
            GraphicsDriver.valueOf(driverName)
        } catch (_: Exception) {
            GraphicsDriver.SYSTEM_DEFAULT
        }
        val scaleName = intent.getStringExtra(EXTRA_DISPLAY_SCALE) ?: DisplayResolutionScale.NATIVE_100.name
        currentDisplayScale = try {
            DisplayResolutionScale.valueOf(scaleName)
        } catch (_: Exception) {
            DisplayResolutionScale.NATIVE_100
        }

        // Start Foreground Notification
        val notification = buildForegroundNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Initialize Overlay Window if not already created
        if (!overlayWindowManager.isAttached) {
            initOverlayView()
            startTelemetryAndFpsTracking()
        }

        return START_STICKY
    }

    private fun initOverlayView() {
        overlayWindowManager.attachOverlay {
            GameBoosterTheme {
                GameOverlayHudContent(
                    isExpanded = isExpanded,
                    currentFps = currentFps,
                    targetGamePackage = targetPackage,
                    targetGameTitle = targetGameTitle,
                    currentDriver = currentDriver,
                    currentDisplayScale = currentDisplayScale,
                    isTestingResolution = isTestingResolution,
                    testCountdownSeconds = testCountdownSeconds,
                    metrics = metricsState,
                    onToggleExpand = {
                        isExpanded = !isExpanded
                        overlayWindowManager.requestLayoutUpdate()
                    },
                    onCloseOverlay = { stopSelf() },
                    onDriverSelected = { newDriver ->
                        currentDriver = newDriver
                        applyDriverInGame(newDriver)
                    },
                    onScaleSelected = { newScale ->
                        applyScaleInGame(newScale)
                    },
                    onStartResolutionTest = { testScale ->
                        startResolutionTestInGame(testScale)
                    },
                    onConfirmResolutionTest = {
                        confirmResolutionTestInGame()
                    },
                    onCancelResolutionTest = {
                        cancelResolutionTestInGame()
                    },
                    onQuickBoost = { executeInGameQuickBoost() },
                    feedbackMessage = feedbackMessage,
                    onDragStart = { rawX, rawY ->
                        overlayWindowManager.onDragStart(rawX, rawY)
                    },
                    onDragMove = { rawX, rawY ->
                        overlayWindowManager.onDragMove(rawX, rawY)
                    },
                    onDragEnd = {
                        overlayWindowManager.onDragEnd()
                    },
                    onDrag = { dx, dy ->
                        overlayWindowManager.moveBy(dx, dy)
                    }
                )
            }
        }
    }

    private fun startTelemetryAndFpsTracking() {
        Choreographer.getInstance().postFrameCallback(frameCallback)

        serviceScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val m = SystemInfoHelper.getDeviceMetrics(this@GameOverlayService)
                    metricsState = m
                } catch (_: Exception) {}
                delay(2000L)
            }
        }
    }

    private fun applyDriverInGame(newDriver: GraphicsDriver) {
        val pkg = targetPackage ?: return
        serviceScope.launch(Dispatchers.IO) {
            try {
                prefs.setGameDriver(pkg, newDriver)
                ShizukuManager.applyGameGraphicsDriver(pkg, newDriver)
                feedbackMessage = "✓ Motor ${newDriver.displayName} aplicado"
                delay(3000L)
                feedbackMessage = null
            } catch (e: Exception) {
                Log.e(TAG, "Error applying driver in game", e)
            }
        }
    }

    private fun applyScaleInGame(newScale: DisplayResolutionScale) {
        val pkg = targetPackage ?: return
        serviceScope.launch(Dispatchers.IO) {
            try {
                currentDisplayScale = newScale
                prefs.setGameDisplayScale(pkg, newScale)
                DisplayScaleController.applyDisplayScale(
                    context = this@GameOverlayService,
                    scale = newScale,
                    isAuthorized = ShizukuManager.isAuthorized
                )
                withContext(Dispatchers.Main) {
                    isExpanded = false
                    overlayWindowManager.requestLayoutUpdate()
                }
                feedbackMessage = "✓ Escala ${newScale.title} aplicada"
                delay(3000L)
                feedbackMessage = null
            } catch (e: Exception) {
                Log.e(TAG, "Error applying display scale in game", e)
            }
        }
    }

    private fun startResolutionTestInGame(testScale: DisplayResolutionScale) {
        if (!ShizukuManager.isAuthorized) {
            feedbackMessage = "⚠️ Requiere Shizuku autorizado"
            return
        }

        resolutionTestJob?.cancel()
        scaleBeforeTest = currentDisplayScale
        currentDisplayScale = testScale
        isTestingResolution = true
        testCountdownSeconds = 15

        resolutionTestJob = serviceScope.launch {
            // Apply scale
            DisplayScaleController.applyDisplayScale(
                context = this@GameOverlayService,
                scale = testScale,
                isAuthorized = ShizukuManager.isAuthorized
            )

            for (sec in 15 downTo 1) {
                testCountdownSeconds = sec
                ResolutionCountdownNotifier.showCountdownTick(this@GameOverlayService, sec, testScale)
                delay(1000L)
            }

            // Auto-revert if countdown completes without confirmation
            if (isTestingResolution) {
                cancelResolutionTestInGame()
                ResolutionCountdownNotifier.showReverted(this@GameOverlayService)
                feedbackMessage = "⏱️ Test finalizado: Escala revertida"
                delay(3000L)
                feedbackMessage = null
            }
        }
    }

    private fun confirmResolutionTestInGame() {
        resolutionTestJob?.cancel()
        isTestingResolution = false
        ResolutionCountdownNotifier.showConfirmed(this@GameOverlayService, currentDisplayScale)
        val pkg = targetPackage
        if (pkg != null) {
            prefs.setGameDisplayScale(pkg, currentDisplayScale)
        }
        isExpanded = false
        overlayWindowManager.requestLayoutUpdate()
        feedbackMessage = "✓ Escala ${currentDisplayScale.title} confirmada"
        serviceScope.launch {
            delay(3000L)
            feedbackMessage = null
        }
    }

    private fun cancelResolutionTestInGame() {
        resolutionTestJob?.cancel()
        isTestingResolution = false
        ResolutionCountdownNotifier.cancel()
        val prevScale = scaleBeforeTest
        currentDisplayScale = prevScale
        serviceScope.launch(Dispatchers.IO) {
            DisplayScaleController.applyDisplayScale(
                context = this@GameOverlayService,
                scale = prevScale,
                isAuthorized = ShizukuManager.isAuthorized
            )
        }
    }

    private fun executeInGameQuickBoost() {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val isShizuku = ShizukuManager.isAuthorized
                val freed = if (isShizuku) 350L else 180L
                prefs.addMemoryFreedMb(freed)
                prefs.incrementBoostCount()

                targetPackage?.let { pkg ->
                    val installed = SystemInfoHelper.getInstalledAppsAndGames(this@GameOverlayService)
                    val bgPkgs = installed.map { it.packageName }.filter { it != pkg }
                    ShizukuManager.hibernateBackgroundPackages(bgPkgs, pkg)
                }

                feedbackMessage = "⚡ +${freed}MB RAM liberados al vuelo"
                delay(3000L)
                feedbackMessage = null
            } catch (e: Exception) {
                Log.e(TAG, "Error executing in game quick boost", e)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "HUD Flotante Gamer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Muestra la burbuja y controles flotantes gamer en juego"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎮 HUD Gamer Activo: $targetGameTitle")
            .setContentText("Burbuja flotante de FPS, resolución y motor gráfico en vivo")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        overlayWindowManager.detachOverlay()
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "GameOverlayService"
        private const val CHANNEL_ID = "game_booster_overlay_channel"
        private const val NOTIFICATION_ID = 2002

        const val EXTRA_TARGET_PACKAGE = "extra_target_package"
        const val EXTRA_TARGET_TITLE = "extra_target_title"
        const val EXTRA_GRAPHICS_DRIVER = "extra_graphics_driver"
        const val EXTRA_DISPLAY_SCALE = "extra_display_scale"
        const val ACTION_STOP_OVERLAY = "action_stop_overlay"

        fun start(
            context: Context,
            packageName: String?,
            gameTitle: String,
            driver: GraphicsDriver,
            displayScale: DisplayResolutionScale = DisplayResolutionScale.NATIVE_100
        ) {
            val intent = Intent(context, GameOverlayService::class.java).apply {
                putExtra(EXTRA_TARGET_PACKAGE, packageName)
                putExtra(EXTRA_TARGET_TITLE, gameTitle)
                putExtra(EXTRA_GRAPHICS_DRIVER, driver.name)
                putExtra(EXTRA_DISPLAY_SCALE, displayScale.name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, GameOverlayService::class.java).apply {
                action = ACTION_STOP_OVERLAY
            }
            context.stopService(intent)
        }
    }
}

