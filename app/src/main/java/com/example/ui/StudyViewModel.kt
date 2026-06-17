@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.squareup.moshi.Types
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StudyViewModel(private val repository: StudyRepository) : ViewModel() {

    // Onboarding step management
    val onboardingStep = MutableStateFlow(1) // 1 to 6
    val inputName = MutableStateFlow("")
    val searchQuery = MutableStateFlow("")
    val selectedCountry = MutableStateFlow("")
    val selectedClassLevel = MutableStateFlow("")
    val selectedSubjectsList = MutableStateFlow<List<String>>(emptyList())
    val termWeeksCount = MutableStateFlow(12) // Default term weeks count (usually 10-12)
    
    // Structure: Subject Name -> (Week Number -> Topic input string)
    val tempWeeklyTopics = MutableStateFlow<Map<String, Map<Int, String>>>(emptyMap())

    val profile: StateFlow<StudentProfile?> = repository.studentProfileFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allSubjects: StateFlow<List<Subject>> = repository.allSubjectsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allStudyMaterials: StateFlow<List<StudyMaterial>> = repository.allStudyMaterialsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedSubjectId = MutableStateFlow<Int?>(null)
    val selectedSubjectId: StateFlow<Int?> = _selectedSubjectId.asStateFlow()

    private val _activeSetupSubjectId = MutableStateFlow<Int?>(null)
    val activeSetupSubjectId: StateFlow<Int?> = _activeSetupSubjectId.asStateFlow()

    val weeks: StateFlow<List<WeekTopic>> = _selectedSubjectId
        .flatMapLatest { subjectId ->
            if (subjectId != null) {
                repository.getWeeksForSubjectFlow(subjectId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentSubjectMaterials: StateFlow<List<StudyMaterial>> = _selectedSubjectId
        .flatMapLatest { subjectId ->
            if (subjectId != null) {
                repository.getStudyMaterialsForSubjectFlow(subjectId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedWeekNum = MutableStateFlow<Int?>(null)
    val selectedWeekNum: StateFlow<Int?> = _selectedWeekNum.asStateFlow()

    private val _currentMaterial = MutableStateFlow<StudyMaterial?>(null)
    val currentMaterial: StateFlow<StudyMaterial?> = _currentMaterial.asStateFlow()

    val parsedQuestions: StateFlow<List<PracticeQuestion>> = _currentMaterial
        .map { material ->
            if (material == null) return@map emptyList()
            try {
                val type = Types.newParameterizedType(List::class.java, PracticeQuestion::class.java)
                val adapter = GeminiNetwork.generalMoshi.adapter<List<PracticeQuestion>>(type)
                adapter.fromJson(material.questionsJson) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationError = MutableStateFlow<String?>(null)
    val generationError: StateFlow<String?> = _generationError.asStateFlow()

    private val _loadingStatus = MutableStateFlow("")
    val loadingStatus: StateFlow<String> = _loadingStatus.asStateFlow()

    // Practice navigation
    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _isAnswerRevealed = MutableStateFlow(false)
    val isAnswerRevealed: StateFlow<Boolean> = _isAnswerRevealed.asStateFlow()

    private val _selectedOptionIndex = MutableStateFlow<Int?>(null)
    val selectedOptionIndex: StateFlow<Int?> = _selectedOptionIndex.asStateFlow()

    private var flowJob: Job? = null

    init {
        // Automatically set onboardingStep based on whether a profile exists
        viewModelScope.launch {
            repository.studentProfileFlow.collect { prof ->
                if (prof != null && prof.onboardingCompleted) {
                    onboardingStep.value = 0 // Onboarding fully done
                }
            }
        }
    }

    fun selectSubject(subjectId: Int?) {
        _selectedSubjectId.value = subjectId
        _activeSetupSubjectId.value = null
        selectWeek(null)
    }

    fun selectWeek(weekNum: Int?) {
        _selectedWeekNum.value = weekNum
        _currentQuestionIndex.value = 0
        _isAnswerRevealed.value = false
        _selectedOptionIndex.value = null
        _generationError.value = null

        flowJob?.cancel()
        val subjectId = _selectedSubjectId.value
        if (weekNum != null && subjectId != null) {
            flowJob = viewModelScope.launch {
                repository.getStudyMaterialFlow(subjectId, weekNum).collect { material ->
                    _currentMaterial.value = material
                }
            }
        } else {
            _currentMaterial.value = null
        }
    }

    fun finishSchemeAndSave() {
        viewModelScope.launch {
            // Clear existing tables first for reconstruction
            repository.clearAllData()

            // 1. Insert Profile
            val prof = StudentProfile(
                name = inputName.value.trim().ifEmpty { "Student" },
                country = selectedCountry.value,
                classLevel = selectedClassLevel.value,
                onboardingCompleted = false
            )
            repository.insertProfile(prof)

            // 2. Insert Subjects (up to 4)
            val selected = selectedSubjectsList.value.take(4)
            for (i in 1..4) {
                if (i <= selected.size) {
                    val subName = selected[i - 1]
                    val totalW = termWeeksCount.value
                    val subObj = Subject(
                        id = i,
                        name = subName,
                        gradeClass = selectedClassLevel.value,
                        totalWeeks = totalW,
                        isAdded = true
                    )
                    repository.insertSubject(subObj)

                    // Insert custom topics mapped or generated
                    val topicsMap = tempWeeklyTopics.value[subName] ?: emptyMap()
                    val weekTopics = (1..totalW).map { w ->
                        WeekTopic(
                            subjectId = i,
                            weekNum = w,
                            topic = topicsMap[w]?.trim() ?: ""
                        )
                    }
                    repository.insertWeeks(weekTopics)
                } else {
                    // Empty slot
                    repository.insertSubject(
                        Subject(
                            id = i,
                            name = "",
                            gradeClass = "",
                            totalWeeks = 0,
                            isAdded = false
                        )
                    )
                }
            }

            // Route to screen 6: First Generation / WOW moment
            onboardingStep.value = 6
        }
    }

    fun generateFirstWeekAction() {
        // Generate materials for Subject ID 1, Week 1 immediately
        val selected = selectedSubjectsList.value
        if (selected.isNotEmpty()) {
            generateStudyMaterialsForWeek(subjectId = 1, weekNum = 1)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val currentProfile = repository.getProfile()
                ?: profile.value
                ?: StudentProfile(
                    name = inputName.value.trim().ifEmpty { "Student" },
                    country = selectedCountry.value,
                    classLevel = selectedClassLevel.value
                )
            val updated = currentProfile.copy(onboardingCompleted = true)
            repository.insertProfile(updated)
            onboardingStep.value = 0 // Finished onboarding!
        }
    }

    fun generateStudyMaterialsForWeek(subjectId: Int, weekNum: Int) {
        _isGenerating.value = true
        _generationError.value = null
        _loadingStatus.value = "Consulting Study Assistant..."

        viewModelScope.launch {
            try {
                val currentProfile = repository.getProfile()
                    ?: profile.value
                    ?: StudentProfile(
                        name = inputName.value.trim().ifEmpty { "Student" },
                        country = selectedCountry.value,
                        classLevel = selectedClassLevel.value
                    )

                val currentSubject = repository.getSubject(subjectId) ?: return@launch
                val currentWeeks = repository.getWeeksForSubjectFlow(subjectId).first()
                val rawTopic = currentWeeks.find { it.weekNum == weekNum }?.topic ?: ""
                val topic = rawTopic.trim().ifEmpty { "General Study of ${currentSubject.name}" }

                val progressJob = launch {
                    val messages = listOf(
                        "Reviewing ${currentProfile.country} educational curriculum...",
                        "Drafting study notes clearly for secondary school level...",
                        "Designing 8 practice questions (MCQs & Theory)...",
                        "Polishing solutions and explanation guidelines..."
                    )
                    var index = 0
                    while (true) {
                        _loadingStatus.value = messages[index % messages.size]
                        kotlinx.coroutines.delay(3000)
                        index++
                    }
                }

                val result = GeminiNetwork.generateStudyMaterials(
                    country = currentProfile.country,
                    classLevel = currentProfile.classLevel,
                    subject = currentSubject.name,
                    weekNum = weekNum,
                    topic = topic,
                    totalWeeks = currentSubject.totalWeeks
                )

                progressJob.cancel()

                val type = Types.newParameterizedType(List::class.java, PracticeQuestion::class.java)
                val adapter = GeminiNetwork.generalMoshi.adapter<List<PracticeQuestion>>(type)
                val questionsJson = adapter.toJson(result.questions)

                val material = StudyMaterial(
                    subjectId = subjectId,
                    weekNum = weekNum,
                    notes = result.notes,
                    questionsJson = questionsJson
                )

                repository.insertStudyMaterial(material)
                
                // If currently viewing this specific week, keep state updated
                if (_selectedSubjectId.value == subjectId && _selectedWeekNum.value == weekNum) {
                    _currentMaterial.value = material
                } else if (onboardingStep.value == 6) {
                    // During Step 6 onboarding, let's also bind to currentMaterial to display notes preview
                    _currentMaterial.value = material
                }

                _isGenerating.value = false
            } catch (e: Exception) {
                e.printStackTrace()
                _generationError.value = e.message ?: "An unexpected error occurred. Please check your network connection."
                _isGenerating.value = false
            }
        }
    }

    fun nextQuestion() {
        val total = parsedQuestions.value.size
        if (_currentQuestionIndex.value < total - 1) {
            _currentQuestionIndex.value += 1
            _isAnswerRevealed.value = false
            _selectedOptionIndex.value = null
        }
    }

    fun prevQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
            _isAnswerRevealed.value = false
            _selectedOptionIndex.value = null
        }
    }

    fun selectOption(index: Int) {
        _selectedOptionIndex.value = index
    }

    fun revealAnswer() {
        _isAnswerRevealed.value = true
    }

    fun resetApp() {
        viewModelScope.launch {
            repository.clearAllData()
            _selectedSubjectId.value = null
            _activeSetupSubjectId.value = null
            selectWeek(null)
            
            // Clean temp flows too
            inputName.value = ""
            selectedCountry.value = ""
            selectedClassLevel.value = ""
            selectedSubjectsList.value = emptyList()
            tempWeeklyTopics.value = emptyMap()
            onboardingStep.value = 1 // Back to welcome screen
        }
    }

    companion object {
        val COUNTRY_DATABASE = mapOf(
            "Afghanistan" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Antigua and Barbuda" to listOf("Form 1", "Form 2", "Form 3", "Form 4", "Form 5"),
            "Australia" to listOf("Year 7", "Year 8", "Year 9", "Year 10", "Year 11", "Year 12"),
            "Bahamas" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Bahrain" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Bangladesh" to listOf("Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "Class 11", "Class 12"),
            "Barbados" to listOf("Form 1", "Form 2", "Form 3", "Form 4", "Form 5", "Lower Sixth", "Upper Sixth"),
            "Belize" to listOf("Form 1", "Form 2", "Form 3", "Form 4", "Form 5", "Lower Sixth", "Upper Sixth"),
            "Bhutan" to listOf("Class VII", "Class VIII", "Class IX", "Class X", "Class XI", "Class XII"),
            "Botswana" to listOf("Form 1", "Form 2", "Form 3", "Form 4", "Form 5"),
            "Brunei" to listOf("Year 7", "Year 8", "Year 9", "Year 10", "Year 11"),
            "Burkina Faso" to listOf("6ème", "5ème", "4ème", "3ème", "Seconde", "Première", "Terminale"),
            "Cambodia" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Cameroon" to listOf("Form 1", "Form 2", "Form 3", "Form 4", "Form 5", "Lower Sixth", "Upper Sixth"),
            "Canada" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Chad" to listOf("6ème", "5ème", "4ème", "3ème", "Seconde", "Première", "Terminale"),
            "Cyprus" to listOf("Gymnasium Year 1", "Gymnasium Year 2", "Gymnasium Year 3", "Lyceum Year 1", "Lyceum Year 2", "Lyceum Year 3"),
            "Denmark" to listOf("7th Grade", "8th Grade", "9th Grade", "10th Grade", "1.G", "2.G", "3.G"),
            "Dominica" to listOf("Form 1", "Form 2", "Form 3", "Form 4", "Form 5"),
            "Egypt" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Eritrea" to listOf("Grade 6", "Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Eswatini" to listOf("Form 1", "Form 2", "Form 3", "Form 4", "Form 5"),
            "Ethiopia" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Fiji" to listOf("Year 9", "Year 10", "Year 11", "Year 12", "Year 13"),
            "Finland" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10 (Optional)", "Lukio Year 1", "Lukio Year 2", "Lukio Year 3"),
            "France" to listOf("6ème", "5ème", "4ème", "3ème", "Seconde", "Première", "Terminale"),
            "Gambia" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Germany" to listOf("Klasse 7", "Klasse 8", "Klasse 9", "Klasse 10", "Klasse 11", "Klasse 12", "Klasse 13"),
            "Ghana" to listOf("JHS1", "JHS2", "JHS3", "SHS1", "SHS2", "SHS3"),
            "Grenada" to listOf("Form 1", "Form 2", "Form 3", "Form 4", "Form 5"),
            "Guinea" to listOf("7ème", "8ème", "9ème", "10ème", "11ème", "12ème", "Terminale"),
            "Guyana" to listOf("Form 1", "Form 2", "Form 3", "Form 4", "Form 5", "Lower Sixth", "Upper Sixth"),
            "Haiti" to listOf("7ème AF", "8ème AF", "9ème AF", "Seconde", "Première", "Terminale"),
            "Hong Kong" to listOf("Secondary 1", "Secondary 2", "Secondary 3", "Secondary 4", "Secondary 5", "Secondary 6"),
            "India" to listOf("Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "Class 11", "Class 12"),
            "Indonesia" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Ireland" to listOf("1st Year", "2nd Year", "3rd Year", "Transition Year (Optional)", "5th Year", "6th Year"),
            "Italy" to listOf("Prima Media", "Seconda Media", "Terza Media", "Prima Superiore", "Seconda Superiore", "Terza Superiore", "Quarta Superiore", "Quinta Superiore"),
            "Ivory Coast" to listOf("6ème", "5ème", "4ème", "3ème", "Seconde", "Première", "Terminale"),
            "Jamaica" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12", "Grade 13"),
            "Jordan" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Kazakhstan" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11"),
            "Kenya" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Kuwait" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Kyrgyzstan" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11"),
            "Laos" to listOf("Grade 6", "Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Lebanon" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Lesotho" to listOf("Form 1", "Form 2", "Form 3", "Form 4", "Form 5"),
            "Liberia" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Madagascar" to listOf("6ème", "5ème", "4ème", "3ème", "Seconde", "Première", "Terminale"),
            "Malawi" to listOf("Form 1", "Form 2", "Form 3", "Form 4"),
            "Malaysia" to listOf("Form 1", "Form 2", "Form 3", "Form 4", "Form 5", "Lower Six", "Upper Six"),
            "Maldives" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Mali" to listOf("6ème", "5ème", "4ème", "3ème", "Seconde", "Première", "Terminale"),
            "Malta" to listOf("Form 1", "Form 2", "Form 3", "Form 4", "Form 5"),
            "Mauritius" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12", "Grade 13"),
            "Mongolia" to listOf("Grade 6", "Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Mozambique" to listOf("Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Myanmar" to listOf("Grade 6", "Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11"),
            "Namibia" to listOf("Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Nepal" to listOf("Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "Class 11", "Class 12"),
            "Netherlands" to listOf("Year 1", "Year 2", "Year 3", "Year 4", "Year 5", "Year 6"),
            "New Zealand" to listOf("Year 9", "Year 10", "Year 11", "Year 12", "Year 13"),
            "Niger" to listOf("6ème", "5ème", "4ème", "3ème", "Seconde", "Première", "Terminale"),
            "Nigeria" to listOf("JSS1", "JSS2", "JSS3", "SS1", "SS2", "SS3"),
            "Norway" to listOf("8th Grade", "9th Grade", "10th Grade", "Vg1", "Vg2", "Vg3"),
            "Oman" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Pakistan" to listOf("Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "Class 11", "Class 12"),
            "Papua New Guinea" to listOf("Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Philippines" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Poland" to listOf("Klasa VII", "Klasa VIII", "Klasa I Liceum", "Klasa II Liceum", "Klasa III Liceum", "Klasa IV Liceum"),
            "Portugal" to listOf("7º Ano", "8º Ano", "9º Ano", "10º Ano", "11º Ano", "12º Ano"),
            "Qatar" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Rwanda" to listOf("Senior 1", "Senior 2", "Senior 3", "Senior 4", "Senior 5", "Senior 6"),
            "Saint Lucia" to listOf("Form 1", "Form 2", "Form 3", "Form 4", "Form 5"),
            "Saint Vincent and the Grenadines" to listOf("Form 1", "Form 2", "Form 3", "Form 4", "Form 5"),
            "Samoa" to listOf("Year 9", "Year 10", "Year 11", "Year 12", "Year 13"),
            "Saudi Arabia" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Senegal" to listOf("6ème", "5ème", "4ème", "3ème", "Seconde", "Première", "Terminale"),
            "Seychelles" to listOf("Secondary 1", "Secondary 2", "Secondary 3", "Secondary 4", "Secondary 5"),
            "Sierra Leone" to listOf("JSS1", "JSS2", "JSS3", "SSS1", "SSS2", "SSS3"),
            "Singapore" to listOf("Secondary 1", "Secondary 2", "Secondary 3", "Secondary 4", "Secondary 5", "Junior College 1", "Junior College 2"),
            "Somalia" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "South Africa" to listOf("Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "South Sudan" to listOf("Senior 1", "Senior 2", "Senior 3", "Senior 4", "Senior 5", "Senior 6"),
            "Spain" to listOf("1º ESO", "2º ESO", "3º ESO", "4º ESO", "1º Bachillerato", "2º Bachillerato"),
            "Sri Lanka" to listOf("Grade 6", "Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12", "Grade 13"),
            "Sudan" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Suriname" to listOf("Year 1", "Year 2", "Year 3", "Year 4", "Year 5", "Year 6"),
            "Sweden" to listOf("Årskurs 7", "Årskurs 8", "Årskurs 9", "Gymnasium Year 1", "Gymnasium Year 2", "Gymnasium Year 3"),
            "Tajikistan" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11"),
            "Tanzania" to listOf("Form 1", "Form 2", "Form 3", "Form 4", "Form 5", "Form 6"),
            "Thailand" to listOf("Mathayom 1", "Mathayom 2", "Mathayom 3", "Mathayom 4", "Mathayom 5", "Mathayom 6"),
            "Trinidad and Tobago" to listOf("Form 1", "Form 2", "Form 3", "Form 4", "Form 5", "Lower Sixth", "Upper Sixth"),
            "Uganda" to listOf("Senior 1", "Senior 2", "Senior 3", "Senior 4", "Senior 5", "Senior 6"),
            "United Arab Emirates" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "United Kingdom" to listOf("Year 7", "Year 8", "Year 9", "Year 10", "Year 11", "Year 12", "Year 13"),
            "United States" to listOf("Grade 6", "Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Uzbekistan" to listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11"),
            "Vietnam" to listOf("Grade 6", "Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Zambia" to listOf("Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
            "Zimbabwe" to listOf("Form 1", "Form 2", "Form 3", "Form 4", "Form 5", "Form 6")
        )

        val COMMON_SUGGESTIONS = listOf(
            "Mathematics", "English Language", "Biology", "Chemistry", 
            "Physics", "General Science", "History", "Geography", 
            "Economics", "Social Studies", "Literature-in-English", "Government"
        )
    }
}

class StudyViewModelFactory(private val repository: StudyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
