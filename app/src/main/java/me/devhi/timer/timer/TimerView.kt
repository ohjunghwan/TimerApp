package me.devhi.timer.timer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import me.devhi.timer.distanceTo
import me.devhi.timer.toFormatString
import me.devhi.timer.toMillisecond
import kotlin.math.atan2
import kotlin.math.min

class TimerView constructor(context: Context, attributeSet: AttributeSet) :
    View(context, attributeSet) {
    private var isTouching = false
    private lateinit var center: PointF
    private lateinit var absoluteCenter: PointF
    private lateinit var rect: RectF
    private var radius: Float = 0f
    private var sweepAngle = 20.0
    private var isTimerStop = false
    private lateinit var currentPoint: PointF

    interface OnTimeEventListener {
        fun onTimerChanged(remainTime: Int)

        fun onTimeExhausted()

        fun onTimerShouldRestart()
    }

    private lateinit var listener: OnTimeEventListener

    fun setOnTimeExhaustedListener(listener: OnTimeEventListener) {
        this.listener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center = PointF(w / 2f, h / 2f)
        val points = IntArray(2)
        getLocationOnScreen(points)
        absoluteCenter = PointF(points[0] + center.x, points[1] + center.y)
        radius = min(w.toFloat(), h.toFloat()) / 2 - PADDING
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
        canvas?.drawArc(rect, 270f, sweepAngle.toFloat(), false, progressPaint)
        canvas?.drawText(
            sweepAngle.toFormatString(),
            center.x - (textPaint.measureText(sweepAngle.toFormatString()) / 2),
            center.y - (textPaint.descent() + textPaint.ascent()) / 2,
            textPaint
        )
    }

    fun consume() {
        if (isTouching) return
        onTick(0.1f)
        invalidate()
    }

    private fun onTick(amount: Float) {
        if (!isTouching && sweepAngle == 0.0) {
            listener.onTimeExhausted()
            isTimerStop = true
            return
        }
        sweepAngle -= amount

        if (sweepAngle < 0)
            sweepAngle = 0.0

    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> onActionDown(it.rawX, it.rawY)
                MotionEvent.ACTION_MOVE -> onActionMove(it.rawX, it.rawY)
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
        listener.onTimerChanged(sweepAngle.toMillisecond())
        if (isTimerStop && sweepAngle != 0.0) {
            isTimerStop = false
            listener.onTimerShouldRestart()
        }
    }

    private fun onActionMove(x: Float, y: Float) {
        val prevRadius = atan2(
            (currentPoint.y - absoluteCenter.y).toDouble(),
            (currentPoint.x - absoluteCenter.x).toDouble()
        )
        currentPoint.x = x
        currentPoint.y = y
        if (absoluteCenter.distanceTo(PointF(x, y)) < 5000) {
            return
        }
        val currentRadius = atan2(
            (y - absoluteCenter.y).toDouble(),
            (x - absoluteCenter.x).toDouble()
        )
        val radiusDiff = currentRadius - prevRadius
        onSwipe(Math.toDegrees(radiusDiff))
    }

    private fun onSwipe(degree: Double) {
        if (degree > 50 || degree < -50) return
        sweepAngle += degree
        if (sweepAngle > 360.0) sweepAngle = 360.0
        else if (sweepAngle < 0.0) sweepAngle = 0.0
    }

    companion object {
        private val tintColor = Color.parseColor("#89cff0")
        private val progressPaint = Paint().apply {
            strokeWidth = 40f
            color = tintColor
            style = Paint.Style.STROKE
        }
        private val textPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            textSize = 60f
        }
        private val backgroundCirclePaint = Paint().apply {
            color = Color.parseColor("#EBEBEB")
            strokeWidth = 80f
            style = Paint.Style.STROKE
            textSize = 20f
        }

        private const val PADDING = 50f
    }
}