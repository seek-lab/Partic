@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.*
import com.example.ui.theme.*

// Theme structures representing soft background and contrast accents for each card slot
data class CardTheme(
    val bg: Color,
    val text: Color,
    val accent: Color
)

fun getCardTheme(id: Int): CardTheme {
    return when(id) {
        1 -> CardTheme(
            bg = ColorGreenBg,
            text = ColorGreenTxt,
            accent = Color(0xFF10B981)
        )
        2 -> CardTheme(
            bg = ColorBlueBg,
            text = ColorBlueTxt,
            accent = BluePrimary
        )
        3 -> CardTheme(
            bg = ColorOrangeBg,
            text = ColorOrangeTxt,
            accent = Color(0xFFF97316)
        )
        else -> CardTheme(
            bg = ColorPurpleBg,
            text = ColorPurpleTxt,
            accent = Color(0xFFA855F7)
        )
    }
}

@Composable
fun AppContent(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val onboardingStep by viewModel.onboardingStep.collectAsState()
    val selectedSubjectId by viewModel.selectedSubjectId.collectAsState()
    val selectedWeekNum by viewModel.selectedWeekNum.collectAsState()
    val weeks by viewModel.weeks.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BaseGrey)
    ) {
        when {
            // 1. ONBOARDING HUB
            onboardingStep != 0 -> {
                OnboardingScreen(viewModel = viewModel)
            }
            // 2. STUDY WORKSPACE - WEEK STUDY DETAIL
            selectedSubjectId != null && selectedWeekNum != null -> {
                val currentWeekNum = selectedWeekNum!!
                val topic = weeks.find { it.weekNum == currentWeekNum }?.topic ?: "Theme Topic"
                WeekDetailScreen(
                    subjectId = selectedSubjectId!!,
                    weekNum = currentWeekNum,
                    topic = topic,
                    viewModel = viewModel,
                    onBack = { viewModel.selectWeek(null) }
                )
            }
            // 3. STUDY WORKSPACE - SUBJECT DASHBOARD
            selectedSubjectId != null -> {
                SubjectDashboardScreen(
                    subjectId = selectedSubjectId!!,
                    weeks = weeks,
                    viewModel = viewModel,
                    onBack = { viewModel.selectSubject(null) }
                )
            }
            // 4. MAIN HUB / APP HOME
            else -> {
                StudentHomeScreen(viewModel = viewModel)
            }
        }
    }
}

// ==========================================
//          ONBOARDING SCREENS WIZARD
// ==========================================

