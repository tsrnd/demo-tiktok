package com.cellhubs.democameraapp.widgets.listeners

import android.view.MotionEvent

/**
 * @author at-hungtruong
 */
interface DraggableView : TouchableView {
    fun onStartDrag(event: MotionEvent) {
        // No-op
    }

    fun onDrag(event: MotionEvent)
}
