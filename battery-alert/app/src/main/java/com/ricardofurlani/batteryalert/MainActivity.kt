package com.ricardofurlani.batteryalert

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var levelText: TextView
    private lateinit var levelBar: ProgressBar
    private lateinit var chargeStatusText: TextView
    private lateinit var monitorSwitch: Switch
    private lateinit var monitorStatusText: TextView
    private lateinit var optimizationButton: Button
    private lateinit var optimizationHint: TextView

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = render(BatteryState.from(intent))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Notifications.createChannels(this)

        levelText = findViewById(R.id.levelText)
        levelBar = findViewById(R.id.levelBar)
        chargeStatusText = findViewById(R.id.chargeStatusText)
        monitorSwitch = findViewById(R.id.monitorSwitch)
        monitorStatusText = findViewById(R.id.monitorStatusText)
        optimizationButton = findViewById(R.id.optimizationButton)
        optimizationHint = findViewById(R.id.optimizationHint)

        monitorSwitch.isChecked = Prefs.isMonitoringEnabled(this)
        monitorSwitch.setOnCheckedChangeListener { _, checked -> setMonitoring(checked) }

        findViewById<Button>(R.id.testButton).setOnClickListener {
            if (ensureNotificationPermission()) Notifications.showFullAlert(this)
        }
        optimizationButton.setOnClickListener { requestIgnoreBatteryOptimizations() }

        // Primeira execução: liga o monitoramento por padrão.
        if (savedInstanceState == null && !getPreferences(MODE_PRIVATE).getBoolean("launched", false)) {
            getPreferences(MODE_PRIVATE).edit().putBoolean("launched", true).apply()
            monitorSwitch.isChecked = true
        } else if (Prefs.isMonitoringEnabled(this)) {
            BatteryMonitorService.start(this)
        }
        renderMonitoring()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?.let { render(BatteryState.from(it)) }
        renderOptimization()
    }

    override fun onPause() {
        unregisterReceiver(batteryReceiver)
        super.onPause()
    }

    private fun setMonitoring(enabled: Boolean) {
        Prefs.setMonitoringEnabled(this, enabled)
        if (enabled) {
            ensureNotificationPermission()
            BatteryMonitorService.start(this)
        } else {
            BatteryMonitorService.stop(this)
            Notifications.cancelFullAlert(this)
        }
        renderMonitoring()
    }

    private fun render(state: BatteryState) {
        levelText.text = if (state.percent >= 0) "${state.percent}%" else "--%"
        levelBar.progress = state.percent.coerceIn(0, 100)
        chargeStatusText.setText(
            when {
                state.plugged && state.full -> R.string.status_full
                state.plugged -> R.string.status_charging
                else -> R.string.status_discharging
            }
        )
    }

    private fun renderMonitoring() {
        monitorStatusText.setText(
            if (Prefs.isMonitoringEnabled(this)) R.string.monitoring_on else R.string.monitoring_off
        )
    }

    private fun renderOptimization() {
        val ignoring = getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(packageName)
        optimizationButton.visibility = if (ignoring) View.GONE else View.VISIBLE
        optimizationHint.setText(
            if (ignoring) R.string.battery_optimization_ok else R.string.battery_optimization_hint
        )
    }

    private fun requestIgnoreBatteryOptimizations() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            .setData(Uri.parse("package:$packageName"))
        try {
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }

    /** Retorna true se já é possível exibir notificações; caso contrário, solicita a permissão. */
    private fun ensureNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATIONS)
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATIONS &&
            grantResults.firstOrNull() != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, R.string.notification_permission_denied, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val REQUEST_NOTIFICATIONS = 1
    }
}
