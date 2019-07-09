package com.pdd.booknow

import android.content.Context
import android.util.DisplayMetrics
import kotlin.math.max
import kotlin.math.min

class AppValues private constructor(private val context: Context) {
    companion object {
        val instance by lazy {AppValues(Utilities.context)}
    }
    private val resources = context.resources
    private val metrics = resources.displayMetrics

    val magazine_marginNews; get() = Utilities.scale(metrics.mindimPixels,0.04)
    val magazine_marginLandscape; get() = (metrics.mindimPixels*0.15).toInt()
    val magazine_imgAspectRatioLandscape; get() = (metrics.aspectRatio*2.3)
}

val Context.values; get() = AppValues.instance

val DisplayMetrics.mindimPixels; get() = min(widthPixels, heightPixels)
val DisplayMetrics.maxdimPixels; get() = max(widthPixels, heightPixels)
val DisplayMetrics.aspectRatio; get() = maxdimPixels.toDouble()/mindimPixels