package com.cellhubs.democameraapp.widgets.listeners

import android.view.MotionEvent

/**
 * @author at-hungtruong
 */
interface ScalableView : TouchableView {
    fun onScale(scale: Float)

    fun onStartScale(event: MotionEvent) {
        // No-op
    }
}
