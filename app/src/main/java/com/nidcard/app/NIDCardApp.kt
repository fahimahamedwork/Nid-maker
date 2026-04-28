package com.nidcard.app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.nidcard.app.data.dao.NIDCardDao
import com.nidcard.app.data.database.AppDatabase

/**
 * Application-level singleton that provides database and shared preferences access.
 * This avoids the need for AndroidViewModel and fixes ClassCastException
 * when viewModel() can't properly pass Application context through Navigation Compose.
 */
object NIDCardApp {
    private lateinit var database: AppDatabase
    private lateinit var prefs: SharedPreferences
    private var initialized = false

    fun init(application: Application) {
        if (initialized) return
        initialized = true
        database = AppDatabase.getDatabase(application)
        prefs = application.getSharedPreferences("nid_admin_prefs", Context.MODE_PRIVATE)
    }

    fun getDatabase(): AppDatabase {
        check(initialized) { "NIDCardApp not initialized! Call NIDCardApp.init(application) in Application.onCreate()" }
        return database
    }

    fun getDao(): NIDCardDao = getDatabase().nidCardDao()

    fun getPrefs(): SharedPreferences {
        check(initialized) { "NIDCardApp not initialized!" }
        return prefs
    }
}
