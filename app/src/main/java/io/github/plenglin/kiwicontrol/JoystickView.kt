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
        color = attrArray.getColorStateList(R.styleable.JoystickView_bgColor)!!.getColorForState(drawableState, Color.WHITE)
        style = Paint.Style.FILL
    }

    private var touchPaint = Paint().apply {
        color = attrArray.getColorStateList(R.styleable.JoystickView_touchColor)!!.getColorForState(drawableState, Color.WHITE)
        style = Paint.Style.FILL
    }

    private val bgRadius get() = attrArray.getDimensionPixelSize(R.styleable.JoystickView_bgRadius, minOf(width, height)/ 2).toFloat()
    private val joystickTouchRadius get() = lazy {attrArray.getDimensionPixelSize(R.styleable.JoystickView_touchRadius, 0).toFloat()}
    private val limitPadding get() = lazy { attrArray.getDimensionPixelSize(R.styleable.JoystickView_joystickLimit, 0).toFloat() }
    private val maxRadius get() = bgRadius - joystickTouchRadius.value - limitPadding.value

    private val centerX get() = this.width/2f
    private val centerY get() = this.height/2f

    private val dx get() = touchX - centerX
    private val dy get() = touchY - centerY
    private val dist get() = Math.sqrt((dx*dx + dy*dy).toDouble()).toFloat()

    private var touchX = 0f
    private var touchY = 0f
    private var touching = false

    /**
     * How far the joystick is from the center.
     */
    val touchRadius get() = if (touching) minOf(dist / maxRadius, 1f).toDouble() else 0.0

    /**
     * The angle of the joystick, relative to right, going CCW.
     */
    val theta get() = -Math.atan2(dy.toDouble(), dx.toDouble())

    /**
     * The angle of the joystick, relative to up, going CW.
     */
    val bearing get() = (5 * Math.PI / 2 - theta) % (2*Math.PI)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!

        var drawX = centerX
        var drawY = centerY
        if (touching) {
            if (dist < maxRadius) {
                drawX += dx
                drawY += dy
            } else {
                drawX += (dx * maxRadius / dist).toFloat()
                drawY += (dy * maxRadius / dist).toFloat()
            }
        }

        canvas.drawCircle(centerX, centerY, bgRadius, bgPaint)
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