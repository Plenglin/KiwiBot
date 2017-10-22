package io.github.plenglin.kiwicontrol

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class JoystickView(context: Context, attributes: AttributeSet) : View(context, attributes) {

    private var attrArray = context.theme.obtainStyledAttributes(attributes, R.styleable.JoystickView, 0, 0)!!

    private var bgPaint = Paint().apply {
        color = attrArray.getColorStateList(R.styleable.JoystickView_bgColor).getColorForState(drawableState, Color.WHITE)
        style = Paint.Style.FILL
    }

    private var touchPaint = Paint().apply {
        color = attrArray.getColorStateList(R.styleable.JoystickView_touchColor).getColorForState(drawableState, Color.WHITE)
        style = Paint.Style.FILL
    }

    private val radius get() = minOf(width, height).toFloat() / 2f
    private val joystickTouchRadius get() = lazy {attrArray.getDimensionPixelSize(R.styleable.JoystickView_touchRadius, 0).toFloat()}
    private val limitPadding get() = lazy { attrArray.getDimensionPixelSize(R.styleable.JoystickView_joystickLimit, 0).toFloat() }

    private val centerX get() = this.width/2f
    private val centerY get() = this.height/2f

    private var touchX = 0f
    private var touchY = 0f
    private var touching = false

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!

        val dx = touchX - centerX
        val dy = touchY - centerY
        val maxDist = radius - joystickTouchRadius.value - limitPadding.value
        val dist = Math.sqrt((dx*dx + dy*dy).toDouble())
        var drawX = centerX
        var drawY = centerY
        if (touching) {
            if (dist < maxDist) {
                drawX += dx
                drawY += dy
            } else {
                drawX += (dx * maxDist / dist).toFloat()
                drawY += (dy * maxDist / dist).toFloat()
            }
        }

        canvas.drawCircle(centerX, centerY, radius, bgPaint)
        canvas.drawCircle(drawX, drawY, joystickTouchRadius.value, touchPaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event!!
        when (event.action) {
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN -> {
                touchX = event.x
                touchY = event.y
                touching = true
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                touchX = 0f
                touchY = 0f
                touching = false
                invalidate()
                return true
            }
        }

        return super.onTouchEvent(event)
    }

}