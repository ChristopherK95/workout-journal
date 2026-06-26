package com.workoutjournal.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.workoutjournal.data.db.dao.ExerciseDao
import com.workoutjournal.data.db.dao.SessionDao
import com.workoutjournal.data.db.dao.SetDao
import com.workoutjournal.data.db.entity.ExerciseEntity
import com.workoutjournal.data.db.entity.SessionEntity
import com.workoutjournal.data.db.entity.SetEntity

@Database(
    entities = [SessionEntity::class, ExerciseEntity::class, SetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun setDao(): SetDao
}
