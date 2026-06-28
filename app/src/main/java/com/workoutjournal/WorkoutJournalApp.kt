package com.workoutjournal

import android.app.Application
import androidx.room.Room
import com.workoutjournal.data.db.WorkoutDatabase
import com.workoutjournal.data.repository.WorkoutRepository

class WorkoutJournalApp : Application() {
    val database by lazy {
        Room.databaseBuilder(this, WorkoutDatabase::class.java, "workout_journal.db")
            .addMigrations(
                WorkoutDatabase.MIGRATION_1_2,
                WorkoutDatabase.MIGRATION_2_3,
                WorkoutDatabase.MIGRATION_3_4
            )
            .build()
    }
    val repository by lazy {
        WorkoutRepository(
            database.sessionDao(),
            database.exerciseDao(),
            database.setDao(),
            database.templateDao()
        )
    }
}
