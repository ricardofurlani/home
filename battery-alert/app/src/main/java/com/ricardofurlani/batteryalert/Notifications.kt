package com.ricardofurlani.batteryalert

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager

object Notifications {
    const val CHANNEL_MONITOR = "monitor"
    const val CHANNEL_ALERT = "battery_full_alarm"
    const val ID_MONITOR = 1
    const val ID_ALERT = 2

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val monitor = NotificationChannel(
            CHANNEL_MONITOR,
            context.getString(R.string.channel_monitor_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.channel_monitor_desc)
            setShowBadge(false)
        }

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val alert = NotificationChannel(
            CHANNEL_ALERT,
            context.getString(R.string.channel_alert_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_alert_desc)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 600, 300, 600, 300, 600)
            enableLights(true)
            setSound(
                alarmSound,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }

        manager.createNotificationChannels(listOf(monitor, alert))
    }

    private fun openAppIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    fun monitorNotification(context: Context, percent: Int): Notification =
        Notification.Builder(context, CHANNEL_MONITOR)
            .setSmallIcon(R.drawable.ic_bolt)
            .setContentTitle(context.getString(R.string.monitor_notification_title, percent.coerceAtLeast(0)))
            .setContentText(context.getString(R.string.monitoring_on))
            .setContentIntent(openAppIntent(context))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

    fun updateMonitor(context: Context, percent: Int) {
        context.getSystemService(NotificationManager::class.java)
            .notify(ID_MONITOR, monitorNotification(context, percent))
    }

    fun showFullAlert(context: Context) {
        val dismiss = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, DismissAlertReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(context, CHANNEL_ALERT)
            .setSmallIcon(R.drawable.ic_bolt)
            .setContentTitle(context.getString(R.string.alert_title))
            .setContentText(context.getString(R.string.alert_text))
            .setCategory(Notification.CATEGORY_ALARM)
            .setContentIntent(openAppIntent(context))
            .setDeleteIntent(dismiss)
            .setAutoCancel(true)
            .addAction(
                Notification.Action.Builder(null, context.getString(R.string.alert_dismiss), dismiss).build()
            )
            .build()
        // Repete o som até o usuário interagir com a notificação ou desconectar o carregador.
        notification.flags = notification.flags or Notification.FLAG_INSISTENT

        context.getSystemService(NotificationManager::class.java).notify(ID_ALERT, notification)
    }

    fun cancelFullAlert(context: Context) {
        context.getSystemService(NotificationManager::class.java).cancel(ID_ALERT)
    }
}
