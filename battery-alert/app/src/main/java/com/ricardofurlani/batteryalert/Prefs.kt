package com.ricardofurlani.batteryalert

import android.content.Context

object Prefs {
    private const val FILE = "battery_alert"
    private const val KEY_ENABLED = "monitoring_enabled"
    private const val KEY_ALERTED = "alerted_this_charge"

    private fun prefs(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun isMonitoringEnabled(context: Context) = prefs(context).getBoolean(KEY_ENABLED, false)

    fun setMonitoringEnabled(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()

    /** Evita repetir o alerta enquanto o aparelho continua no carregador após chegar a 100%. */
    fun hasAlertedThisCharge(context: Context) = prefs(context).getBoolean(KEY_ALERTED, false)

    fun setAlertedThisCharge(context: Context, alerted: Boolean) =
        prefs(context).edit().putBoolean(KEY_ALERTED, alerted).apply()
}
