package com.ricardofurlani.batteryalert

import android.content.Intent
import android.os.BatteryManager

/** Leitura de um intent ACTION_BATTERY_CHANGED. */
data class BatteryState(val percent: Int, val plugged: Boolean, val full: Boolean) {
    companion object {
        fun from(intent: Intent): BatteryState {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            val percent = if (level >= 0 && scale > 0) level * 100 / scale else -1
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val full = percent >= 100 || status == BatteryManager.BATTERY_STATUS_FULL
            return BatteryState(percent, plugged, full)
        }
    }
}
