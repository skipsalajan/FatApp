package com.skip.FatApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skip.FatApp.data.FatAppDatabase
import com.skip.FatApp.data.WeightEntry
import com.skip.FatApp.presentation.WeightViewModel
import com.skip.FatApp.presentation.WeightViewModelFactory
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.enableEdgeToEdge
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.IconButton
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.outlined.DirectionsWalk
import com.skip.FatApp.presentation.ActivityViewModel
import com.skip.FatApp.presentation.ActivityViewModelFactory
import com.skip.FatApp.presentation.activity.ActivityScreen
import com.skip.FatApp.presentation.bmi.BmiCard
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.activity.result.ActivityResultLauncher
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import com.skip.FatApp.data.health.HealthConnectStepsRepository
import com.skip.FatApp.data.ActivityEntry
import com.skip.FatApp.data.ActivityType
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Settings
import com.skip.FatApp.presentation.SettingsDialog
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import com.skip.FatApp.presentation.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.skip.FatApp.data.AutoWalkingLogger


private val AppBackground = Color(0xFFF5F8F7)
private val DarkText = Color(0xFF19312F)
private val PrimaryTeal = Color(0xFF19766C)
private val AccentTeal = Color(0xFFDCEEE9)
private val BorderColor = Color(0xFFDCE7E4)
private val MutedText = Color(0xFF6B7D7A)
private val TipBackground = Color(0xFFE9F3EF)

class MainActivity : ComponentActivity() {

    private lateinit var stepsRepository: HealthConnectStepsRepository
    private lateinit var healthConnectPermissionLauncher: ActivityResultLauncher<Set<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                var showSplash by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(2000)
                    showSplash = false
                }

                if (showSplash) {
                    SplashScreen()
                } else {
                    FatApp()
                }
            }
        }
        stepsRepository = HealthConnectStepsRepository(this)

        val permissionContract = PermissionController.createRequestPermissionResultContract()

        healthConnectPermissionLauncher =
            registerForActivityResult(permissionContract) { granted: Set<String> ->
                val stepsReadGranted =
                    granted.contains("android.permission.health.READ_STEPS")

                if (stepsReadGranted) {
                    syncYesterdaySteps()
                }
            }
        requestStepsPermission()
    }
    private fun requestStepsPermission() {
        if (!stepsRepository.isHealthConnectAvailable()) {
            // Health Connect not available – we can show a message later
            return
        }

        val permissions = setOf(
            HealthPermission.getReadPermission(StepsRecord::class)
        )

        healthConnectPermissionLauncher.launch(permissions)
    }
    private fun syncYesterdaySteps() {
        val database = FatAppDatabase.getInstance(applicationContext)
        val activityDao = database.activityDao()

        val yesterday = LocalDate.now().minusDays(1)
        val zoneId = ZoneId.systemDefault()

        val yesterdayStartMillis = yesterday
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

        lifecycleScope.launch {
            val existingEntries = withContext(Dispatchers.IO) {
                activityDao.getEntriesForDate(yesterdayStartMillis)
            }

            val alreadySynced = existingEntries.any { entry ->
                entry.type == ActivityType.WALKING &&
                        entry.activityName == "Walking"
            }

            if (alreadySynced) {
                return@launch
            }

            val steps = try {
                stepsRepository.getStepsForDate(
                    date = yesterday,
                    zoneId = zoneId
                )
            } catch (exception: Exception) {
                return@launch
            }

            if (steps <= 0) {
                return@launch
            }

            val walkingEntry = ActivityEntry(
                type = ActivityType.WALKING,
                activityName = "Walking",
                dateMillis = yesterdayStartMillis,
                steps = steps.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                durationMinutes = null,
                estimatedCalories = 0
            )

            withContext(Dispatchers.IO) {
                activityDao.insert(walkingEntry)
            }
        }
    }
    }


