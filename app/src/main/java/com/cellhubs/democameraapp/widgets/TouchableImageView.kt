package com.cellhubs.democameraapp.widgets

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import com.cellhubs.democameraapp.utils.ScreenUtils
import com.cellhubs.democameraapp.widgets.listeners.DraggableView
import com.cellhubs.democameraapp.widgets.listeners.RotatableView
import com.cellhubs.democameraapp.widgets.listeners.ScalableView

/**
 * @author at-hungtruong
 */
class TouchableImageView : android.support.v7.widget.AppCompatImageView, DraggableView, ScalableView, RotatableView {
    private var currentViewWidth = 0F
    private var currentViewHeight = 0F

    private var currentPoint = PointF()

    private var savedZoom = 1F
    private var currentZoom = 1F

    private var savedDegree = 0F
    private var currentDegree = 0F

    private var maxDragX: Float = 0F
    private var minDragX: Float = 0F
    private var minDragY: Float = 0F
    private var maxDragY: Float = 0F
    private val screenWidth = ScreenUtils.getWidth()
    // Screen height need to minus height of status bar and toolbar.
    private val screenHeight = ScreenUtils.getHeight()

    private val currentViewPoint = IntArray(2)

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
        currentPoint.set(event.x, event.y)
        currentViewWidth = width * savedZoom
        currentViewHeight = height * savedZoom
        maxDragX = screenWidth - currentViewWidth / 2
        minDragX = -screenWidth + currentViewWidth / 2
        maxDragY = screenHeight - currentViewHeight / 2
        minDragY = -screenHeight + currentViewHeight / 2
    }

    override fun onDrag(event: MotionEvent) {
        getLocationOnScreen(currentViewPoint)
        val offsetX = event.x - currentPoint.x
        val offsetY = event.y - currentPoint.y
        if (offsetX > 0 && currentViewPoint[0] < maxDragX ||
            offsetX < 0 && currentViewPoint[0] > minDragX ||
            offsetY > 0 && currentViewPoint[1] < maxDragY ||
            offsetY < 0 && currentViewPoint[1] > minDragY
        ) {
            var dx = 0f
            var dy = 0f
            if (offsetX > 0 && currentViewPoint[0] < maxDragX || offsetX < 0 && currentViewPoint[0] > minDragX) {
                dx = offsetX
                if (currentViewPoint[0] + offsetX > maxDragX) {
                    dx = maxDragX - currentViewPoint[0]
                }
                if (currentViewPoint[0] + offsetX < minDragX) {
                    dx = minDragX - currentViewPoint[0]
                }
            }
            if (offsetY > 0 && currentViewPoint[1] < maxDragY || offsetY < 0 && currentViewPoint[1] > minDragY) {
                dy = offsetY
                if (currentViewPoint[1] + offsetY > maxDragY) {
                    dy = maxDragY - currentViewPoint[1]
                }
                if (currentViewPoint[1] + offsetY < minDragY) {
                    dy = minDragY - currentViewPoint[1]
                }
            }
            if (Math.abs(dx) > 0 || Math.abs(dy) > 0) {
                x += dx
                y += dy
            }
        }
        currentPoint.set(event.x, event.y)
    }

    override fun onStartScale(event: MotionEvent) {
        savedZoom = currentZoom
    }

    override fun onScale(scale: Float) {
        currentZoom = savedZoom * scale
        scaleX = currentZoom
        scaleY = currentZoom
    }

    override fun onStartRotate(event: MotionEvent) {
        savedDegree = currentDegree
    }

    override fun onRotate(degree: Float) {
        currentDegree = (degree + currentDegree) % 360
        rotation = degree
    }
}
