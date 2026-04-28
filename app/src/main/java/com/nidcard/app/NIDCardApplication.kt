package com.nidcard.app

import android.app.Application

class NIDCardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NIDCardApp.init(this)
    }
}
