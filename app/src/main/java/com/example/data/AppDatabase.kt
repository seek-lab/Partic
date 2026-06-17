package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentProfileDao {
    @Query("SELECT * FROM student_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<StudentProfile?>

    @Query("SELECT * FROM student_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): StudentProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: StudentProfile)

    @Query("DELETE FROM student_profile")
    suspend fun clearProfile()
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY id ASC")
    fun getAllSubjectsFlow(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubject(id: Int): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubject(id: Int)

    @Query("DELETE FROM subjects")
    suspend fun clearSubjects()
}

@Dao
interface WeekTopicDao {
    @Query("SELECT * FROM week_topic WHERE subjectId = :subjectId ORDER BY weekNum ASC")
    fun getWeeksForSubjectFlow(subjectId: Int): Flow<List<WeekTopic>>

    @Query("SELECT * FROM week_topic ORDER BY weekNum ASC")
    fun getAllWeeksFlow(): Flow<List<WeekTopic>>

    @Query("SELECT * FROM week_topic WHERE subjectId = :subjectId AND weekNum = :weekNum LIMIT 1")
    suspend fun getWeekTopic(subjectId: Int, weekNum: Int): WeekTopic?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeks(weeks: List<WeekTopic>)

    @Query("DELETE FROM week_topic WHERE subjectId = :subjectId")
    suspend fun clearWeeksForSubject(subjectId: Int)

    @Query("DELETE FROM week_topic")
    suspend fun clearAllWeeks()
}

@Dao
interface StudyMaterialDao {
    @Query("SELECT * FROM study_material WHERE subjectId = :subjectId AND weekNum = :weekNum LIMIT 1")
    fun getStudyMaterialFlow(subjectId: Int, weekNum: Int): Flow<StudyMaterial?>

    @Query("SELECT * FROM study_material WHERE subjectId = :subjectId AND weekNum = :weekNum LIMIT 1")
    suspend fun getStudyMaterial(subjectId: Int, weekNum: Int): StudyMaterial?

    @Query("SELECT * FROM study_material")
    fun getAllStudyMaterialsFlow(): Flow<List<StudyMaterial>>

    @Query("SELECT * FROM study_material WHERE subjectId = :subjectId")
    fun getStudyMaterialsForSubjectFlow(subjectId: Int): Flow<List<StudyMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyMaterial(material: StudyMaterial)

    @Query("DELETE FROM study_material WHERE subjectId = :subjectId")
    suspend fun clearStudyMaterialsForSubject(subjectId: Int)

    @Query("DELETE FROM study_material")
    suspend fun clearAllStudyMaterials()
}

@Database(
    entities = [StudentProfile::class, Subject::class, WeekTopic::class, StudyMaterial::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentProfileDao(): StudentProfileDao
    abstract fun subjectDao(): SubjectDao
    abstract fun weekTopicDao(): WeekTopicDao
    abstract fun studyMaterialDao(): StudyMaterialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "naija_study_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
