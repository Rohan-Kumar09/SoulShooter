package com.example.survivalgame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class JoystickView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val basePaint = Paint().apply {
        color = Color.RED
        isAntiAlias = true
    }
    private val stickPaint = Paint().apply {
        color = Color.BLUE
        isAntiAlias = true
    }

    private val baseRadius = 150f
    private val stickRadius = 50f
    private var stickX = 0f
    private var stickY = 0f
    private var baseX = 0f
    private var baseY = 0f

    private val updateInterval = 16L // 60 FPS (16ms per update)
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (stickX != 0f || stickY != 0f) {
                // Notify listener of movement
                onJoystickMoved?.invoke(stickX / baseRadius, stickY / baseRadius) // Normalized values
                handler.postDelayed(this, updateInterval)
            }
        }
    }


    // Callback for movement updates
    var onJoystickMoved: ((x: Float, y: Float) -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        baseX = (w / 2).toFloat()
        baseY = (h - 200).toFloat() // Position joystick at the bottom of the screen
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(baseX, baseY, baseRadius, basePaint) // Draw base
        canvas.drawCircle(stickX + baseX, stickY + baseY, stickRadius, stickPaint) // Draw stick
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val dx = event.x - baseX
                val dy = event.y - baseY
                val distance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                if (distance < baseRadius) {
                    stickX = dx
                    stickY = dy
                } else {
                    val angle = atan2(dy.toDouble(), dx.toDouble()).toFloat()
                    stickX = (cos(angle.toDouble()) * baseRadius).toFloat()
                    stickY = (sin(angle.toDouble()) * baseRadius).toFloat()
                }

                if (stickX != 0f || stickY != 0f) {
                    startContinuousUpdates()
                }
            }
            MotionEvent.ACTION_UP -> {
                stickX = 0f
                stickY = 0f
                onJoystickMoved?.invoke(0f, 0f) // Notify listener that joystick is centered
                stopContinuousUpdates()
            }
        }

        invalidate() // Redraw the joystick
        return true
    }

    private fun startContinuousUpdates() {
        handler.removeCallbacks(updateRunnable)
        handler.post(updateRunnable)
    }

    private fun stopContinuousUpdates() {
        handler.removeCallbacks(updateRunnable)
    }
}
