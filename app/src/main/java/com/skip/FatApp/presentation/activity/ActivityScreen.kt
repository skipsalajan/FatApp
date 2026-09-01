package com.skip.FatApp.presentation.activity

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.SportsTennis
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skip.FatApp.data.ActivityType
import com.skip.FatApp.domain.ActivityCalories
import com.skip.FatApp.presentation.ActivityViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant

private val ActivityBackground = Color(0xFFF5F8F7)
private val DarkText = Color(0xFF19312F)
private val PrimaryTeal = Color(0xFF19766C)
private val AccentTeal = Color(0xFFDCEEE9)
private val BorderColor = Color(0xFFDCE7E4)
private val MutedText = Color(0xFF6B7D7A)
private val TipBackground = Color(0xFFE9F3EF)

@Composable
fun ActivityScreen(
    stepTrackingEnabled: Boolean,
    viewModel: ActivityViewModel,
    entries: List<com.skip.FatApp.data.ActivityEntry>,
    onBackToMain: () -> Unit
) {
    var selectedView by remember { mutableStateOf("Log") }

    if (selectedView == "Log") {
        ActivityLogScreen(
            stepTrackingEnabled = stepTrackingEnabled,
            viewModel = viewModel,
            onShowHistory = {
                selectedView = "History"
            }
        )
    } else {
        ActivityHistoryScreen(
            entries = entries,
            onShowLog = {
                selectedView = "Log"
            },
            onDeleteEntry = viewModel::deleteActivity
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogScreen(
    stepTrackingEnabled: Boolean,
    viewModel: ActivityViewModel,
    onShowHistory: () -> Unit
) {
    var selectedType by remember {
        mutableStateOf(ActivityType.WALKING)
    }

    var stepsText by remember {
        mutableStateOf("")
    }

    var tennisMinutesText by remember {
        mutableStateOf("")
    }

    var otherNameText by remember {
        mutableStateOf("")
    }

    var otherMinutesText by remember {
        mutableStateOf("")
    }

    var otherCaloriesText by remember {
        mutableStateOf("")
    }

    var todaySteps by remember {
        mutableStateOf(0)
    }

    val today = LocalDate.now()

    var selectedDate by remember {
        mutableStateOf(today)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    val readableDate = selectedDate.format(
        DateTimeFormatter.ofPattern(
            "d MMM yyyy",
            Locale.getDefault()
        )
    )

    val selectedDateMillis = selectedDate
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    val walkingSteps = stepsText
        .replace(",", "")
        .replace(" ", "")
        .toIntOrNull()

    val walkingCalories = if (walkingSteps != null && walkingSteps > 0) {
        ActivityCalories.walkingCalories(walkingSteps)
    } else {
        null
    }

    val tennisMinutes = tennisMinutesText.toIntOrNull()

    val tennisCalories = if (tennisMinutes != null && tennisMinutes > 0) {
        ActivityCalories.tennisCalories(tennisMinutes)
    } else {
        null
    }

    if (showDatePicker) {
        ActivityDatePickerDialog(
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

    val context = LocalContext.current

    if (stepTrackingEnabled) {
        StepSensorEffect(
            context = context,
            onStepCount = { count ->
                todaySteps = com.skip.FatApp.data.DailyStepsCalculator.getTodaySteps(
                    context,
                    count.toLong()
                )
                com.skip.FatApp.data.AutoWalkingLogger.updateYesterdayMax(
                    context,
                    todaySteps
                )
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ActivityBackground)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 18.dp)
            .padding(bottom = 120.dp)
    ) {
        ActivityFeatureHeader()

        StepTrackerCard(
            modifier = Modifier.padding(top = 18.dp),
            todaySteps = todaySteps,
            label = "Live steps today"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                ActivityEyebrow("Move more")

                Text(
                    text = "Log activity",
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
                ActivityIconBox(
                    icon = Icons.Outlined.CalendarMonth,
                    size = 46.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        ActivitySegmentedControl(
            selectedView = "Log",
            onLogClick = { },
            onHistoryClick = onShowHistory
        )

        Spacer(modifier = Modifier.height(18.dp))

        ActivityTypeCard(
            selectedType = selectedType,
            onSelect = { selectedType = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedType) {
            ActivityType.WALKING -> {
                WalkingDetailsCard(
                    stepsText = stepsText,
                    onStepsChange = {
                        stepsText = it.filter { character ->
                            character.isDigit() ||
                                    character == ',' ||
                                    character == ' '
                        }
                    },
                    calories = walkingCalories,
                    dateText = activitySelectedDateText(
                        selectedDate = selectedDate,
                        today = today,
                        readableDate = readableDate
                    ),
                    onDateClick = {
                        showDatePicker = true
                    },
                    onSave = {
                        viewModel.saveWalking(
                            stepsText = stepsText,
                            dateMillis = selectedDateMillis
                        )
                        stepsText = ""
                    }
                )
            }

            ActivityType.TENNIS -> {
                TennisDetailsCard(
                    minutesText = tennisMinutesText,
                    onMinutesChange = {
                        tennisMinutesText = it.filter { character ->
                            character.isDigit()
                        }
                    },
                    calories = tennisCalories,
                    dateText = activitySelectedDateText(
                        selectedDate = selectedDate,
                        today = today,
                        readableDate = readableDate
                    ),
                    onDateClick = {
                        showDatePicker = true
                    },
                    onSave = {
                        val minutes = tennisMinutesText.toIntOrNull() ?: 0

                        viewModel.saveTennis(
                            durationMinutes = minutes,
                            dateMillis = selectedDateMillis
                        )
                        tennisMinutesText = ""
                    }
                )
            }

            ActivityType.OTHER -> {
                OtherActivityDetailsCard(
                    nameText = otherNameText,
                    onNameChange = { otherNameText = it },
                    minutesText = otherMinutesText,
                    onMinutesChange = {
                        otherMinutesText = it.filter { character ->
                            character.isDigit()
                        }
                    },
                    caloriesText = otherCaloriesText,
                    onCaloriesChange = {
                        otherCaloriesText = it.filter { character ->
                            character.isDigit()
                        }
                    },
                    dateText = activitySelectedDateText(
                        selectedDate = selectedDate,
                        today = today,
                        readableDate = readableDate
                    ),
                    onDateClick = {
                        showDatePicker = true
                    },
                    onSave = {
                        val minutes = otherMinutesText.toIntOrNull() ?: 0

                        viewModel.saveOther(
                            activityName = otherNameText,
                            durationMinutes = minutes,
                            caloriesText = otherCaloriesText,
                            dateMillis = selectedDateMillis
                        )

                        otherNameText = ""
                        otherMinutesText = ""
                        otherCaloriesText = ""
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ActivityTipCard()
    }
}

@Composable
fun ActivityFeatureHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActivityIconBox(
            icon = Icons.Outlined.FitnessCenter,
            size = 44.dp
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            ActivityEyebrow("Personal health")

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
                .size(38.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFD5E7E2)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "F",
                color = PrimaryTeal,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ActivitySegmentedControl(
    selectedView: String,
    onLogClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEDF3F1))
            .padding(4.dp)
    ) {
        ActivitySegmentButton(
            text = "Log activity",
            selected = selectedView == "Log",
            onClick = onLogClick,
            modifier = Modifier.weight(1f)
        )

        ActivitySegmentButton(
            text = "Activity history",
            selected = selectedView == "History",
            onClick = onHistoryClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActivitySegmentButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) PrimaryTeal else MutedText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ActivityTypeCard(
    selectedType: ActivityType,
    onSelect: (ActivityType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            BorderColor
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            ActivityEyebrow("Activity type")

            Spacer(modifier = Modifier.height(18.dp))

            ActivityTypeRow(
                title = "Walking",
                subtitle = "Steps & calorie estimate",
                icon = Icons.Outlined.DirectionsWalk,
                selected = selectedType == ActivityType.WALKING,
                onClick = {
                    onSelect(ActivityType.WALKING)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            ActivityTypeRow(
                title = "Tennis",
                subtitle = "Duration & calorie estimate",
                icon = Icons.Outlined.SportsTennis,
                selected = selectedType == ActivityType.TENNIS,
                onClick = {
                    onSelect(ActivityType.TENNIS)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            ActivityTypeRow(
                title = "Other activity",
                subtitle = "Duration & manual calories",
                icon = Icons.Outlined.FitnessCenter,
                selected = selectedType == ActivityType.OTHER,
                onClick = {
                    onSelect(ActivityType.OTHER)
                }
            )
        }
    }
}

@Composable
fun ActivityTypeRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) AccentTeal else Color(0xFFEDF3F1)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PrimaryTeal,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                color = DarkText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = MutedText,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "›",
            color = MutedText,
            fontSize = 28.sp
        )
    }
}

@Composable
fun WalkingDetailsCard(
    stepsText: String,
    onStepsChange: (String) -> Unit,
    calories: Int?,
    dateText: String,
    onDateClick: () -> Unit,
    onSave: () -> Unit
) {
    ActivityDetailsCard(
        title = "Walking details",
        dateText = dateText,
        onDateClick = onDateClick,
        saveEnabled = calories != null,
        onSave = onSave
    ) {
        Text(
            text = "Steps",
            color = MutedText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = stepsText,
            onValueChange = onStepsChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    text = "0",
                    color = Color(0xFF9AABA8)
                )
            },
            textStyle = TextStyle(
                color = DarkText,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-2).sp
            ),
            trailingIcon = {
                Text(
                    text = "steps",
                    color = MutedText,
                    fontWeight = FontWeight.Bold
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BorderColor,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Estimated calories",
            color = MutedText,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (calories == null) "—" else "$calories kcal",
            color = DarkText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Calculated at 0.06546 kcal per step.",
            color = MutedText,
            fontSize = 12.sp
        )
    }
}

@Composable
fun TennisDetailsCard(
    minutesText: String,
    onMinutesChange: (String) -> Unit,
    calories: Int?,
    dateText: String,
    onDateClick: () -> Unit,
    onSave: () -> Unit
) {
    ActivityDetailsCard(
        title = "Tennis details",
        dateText = dateText,
        onDateClick = onDateClick,
        saveEnabled = calories != null,
        onSave = onSave
    ) {
        Text(
            text = "Duration",
            color = MutedText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = minutesText,
            onValueChange = onMinutesChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    text = "0",
                    color = Color(0xFF9AABA8)
                )
            },
            textStyle = TextStyle(
                color = DarkText,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-2).sp
            ),
            trailingIcon = {
                Text(
                    text = "minutes",
                    color = MutedText,
                    fontWeight = FontWeight.Bold
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BorderColor,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Estimated calories",
            color = MutedText,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (calories == null) "—" else "$calories kcal",
            color = DarkText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Calculated at 380 kcal per hour.",
            color = MutedText,
            fontSize = 12.sp
        )
    }
}

@Composable
fun OtherActivityDetailsCard(
    nameText: String,
    onNameChange: (String) -> Unit,
    minutesText: String,
    onMinutesChange: (String) -> Unit,
    caloriesText: String,
    onCaloriesChange: (String) -> Unit,
    dateText: String,
    onDateClick: () -> Unit,
    onSave: () -> Unit
) {
    val isReady = nameText.isNotBlank() &&
            (minutesText.toIntOrNull() ?: 0) > 0 &&
            (caloriesText.toIntOrNull() ?: 0) > 0

    ActivityDetailsCard(
        title = "Other activity details",
        dateText = dateText,
        onDateClick = onDateClick,
        saveEnabled = isReady,
        onSave = onSave
    ) {
        Text(
            text = "Activity name",
            color = MutedText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = nameText,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    text = "Example: Gym workout",
                    color = Color(0xFF9AABA8)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BorderColor,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = minutesText,
                onValueChange = onMinutesChange,
                modifier = Modifier.weight(1f),
                label = {
                    Text("Minutes")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BorderColor,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            OutlinedTextField(
                value = caloriesText,
                onValueChange = onCaloriesChange,
                modifier = Modifier.weight(1f),
                label = {
                    Text("Calories")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BorderColor,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun ActivityDetailsCard(
    title: String,
    dateText: String,
    onDateClick: () -> Unit,
    saveEnabled: Boolean,
    onSave: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            BorderColor
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            ActivityEyebrow(title)

            Spacer(modifier = Modifier.height(22.dp))

            content()

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onDateClick)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "Activity date",
                    tint = PrimaryTeal,
                    modifier = Modifier.size(17.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = dateText,
                    color = MutedText,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = onSave,
                enabled = saveEnabled,
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
                    text = "Save activity",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ActivityTipCard() {
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
            modifier = Modifier.size(21.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "Every activity adds up.",
                color = DarkText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "Tracking your movement helps you see the bigger picture.",
                color = MutedText,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ActivityHistoryPlaceholderScreen(
    onShowLog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ActivityBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(bottom = 120.dp)
    ) {
        ActivityFeatureHeader()

        Spacer(modifier = Modifier.height(30.dp))

        ActivityEyebrow("Your movement")

        Text(
            text = "Activity",
            color = DarkText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1.5).sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        ActivitySegmentedControl(
            selectedView = "History",
            onLogClick = onShowLog,
            onHistoryClick = { }
        )

        Spacer(modifier = Modifier.height(30.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                BorderColor
            )
        ) {
            Column(
                modifier = Modifier.padding(22.dp)
            ) {
                ActivityEyebrow("Coming next")

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Activity history",
                    color = DarkText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = "Your activity list, delete actions, and day/week/month graph will be added in the next stage.",
                    color = MutedText,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ActivityIconBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    size: androidx.compose.ui.unit.Dp
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
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
fun ActivityEyebrow(text: String) {
    Text(
        text = text.uppercase(),
        color = MutedText,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.2.sp
    )
}

fun activitySelectedDateText(
    selectedDate: LocalDate,
    today: LocalDate,
    readableDate: String
): String {
    return if (selectedDate == today) {
        "Today, $readableDate"
    } else {
        readableDate
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedDateMillis = selectedDate
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis,
        yearRange = 2020..LocalDate.now().year
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis

                    if (selectedMillis != null) {
                        val newDate = Instant.ofEpochMilli(selectedMillis)
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
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = MutedText
                )
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = false
        )
    }
}