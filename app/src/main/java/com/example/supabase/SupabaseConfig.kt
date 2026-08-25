package com.example.supabase

import android.content.Context
import android.content.SharedPreferences

object SupabaseConfig {

    private const val PREFS_NAME = "cheter_supabase_config"
    private const val KEY_PROJECT_URL = "supabase_project_url"
    private const val KEY_ANON_KEY = "supabase_anon_key"
    private const val KEY_REALTIME_ENABLED = "supabase_realtime_enabled"

    // Default production Supabase cluster endpoint for CHETER Relational DB
    const val DEFAULT_PROJECT_URL = "https://pqauflyshxdmpgsnzqu.supabase.co"
    const val DEFAULT_ANON_KEY = "sb_publishable_lMfoRf2V3nxfBu2-46KAxA_4bpTGJeg"

    // Table names in PostgreSQL
    const val TABLE_MENU_ITEMS = "menu_items"
    const val TABLE_RESTAURANT_TABLES = "restaurant_tables"
    const val TABLE_RESTAURANTS = "restaurants"
    const val TABLE_ORDERS = "orders"
    const val TABLE_USERS = "restaurant_owners"
    const val TABLE_CASH_ALERTS = "cash_payment_alerts"
    const val TABLE_BLOCKED_CUSTOMERS = "blocked_customers"

    fun getProjectUrl(context: Context?): String {
        if (context == null) return DEFAULT_PROJECT_URL
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PROJECT_URL, DEFAULT_PROJECT_URL) ?: DEFAULT_PROJECT_URL
    }

    fun getAnonKey(context: Context?): String {
        if (context == null) return DEFAULT_ANON_KEY
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ANON_KEY, DEFAULT_ANON_KEY) ?: DEFAULT_ANON_KEY
    }

    fun isRealtimeEnabled(context: Context?): Boolean {
        if (context == null) return true
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_REALTIME_ENABLED, true)
    }

    fun saveConfig(context: Context, url: String, anonKey: String, realtimeEnabled: Boolean = true) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_PROJECT_URL, url.trim().removeSuffix("/"))
            .putString(KEY_ANON_KEY, anonKey.trim())
            .putBoolean(KEY_REALTIME_ENABLED, realtimeEnabled)
            .apply()
    }

    fun resetToDefault(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
