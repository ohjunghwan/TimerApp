package me.devhi.timer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

val tintColor = Color.parseColor("#03DAC5")
val progressPaint = Paint().apply {
    strokeWidth = 30f
    color = tintColor
    style = Paint.Style.STROKE
}
val textPaint = Paint().apply {
    color = Color.BLACK
    style = Paint.Style.FILL
    textSize = 60f
}
val backgroundCirclePaint = Paint().apply {
    color = Color.parseColor("#EBEBEB")
    strokeWidth = 80f
    style = Paint.Style.STROKE
    textSize = 20f
}

const val padding = 50f
const val sweepAmount = 2f


class TimerView constructor(context: Context, attributeSet: AttributeSet) :
    View(context, attributeSet) {
    private var isTouching = false
    private lateinit var center: PointF
    private lateinit var rect: RectF
    private var radius: Float = 0f
    private var sweepAngle = 20f
    private var isTimerStop = false
    private lateinit var currentPoint: PointF

    interface OnTimeEventListener {
        fun onTimeExhausted()

        fun onTimerShouldStart()
    }

    private lateinit var listener: OnTimeEventListener

    fun setOnTimeExhaustedListener(listener: OnTimeEventListener) {
        this.listener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center = PointF(w / 2f, h / 2f)
        radius = min(w.toFloat(), h.toFloat()) / 2 - padding
        rect = RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawCircle(center.x, center.y, radius, backgroundCirclePaint)
        canvas?.drawArc(rect, 270f, sweepAngle, false, progressPaint)
        canvas?.drawText(
            convertToFormatString(sweepAngle),
            center.x - (textPaint.measureText(convertToFormatString(sweepAngle)) / 2),
            center.y - (textPaint.descent() + textPaint.ascent()) / 2,
            textPaint
        )
    }

    fun consume() {
        if (isTouching) return
        decreaseProgress(0.1f)
        invalidate()
    }

    private fun increaseProgressBySweep() {
        sweepAngle += sweepAmount
        if (sweepAngle >= 360f) sweepAngle = 360f
    }

    private fun decreaseProgressBySweep() {
        decreaseProgress(sweepAmount)
    }

    private fun decreaseProgress(amount: Float) {
        if (!isTouching && sweepAngle == 0f) {
            listener.onTimeExhausted()
            isTimerStop = true
            return
        }
        sweepAngle -= amount

        if (sweepAngle < 0f)
            sweepAngle = 0f

    }

    private fun convertToFormatString(sweep: Float): String {
        val time = (sweep * 10).toInt()
        return "${time / 60}분 ${if (time % 60 < 10) "0" + (time % 60) else time % 60} 초"
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> onActionDown(it.x, it.y)
                MotionEvent.ACTION_MOVE -> onActionMove(it.x, it.y)
                MotionEvent.ACTION_UP -> onActionUp()
            }
            invalidate()
        }
        return true
    }


    private fun onActionDown(x: Float, y: Float) {
        currentPoint = PointF(x, y)
        isTouching = true
    }

    private fun onActionUp() {
        isTouching = false
        if (isTimerStop && sweepAngle != 0f) {
            isTimerStop = false
            listener.onTimerShouldStart()
        }
    }

    private fun onActionMove(x: Float, y: Float) {
        if (x >= width / 2 && y < height / 2) {
            if (currentPoint.x <= x && currentPoint.y <= y) {
                increaseProgressBySweep()
            } else {
                decreaseProgressBySweep()
            }
        } else if (x >= width / 2 && y >= height / 2) {
            if (currentPoint.x >= x && currentPoint.y <= y) {
                increaseProgressBySweep()
            } else {
                decreaseProgressBySweep()
            }
        } else if (x < width / 2 && y >= height / 2) {
            if (currentPoint.x >= x && currentPoint.y >= y) {
                increaseProgressBySweep()
            } else {
                decreaseProgressBySweep()
            }
        } else {
            if (currentPoint.x <= x && currentPoint.y >= y) {
                increaseProgressBySweep()
            } else {
                decreaseProgressBySweep()
            }
        }
        currentPoint.x = x
        currentPoint.y = y
    }


}