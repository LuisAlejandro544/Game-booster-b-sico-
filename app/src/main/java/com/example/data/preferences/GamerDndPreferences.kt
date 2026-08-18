package com.example.data.preferences

import android.content.SharedPreferences

/**
 * Handles Gamer DND (Do Not Disturb), call blocking, heads-up blocking, and exceptions list.
 */
class GamerDndPreferences(private val prefs: SharedPreferences) {

    fun getGameDndEnabled(packageName: String): Boolean {
        return prefs.getBoolean("dnd_enabled_$packageName", true)
    }

    fun setGameDndEnabled(packageName: String, enabled: Boolean) {
        prefs.edit().putBoolean("dnd_enabled_$packageName", enabled).apply()
    }

    fun isDndActive(): Boolean {
        return prefs.getBoolean("dnd_is_active", false)
    }

    fun setDndActive(active: Boolean) {
        prefs.edit().putBoolean("dnd_is_active", active).apply()
    }

    fun getDndAllowCalls(): Boolean {
        return prefs.getBoolean("dnd_allow_calls", true)
    }

    fun setDndAllowCalls(allow: Boolean) {
        prefs.edit().putBoolean("dnd_allow_calls", allow).apply()
    }

    fun getDndBlockHeadsUp(): Boolean {
        return prefs.getBoolean("dnd_block_heads_up", true)
    }

    fun setDndBlockHeadsUp(block: Boolean) {
        prefs.edit().putBoolean("dnd_block_heads_up", block).apply()
    }

    fun getDndExceptions(): Set<String> {
        val defaultSet = setOf(
            "com.google.android.dialer",
            "com.android.dialer",
            "com.samsung.android.dialer",
            "com.discord",
            "com.whatsapp"
        )
        return prefs.getStringSet("dnd_exceptions", defaultSet) ?: defaultSet
    }

    fun setDndExceptions(pkgs: Set<String>) {
        prefs.edit().putStringSet("dnd_exceptions", pkgs).apply()
    }

    fun addDndException(pkg: String) {
        val set = getDndExceptions().toMutableSet()
        set.add(pkg)
        setDndExceptions(set)
    }

    fun removeDndException(pkg: String) {
        val set = getDndExceptions().toMutableSet()
        set.remove(pkg)
        setDndExceptions(set)
    }

    fun getSavedZenMode(): Int {
        return prefs.getInt("saved_zen_mode", -1)
    }

    fun setSavedZenMode(mode: Int) {
        prefs.edit().putInt("saved_zen_mode", mode).apply()
    }

    fun getSavedHeadsUp(): Int {
        return prefs.getInt("saved_heads_up", -1)
    }

    fun setSavedHeadsUp(state: Int) {
        prefs.edit().putInt("saved_heads_up", state).apply()
    }
}
