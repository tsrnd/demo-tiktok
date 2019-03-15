package com.cellhubs.democameraapp.widgets.listeners

import android.view.MotionEvent

/**
 * @author at-hungtruong
 */
interface RotatableView : TouchableView {
    fun onRotate(degree: Float)

    fun onStartRotate(event: MotionEvent) {
        // No-op
    }
}
