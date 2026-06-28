package com.workoutjournal.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.workoutjournal.data.db.dao.ExerciseDao
import com.workoutjournal.data.db.dao.SessionDao
import com.workoutjournal.data.db.dao.SetDao
import com.workoutjournal.data.db.dao.TemplateDao
import com.workoutjournal.data.db.entity.ExerciseEntity
import com.workoutjournal.data.db.entity.SessionEntity
import com.workoutjournal.data.db.entity.SetEntity
import com.workoutjournal.data.db.entity.TemplateEntity
import com.workoutjournal.data.db.entity.TemplateExerciseEntity

@Database(
    entities = [
        SessionEntity::class,
        ExerciseEntity::class,
        SetEntity::class,
        TemplateEntity::class,
        TemplateExerciseEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun setDao(): SetDao
    abstract fun templateDao(): TemplateDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE sessions ADD COLUMN startedAtEpochSecond INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE sessions ADD COLUMN endedAtEpochSecond INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE sessions ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `templates` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `createdAtEpochDay` INTEGER NOT NULL)"
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `template_exercises` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `templateId` INTEGER NOT NULL, `name` TEXT NOT NULL, `orderIndex` INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(`templateId`) REFERENCES `templates`(`id`) ON DELETE CASCADE)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_template_exercises_templateId` ON `template_exercises` (`templateId`)"
                )
            }
        }
    }
}
