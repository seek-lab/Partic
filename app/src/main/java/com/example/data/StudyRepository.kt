package com.example.data

import kotlinx.coroutines.flow.Flow

class StudyRepository(private val db: AppDatabase) {

    val studentProfileFlow: Flow<StudentProfile?> = db.studentProfileDao().getProfileFlow()
    val allSubjectsFlow: Flow<List<Subject>> = db.subjectDao().getAllSubjectsFlow()
    val allStudyMaterialsFlow: Flow<List<StudyMaterial>> = db.studyMaterialDao().getAllStudyMaterialsFlow()

    suspend fun getProfile(): StudentProfile? {
        return db.studentProfileDao().getProfile()
    }

    suspend fun insertProfile(profile: StudentProfile) {
        db.studentProfileDao().insertProfile(profile)
    }

    suspend fun insertSubject(subject: Subject) {
        db.subjectDao().insertSubject(subject)
    }

    suspend fun getSubject(id: Int): Subject? {
        return db.subjectDao().getSubject(id)
    }

    fun getWeeksForSubjectFlow(subjectId: Int): Flow<List<WeekTopic>> {
        return db.weekTopicDao().getWeeksForSubjectFlow(subjectId)
    }

    fun getStudyMaterialsForSubjectFlow(subjectId: Int): Flow<List<StudyMaterial>> {
        return db.studyMaterialDao().getStudyMaterialsForSubjectFlow(subjectId)
    }

    fun getStudyMaterialFlow(subjectId: Int, weekNum: Int): Flow<StudyMaterial?> {
        return db.studyMaterialDao().getStudyMaterialFlow(subjectId, weekNum)
    }

    suspend fun getStudyMaterial(subjectId: Int, weekNum: Int): StudyMaterial? {
        return db.studyMaterialDao().getStudyMaterial(subjectId, weekNum)
    }

    suspend fun insertWeeks(weeks: List<WeekTopic>) {
        db.weekTopicDao().insertWeeks(weeks)
    }

    suspend fun insertStudyMaterial(material: StudyMaterial) {
        db.studyMaterialDao().insertStudyMaterial(material)
    }

    suspend fun clearAllData() {
        db.studentProfileDao().clearProfile()
        db.subjectDao().clearSubjects()
        db.weekTopicDao().clearAllWeeks()
        db.studyMaterialDao().clearAllStudyMaterials()
    }
}
