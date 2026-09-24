package com.ricardofurlani.batteryalert

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder

/**
 * Serviço em primeiro plano que escuta ACTION_BATTERY_CHANGED (esse broadcast só pode ser
 * recebido por receivers registrados em tempo de execução) e dispara o alerta ao chegar a 100%.
 */
class BatteryMonitorService : Service() {

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = handle(BatteryState.from(intent))
    }

    private var lastPercent = -1

    override fun onCreate() {
        super.onCreate()
        Notifications.createChannels(this)
        val notification = Notifications.monitorNotification(this, currentPercent())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                Notifications.ID_MONITOR,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(Notifications.ID_MONITOR, notification)
        }
        // O broadcast é "sticky": o registro já entrega o estado atual.
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Prefs.isMonitoringEnabled(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(batteryReceiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun currentPercent(): Int =
        registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?.let { BatteryState.from(it).percent } ?: -1

    private fun handle(state: BatteryState) {
        if (state.percent != lastPercent) {
            lastPercent = state.percent
            Notifications.updateMonitor(this, state.percent)
        }

        if (!state.plugged) {
            // Carregador removido: rearma o alerta para a próxima carga.
            if (Prefs.hasAlertedThisCharge(this)) Prefs.setAlertedThisCharge(this, false)
            Notifications.cancelFullAlert(this)
            return
        }

        if (state.full && !Prefs.hasAlertedThisCharge(this)) {
            Prefs.setAlertedThisCharge(this, true)
            Notifications.showFullAlert(this)
        }
    }

    companion object {
        fun start(context: Context) {
            context.startForegroundService(Intent(context, BatteryMonitorService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, BatteryMonitorService::class.java))
        }
    }
}
