package com.workoutjournal.data.db.dao

import androidx.room.*
import com.workoutjournal.data.db.entity.TemplateEntity
import com.workoutjournal.data.db.entity.TemplateExerciseEntity
import kotlinx.coroutines.flow.Flow

data class TemplateSummaryRaw(
    val id: Long,
    val name: String,
    val exerciseCount: Int
)

@Dao
interface TemplateDao {
    @Query("""
        SELECT t.id, t.name, COUNT(te.id) AS exerciseCount
        FROM templates t
        LEFT JOIN template_exercises te ON te.templateId = t.id
        GROUP BY t.id
        ORDER BY t.createdAtEpochDay DESC
    """)
    fun getAllTemplates(): Flow<List<TemplateSummaryRaw>>

    @Insert
    suspend fun insertTemplate(template: TemplateEntity): Long

    @Insert
    suspend fun insertExercises(exercises: List<TemplateExerciseEntity>)

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun deleteTemplateById(id: Long)

    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId ORDER BY orderIndex ASC")
    suspend fun getExercisesForTemplate(templateId: Long): List<TemplateExerciseEntity>
}
