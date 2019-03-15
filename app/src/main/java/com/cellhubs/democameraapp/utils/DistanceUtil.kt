package com.cellhubs.democameraapp.utils

import android.graphics.PointF
import android.view.MotionEvent

/**
 * @author at-hungtruong
 */
object DistanceUtil {
    fun getDistanceBetweenTwoPoints(x1: Float, y1: Float,
                                    x2: Float, y2: Float): Float {
        return Math.sqrt(Math.pow((x2 - x1).toDouble(), 2.0) + Math.pow((y2 - y1).toDouble(), 2.0)).toFloat()
    }

    fun distance(event: MotionEvent, mid: PointF): Float {
        return getDistanceBetweenTwoPoints(event.getX(0), event.getY(0), mid.x, mid.y)
    }

    private fun midPoint(event: MotionEvent): PointF {
        return getMidPointBetweenTwoPoints(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
    }

    private fun getMidPointBetweenTwoPoints(x1: Float, y1: Float, x2: Float, y2: Float): PointF {
        return PointF((x1 + x2) / 2, (y1 + y2) / 2)
    }

    fun rotation(event: MotionEvent, mid: PointF): Float {
        return getDegreeBetweenTwoPoints(event.getX(0), event.getY(0), mid.x, mid.y)
    }

    private fun getDegreeBetweenTwoPoints(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val radians = Math.atan2((y1 - y2).toDouble(), (x1 - x2).toDouble())
        return Math.toDegrees(radians).toFloat()
    }
}
