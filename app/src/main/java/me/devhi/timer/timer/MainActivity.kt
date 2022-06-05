package me.devhi.timer.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.devhi.timer.*
import me.devhi.timer.setting.SettingActivity
import java.util.*


class MainActivity : AppCompatActivity(), TimerConsumer, TimerView.OnTimeEventListener {
    private lateinit var timerService: TimerService
    private lateinit var timerView: TimerView
    private var shouldVibrate = false
    private var shouldSound = false
    private var shouldScreen = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            timerService = (service as TimerService.TimerBinder).service
            timerService.consumer = this@MainActivity
            timerService.startTimer()
        }

        override fun onServiceDisconnected(name: ComponentName?) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        timerView = findViewById(R.id.timer_view)
        timerView.setOnTimeExhaustedListener(this@MainActivity)
        val intent = Intent(this@MainActivity, TimerService::class.java)
        bindService(intent, connection, BIND_AUTO_CREATE)
        loadOptions()

    }


    private fun loadOptions() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                dataStore.data
                    .collect { preferences ->
                        shouldSound = preferences[soundKey] ?: false
                        shouldVibrate = preferences[vibrateKey] ?: false
                        setKeepScreen(preferences[screenKey] ?: false)
                    }
            }
        }
    }

    private fun setKeepScreen(keepScreen: Boolean) {
        if (keepScreen){
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_action_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_setting -> startActivity(Intent(this, SettingActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onTick() {
        timerView.consume()
    }

    private fun setAlarm(remainTime: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(
                this,
                100,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            Calendar.getInstance().timeInMillis + remainTime,
            pendingIntent
        )
    }

    override fun onTimerChanged(remainTime: Int) {
        setAlarm(remainTime)
    }

    override fun onTimeExhausted() {
        timerService.stopTimer()
        if (shouldVibrate) {
            (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(2000)
        }
        if (shouldSound) {
            val mediaPlayer: MediaPlayer? = MediaPlayer.create(this, R.raw.sound_dingdong)
            mediaPlayer?.start()
        }
    }

    override fun onTimerShouldRestart() {
        timerService.startTimer()
    }
}
