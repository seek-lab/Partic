package com.example.data

import androidx.room.*
import com.squareup.moshi.JsonClass

@Entity(tableName = "student_profile")
data class StudentProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val country: String = "",
    val classLevel: String = "",
    val onboardingCompleted: Boolean = false
)

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey val id: Int, // 1, 2, 3, or 4
    val name: String,
    val gradeClass: String, // JSS1, JSS2, JSS3, SS1, SS2, SS3
    val totalWeeks: Int,
    val isAdded: Boolean = false
)

@Entity(
    tableName = "week_topic",
    primaryKeys = ["subjectId", "weekNum"]
)
data class WeekTopic(
    val subjectId: Int,
    val weekNum: Int,
    val topic: String
)

@Entity(
    tableName = "study_material",
    primaryKeys = ["subjectId", "weekNum"]
)
data class StudyMaterial(
    val subjectId: Int,
    val weekNum: Int,
    val notes: String,
    val questionsJson: String // Serialized list of PracticeQuestion
)

@JsonClass(generateAdapter = true)
data class PracticeQuestion(
    val questionText: String,
    val type: String, // MCQ, Theory
    val options: List<String>, // Empty for theory
    val correctOptionIndex: Int, // -1 or index
    val answer: String // Correct option text or Theory answer detailed explanation
)

@JsonClass(generateAdapter = true)
data class GeneratedMaterialResponse(
    val notes: String,
    val questions: List<PracticeQuestion>
)
