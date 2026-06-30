package com.example.scorda

import android.app.Application
import com.example.scorda.data.AppContainer
import com.example.scorda.data.DefaultAppContainer

class ScordaApplication : Application() {
    /**
     * Override Application so that singleton objects like database can be initialized once for the life of the application
     */

    lateinit var container: AppContainer
    
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}