@Composable
fun FatApp() {
    val context = LocalContext.current

    val database = remember { FatAppDatabase.getInstance(context.applicationContext) }

    val activityDao = remember { database.activityDao() }

    val scope = rememberCoroutineScope()

    val weightViewModel: WeightViewModel = viewModel(
        factory = WeightViewModelFactory(database.weightDao())
    )
    val activityViewModel: ActivityViewModel = viewModel(
        factory = ActivityViewModelFactory(database.activityDao())
    )

    val activityEntries by activityViewModel.entries.collectAsStateWithLifecycle()
    val entries by weightViewModel.entries.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf("Log") }
    var showSettings by remember { mutableStateOf(false) }

    var stepTrackingEnabled by remember {
        mutableStateOf(
            com.skip.FatApp.data.StepTrackingPrefs.isStepTrackingEnabled(context)
        )
    }
    // On each app start, check if we need to create yesterday's Walking entry
    scope.launch {
        // We pass 0L as currentCounter; AutoWalkingLogger only uses it for baseline reset
        AutoWalkingLogger.maybeCreateYesterdayWalkingEntry(
            context = context,
            activityDao = activityDao,
            currentCounter = 0L
        )
    }

    val activityRecognitionPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            stepTrackingEnabled = true
            com.skip.FatApp.data.StepTrackingPrefs.setStepTrackingEnabled(context, true)
        } else {
            stepTrackingEnabled = false
            com.skip.FatApp.data.StepTrackingPrefs.setStepTrackingEnabled(context, false)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        color = AppBackground
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                "Log" -> LogWeightScreen(
                    entries = entries,
                    onSaveWeight = { weightText, dateMillis ->
                        weightViewModel.saveWeight(weightText, dateMillis)
                    }
                )

                "History" -> HistoryScreen(
                    entries = entries,
                    onDeleteEntry = weightViewModel::deleteWeight,
                    onOpenSettings = {
                        showSettings = true
                    }
                )

                "Trend" -> TrendForecastScreen(entries = entries)
                "Activity" -> ActivityScreen(
                    stepTrackingEnabled = stepTrackingEnabled,
                    viewModel = activityViewModel,
                    entries = activityEntries,
                    onBackToMain = {
                        selectedTab = "Log"
                    }
                )
            }

            BottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            if (showSettings) {
                SettingsDialog(
                    entries = entries,
                    stepTrackingEnabled = stepTrackingEnabled,
                    onSetStepTrackingEnabled = { enabled ->
                        stepTrackingEnabled = enabled

                        com.skip.FatApp.data.StepTrackingPrefs.setStepTrackingEnabled(
                            context,
                            enabled
                        )

                        if (enabled) {
                            com.skip.FatApp.data.AutoWalkingLogger.resetBaseline(context)
                        }
                    },
                    onDismiss = {
                        showSettings = false
                    }
                )
            }
        }
    }


}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun LogWeightScreen(
    entries: List<WeightEntry>,
    onSaveWeight: (String, Long) -> Unit
) {
    var weightText by remember { mutableStateOf("") }

    val today = LocalDate.now()

    var selectedDate by remember {
        mutableStateOf(today)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    val headingDate = selectedDate.format(
        DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
    ).uppercase()

    val readableDate = selectedDate.format(
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
    )

    val selectedDateMillis = selectedDate
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    val latestEntry = entries.firstOrNull()

    if (showDatePicker) {
        WeightDatePickerDialog(
            selectedDate = selectedDate,
            onDateSelected = { newDate ->
                selectedDate = newDate
                showDatePicker = false
            },
            onDismiss = {
                showDatePicker = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(bottom = 130.dp)
    ) {
        AppHeader()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 34.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                SmallCapsText(headingDate)

                Text(
                    text = "Log weight",
                    color = DarkText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1.5).sp
                )
            }

            Box(
                modifier = Modifier.clickable {
                    showDatePicker = true
                }
            ) {
                IconBox(icon = Icons.Outlined.CalendarMonth)
            }
        }

        Spacer(modifier = Modifier.height(27.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Weight",
                        color = DarkText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "(kg)",
                        color = MutedText,
                        fontSize = 13.sp
                    )
                }

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { value ->
                        weightText = value.filter {
                            it.isDigit() || it == '.' || it == ','
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    singleLine = true,
                    textStyle = TextStyle(
                        color = DarkText,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-3).sp
                    ),
                    trailingIcon = {
                        Text(
                            text = "kg",
                            color = MutedText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BorderColor,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = PrimaryTeal
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDatePicker = true
                        }
                        .padding(top = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Choose date",
                        tint = PrimaryTeal,
                        modifier = Modifier.size(17.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (selectedDate == today) {
                            "Today, $readableDate"
                        } else {
                            readableDate
                        },
                        color = MutedText,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = {
                        onSaveWeight(weightText, selectedDateMillis)
                        weightText = ""
                    },
                    enabled = weightText
                        .replace(",", ".")
                        .toDoubleOrNull() != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryTeal,
                        contentColor = Color.White,
                        disabledContainerColor = BorderColor,
                        disabledContentColor = MutedText
                    )
                ) {
                    Text(
                        text = "Save weight",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        LatestEntryRow(latestEntry = latestEntry)
        BmiCard(
            latestWeightKg = latestEntry?.weightKg
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TipBackground)
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Timeline,
                contentDescription = null,
                tint = PrimaryTeal,
                modifier = Modifier
                    .size(22.dp)
                    .padding(top = 1.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Small steps, steady progress.",
                    color = DarkText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Logging regularly helps you see the bigger picture.",
                    color = MutedText,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun AppHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBox(
            icon = Icons.Outlined.MonitorWeight,
            size = 38.dp,
            iconSize = 20.dp
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            SmallCapsText("Personal health")
            Text(
                text = "FatApp",
                color = DarkText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFD5E7E2)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "F",
                color = PrimaryTeal,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun LatestEntryRow(latestEntry: WeightEntry?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBox(
            icon = Icons.Outlined.History,
            size = 38.dp,
            iconSize = 19.dp
        )
        Spacer(modifier = Modifier.width(13.dp))
        Column {
            SmallCapsText("Latest entry")
            Spacer(modifier = Modifier.height(4.dp))
            if (latestEntry == null) {
                Text(
                    text = "No weight logged yet",
                    color = MutedText,
                    fontSize = 13.sp
                )
            } else {
                Text(
                    text = "${formatWeight(latestEntry.weightKg)} kg  ·  ${formatEntryDate(latestEntry.dateMillis)}",
                    color = DarkText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "›",
            color = MutedText,
            fontSize = 30.sp,
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
fun IconBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    size: Dp = 46.dp,
    iconSize: Dp = 21.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(AccentTeal),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryTeal,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun SmallCapsText(text: String) {
    Text(
        text = text.uppercase(),
        color = MutedText,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.2.sp
    )
}

@Composable
fun PlaceholderScreen(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp)
            .padding(bottom = 100.dp)
    ) {
        AppHeader()
        Spacer(modifier = Modifier.height(45.dp))
        Text(
            text = title,
            color = DarkText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = message,
            color = MutedText,
            fontSize = 16.sp
        )
    }
}

@Composable
fun BottomNavigation(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 10.dp, horizontal = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NavigationItem(
                label = "Log",
                icon = Icons.Outlined.Add,
                isSelected = selectedTab == "Log",
                onClick = { onTabSelected("Log") }
            )
            NavigationItem(
                label = "History",
                icon = Icons.Outlined.History,
                isSelected = selectedTab == "History",
                onClick = { onTabSelected("History") }
            )
            NavigationItem(
                label = "Trend",
                icon = Icons.Outlined.ShowChart,
                isSelected = selectedTab == "Trend",
                onClick = { onTabSelected("Trend") }
            )
            NavigationItem(
                label = "Activity",
                icon = Icons.Outlined.DirectionsWalk,
                isSelected = selectedTab == "Activity",
                onClick = { onTabSelected("Activity") }
            )
        }
    }
}

@Composable
fun NavigationItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(27.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) AccentTeal else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) PrimaryTeal else Color(0xFF8A9B98),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isSelected) PrimaryTeal else Color(0xFF8A9B98),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun formatWeight(weight: Double): String {
    return String.format(Locale.getDefault(), "%.1f", weight)
}

fun formatEntryDate(dateMillis: Long): String {
    val date = Instant.ofEpochMilli(dateMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    return date.format(
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
    )
}
@Composable
fun HistoryScreen(
    entries: List<WeightEntry>,
    onDeleteEntry: (WeightEntry) -> Unit,
    onOpenSettings: () -> Unit
) {
    var entryToDelete by remember {
        mutableStateOf<WeightEntry?>(null)
    }

    val newestFirst = entries
    val oldestFirst = entries.reversed()
    val latestEntry = newestFirst.firstOrNull()
    val previousEntry = newestFirst.getOrNull(1)

    val change = if (latestEntry != null && previousEntry != null) {
        latestEntry.weightKg - previousEntry.weightKg
    } else {
        null
    }

    if (entryToDelete != null) {
        DeleteEntryDialog(
            entry = entryToDelete!!,
            onConfirmDelete = {
                onDeleteEntry(entryToDelete!!)
                entryToDelete = null
            },
            onDismiss = {
                entryToDelete = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp)
            .padding(bottom = 88.dp)
    ) {
        AppHeader()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 34.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                SmallCapsText("Your journey")

                Text(
                    text = "History",
                    color = DarkText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1.5).sp
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .clickable(onClick = onOpenSettings),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = PrimaryTeal,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(27.dp))

        if (entries.isEmpty()) {
            EmptyHistoryCard()
        } else {
            HistoryChartCard(
                latestEntry = latestEntry!!,
                change = change,
                entriesOldestFirst = oldestFirst
            )

            Spacer(modifier = Modifier.height(27.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All entries",
                    color = DarkText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "${entries.size} ${if (entries.size == 1) "log" else "logs"}",
                    color = MutedText,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = newestFirst,
                    key = { entry -> entry.id }
                ) { entry ->
                    HistoryEntryRow(
                        entry = entry,
                        isLatest = entry.id == latestEntry.id,
                        onClick = {
                            entryToDelete = entry
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            SmallCapsText("Weight over time")

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No entries yet",
                color = DarkText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Save your first weight on the Log tab to see your progress here.",
                color = MutedText,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun HistoryChartCard(
    latestEntry: WeightEntry,
    change: Double?,
    entriesOldestFirst: List<WeightEntry>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                start = 18.dp,
                top = 21.dp,
                end = 18.dp,
                bottom = 16.dp
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    SmallCapsText("Weight over time")

                    Spacer(modifier = Modifier.height(5.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatWeight(latestEntry.weightKg),
                            color = DarkText,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1.2).sp
                        )

                        Spacer(modifier = Modifier.width(5.dp))

                        Text(
                            text = "kg",
                            color = MutedText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                if (change != null) {
                    val isDown = change < 0
                    val label = if (isDown) {
                        "↘ ${formatSignedWeight(change)} kg"
                    } else {
                        "↗ ${formatSignedWeight(change)} kg"
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(AccentTeal)
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            color = if (isDown) PrimaryTeal else DarkText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            WeightHistoryChart(
                entries = entriesOldestFirst,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            )
        }
    }
}

@Composable
fun WeightHistoryChart(
    entries: List<WeightEntry>,
    modifier: Modifier = Modifier
) {
    val weights = entries.map { it.weightKg }

    val rawMin = weights.minOrNull() ?: 0.0
    val rawMax = weights.maxOrNull() ?: 1.0

    val paddingWeight = if (rawMax == rawMin) 1.0 else (rawMax - rawMin) * 0.25
    val minWeight = rawMin - paddingWeight
    val maxWeight = rawMax + paddingWeight
    val range = (maxWeight - minWeight).coerceAtLeast(0.5)

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 26.dp, end = 4.dp)
            ) {
                val chartWidth = size.width
                val chartHeight = size.height
                val gridLines = 4

                for (i in 0..gridLines) {
                    val y = chartHeight * i / gridLines
                    drawLine(
                        color = Color(0xFFE8EFED),
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f
                    )
                }

                if (entries.size == 1) {
                    val y = chartHeight / 2f
                    drawCircle(
                        color = PrimaryTeal,
                        radius = 8f,
                        center = Offset(chartWidth / 2f, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(chartWidth / 2f, y)
                    )
                } else {
                    val stepX = chartWidth / (entries.size - 1)

                    val points = entries.mapIndexed { index, entry ->
                        val x = index * stepX
                        val y = chartHeight -
                                (((entry.weightKg - minWeight) / range) * chartHeight).toFloat()

                        Offset(x, y)
                    }

                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)

                        for (point in points.drop(1)) {
                            lineTo(point.x, point.y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = PrimaryTeal,
                        style = Stroke(
                            width = 5f,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                        )
                    )

                    val last = points.last()

                    drawCircle(
                        color = Color.White,
                        radius = 9f,
                        center = last
                    )

                    drawCircle(
                        color = PrimaryTeal,
                        radius = 6f,
                        center = last
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(5) { index ->
                    val value = maxWeight - (range * index / 4)
                    Text(
                        text = String.format(Locale.getDefault(), "%.0f", value),
                        color = Color(0xFF92A19F),
                        fontSize = 9.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 26.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val first = entries.first()
            val middle = entries[entries.size / 2]
            val last = entries.last()

            ChartDateLabel(first.dateMillis)
            if (entries.size > 2) {
                ChartDateLabel(middle.dateMillis)
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            ChartDateLabel(last.dateMillis)
        }
    }
}

@Composable
fun ChartDateLabel(dateMillis: Long) {
    val date = Instant.ofEpochMilli(dateMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    Text(
        text = date.format(
            DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
        ),
        color = Color(0xFF92A19F),
        fontSize = 9.sp
    )
}

@Composable
fun HistoryEntryRow(
    entry: WeightEntry,
    isLatest: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (isLatest) 12.dp else 8.dp)
                .clip(CircleShape)
                .background(if (isLatest) PrimaryTeal else Color(0xFFBED3CE))
        )

        Spacer(modifier = Modifier.width(13.dp))

        Column {
            Text(
                text = "${formatWeight(entry.weightKg)} kg",
                color = DarkText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = formatEntryDate(entry.dateMillis),
                color = MutedText,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (isLatest) {
            Text(
                text = "Latest",
                color = PrimaryTeal,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        } else {
            Text(
                text = "Delete",
                color = MutedText,
                fontSize = 11.sp
            )
        }
    }
}

fun formatSignedWeight(weight: Double): String {
    return String.format(
        Locale.getDefault(),
        "%+.1f",
        weight
    )
}
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun WeightDatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedDateMillis = selectedDate
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis,
        yearRange = 2020..LocalDate.now().year
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis

                    if (millis != null) {
                        val newDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        if (!newDate.isAfter(LocalDate.now())) {
                            onDateSelected(newDate)
                        }
                    }
                }
            ) {
                Text(
                    text = "Select",
                    color = PrimaryTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancel",
                    color = MutedText
                )
            }
        }
    ) {
        androidx.compose.material3.DatePicker(
            state = datePickerState,
            showModeToggle = false
        )
    }
}
@Composable
fun DeleteEntryDialog(
    entry: WeightEntry,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete entry?",
                color = DarkText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Delete ${formatWeight(entry.weightKg)} kg logged on ${formatEntryDate(entry.dateMillis)}? This cannot be undone.",
                color = MutedText
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = onConfirmDelete
            ) {
                Text(
                    text = "Delete",
                    color = Color(0xFFB3261E),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancel",
                    color = PrimaryTeal
                )
            }
        }
    )
}
data class ForecastPoint(
    val dateMillis: Long,
    val weightKg: Double
)

data class TrendForecast(
    val slopeKgPerDay: Double,
    val projectedWeightKg: Double,
    val forecastPoints: List<ForecastPoint>
)

@Composable
fun TrendForecastScreen(entries: List<WeightEntry>) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val oldestFirst = entries.reversed()
    val trend = calculateTrendForecast(oldestFirst)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp)
            .padding(bottom = 88.dp)
    ) {
        AppHeader()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 34.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                SmallCapsText("Looking ahead")

                Text(
                    text = "Trend & forecast",
                    color = DarkText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1.5).sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (trend != null && oldestFirst.isNotEmpty()) {
                            scope.launch {
                                val imageUri = exportTrendReportImage(
                                    context = context,
                                    historyEntries = oldestFirst,
                                    trend = trend
                                )

                                if (imageUri != null) {
                                    shareImage(context, imageUri)
                                }
                            }
                        }
                    },
                    enabled = trend != null,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (trend != null) Color.White else BorderColor
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Export trend image",
                        tint = if (trend != null) PrimaryTeal else MutedText,
                        modifier = Modifier.size(21.dp)
                    )
                }

                IconBox(icon = Icons.Outlined.ShowChart)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (trend == null) {
            EmptyTrendCard()
        } else {
            TrendMetricCards(trend)

            Spacer(modifier = Modifier.height(13.dp))

            ForecastChartCard(
                historyEntries = oldestFirst,
                trend = trend
            )

            Spacer(modifier = Modifier.height(14.dp))

            ForecastSummaryCard(trend)
        }
    }
}

@Composable
fun EmptyTrendCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            SmallCapsText("Forecast unavailable")

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Add at least 2 entries",
                color = DarkText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = "Log weights on at least two different dates to calculate a meaningful trend and three-month forecast.",
                color = MutedText,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun TrendMetricCards(trend: TrendForecast) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TrendMetricCard(
            modifier = Modifier.weight(1f),
            label = "Current trend",
            value = formatSignedWeight(trend.slopeKgPerDay),
            unit = "kg/day",
            note = if (trend.slopeKgPerDay < 0) "Gently falling" else "Gently rising",
            valueColor = if (trend.slopeKgPerDay < 0) PrimaryTeal else DarkText
        )

        TrendMetricCard(
            modifier = Modifier.weight(1f),
            label = "Monthly pace",
            value = formatSignedWeight(trend.slopeKgPerDay * 30),
            unit = "kg/month",
            note = "Based on your logs",
            valueColor = DarkText
        )
    }
}

@Composable
fun TrendMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String,
    note: String,
    valueColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            SmallCapsText(label)

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = value,
                color = valueColor,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.2).sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = unit,
                color = MutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = note,
                color = MutedText,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun ForecastChartCard(
    historyEntries: List<WeightEntry>,
    trend: TrendForecast
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                start = 18.dp,
                top = 21.dp,
                end = 18.dp,
                bottom = 16.dp
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    SmallCapsText("Projected weight")

                    Spacer(modifier = Modifier.height(5.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatWeight(trend.projectedWeightKg),
                            color = DarkText,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1.2).sp
                        )

                        Spacer(modifier = Modifier.width(5.dp))

                        Text(
                            text = "kg",
                            color = MutedText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                Column {
                    LegendItem(
                        label = "History",
                        color = PrimaryTeal,
                        dashed = false
                    )

                    Spacer(modifier = Modifier.height(7.dp))

                    LegendItem(
                        label = "Forecast",
                        color = Color(0xFF68A883),
                        dashed = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            TrendForecastChart(
                historyEntries = historyEntries,
                forecastPoints = trend.forecastPoints,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            )
        }
    }
}

@Composable
fun LegendItem(
    label: String,
    color: Color,
    dashed: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            modifier = Modifier
                .width(14.dp)
                .height(4.dp)
        ) {
            drawLine(
                color = color,
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 3f,
                pathEffect = if (dashed) {
                    PathEffect.dashPathEffect(floatArrayOf(5f, 4f))
                } else {
                    null
                }
            )
        }

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = label,
            color = MutedText,
            fontSize = 10.sp
        )
    }
}

