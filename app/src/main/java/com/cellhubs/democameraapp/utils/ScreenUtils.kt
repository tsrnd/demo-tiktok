package com.cellhubs.democameraapp.utils

import android.content.res.Resources

/**
 * @author at-hungtruong
 */
object ScreenUtils {
    fun getWidth() = Resources.getSystem().displayMetrics.widthPixels

    fun getHeight() = Resources.getSystem().displayMetrics.heightPixels
}
