package com.example.data.preferences

import android.content.SharedPreferences

/**
 * Handles background app hibernation lists, user exceptions (whitelist), and custom targets (blacklist).
 */
class HibernationPreferences(private val prefs: SharedPreferences) {

    fun getHibernationExceptions(): Set<String> {
        val defaultExceptions = setOf(
            "com.discord",
            "com.spotify.music",
            "com.whatsapp",
            "org.telegram.messenger"
        )
        return prefs.getStringSet("hibernation_exceptions", defaultExceptions) ?: defaultExceptions
    }

    fun setHibernationExceptions(pkgs: Set<String>) {
        prefs.edit().putStringSet("hibernation_exceptions", pkgs).apply()
    }

    fun addHibernationException(pkg: String) {
        val set = getHibernationExceptions().toMutableSet()
        set.add(pkg)
        setHibernationExceptions(set)
    }

    fun removeHibernationException(pkg: String) {
        val set = getHibernationExceptions().toMutableSet()
        set.remove(pkg)
        setHibernationExceptions(set)
    }

    fun getHibernationCustomTargets(): Set<String> {
        return prefs.getStringSet("hibernation_custom_targets", emptySet()) ?: emptySet()
    }

    fun setHibernationCustomTargets(pkgs: Set<String>) {
        prefs.edit().putStringSet("hibernation_custom_targets", pkgs).apply()
    }

    fun addHibernationTarget(pkg: String) {
        val set = getHibernationCustomTargets().toMutableSet()
        set.add(pkg)
        setHibernationCustomTargets(set)
    }

    fun removeHibernationTarget(pkg: String) {
        val set = getHibernationCustomTargets().toMutableSet()
        set.remove(pkg)
        setHibernationCustomTargets(set)
    }

    fun getCurrentlyHibernatedPackages(): Set<String> {
        return prefs.getStringSet("currently_hibernated_pkgs", emptySet()) ?: emptySet()
    }

    fun setCurrentlyHibernatedPackages(pkgs: Set<String>) {
        prefs.edit().putStringSet("currently_hibernated_pkgs", pkgs).apply()
    }
}