@Composable
fun TrendForecastChart(
    historyEntries: List<WeightEntry>,
    forecastPoints: List<ForecastPoint>,
    modifier: Modifier = Modifier
) {
    val allPoints = historyEntries.map {
        ForecastPoint(it.dateMillis, it.weightKg)
    } + forecastPoints

    val allWeights = allPoints.map { it.weightKg }
    val rawMin = allWeights.minOrNull() ?: 0.0
    val rawMax = allWeights.maxOrNull() ?: 1.0
    val paddingWeight = if (rawMax == rawMin) 1.0 else (rawMax - rawMin) * 0.25
    val minWeight = rawMin - paddingWeight
    val maxWeight = rawMax + paddingWeight
    val range = (maxWeight - minWeight).coerceAtLeast(0.5)

    val firstDate = historyEntries.first().dateMillis
    val lastForecastDate = forecastPoints.last().dateMillis
    val totalDays = ((lastForecastDate - firstDate) / 86_400_000.0).coerceAtLeast(1.0)

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 26.dp, end = 4.dp)
            ) {
                val chartWidth = size.width
                val chartHeight = size.height
                val gridLines = 4

                for (i in 0..gridLines) {
                    val y = chartHeight * i / gridLines
                    drawLine(
                        color = Color(0xFFE8EFED),
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f
                    )
                }

                fun toPoint(point: ForecastPoint): Offset {
                    val dayFromStart = (point.dateMillis - firstDate) / 86_400_000.0
                    val x = (dayFromStart / totalDays * chartWidth).toFloat()
                    val y = chartHeight -
                            (((point.weightKg - minWeight) / range) * chartHeight).toFloat()

                    return Offset(x, y)
                }

                val historyPoints = historyEntries.map {
                    toPoint(ForecastPoint(it.dateMillis, it.weightKg))
                }

                if (historyPoints.size >= 2) {
                    val historyPath = Path().apply {
                        moveTo(historyPoints.first().x, historyPoints.first().y)

                        for (point in historyPoints.drop(1)) {
                            lineTo(point.x, point.y)
                        }
                    }

                    drawPath(
                        path = historyPath,
                        color = PrimaryTeal,
                        style = Stroke(
                            width = 5f,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                        )
                    )
                }

                val forecastStart = historyPoints.last()
                val forecastChartPoints = forecastPoints.map(::toPoint)

                val forecastPath = Path().apply {
                    moveTo(forecastStart.x, forecastStart.y)

                    for (point in forecastChartPoints) {
                        lineTo(point.x, point.y)
                    }
                }

                drawPath(
                    path = forecastPath,
                    color = Color(0xFF68A883),
                    style = Stroke(
                        width = 4f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(10f, 8f)
                        )
                    )
                )

                val historyLast = historyPoints.last()
                drawCircle(
                    color = Color.White,
                    radius = 9f,
                    center = historyLast
                )
                drawCircle(
                    color = PrimaryTeal,
                    radius = 6f,
                    center = historyLast
                )

                val forecastLast = forecastChartPoints.last()
                drawCircle(
                    color = Color.White,
                    radius = 9f,
                    center = forecastLast
                )
                drawCircle(
                    color = Color(0xFF68A883),
                    radius = 6f,
                    center = forecastLast
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(5) { index ->
                    val value = maxWeight - (range * index / 4)
                    Text(
                        text = String.format(Locale.getDefault(), "%.0f", value),
                        color = Color(0xFF92A19F),
                        fontSize = 9.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 26.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ChartDateLabel(historyEntries.first().dateMillis)
            ChartDateLabel(historyEntries.last().dateMillis)
            ChartDateLabel(forecastPoints.last().dateMillis)
        }
    }
}

