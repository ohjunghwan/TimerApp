package me.devhi.timer

import android.content.Context
import android.graphics.PointF
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore


fun Double.toFormatString(): String {
    val time = (this * 10).toInt()
    return "${time / 60}분 ${if (time % 60 < 10) "0" + (time % 60) else time % 60}초"
}

fun Double.toMillisecond() = (this * 10 * 1_000).toInt()

fun PointF.distanceTo(point: PointF): Float {
    val dx = this.x - point.x
    val dy = this.y - point.y
    return dx * dx + dy * dy
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val soundKey = booleanPreferencesKey("SOUND_KEY")
val vibrateKey = booleanPreferencesKey("VIBRATE_KEY")
val screenKey = booleanPreferencesKey("SCREEN_KEY")