@Composable
fun OnboardingScreen(viewModel: StudyViewModel) {
    val step by viewModel.onboardingStep.collectAsState()
    val nameInput by viewModel.inputName.collectAsState()
    val countrySearch by viewModel.searchQuery.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val selectedClassLevel by viewModel.selectedClassLevel.collectAsState()
    val selectedSubjectsList by viewModel.selectedSubjectsList.collectAsState()
    val termWeeksCount by viewModel.termWeeksCount.collectAsState()
    val tempWeeklyTopics by viewModel.tempWeeklyTopics.collectAsState()

    // Loading status for step 6 (first generation)
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generationError by viewModel.generationError.collectAsState()
    val loadingStatus by viewModel.loadingStatus.collectAsState()
    val generatedMaterial by viewModel.currentMaterial.collectAsState()

    Scaffold(
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // STEP HEADERS (For steps 2 through 5)
            if (step in 2..5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Step $step of 5",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = BluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    IconButton(
                        onClick = { viewModel.onboardingStep.value = step - 1 }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
                
                // Progress Bar
                LinearProgressIndicator(
                    progress = (step - 1) / 4f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BluePrimary,
                    trackColor = BlueLight
                )
            }

            // DYNAMIC STEPS CONTENT
            Box(modifier = Modifier.weight(1f)) {
                when (step) {
                    // SCREEN 1: WELCOME SCREEN
                    1 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Partic",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = BluePrimary,
                                    fontSize = 44.sp,
                                    letterSpacing = (-1).sp
                                ),
                                modifier = Modifier.padding(bottom = 4.dp).testTag("app_logo")
                            )
                            Text(
                                text = "Learn smarter, not harder",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextMuted,
                                    fontStyle = FontStyle.Italic
                                ),
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Display the customized high agency generated hero image here
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 24.dp)
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(BlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.partic_welcome_hero_1781687515684),
                                    contentDescription = "Partic Welcome Hero",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = BaseGrey),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "What is your name?",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    OutlinedTextField(
                                        value = nameInput,
                                        onValueChange = { viewModel.inputName.value = it },
                                        placeholder = { Text("Enter your name") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BluePrimary) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("name_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextDark,
                                            unfocusedTextColor = TextDark,
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedPlaceholderColor = TextMuted,
                                            unfocusedPlaceholderColor = TextMuted,
                                            focusedBorderColor = BluePrimary,
                                            unfocusedBorderColor = BorderLight
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { 
                                    if (nameInput.isNotBlank()) {
                                        viewModel.onboardingStep.value = 2 
                                    }
                                },
                                enabled = nameInput.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("get_started_btn"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                                }
                            }
                        }
                    }

                    // SCREEN 2: COUNTRY SELECTION
                    2 -> {
                        val filteredCountries = remember(countrySearch) {
                            StudyViewModel.COUNTRY_DATABASE.keys.filter {
                                it.contains(countrySearch, ignoreCase = true)
                            }.sorted()
                        }

                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Where are you studying?",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Text(
                                text = "Select your country to load your local secondary school system and curriculum structure.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = countrySearch,
                                onValueChange = { viewModel.searchQuery.value = it },
                                placeholder = { Text("Search 100+ countries...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BluePrimary) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("country_search"),
                                 colors = OutlinedTextFieldDefaults.colors(
                                     focusedTextColor = TextDark,
                                     unfocusedTextColor = TextDark,
                                     focusedContainerColor = Color.White,
                                     unfocusedContainerColor = Color.White,
                                     focusedPlaceholderColor = TextMuted,
                                     unfocusedPlaceholderColor = TextMuted,
                                     focusedBorderColor = BluePrimary,
                                     unfocusedBorderColor = BorderLight
                                 ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredCountries) { country ->
                                    val isSelected = selectedCountry == country
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                viewModel.selectedCountry.value = country
                                                // Pre-reset classLevel when country changes
                                                viewModel.selectedClassLevel.value = ""
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) BlueLight else BaseGrey
                                        ),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isSelected) BluePrimary else Color.Transparent
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = country,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) BluePrimary else TextDark
                                            )
                                            if (isSelected) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = BluePrimary)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.onboardingStep.value = 3 },
                                enabled = selectedCountry.isNotEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("country_next_btn"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                            ) {
                                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // SCREEN 3: CLASS LEVEL SELECTION
                    3 -> {
                        val levelOptions = StudyViewModel.COUNTRY_DATABASE[selectedCountry] ?: emptyList()

                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Select your grade / class",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Text(
                                text = "Education classes tailored specifically for $selectedCountry:",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(levelOptions) { level ->
                                    val isSelected = selectedClassLevel == level
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.selectedClassLevel.value = level },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) BlueLight else BaseGrey
                                        ),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isSelected) BluePrimary else Color.Transparent
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) BluePrimary else Color.White),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = if (isSelected) Color.White else BluePrimary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text(
                                                    text = level,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isSelected) BluePrimary else TextDark
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = BluePrimary)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.onboardingStep.value = 4 },
                                enabled = selectedClassLevel.isNotEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("class_level_next_btn"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                            ) {
                                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // SCREEN 4: SUBJECT SELECTION
                    4 -> {
                        var customSubjectText by remember { mutableStateOf("") }

                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "What are you studying?",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Select up to 4 subjects:",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                Text(
                                    text = "${selectedSubjectsList.size}/4 Picked",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (selectedSubjectsList.size > 4) Color.Red else BluePrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            // Horizontal list / wraps of currently active selections
                            if (selectedSubjectsList.isNotEmpty()) {
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    selectedSubjectsList.forEach { sub ->
                                        AssistChip(
                                            onClick = { 
                                                viewModel.selectedSubjectsList.value = selectedSubjectsList - sub 
                                            },
                                            label = { Text(sub) },
                                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp)) },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = BlueLight,
                                                labelColor = BluePrimary
                                            )
                                        )
                                    }
                                }
                            }

                            // Custom Input
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customSubjectText,
                                    onValueChange = { customSubjectText = it },
                                    placeholder = { Text("Add custom subject...") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextDark,
                                        unfocusedTextColor = TextDark,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedPlaceholderColor = TextMuted,
                                        unfocusedPlaceholderColor = TextMuted,
                                        focusedBorderColor = BluePrimary,
                                        unfocusedBorderColor = BorderLight
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (customSubjectText.isNotBlank() && !selectedSubjectsList.contains(customSubjectText.trim()) && selectedSubjectsList.size < 4) {
                                            viewModel.selectedSubjectsList.value = selectedSubjectsList + customSubjectText.trim()
                                            customSubjectText = ""
                                        }
                                    },
                                    enabled = customSubjectText.isNotBlank() && selectedSubjectsList.size < 4,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add custom")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Or pick from common subjects:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(StudyViewModel.COMMON_SUGGESTIONS) { suggestion ->
                                    val isSelected = selectedSubjectsList.contains(suggestion)
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                if (isSelected) {
                                                    viewModel.selectedSubjectsList.value = selectedSubjectsList - suggestion
                                                } else {
                                                    if (selectedSubjectsList.size < 4) {
                                                        viewModel.selectedSubjectsList.value = selectedSubjectsList + suggestion
                                                    }
                                                }
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) BlueLight else BaseGrey
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = suggestion,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) BluePrimary else TextDark
                                            )
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = "Added", tint = BluePrimary)
                                            } else {
                                                Icon(Icons.Default.Add, contentDescription = "Add", tint = TextMuted)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { 
                                    // Prepopulate weekly work schemes for the chosen subjects so they don't start blank
                                    val defaultTermWeeks = termWeeksCount
                                    val topicsMap = mutableMapOf<String, Map<Int, String>>()
                                    selectedSubjectsList.forEach { sub ->
                                        val topicWeeks = mutableMapOf<Int, String>()
                                        (1..defaultTermWeeks).forEach { w ->
                                            topicWeeks[w] = ""
                                        }
                                        topicsMap[sub] = topicWeeks
                                    }
                                    viewModel.tempWeeklyTopics.value = topicsMap
                                    viewModel.onboardingStep.value = 5 
                                },
                                enabled = selectedSubjectsList.isNotEmpty() && selectedSubjectsList.size <= 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("subjects_next_btn"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                            ) {
                                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // SCREEN 5: SCHEME SETUP WEEK BY WEEK
                    5 -> {
                        var activeSubjectTab by remember { mutableStateOf(selectedSubjectsList.firstOrNull() ?: "") }
                        
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Setup Weekly Syllabus",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Text(
                                text = "Enter what you study week by week for each of your selected subjects.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // How many weeks in term?
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BaseGrey, RoundedCornerShape(12.dp))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Weeks in your Term:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                                    Text("Generates study cards for these weeks", fontSize = 12.sp, color = TextMuted)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Decrease Button
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(BluePrimary)
                                            .clickable {
                                                if (termWeeksCount > 4) {
                                                    viewModel.termWeeksCount.value = termWeeksCount - 1
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    
                                    Text(
                                        text = "$termWeeksCount",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = TextDark,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    
                                    // Increase Button
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(BluePrimary)
                                            .clickable {
                                                if (termWeeksCount < 20) {
                                                    viewModel.termWeeksCount.value = termWeeksCount + 1
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // TabRow for active subjects
                            ScrollableTabRow(
                                selectedTabIndex = selectedSubjectsList.indexOf(activeSubjectTab).coerceAtLeast(0),
                                containerColor = Color.White,
                                contentColor = BluePrimary,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                selectedSubjectsList.forEach { sub ->
                                    Tab(
                                        selected = activeSubjectTab == sub,
                                        onClick = { activeSubjectTab = sub },
                                        text = { Text(sub, fontWeight = FontWeight.Bold) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Weekly entry text fields
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items((1..termWeeksCount).toList()) { week ->
                                    val currentTopicsForActiveSub = tempWeeklyTopics[activeSubjectTab] ?: emptyMap()
                                    val textValue = currentTopicsForActiveSub[week] ?: ""

                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = BaseGrey)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Wk $week",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = BluePrimary,
                                                modifier = Modifier.width(48.dp)
                                            )
                                            OutlinedTextField(
                                                value = textValue,
                                                onValueChange = { newVal ->
                                                    val updatedMap = tempWeeklyTopics.toMutableMap()
                                                    val innerMap = (updatedMap[activeSubjectTab] ?: emptyMap()).toMutableMap()
                                                    innerMap[week] = newVal
                                                    updatedMap[activeSubjectTab] = innerMap
                                                    viewModel.tempWeeklyTopics.value = updatedMap
                                                },
                                                placeholder = { Text("e.g. Newton's laws of motion...") },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = TextDark,
                                                    unfocusedTextColor = TextDark,
                                                    focusedContainerColor = Color.White,
                                                    unfocusedContainerColor = Color.White,
                                                    focusedPlaceholderColor = TextMuted,
                                                    unfocusedPlaceholderColor = TextMuted,
                                                    focusedBorderColor = BluePrimary,
                                                    unfocusedBorderColor = BorderLight
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.finishSchemeAndSave() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("scheme_final_btn"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                            ) {
                                Text("Save Syllabus & See AI", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // SCREEN 6: FIRST GENERATION ("Wow" Moment!)
                    6 -> {
                        val firstSubjectName = selectedSubjectsList.firstOrNull() ?: "Subject"
                        val week1Topic = tempWeeklyTopics[firstSubjectName]?.get(1) ?: "Week 1 Topic"

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            Text(
                                text = "Your " + firstSubjectName + " Guide is Ready!",
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Experience Partic AI in action instantly. Watch it curate notes and practice questions modeled exactly for your classroom.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            // GENERATE PROMPT TRIGGER
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = BlueLight),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "WEEK 1 CURRICULUM PREVIEW:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BluePrimary)
                                    )
                                    Text(
                                        text = week1Topic,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                    )

                                    if (generatedMaterial == null && !isGenerating) {
                                        Button(
                                            onClick = { viewModel.generateFirstWeekAction() },
                                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("generate_week_1_btn")
                                        ) {
                                            Icon(Icons.Filled.Star, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Generate my Week 1 Notes", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // GENERATION LOADING STATE
                            if (isGenerating) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = BaseGrey),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(color = BluePrimary)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = loadingStatus,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            textAlign = TextAlign.Center,
                                            color = BluePrimary
                                        )
                                        Text(
                                            text = "Tailoring response to $selectedCountry curriculum guides...",
                                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }

                            // GENERATION ERROR STATE
                            if (generationError != null) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Generation Error",
                                            color = Color(0xFF991B1B),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = generationError ?: "Unknown generation glitch",
                                            color = Color(0xFFB91C1C),
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { viewModel.generateFirstWeekAction() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Retry Generation")
                                        }
                                    }
                                }
                            }

                            // SUCCESS VIEW PREVIEW
                            if (generatedMaterial != null) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = BaseGrey),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF10B981))
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "GENERATED SUCCESSFULLY",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF047857)
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = generatedMaterial!!.notes.take(300) + "...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextDark
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "✓ Includes 8 customized practice questions & answers!",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = BluePrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = { viewModel.completeOnboarding() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .testTag("onboarding_complete_btn"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                                ) {
                                    Text("Enter Study Dashboard 🚀", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
//          STUDENT HOME DASHBOARD
// ==========================================

@Composable
fun StudentHomeScreen(viewModel: StudyViewModel) {
    val profile by viewModel.profile.collectAsState()
    val subjects by viewModel.allSubjects.collectAsState()
    val materials by viewModel.allStudyMaterials.collectAsState()

    if (profile == null) return

    val totalWeeksSupported = subjects.firstOrNull { it.isAdded }?.totalWeeks ?: 12
    val totalUnlocked = materials.size

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp)
                    .padding(top = 28.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello, ${profile!!.name}! 👋",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = TextDark,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Text(
                            text = "${profile!!.country} • ${profile!!.classLevel}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    IconButton(
                        onClick = { viewModel.resetApp() },
                        modifier = Modifier.background(BaseGrey, CircleShape)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Application", tint = BluePrimary)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Welcome tagline or statistics block
                Card(
                     shape = RoundedCornerShape(16.dp),
                     colors = CardDefaults.cardColors(containerColor = BlueLight),
                     modifier = Modifier.fillMaxWidth()
                ) {
                     Row(
                         modifier = Modifier.padding(16.dp),
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                         Box(
                             modifier = Modifier
                                 .size(48.dp)
                                 .clip(RoundedCornerShape(10.dp))
                                 .background(BluePrimary),
                             contentAlignment = Alignment.Center
                         ) {
                             Icon(Icons.Default.Star, contentDescription = null, tint = Color.White)
                         }
                         Spacer(modifier = Modifier.width(16.dp))
                         Column {
                             Text(
                                 "Unlock your studies",
                                 fontWeight = FontWeight.Bold,
                                 fontSize = 15.sp,
                                 color = BlueDark
                             )
                             Text(
                                 "You've generated $totalUnlocked study guides so far.",
                                 color = TextMuted,
                                 fontSize = 12.sp
                             )
                         }
                     }
                }
            }
        },
        containerColor = BaseGrey
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "YOUR SUBJECTS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
            )

            // Let's grid display 4 Subject Cards in standard 2x2 design
            // Slots are mapped precisely to Subject ID 1, 2, 3, 4
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                for (row in 0..1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (col in 0..1) {
                            val subjectId = row * 2 + col + 1
                            val currentSubject = subjects.find { it.id == subjectId }
                            val cardTheme = getCardTheme(subjectId)
                            
                            Box(modifier = Modifier.weight(1f)) {
                                if (currentSubject != null && currentSubject.isAdded) {
                                    // Count unlocked material for this subject
                                    val subjectUnlocked = materials.count { it.subjectId == subjectId }
                                    SubjectCard(
                                        subject = currentSubject,
                                        unlockedCount = subjectUnlocked,
                                        totalWeeks = currentSubject.totalWeeks,
                                        theme = cardTheme,
                                        onClick = { viewModel.selectSubject(subjectId) }
                                    )
                                } else {
                                    // Empty dashed slot
                                    EmptySubjectCardSlot()
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SubjectCard(
    subject: Subject,
    unlockedCount: Int,
    totalWeeks: Int,
    theme: CardTheme,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = theme.bg),
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() }
            .testTag("subject_card_${subject.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.text
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = theme.text,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.labelSmall.copy(color = theme.text.copy(alpha = 0.8f))
                    )
                    Text(
                        text = "Wk $unlockedCount/$totalWeeks",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = theme.text
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = if (totalWeeks > 0) unlockedCount / totalWeeks.toFloat() else 0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = theme.text,
                    trackColor = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun EmptySubjectCardSlot() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            width = 1.5.dp,
            color = BorderLight
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = TextMuted.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Subject Slot",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted.copy(alpha = 0.5f)
                )
            }
        }
    }
}


// ==========================================
//          SUBJECT WEEKLY CURRICULUM
// ==========================================

@Composable
fun SubjectDashboardScreen(
    subjectId: Int,
    weeks: List<WeekTopic>,
    viewModel: StudyViewModel,
    onBack: () -> Unit
) {
    val allSubjects by viewModel.allSubjects.collectAsState()
    val subject = allSubjects.find { it.id == subjectId } ?: return
    val unlockedMaterials by viewModel.currentSubjectMaterials.collectAsState()
    val theme = getCardTheme(subjectId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                        )
                        Text(
                            text = "Course Scheme of Work",
                            style = MaterialTheme.typography.bodySmall.copy(color = theme.text)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = theme.text)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = theme.bg,
                    titleContentColor = theme.text
                )
            )
        },
        containerColor = BaseGrey
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(weeks) { wk ->
                val isUnlocked = unlockedMaterials.any { it.weekNum == wk.weekNum }
                
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectWeek(wk.weekNum) }
                        .testTag("week_row_${wk.weekNum}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isUnlocked) theme.bg else BaseGrey),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Wk ${wk.weekNum}",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = if (isUnlocked) theme.text else TextMuted
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = wk.topic.ifEmpty { "General Week ${wk.weekNum} Studies" },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnlocked) TextDark else TextDark.copy(alpha = 0.6f)
                                )
                            )
                            Text(
                                text = if (isUnlocked) "Materials study ready • Unlocked 📖" else "Syllabus set • Generated index",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isUnlocked) ColorGreenTxt else TextMuted
                                )
                            )
                        }

                        Icon(
                            imageVector = if (isUnlocked) Icons.Default.Edit else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isUnlocked) theme.text else TextMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}


