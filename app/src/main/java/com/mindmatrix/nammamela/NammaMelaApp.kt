package com.mindmatrix.nammamela

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp

class NammaMelaApp : Application() {
    val database by lazy { NammaMelaDatabase.getDatabase(this) }
    val repository by lazy { MelaRepository(this, database.melaDao()) }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        runCatching { FirebaseApp.initializeApp(this) }
    }
}
