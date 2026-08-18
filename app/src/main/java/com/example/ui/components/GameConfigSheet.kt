package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.model.DisplayResolutionScale
import com.example.model.GameItem
import com.example.model.GraphicsDriver
import com.example.ui.components.sheet.SheetActionButtons
import com.example.ui.components.sheet.SheetAdvancedOptionsSection
import com.example.ui.components.sheet.SheetDriversSection
import com.example.ui.components.sheet.SheetHeaderSection
import com.example.ui.components.sheet.SheetResolutionCountdownBanner
import com.example.ui.components.sheet.SheetResolutionSection
import com.example.ui.theme.GamerDarkBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameConfigSheet(
    game: GameItem,
    isShizukuAuthorized: Boolean,
    isTestingResolution: Boolean,
    testCountdownSeconds: Int,
    onDriverSelected: (GraphicsDriver) -> Unit,
    onScaleSelected: (DisplayResolutionScale) -> Unit,
    onTestScale: (DisplayResolutionScale) -> Unit,
    onConfirmTest: (GameItem) -> Unit,
    onCancelTest: () -> Unit,
    onSaveConfig: (GraphicsDriver, DisplayResolutionScale, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit,
    onBoostAndLaunch: (GameItem, GraphicsDriver, DisplayResolutionScale, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedDriver by remember(game) { mutableStateOf(game.graphicsDriver) }
    var selectedScale by remember(game) { mutableStateOf(game.displayScale) }
    var deepHibernate by remember(game) { mutableStateOf(game.deepBackgroundHibernate) }
    var hibernateGoogle by remember(game) { mutableStateOf(game.hibernateGoogleServices) }
    var enableOverlayHud by remember(game) { mutableStateOf(game.enableOverlayHud) }
    var enableDnd by remember(game) { mutableStateOf(game.enableDnd) }
    var dndAllowCalls by remember(game) { mutableStateOf(game.dndAllowCalls) }
    var dndBlockHeadsUp by remember(game) { mutableStateOf(game.dndBlockHeadsUp) }
    var enableTouchBoost by remember(game) { mutableStateOf(game.enableTouchBoost) }
    var enableWifiHighPerf by remember(game) { mutableStateOf(game.enableWifiHighPerf) }
    var enableCrosshair by remember(game) { mutableStateOf(game.enableCrosshair) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GamerDarkBackground,
        dragHandle = null,
        modifier = modifier.navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Title, icon and close button
            SheetHeaderSection(game = game, onDismiss = onDismiss)

            Spacer(modifier = Modifier.height(16.dp))

            // Active Countdown Banner during live testing
            AnimatedVisibility(visible = isTestingResolution) {
                Column {
                    SheetResolutionCountdownBanner(
                        game = game,
                        countdownSeconds = testCountdownSeconds,
                        onCancelTest = onCancelTest,
                        onConfirmTest = onConfirmTest
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // Section 1: GPU Drivers Selection
            SheetDriversSection(
                selectedDriver = selectedDriver,
                onDriverSelected = { driver ->
                    selectedDriver = driver
                    onDriverSelected(driver)
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Section 2: Screen Resolution & Proportional DPI Scaling (5-layer failsafe)
            SheetResolutionSection(
                selectedScale = selectedScale,
                isShizukuAuthorized = isShizukuAuthorized,
                onScaleSelected = { scale ->
                    selectedScale = scale
                    onScaleSelected(scale)
                },
                onTestScale = { scale ->
                    onTestScale(scale)
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Section 3: Advanced Optimization Options (Hibernation, GMS, DND, Touch, Wi-Fi, Crosshair)
            SheetAdvancedOptionsSection(
                deepHibernate = deepHibernate,
                onDeepHibernateChange = { deepHibernate = it },
                hibernateGoogle = hibernateGoogle,
                onHibernateGoogleChange = { hibernateGoogle = it },
                enableOverlayHud = enableOverlayHud,
                onEnableOverlayHudChange = { enableOverlayHud = it },
                enableDnd = enableDnd,
                onEnableDndChange = { enableDnd = it },
                dndAllowCalls = dndAllowCalls,
                onDndAllowCallsChange = { dndAllowCalls = it },
                dndBlockHeadsUp = dndBlockHeadsUp,
                onDndBlockHeadsUpChange = { dndBlockHeadsUp = it },
                enableTouchBoost = enableTouchBoost,
                onEnableTouchBoostChange = { enableTouchBoost = it },
                enableWifiHighPerf = enableWifiHighPerf,
                onEnableWifiHighPerfChange = { enableWifiHighPerf = it },
                enableCrosshair = enableCrosshair,
                onEnableCrosshairChange = { enableCrosshair = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section 4: Action Buttons (Save & Boost/Launch)
            SheetActionButtons(
                onSave = {
                    onSaveConfig(
                        selectedDriver,
                        selectedScale,
                        deepHibernate,
                        hibernateGoogle,
                        enableOverlayHud,
                        enableDnd,
                        dndAllowCalls,
                        dndBlockHeadsUp,
                        enableTouchBoost,
                        enableWifiHighPerf,
                        enableCrosshair
                    )
                    onDismiss()
                },
                onBoostAndLaunch = {
                    onBoostAndLaunch(
                        game,
                        selectedDriver,
                        selectedScale,
                        deepHibernate,
                        hibernateGoogle,
                        enableOverlayHud,
                        enableDnd,
                        dndAllowCalls,
                        dndBlockHeadsUp,
                        enableTouchBoost,
                        enableWifiHighPerf,
                        enableCrosshair
                    )
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
