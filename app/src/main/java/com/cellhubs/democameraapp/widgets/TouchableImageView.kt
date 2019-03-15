package com.cellhubs.democameraapp.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.cellhubs.democameraapp.widgets.listeners.DraggableView
import com.cellhubs.democameraapp.widgets.listeners.RotatableView
import com.cellhubs.democameraapp.widgets.listeners.ScalableView

/**
 * @author at-hungtruong
 */
class TouchableImageView : android.support.v7.widget.AppCompatImageView, DraggableView, ScalableView, RotatableView {
    private var currentX = 0F
    private var currentY = 0F

    private var savedZoom = 1F
    private var currentZoom = 1F

    private var savedDegree = 0F
    private var currentDegree = 0F

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        isFocusableInTouchMode = false
        isClickable = false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false
    }

    override fun onStartDrag(event: MotionEvent) {
        currentX = event.x
        currentY = event.y
    }

    override fun onDrag(event: MotionEvent) {
        val offsetX = event.x - currentX
        val offsetY = event.y - currentY
        if (Math.abs(offsetX) > 0 || Math.abs(offsetY) > 0) {
            animate()
                    .translationX(offsetX)
                    .translationY(offsetY)
                    .setDuration(0)
                    .start()
        }
    }

    override fun onStartScale(event: MotionEvent) {
        savedZoom = currentZoom
    }

    override fun onScale(scale: Float) {
        currentZoom = savedZoom * scale
        animate()
                .scaleX(currentZoom)
                .scaleY(currentZoom)
                .setDuration(0)
                .start()
    }

    override fun onStartRotate(event: MotionEvent) {
        savedDegree = currentDegree
    }

    override fun onRotate(degree: Float) {
        currentDegree = (savedDegree + degree) % 360
        animate()
                .rotation(currentDegree)
                .setDuration(0)
                .start()
    }
}
