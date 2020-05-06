package me.devhi.timer

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class TimerService : Service(), CoroutineScope {
    private var job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    lateinit var activity: MainActivity
    private val binder by lazy {
        TimerBinder(this@TimerService)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    fun startTimer() {
        if (job.isCompleted) {
            job = Job()
        }
        launch {
            while (isActive) {
                delay(1000)
                activity.consumeTime()
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