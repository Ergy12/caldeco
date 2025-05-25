package com.example.finalpaycalculator

import android.app.Application
import com.example.finalpaycalculator.domain.logic.InMemoryDataStore

class FinalPayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        InMemoryDataStore.initialize(this)
    }
}
