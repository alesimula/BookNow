package com.pdd.booknow

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.preference.PreferenceManager
import com.pdd.booknow.delegate.LazyWithReceiver

object Utilities {
    private val Context?.instance by LazyWithReceiver<Context, Context>(null) {applicationContext}
    fun init(context: Context) {context.instance}

    val context by lazy {(null as Context?).instance}
    val resources by lazy {context.resources}
    val values by lazy {context.values}
    val displayMetrics; get() = resources.displayMetrics

    val preferences by lazy {PreferenceManager.getDefaultSharedPreferences(context)}
    fun editPrefs (block: SharedPreferences.Editor.()->Unit) = preferences.edit().apply(block).apply()

    fun isPortrait() = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    fun isLandscape() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    fun scale(metric: Number, value: Number) = (metric.toDouble() * value.toDouble()).toInt()
}