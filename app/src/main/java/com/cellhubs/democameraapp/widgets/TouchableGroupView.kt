package com.cellhubs.democameraapp.widgets

import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import com.cellhubs.democameraapp.utils.DistanceUtil.distance
import com.cellhubs.democameraapp.utils.DistanceUtil.getDistanceBetweenTwoPoints
import com.cellhubs.democameraapp.utils.DistanceUtil.rotation
import com.cellhubs.democameraapp.widgets.listeners.*

/**
 * @author at-hungtruong
 */
class TouchableGroupView : RelativeLayout {
    private var oldDist = 0f
    private var oldDegree = 0f
    private val startTouch = PointF()
    private val mid = PointF()
    private var currentTouchState = TouchState.TOUCH_STATE_NO_ACTION

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return true
        }
        val touchedChild = getChildViewAtPosition(event) ?: return true
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startTouch.set(event.x, event.y)
                currentTouchState = TouchState.TOUCH_STATE_DRAG
                (touchedChild as? DraggableView)?.onStartDrag(event)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    startTouch.set(event.getX(0), event.getY(0))
                    currentTouchState = TouchState.TOUCH_STATE_TWO_POINTED

                    oldDist = distance(event, mid)
                    oldDegree = rotation(event, mid)
                    (touchedChild as? RotatableView)?.onStartRotate(event)
                    (touchedChild as? ScalableView)?.onStartScale(event)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                when (currentTouchState) {
                    TouchState.TOUCH_STATE_DRAG -> {
                        (touchedChild as? DraggableView)?.onDrag(event)
                    }
                    TouchState.TOUCH_STATE_TWO_POINTED -> {
                        val x = oldDist
                        val y = getDistanceBetweenTwoPoints(event.getX(0),
                                event.getY(0), startTouch.x, startTouch.y)
                        val z = distance(event, mid)
                        val cos = (x * x + y * y - z * z) / (2f * x * y)
                        val degree = Math.toDegrees(Math.acos(cos.toDouble())).toFloat()

                        if (degree < 120 && degree > 45) {
                            oldDegree = rotation(event, mid)
                            currentTouchState = TouchState.TOUCH_STATE_ROTATE
                        } else {
                            oldDist = distance(event, mid)
                            currentTouchState = TouchState.TOUCH_STATE_SCALE
                        }
                    }
                    TouchState.TOUCH_STATE_SCALE -> {
                        val newDist = distance(event, mid)
                        val scale = newDist / oldDist
                        (touchedChild as? ScalableView)?.onScale(scale)
                    }
                    TouchState.TOUCH_STATE_ROTATE -> {
                        val newDegree = rotation(event, mid)
                        val rotate = newDegree - oldDegree
                        (touchedChild as? RotatableView)?.onRotate(rotate)
                    }
                    else -> {

                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                currentTouchState = TouchState.TOUCH_STATE_NO_ACTION
            }
            MotionEvent.ACTION_POINTER_UP -> {
                currentTouchState = TouchState.TOUCH_STATE_NO_ACTION
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getChildViewAtPosition(event: MotionEvent): View? {
        val hitRect = Rect()
        for (i in 0..childCount) {
            val child = getChildAt(i)
            if (child !is TouchableView) {
                continue
            }
            child.getHitRect(hitRect)
            if (hitRect.contains(event.x.toInt(), event.y.toInt())) {
                return child.apply {
                    child.onSelected(event)
                }
            }
        }
        return null
    }
}
