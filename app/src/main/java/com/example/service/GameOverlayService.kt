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
import com.example.service.overlay.FpsTracker
import com.example.service.overlay.OverlayGamerActions
import com.example.service.overlay.OverlayResolutionTester
import com.example.ui.components.GameOverlayHudContent
import com.example.ui.theme.GameBoosterTheme
import com.example.util.SystemInfoHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameOverlayService : Service() {

    private lateinit var overlayWindowManager: DraggableOverlayWindowManager
    private lateinit var prefs: BoosterPreferences
    private lateinit var fpsTracker: FpsTracker
    private lateinit var resolutionTester: OverlayResolutionTester
    private lateinit var gamerActions: OverlayGamerActions

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

    // DND & Hibernation in-game state
    private var isDndActive by mutableStateOf(true)
    private var blockHeadsUp by mutableStateOf(true)
    private var allowCalls by mutableStateOf(true)
    private var dndExceptions by mutableStateOf<Set<String>>(emptySet())
    private var hibernatedPackages by mutableStateOf<Set<String>>(emptySet())
    private var hibernationExceptions by mutableStateOf<Set<String>>(emptySet())

    // Resolution Testing States
    private var isTestingResolution by mutableStateOf(false)
    private var testCountdownSeconds by mutableIntStateOf(15)

    override fun onCreate() {
        super.onCreate()
        prefs = BoosterPreferences(this)
        overlayWindowManager = DraggableOverlayWindowManager(this)
        createNotificationChannel()

        fpsTracker = FpsTracker { fps -> currentFps = fps }
        resolutionTester = OverlayResolutionTester(this, serviceScope, prefs) { isTesting, countdown, activeScale, feedback ->
            isTestingResolution = isTesting
            testCountdownSeconds = countdown
            currentDisplayScale = activeScale
            if (feedback != null) {
                feedbackMessage = feedback
                serviceScope.launch {
                    delay(3000L)
                    feedbackMessage = null
                }
            }
        }
        gamerActions = OverlayGamerActions(this, serviceScope, prefs) { msg ->
            feedbackMessage = msg
        }

        // Load initial DND & Hibernation prefs
        isDndActive = targetPackage?.let { prefs.getGameDndEnabled(it) } ?: true
        blockHeadsUp = prefs.getDndBlockHeadsUp()
        allowCalls = prefs.getDndAllowCalls()
        dndExceptions = prefs.getDndExceptions()
        hibernatedPackages = prefs.getCurrentlyHibernatedPackages()
        hibernationExceptions = prefs.getHibernationExceptions()
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
        resolutionTester.setInitialScale(currentDisplayScale)

        targetPackage?.let {
            isDndActive = prefs.getGameDndEnabled(it)
        }
        blockHeadsUp = prefs.getDndBlockHeadsUp()
        allowCalls = prefs.getDndAllowCalls()
        dndExceptions = prefs.getDndExceptions()
        hibernatedPackages = prefs.getCurrentlyHibernatedPackages()
        hibernationExceptions = prefs.getHibernationExceptions()

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
                    isDndActive = isDndActive,
                    blockHeadsUp = blockHeadsUp,
                    allowCalls = allowCalls,
                    dndExceptions = dndExceptions,
                    hibernatedPackages = hibernatedPackages,
                    hibernationExceptions = hibernationExceptions,
                    onToggleDnd = { enabled ->
                        isDndActive = enabled
                        gamerActions.toggleDnd(targetPackage, enabled, allowCalls, blockHeadsUp, dndExceptions)
                    },
                    onToggleBlockHeadsUp = { blocked ->
                        blockHeadsUp = blocked
                        gamerActions.toggleHeadsUp(isDndActive, blocked, allowCalls, dndExceptions)
                    },
                    onToggleAllowCalls = { allowed ->
                        allowCalls = allowed
                        gamerActions.toggleAllowCalls(isDndActive, allowed, blockHeadsUp, dndExceptions)
                    },
                    onToggleDndAppException = { pkg ->
                        gamerActions.toggleDndAppException(
                            isDndActive = isDndActive,
                            pkg = pkg,
                            currentExceptions = dndExceptions,
                            allowCalls = allowCalls,
                            blockHeadsUp = blockHeadsUp
                        ) { updated -> dndExceptions = updated }
                    },
                    onToggleAppHibernation = { pkg, shouldHibernate ->
                        gamerActions.toggleAppHibernation(
                            targetPackage = targetPackage,
                            pkg = pkg,
                            shouldHibernate = shouldHibernate,
                            hibernatedPackages = hibernatedPackages,
                            hibernationExceptions = hibernationExceptions
                        ) { updatedHib, updatedEx ->
                            hibernatedPackages = updatedHib
                            hibernationExceptions = updatedEx
                        }
                    },
                    onToggleExpand = {
                        isExpanded = !isExpanded
                        overlayWindowManager.requestLayoutUpdate()
                    },
                    onCloseOverlay = { stopSelf() },
                    onDriverSelected = { newDriver ->
                        currentDriver = newDriver
                        gamerActions.applyDriver(targetPackage, newDriver)
                    },
                    onScaleSelected = { newScale ->
                        currentDisplayScale = newScale
                        gamerActions.applyScale(targetPackage, newScale) {
                            isExpanded = false
                            overlayWindowManager.requestLayoutUpdate()
                        }
                    },
                    onStartResolutionTest = { testScale ->
                        resolutionTester.startTest(testScale)
                    },
                    onConfirmResolutionTest = {
                        resolutionTester.confirmTest(targetPackage)
                        isExpanded = false
                        overlayWindowManager.requestLayoutUpdate()
                    },
                    onCancelResolutionTest = {
                        resolutionTester.cancelTest()
                    },
                    onQuickBoost = {
                        gamerActions.executeQuickBoost(targetPackage, hibernationExceptions)
                    },
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
        fpsTracker.start()

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
            .setContentText("Burbuja flotante de FPS, resolución, DND y motor gráfico")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        fpsTracker.stop()
        resolutionTester.cancel()
        overlayWindowManager.detachOverlay()
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
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
