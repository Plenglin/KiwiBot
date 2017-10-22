package io.github.plenglin.kiwicontrol

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
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

    private val centerX get() = this.width/2f
    private val centerY get() = this.height/2f

    private var touchX = 0f
    private var touchY = 0f
    private var touching = false

    override fun onDraw(canvas: Canvas?) {
        canvas!!
        canvas.drawCircle(centerX, centerY, radius, bgPaint)
        if (touching) {
            canvas.drawCircle(touchX, touchY, radius, touchPaint)
        } else {
            canvas.drawCircle(centerX, centerY, joystickTouchRadius.value, touchPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event!!
        when (event.action) {
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN -> {
                val dx = event.x - centerX
                val dy = event.y - centerY
                if (dx*dx + dy*dy <= radius*radius) {
                    touchX = event.x
                    touchY = event.y
                    invalidate()
                    return true
                }
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