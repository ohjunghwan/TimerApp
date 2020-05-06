package me.devhi.timer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.Vibrator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var timerService: TimerService
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            timerService = (service as TimerService.TimerBinder).service
            timerService.activity = this@MainActivity
            timerService.startTimer()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }

    }

    fun consumeTime() {
        timer_view.consume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        timer_view.setOnTimeExhaustedListener(object : TimerView.OnTimeEventListener {
            override fun onTimeExhausted() {
                timerService.stopTimer()
                (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(2000)
            }

            override fun onTimerShouldStart() {
                timerService.startTimer()
            }
        })

        val intent = Intent(this@MainActivity, TimerService::class.java)
        bindService(intent, connection, BIND_AUTO_CREATE)

        toggle_button.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                timerService.stopTimer()
            } else {
                timerService.startTimer()
            }
        }
    }
}
