package com.example.appdynamicssample

import android.app.Application
import com.appdynamics.eumagent.runtime.Instrumentation

class TestApp: Application() {

    override fun onCreate() {
        super.onCreate()
        Instrumentation.start("APPDYNAMICS_APP_KEY", this)
    }

}