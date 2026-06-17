package com.example.data

import android.app.Application

class StudyApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { StudyRepository(database) }
}
