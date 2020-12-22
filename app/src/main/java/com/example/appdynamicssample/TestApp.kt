package com.example.appdynamicssample

import android.app.Application
import com.appdynamics.eumagent.runtime.Instrumentation

class TestApp: Application() {

    override fun onCreate() {
        super.onCreate()
        Instrumentation.start(BuildConfig.APPDYNAMICS_APP_KEY, this)
    }

}