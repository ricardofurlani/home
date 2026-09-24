package com.ricardofurlani.batteryalert

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Reinicia o monitoramento após reiniciar o aparelho ou atualizar o app. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                if (!Prefs.isMonitoringEnabled(context)) return
                try {
                    BatteryMonitorService.start(context)
                } catch (e: IllegalStateException) {
                    // Sistema recusou iniciar o serviço em segundo plano; o usuário pode abrir o app.
                }
            }
        }
    }
}
