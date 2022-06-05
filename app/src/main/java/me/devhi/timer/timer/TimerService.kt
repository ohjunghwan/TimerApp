package me.devhi.timer.timer

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class TimerService : Service(), CoroutineScope {
    lateinit var consumer: TimerConsumer
    private val binder by lazy {
        TimerBinder(this@TimerService)
    }
    private var job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun startTimer() {
        if (job.isCompleted) {
            job = Job()
        }
        launch {
            while (isActive) {
                delay(1000)
                consumer.onTick()
            }
        }
    }

    fun stopTimer() {
        cancel()
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

    class TimerBinder(val service: TimerService) : Binder()
}