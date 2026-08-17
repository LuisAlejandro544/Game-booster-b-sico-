package com.example.ui.viewmodel.modules

import android.content.Context
import com.example.data.BoosterPreferences
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

            // Auto-detect installed games + user manually added apps
            for (app in installed) {
                if (addedPackages.contains(app.packageName) || app.category == "Juego") {
                    val savedDriver = prefs.getGameDriver(app.packageName)
                    val deepHib = prefs.getGameDeepHibernate(app.packageName)
                    val hibGoogle = prefs.getGameHibernateGoogle(app.packageName)
                    val overlayHud = prefs.getGameOverlayHud(app.packageName)
                    list.add(
                        app.copy(
                            graphicsDriver = savedDriver,
                            deepBackgroundHibernate = deepHib,
                            hibernateGoogleServices = hibGoogle,
                            enableOverlayHud = overlayHud
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
        deepHibernate: Boolean,
        hibernateGoogle: Boolean,
        enableOverlayHud: Boolean
    ): GameItem {
        prefs.setGameDriver(game.packageName, driver)
        prefs.setGameDeepHibernate(game.packageName, deepHibernate)
        prefs.setGameHibernateGoogle(game.packageName, hibernateGoogle)
        prefs.setGameOverlayHud(game.packageName, enableOverlayHud)

        val updatedGame = game.copy(
            graphicsDriver = driver,
            deepBackgroundHibernate = deepHibernate,
            hibernateGoogleServices = hibernateGoogle,
            enableOverlayHud = enableOverlayHud
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
}
