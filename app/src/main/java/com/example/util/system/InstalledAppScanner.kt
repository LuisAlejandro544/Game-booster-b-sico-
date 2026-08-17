package com.example.util.system

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.data.BoosterPreferences
import com.example.model.GameItem

/**
 * Scans installed Android packages to detect games and gaming-related apps.
 */
object InstalledAppScanner {

    fun getInstalledAppsAndGames(context: Context): List<GameItem> {
        val pm = context.packageManager
        val prefs = BoosterPreferences(context)
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val list = mutableListOf<GameItem>()

        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == context.packageName) continue

            val appInfo = try {
                pm.getApplicationInfo(pkg, 0)
            } catch (_: Exception) {
                continue
            }

            val appName = info.loadLabel(pm).toString()
            val icon = info.loadIcon(pm)

            val isGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appInfo.category == ApplicationInfo.CATEGORY_GAME
            } else {
                (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }

            val isGameKeyword = pkg.contains("game", ignoreCase = true) ||
                    pkg.contains("play", ignoreCase = true) ||
                    pkg.contains("pubg", ignoreCase = true) ||
                    pkg.contains("freefire", ignoreCase = true) ||
                    pkg.contains("roblox", ignoreCase = true) ||
                    pkg.contains("riot", ignoreCase = true) ||
                    pkg.contains("unity", ignoreCase = true) ||
                    pkg.contains("epic", ignoreCase = true)

            val savedDriver = prefs.getGameDriver(pkg)

            list.add(
                GameItem(
                    id = pkg,
                    title = appName,
                    packageName = pkg,
                    isBuiltIn = false,
                    category = if (isGame || isGameKeyword) "Juego" else "Aplicación",
                    iconDrawable = icon,
                    isCustomAdded = false,
                    graphicsDriver = savedDriver
                )
            )
        }

        return list.sortedBy { it.title }
    }
}
