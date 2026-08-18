package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.components.AppPickerSheet
import com.example.ui.components.BoostDialog
import com.example.ui.components.BoostProfileSelector
import com.example.ui.components.BoosterHeaderBar
import com.example.ui.components.GameConfigSheet
import com.example.ui.components.GameLauncherSection
import com.example.ui.components.GamerHudGauge
import com.example.ui.components.MetricCardsGrid
import com.example.ui.components.QuickToolsBanner
import com.example.ui.components.ShizukuControlCard
import com.example.ui.components.SpeedTestDialog
import com.example.ui.theme.GamerDarkBackground
import com.example.ui.viewmodel.BoosterViewModel

@Composable
fun BoosterHomeScreen(
    viewModel: BoosterViewModel,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.metrics.collectAsState()
    val shizukuStatus by viewModel.shizukuStatus.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val isBoosting by viewModel.isBoosting.collectAsState()
    val boostProgress by viewModel.boostProgress.collectAsState()
    val boostStatusText by viewModel.boostStatusText.collectAsState()
    val lastBoostResult by viewModel.lastBoostResult.collectAsState()
    val showBoostDialog by viewModel.showBoostDialog.collectAsState()
    val gamesList by viewModel.gamesList.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val showAddGameSheet by viewModel.showAddGameSheet.collectAsState()
    val showSpeedTester by viewModel.showSpeedTester.collectAsState()
    val selectedGameForConfig by viewModel.selectedGameForConfig.collectAsState()
    val isTestingResolution by viewModel.isTestingResolution.collectAsState()
    val testCountdownSeconds by viewModel.testCountdownSeconds.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GamerDarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            item {
                Spacer(modifier = Modifier.height(4.dp))
                BoosterHeaderBar(
                    deviceModel = metrics.deviceModel,
                    refreshRate = metrics.refreshRateHz,
                    shizukuState = shizukuStatus.state,
                    onOpenSpeedTest = { viewModel.toggleSpeedTester(true) },
                    onRefreshShizuku = { viewModel.refreshShizuku() },
                    onToggleOverlayHud = { viewModel.toggleTestOverlayHud() }
                )
            }

            // Central Interactive HUD Gauge
            item {
                GamerHudGauge(
                    metrics = metrics,
                    isBoosting = isBoosting,
                    onBoostClick = { viewModel.triggerBoost() }
                )
            }

            // Shizuku Elevated ADB / Root Controller Card
            item {
                ShizukuControlCard(
                    shizukuStatus = shizukuStatus,
                    onRequestPermission = { viewModel.requestShizukuPermission() },
                    onOpenShizuku = { viewModel.openShizukuApp() },
                    onRefreshStatus = { viewModel.refreshShizuku() }
                )
            }

            // Performance Mode Selector
            item {
                BoostProfileSelector(
                    selectedProfile = activeProfile,
                    onSelectProfile = { viewModel.setProfile(it) }
                )
            }

            // Real-time Telemetry Grid
            item {
                MetricCardsGrid(
                    metrics = metrics,
                    onPingCardClick = { viewModel.toggleSpeedTester(true) },
                    onStorageCardClick = { viewModel.triggerBoost() }
                )
            }

            // Game Library / Launcher
            item {
                GameLauncherSection(
                    games = gamesList,
                    onAddGameClick = { viewModel.toggleAddGameSheet(true) },
                    onLaunchGame = { game -> viewModel.openGameConfig(game) },
                    onConfigureGame = { game -> viewModel.openGameConfig(game) },
                    onRemoveGame = { game -> viewModel.removeGameFromDashboard(game) }
                )
            }

            // Quick Deep Clean & Optimization Banner
            item {
                QuickToolsBanner(
                    onDeepClean = { viewModel.triggerBoost() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Individual Game Configuration Sheet
    selectedGameForConfig?.let { gameToConfig ->
        GameConfigSheet(
            game = gameToConfig,
            isShizukuAuthorized = shizukuStatus.state == com.example.util.ShizukuState.AUTHORIZED,
            isTestingResolution = isTestingResolution,
            testCountdownSeconds = testCountdownSeconds,
            onDriverSelected = { driver ->
                viewModel.updateGameGraphicsDriver(gameToConfig, driver)
            },
            onScaleSelected = { scale ->
                viewModel.updateGameDisplayScale(gameToConfig, scale)
            },
            onTestScale = { scale ->
                viewModel.startResolutionTest(scale)
            },
            onConfirmTest = { game ->
                viewModel.confirmResolutionTest(game)
            },
            onCancelTest = {
                viewModel.cancelResolutionTest()
            },
            onSaveConfig = { driver, scale, deepHib, hibGoogle, overlayHud, dnd, allowCalls, blockHeadsUp ->
                viewModel.saveGameConfiguration(gameToConfig, driver, scale, deepHib, hibGoogle, overlayHud, dnd, allowCalls, blockHeadsUp)
            },
            onBoostAndLaunch = { game, driver, scale, deepHib, hibGoogle, overlayHud, dnd, allowCalls, blockHeadsUp ->
                viewModel.triggerBoost(
                    targetGame = game,
                    forcedDriver = driver,
                    forcedDisplayScale = scale,
                    deepHibernate = deepHib,
                    hibernateGoogle = hibGoogle,
                    enableOverlayHud = overlayHud,
                    enableDnd = dnd,
                    dndAllowCalls = allowCalls,
                    dndBlockHeadsUp = blockHeadsUp
                )
            },
            onDismiss = { viewModel.closeGameConfig() }
        )
    }

    // Boost in-progress or summary dialog
    if (showBoostDialog) {
        BoostDialog(
            isBoosting = isBoosting,
            progress = boostProgress,
            statusText = boostStatusText,
            result = lastBoostResult,
            onDismiss = { viewModel.dismissBoostDialog() }
        )
    }

    // Add Game from installed apps Sheet
    if (showAddGameSheet) {
        AppPickerSheet(
            installedApps = installedApps,
            addedPackageNames = gamesList.map { it.packageName }.toSet(),
            onSelectApp = { app -> viewModel.addGameToDashboard(app) },
            onDismiss = { viewModel.toggleAddGameSheet(false) }
        )
    }

    // Network & Ping Diagnostic Dialog
    if (showSpeedTester) {
        SpeedTestDialog(
            currentMetrics = metrics,
            onDismiss = { viewModel.toggleSpeedTester(false) }
        )
    }
}