// ==========================================
//          WEEK DETAIL (STUDY NOTES & QUIZ)
// ==========================================

@Composable
fun WeekDetailScreen(
    subjectId: Int,
    weekNum: Int,
    topic: String,
    viewModel: StudyViewModel,
    onBack: () -> Unit
) {
    val material by viewModel.currentMaterial.collectAsState()
    val questions by viewModel.parsedQuestions.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generationError by viewModel.generationError.collectAsState()
    val loadingStatus by viewModel.loadingStatus.collectAsState()

    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val isAnswerRevealed by viewModel.isAnswerRevealed.collectAsState()
    val selectedOptionIndex by viewModel.selectedOptionIndex.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Notes, 1 = Quiz
    val theme = getCardTheme(subjectId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Week $weekNum Syllabus",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                        )
                        Text(
                            text = topic.ifEmpty { "General Week $weekNum Studies" },
                            style = MaterialTheme.typography.bodySmall.copy(color = theme.text),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back", tint = theme.text)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = theme.bg,
                    titleContentColor = theme.text
                )
            )
        },
        containerColor = BaseGrey
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isGenerating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = theme.text)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = loadingStatus,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = theme.text),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Compiling detailed custom notes and 8 questions dynamically...",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                                modifier = Modifier.padding(top = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else if (material == null) {
                // GENERATION INITIAL REQUIRED
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = theme.text,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No Materials Generated Yet!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                            )
                            Text(
                                "Partic is ready to analyze this week's topic and curate your notes and custom practice exams.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                            )

                            if (generationError != null) {
                                Text(
                                    text = generationError!!,
                                    color = Color.Red,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }

                            Button(
                                onClick = { viewModel.generateStudyMaterialsForWeek(subjectId, weekNum) },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.text),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Generate Materials with AI", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // RENDER STUDY GUIDE TAB CONTENT
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.White,
                    contentColor = theme.text,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Study Notes 📖", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Practice Quiz 📝", fontWeight = FontWeight.Bold) }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    if (activeTab == 0) {
                        // NOTES RENDER
                        Box(modifier = Modifier.fillMaxSize()) {
                            StudyNotesView(notesText = material!!.notes)
                        }
                    } else {
                        // PRACTICE QUIZ CARDS
                        if (questions.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Parsing practice questions index...")
                            }
                        } else {
                            val activeQuestion = questions[currentQuestionIndex]
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Header metrics
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = theme.text,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(theme.bg, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = activeQuestion.type,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = theme.text
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Question text card
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = activeQuestion.questionText,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextDark,
                                            modifier = Modifier.padding(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // MCQ Option buttons list
                                    if (activeQuestion.type == "MCQ") {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            activeQuestion.options.forEachIndexed { optIndex, optionText ->
                                                val optionLetter = when(optIndex) {
                                                    0 -> "A"
                                                    1 -> "B"
                                                    2 -> "C"
                                                    else -> "D"
                                                }
                                                val isSelected = selectedOptionIndex == optIndex
                                                val isCorrect = activeQuestion.correctOptionIndex == optIndex

                                                val cardBorder = if (isAnswerRevealed) {
                                                    if (isCorrect) BorderStroke(1.5.dp, Color(0xFF10B981))
                                                    else if (isSelected) BorderStroke(1.5.dp, Color(0xFFEF4444))
                                                    else null
                                                } else {
                                                    if (isSelected) BorderStroke(1.5.dp, theme.text)
                                                    else null
                                                }

                                                val cardBg = if (isAnswerRevealed) {
                                                    if (isCorrect) Color(0xFFD1FAE5)
                                                    else if (isSelected) Color(0xFFFEE2E2)
                                                    else Color.White
                                                } else {
                                                    if (isSelected) theme.bg else Color.White
                                                }

                                                Card(
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                                    border = cardBorder,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { 
                                                            if (!isAnswerRevealed) {
                                                                viewModel.selectOption(optIndex)
                                                            }
                                                        }
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(14.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(28.dp)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(if (isSelected) theme.text else BaseGrey),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = optionLetter,
                                                                color = if (isSelected) Color.White else theme.text,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(14.dp))
                                                        Text(
                                                            text = optionText,
                                                            fontWeight = FontWeight.Medium,
                                                            color = TextDark
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // Theory helper placeholder
                                        Card(
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    "Theory Question",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = theme.text
                                                )
                                                Text(
                                                    "Think of your solution or write it down, then tap 'Reveal Model Answer' to review teacher instructions.",
                                                    fontSize = 12.sp,
                                                    color = TextMuted,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Answer reveal explanation card
                                    if (isAnswerRevealed) {
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Card(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    text = "Teacher's Evaluation:",
                                                    fontWeight = FontWeight.Bold,
                                                    color = BluePrimary,
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = activeQuestion.answer,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextDark
                                                )
                                            }
                                        }
                                    }
                                }

                                // BOTTOM NAVIGATION TRIGGERS
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        if (!isAnswerRevealed) {
                                            Button(
                                                onClick = { viewModel.revealAnswer() },
                                                colors = ButtonDefaults.buttonColors(containerColor = theme.text),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Info, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Reveal Answer", fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Button(
                                                onClick = { viewModel.prevQuestion() },
                                                enabled = currentQuestionIndex > 0,
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = theme.text),
                                                border = BorderStroke(1.dp, theme.text),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Prev")
                                            }
                                            Button(
                                                onClick = { viewModel.nextQuestion() },
                                                enabled = currentQuestionIndex < questions.size - 1,
                                                colors = ButtonDefaults.buttonColors(containerColor = theme.text),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Next")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
//          DYNAMIC MARKDOWN TEXT VIEWER
// ==========================================

@Composable
fun StudyNotesView(notesText: String) {
    val itemsList = remember(notesText) {
        notesText.split("\n")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(itemsList) { line ->
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("# ") -> {
                        Text(
                            text = trimmed.substring(2).trim(),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = BluePrimary,
                                fontSize = 22.sp
                            ),
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        )
                    }
                    trimmed.startsWith("## ") -> {
                        Text(
                            text = trimmed.substring(3).trim(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = BlueDark,
                                fontSize = 18.sp
                            ),
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }
                    trimmed.startsWith("### ") -> {
                        Text(
                            text = trimmed.substring(4).trim(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                    }
                    trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 6.dp, top = 2.dp, bottom = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                "• ",
                                color = BluePrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (trimmed.startsWith("* ")) trimmed.substring(2).trim() else trimmed.substring(2).trim(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextDark
                            )
                        }
                    }
                    trimmed.isEmpty() -> {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    else -> {
                        Text(
                            text = trimmed,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextDark,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// Simple FlowRow helper representation for chip lists
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val xSpace = 8.dp.roundToPx()
        val ySpace = 8.dp.roundToPx()
        var currentX = 0
        var currentY = 0
        var maxRowHeight = 0
        val placeables = measurables.map { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            if (currentX + placeable.width > constraints.maxWidth && currentX > 0) {
                currentX = 0
                currentY += maxRowHeight + ySpace
                maxRowHeight = 0
            }
            maxRowHeight = maxOf(maxRowHeight, placeable.height)
            currentX += placeable.width + xSpace
            placeable
        }

        layout(
            width = constraints.maxWidth,
            height = if (placeables.isEmpty()) 0 else currentY + maxRowHeight
        ) {
            var x = 0
            var y = 0
            var rowH = 0
            placeables.forEach { placeable ->
                if (x + placeable.width > constraints.maxWidth && x > 0) {
                    x = 0
                    y += rowH + ySpace
                    rowH = 0
                }
                placeable.placeRelative(x, y)
                rowH = maxOf(rowH, placeable.height)
                x += placeable.width + xSpace
            }
        }
    }
}
