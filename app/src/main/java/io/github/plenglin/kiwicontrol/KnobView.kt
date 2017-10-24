package io.github.plenglin.kiwicontrol

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class KnobView(context: Context, attributes: AttributeSet) : View(context, attributes) {

    private var attrArray = context.theme.obtainStyledAttributes(attributes, R.styleable.KnobView, 0, 0)!!

    private val knobRadius get() = minOf(width, height).toFloat() / 2f

    private val centerX get() = this.width/2f
    private val centerY get() = this.height/2f

    private val indicatorLength = attrArray.getDimension(R.styleable.KnobView_indicatorLength, 10f)

    private var knobPaint = Paint().apply {
        color = attrArray.getColorStateList(R.styleable.KnobView_knobColor)!!.getColorForState(drawableState, Color.WHITE)
        style = Paint.Style.FILL
    }

    private var indicatorPaint = Paint().apply {
        color = attrArray.getColorStateList(R.styleable.KnobView_indicatorColor)!!.getColorForState(drawableState, Color.BLACK)
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = attrArray.getDimension(R.styleable.KnobView_indicatorWidth, 0f)
    }

    var theta: Double = Math.toRadians(attrArray.getFloat(R.styleable.KnobView_initialAngle, 0f).toDouble())
        get() = (field + 2 * Math.PI) % (2 * Math.PI)
        private set(value) {field = value}

    val bearing get() = (5 * Math.PI / 2 - theta) % (2*Math.PI)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!

        val indicatorEnd = knobRadius - indicatorLength
        val dx = Math.cos(theta).toFloat()
        val dy = -Math.sin(theta).toFloat()

        canvas.drawCircle(centerX, centerY, knobRadius, knobPaint)
        canvas.drawLine(
                knobRadius   * dx + centerX,    knobRadius   * dy   + centerY,
                indicatorEnd * dx + centerX,    indicatorEnd * dy   + centerY,
                indicatorPaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event!!
        when (event.action) {
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN -> {
                theta = -Math.atan2((event.y - centerY).toDouble(), (event.x - centerX).toDouble())
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)

    }

}