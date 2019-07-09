package com.pdd.booknow

import android.app.Application

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Utilities.init(this)
    }
}