@Composable
fun ForecastSummaryCard(trend: TrendForecast) {
    val endDate = trend.forecastPoints.last().dateMillis

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBox(
                icon = Icons.Outlined.ShowChart,
                size = 36.dp,
                iconSize = 18.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                SmallCapsText("In 3 months")

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "${formatWeight(trend.projectedWeightKg)} kg",
                    color = DarkText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Expected on ${formatEntryDate(endDate)}",
                    color = MutedText,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "›",
                color = MutedText,
                fontSize = 30.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

fun calculateTrendForecast(
    entriesOldestFirst: List<WeightEntry>
): TrendForecast? {
    if (entriesOldestFirst.size < 2) {
        return null
    }

    val firstDate = entriesOldestFirst.first().dateMillis
    val xValues = entriesOldestFirst.map {
        (it.dateMillis - firstDate) / 86_400_000.0
    }
    val yValues = entriesOldestFirst.map { it.weightKg }

    val count = entriesOldestFirst.size.toDouble()
    val meanX = xValues.average()
    val meanY = yValues.average()

    val numerator = xValues.indices.sumOf { index ->
        (xValues[index] - meanX) * (yValues[index] - meanY)
    }

    val denominator = xValues.sumOf { x ->
        (x - meanX) * (x - meanX)
    }

    if (denominator == 0.0) {
        return null
    }

    val slope = numerator / denominator
    val intercept = meanY - slope * meanX

    val lastEntry = entriesOldestFirst.last()
    val lastDay = (lastEntry.dateMillis - firstDate) / 86_400_000.0
    val forecastDays = 90

    val forecastPoints = (1..forecastDays).map { dayAfterLast ->
        val dayFromStart = lastDay + dayAfterLast
        val predictedWeight = intercept + slope * dayFromStart
        val dateMillis = lastEntry.dateMillis + (dayAfterLast * 86_400_000L)

        ForecastPoint(
            dateMillis = dateMillis,
            weightKg = predictedWeight
        )
    }

    return TrendForecast(
        slopeKgPerDay = slope,
        projectedWeightKg = forecastPoints.last().weightKg,
        forecastPoints = forecastPoints
    )
}
suspend fun exportTrendReportImage(
    context: Context,
    historyEntries: List<WeightEntry>,
    trend: TrendForecast
): Uri? {
    return try {
        val imageWidth = 1080
        val imageHeight = 1350

        val bitmap = Bitmap.createBitmap(
            imageWidth,
            imageHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = AndroidCanvas(bitmap)

        val backgroundPaint = Paint().apply {
            color = AndroidColor.rgb(245, 248, 247)
        }

        canvas.drawRect(
            0f,
            0f,
            imageWidth.toFloat(),
            imageHeight.toFloat(),
            backgroundPaint
        )

        val darkTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(25, 49, 47)
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            )
        }

        val mutedTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(107, 125, 122)
        }

        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
        }

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(220, 231, 228)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        val historyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(25, 118, 108)
            style = Paint.Style.STROKE
            strokeWidth = 9f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val forecastPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(104, 168, 131)
            style = Paint.Style.STROKE
            strokeWidth = 8f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            pathEffect = android.graphics.DashPathEffect(
                floatArrayOf(18f, 14f),
                0f
            )
        }

        fun drawCard(
            left: Float,
            top: Float,
            right: Float,
            bottom: Float
        ) {
            val rectangle = android.graphics.RectF(
                left,
                top,
                right,
                bottom
            )

            canvas.drawRoundRect(
                rectangle,
                34f,
                34f,
                cardPaint
            )

            canvas.drawRoundRect(
                rectangle,
                34f,
                34f,
                borderPaint
            )
        }

        // Header
        mutedTextPaint.textSize = 24f
        canvas.drawText(
            "PERSONAL HEALTH",
            70f,
            90f,
            mutedTextPaint
        )

        darkTextPaint.textSize = 44f
        canvas.drawText(
            "FatApp",
            70f,
            142f,
            darkTextPaint
        )

        mutedTextPaint.textSize = 23f
        canvas.drawText(
            "LOOKING AHEAD",
            70f,
            230f,
            mutedTextPaint
        )

        darkTextPaint.textSize = 58f
        canvas.drawText(
            "Trend & forecast",
            70f,
            300f,
            darkTextPaint
        )

        // Trend cards
        drawCard(70f, 350f, 520f, 585f)
        drawCard(560f, 350f, 1010f, 585f)

        mutedTextPaint.textSize = 19f
        canvas.drawText(
            "CURRENT TREND",
            100f,
            400f,
            mutedTextPaint
        )
        canvas.drawText(
            "MONTHLY PACE",
            590f,
            400f,
            mutedTextPaint
        )

        darkTextPaint.textSize = 52f
        canvas.drawText(
            formatSignedWeight(trend.slopeKgPerDay),
            100f,
            474f,
            darkTextPaint
        )
        canvas.drawText(
            formatSignedWeight(trend.slopeKgPerDay * 30),
            590f,
            474f,
            darkTextPaint
        )

        mutedTextPaint.textSize = 23f
        canvas.drawText(
            "kg/day",
            100f,
            515f,
            mutedTextPaint
        )
        canvas.drawText(
            "kg/month",
            590f,
            515f,
            mutedTextPaint
        )

        // Chart card
        drawCard(70f, 625f, 1010f, 1110f)

        mutedTextPaint.textSize = 19f
        canvas.drawText(
            "PROJECTED WEIGHT",
            105f,
            680f,
            mutedTextPaint
        )

        darkTextPaint.textSize = 55f
        canvas.drawText(
            "${formatWeight(trend.projectedWeightKg)} kg",
            105f,
            752f,
            darkTextPaint
        )

        val chartLeft = 125f
        val chartTop = 800f
        val chartRight = 955f
        val chartBottom = 1035f
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        val allPoints = historyEntries.map { entry ->
            ForecastPoint(
                dateMillis = entry.dateMillis,
                weightKg = entry.weightKg
            )
        } + trend.forecastPoints

        val allWeights = allPoints.map { it.weightKg }
        val rawMin = allWeights.minOrNull() ?: 0.0
        val rawMax = allWeights.maxOrNull() ?: 1.0
        val extraPadding = if (rawMax == rawMin) {
            1.0
        } else {
            (rawMax - rawMin) * 0.25
        }

        val minWeight = rawMin - extraPadding
        val maxWeight = rawMax + extraPadding
        val weightRange = (maxWeight - minWeight).coerceAtLeast(0.5)

        val firstDate = historyEntries.first().dateMillis
        val finalForecastDate = trend.forecastPoints.last().dateMillis
        val totalDays = (
                (finalForecastDate - firstDate) / 86_400_000.0
                ).coerceAtLeast(1.0)

        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(232, 239, 237)
            strokeWidth = 2f
        }

        for (index in 0..4) {
            val y = chartTop + (chartHeight * index / 4f)

            canvas.drawLine(
                chartLeft,
                y,
                chartRight,
                y,
                gridPaint
            )
        }

        fun chartPoint(point: ForecastPoint): android.graphics.PointF {
            val dayFromStart = (
                    point.dateMillis - firstDate
                    ) / 86_400_000.0

            val x = chartLeft + (
                    dayFromStart / totalDays * chartWidth
                    ).toFloat()

            val y = chartBottom - (
                    ((point.weightKg - minWeight) / weightRange) * chartHeight
                    ).toFloat()

            return android.graphics.PointF(x, y)
        }

        val historyPoints = historyEntries.map { entry ->
            chartPoint(
                ForecastPoint(
                    dateMillis = entry.dateMillis,
                    weightKg = entry.weightKg
                )
            )
        }

        if (historyPoints.size >= 2) {
            val historyPath = AndroidPath().apply {
                moveTo(
                    historyPoints.first().x,
                    historyPoints.first().y
                )

                historyPoints.drop(1).forEach { point ->
                    lineTo(point.x, point.y)
                }
            }

            canvas.drawPath(historyPath, historyPaint)
        }

        val forecastPoints = trend.forecastPoints.map(::chartPoint)

        val forecastPath = AndroidPath().apply {
            moveTo(
                historyPoints.last().x,
                historyPoints.last().y
            )

            forecastPoints.forEach { point ->
                lineTo(point.x, point.y)
            }
        }

        canvas.drawPath(forecastPath, forecastPaint)

        val latestHistoryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(25, 118, 108)
            style = Paint.Style.FILL
        }

        val lastHistoryPoint = historyPoints.last()

        canvas.drawCircle(
            lastHistoryPoint.x,
            lastHistoryPoint.y,
            11f,
            latestHistoryPaint
        )

        val finalForecastPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(104, 168, 131)
            style = Paint.Style.FILL
        }

        val lastForecastPoint = forecastPoints.last()

        canvas.drawCircle(
            lastForecastPoint.x,
            lastForecastPoint.y,
            11f,
            finalForecastPaint
        )

        // Final forecast summary
        drawCard(70f, 1150f, 1010f, 1290f)

        mutedTextPaint.textSize = 19f
        canvas.drawText(
            "IN 3 MONTHS",
            105f,
            1200f,
            mutedTextPaint
        )

        darkTextPaint.textSize = 43f
        canvas.drawText(
            "${formatWeight(trend.projectedWeightKg)} kg",
            105f,
            1255f,
            darkTextPaint
        )

        mutedTextPaint.textSize = 20f
        canvas.drawText(
            "Expected on ${formatEntryDate(finalForecastDate)}",
            380f,
            1255f,
            mutedTextPaint
        )

        saveTrendBitmap(context, bitmap)
    } catch (exception: Exception) {
        null
    }
}

fun saveTrendBitmap(
    context: Context,
    bitmap: Bitmap
): Uri? {
    val fileName = "FatApp_Trend_${System.currentTimeMillis()}.png"

    val values = ContentValues().apply {
        put(
            MediaStore.Images.Media.DISPLAY_NAME,
            fileName
        )
        put(
            MediaStore.Images.Media.MIME_TYPE,
            "image/png"
        )
        put(
            MediaStore.Images.Media.RELATIVE_PATH,
            "Pictures/FatApp"
        )
    }

    val uri = context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        values
    ) ?: return null

    val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)

    outputStream?.use { stream ->
        bitmap.compress(
            Bitmap.CompressFormat.PNG,
            100,
            stream
        )
    }

    return uri
}

fun shareImage(
    context: Context,
    imageUri: Uri
) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, imageUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(
            shareIntent,
            "Share trend image"
        )
    )
}