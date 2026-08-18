package com.example.ui.viewmodel.modules

import android.content.Context
import com.example.data.BoosterPreferences
import com.example.model.DisplayResolutionScale
import com.example.model.GameItem
import com.example.model.GraphicsDriver
import com.example.util.SystemInfoHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameCatalogManager(
    private val context: Context,
    private val prefs: BoosterPreferences,
    private val scope: CoroutineScope
) {
    private val _gamesList = MutableStateFlow<List<GameItem>>(emptyList())
    val gamesList: StateFlow<List<GameItem>> = _gamesList.asStateFlow()

    private val _installedApps = MutableStateFlow<List<GameItem>>(emptyList())
    val installedApps: StateFlow<List<GameItem>> = _installedApps.asStateFlow()

    fun loadGames() {
        scope.launch(Dispatchers.IO) {
            val installed = SystemInfoHelper.getInstalledAppsAndGames(context)
            _installedApps.value = installed

            val addedPackages = prefs.getAddedGamePackages()
            val list = mutableListOf<GameItem>()

            // Only show games and apps explicitly added by the user
            for (app in installed) {
                if (addedPackages.contains(app.packageName)) {
                    val savedDriver = prefs.getGameDriver(app.packageName)
                    val savedScale = prefs.getGameDisplayScale(app.packageName)
                    val deepHib = prefs.getGameDeepHibernate(app.packageName)
                    val hibGoogle = prefs.getGameHibernateGoogle(app.packageName)
                    val overlayHud = prefs.getGameOverlayHud(app.packageName)
                    val dndEnabled = prefs.getGameDndEnabled(app.packageName)
                    val dndCalls = prefs.getDndAllowCalls()
                    val dndHeadsUp = prefs.getDndBlockHeadsUp()
                    list.add(
                        app.copy(
                            graphicsDriver = savedDriver,
                            displayScale = savedScale,
                            deepBackgroundHibernate = deepHib,
                            hibernateGoogleServices = hibGoogle,
                            enableOverlayHud = overlayHud,
                            enableDnd = dndEnabled,
                            dndAllowCalls = dndCalls,
                            dndBlockHeadsUp = dndHeadsUp,
                            isCustomAdded = true
                        )
                    )
                }
            }

            _gamesList.value = list
        }
    }

    fun addGame(app: GameItem) {
        prefs.addGamePackage(app.packageName)
        loadGames()
    }

    fun removeGame(app: GameItem) {
        prefs.removeGamePackage(app.packageName)
        loadGames()
    }

    fun saveGameConfiguration(
        game: GameItem,
        driver: GraphicsDriver,
        displayScale: DisplayResolutionScale,
        deepHibernate: Boolean,
        hibernateGoogle: Boolean,
        enableOverlayHud: Boolean,
        enableDnd: Boolean = true,
        dndAllowCalls: Boolean = true,
        dndBlockHeadsUp: Boolean = true
    ): GameItem {
        prefs.setGameDriver(game.packageName, driver)
        prefs.setGameDisplayScale(game.packageName, displayScale)
        prefs.setGameDeepHibernate(game.packageName, deepHibernate)
        prefs.setGameHibernateGoogle(game.packageName, hibernateGoogle)
        prefs.setGameOverlayHud(game.packageName, enableOverlayHud)
        prefs.setGameDndEnabled(game.packageName, enableDnd)
        prefs.setDndAllowCalls(dndAllowCalls)
        prefs.setDndBlockHeadsUp(dndBlockHeadsUp)

        val updatedGame = game.copy(
            graphicsDriver = driver,
            displayScale = displayScale,
            deepBackgroundHibernate = deepHibernate,
            hibernateGoogleServices = hibernateGoogle,
            enableOverlayHud = enableOverlayHud,
            enableDnd = enableDnd,
            dndAllowCalls = dndAllowCalls,
            dndBlockHeadsUp = dndBlockHeadsUp
        )

        _gamesList.value = _gamesList.value.map {
            if (it.packageName == game.packageName) updatedGame else it
        }

        return updatedGame
    }

    fun updateGameDriver(game: GameItem, driver: GraphicsDriver): GameItem {
        prefs.setGameDriver(game.packageName, driver)
        val updated = game.copy(graphicsDriver = driver)
        _gamesList.value = _gamesList.value.map {
            if (it.packageName == game.packageName) updated else it
        }
        return updated
    }

    fun updateGameDisplayScale(game: GameItem, scale: DisplayResolutionScale): GameItem {
        prefs.setGameDisplayScale(game.packageName, scale)
        val updated = game.copy(displayScale = scale)
        _gamesList.value = _gamesList.value.map {
            if (it.packageName == game.packageName) updated else it
        }
        return updated
    }
}
