package com.skip.FatApp.presentation.activity

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.SportsTennis
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skip.FatApp.data.ActivityEntry
import com.skip.FatApp.data.ActivityType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState

private val ActivityBackground = Color(0xFFF5F8F7)
private val DarkText = Color(0xFF19312F)
private val PrimaryTeal = Color(0xFF19766C)
private val AccentTeal = Color(0xFFDCEEE9)
private val BorderColor = Color(0xFFDCE7E4)
private val MutedText = Color(0xFF6B7D7A)
private val LightCard = Color(0xFFEDF3F1)
private val ChartTeal = Color(0xFF67A991)

enum class ActivityChartPeriod {
    DAY,
    WEEK,
    MONTH
}

@Composable
fun ActivityHistoryScreen(
    entries: List<ActivityEntry>,
    onShowLog: () -> Unit,
    onDeleteEntry: (ActivityEntry) -> Unit
) {
    var selectedPeriod by remember {
        mutableStateOf(ActivityChartPeriod.WEEK)
    }

    var periodAnchorDate by remember {
        mutableStateOf(LocalDate.now())
    }

    var entryToDelete by remember {
        mutableStateOf<ActivityEntry?>(null)
    }

    val summary = activitySummary(
        entries = entries,
        period = selectedPeriod,
        anchorDate = periodAnchorDate
    )

    if (entryToDelete != null) {
        DeleteActivityDialog(
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ActivityBackground),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 0.dp,
            bottom = 130.dp
        )
    ) {
        item {
            ActivityFeatureHeader()
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    ActivityEyebrow("Your movement")

                    Text(
                        text = "Activity",
                        color = DarkText,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.5).sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .clickable(onClick = onShowLog),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Log activity",
                        tint = PrimaryTeal,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }

        item {
            ActivitySegmentedControl(
                selectedView = "History",
                onLogClick = onShowLog,
                onHistoryClick = { }
            )
        }

        item {
            Spacer(modifier = Modifier.height(18.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActivityMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Total activity",
                    value = formatDuration(summary.totalMinutes),
                    unit = periodLabel(selectedPeriod)
                )

                ActivityMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Est. calories",
                    value = formatCalories(summary.totalCalories),
                    unit = periodLabel(selectedPeriod)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .clickable {
                            periodAnchorDate = previousPeriodDate(
                                periodAnchorDate,
                                selectedPeriod
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "‹",
                        color = PrimaryTeal,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = periodTitleText(
                        periodAnchorDate,
                        selectedPeriod
                    ),
                    color = DarkText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .clickable {
                            periodAnchorDate = nextPeriodDate(
                                periodAnchorDate,
                                selectedPeriod
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "›",
                        color = PrimaryTeal,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            ActivityChartCard(
                entries = entries,
                selectedPeriod = selectedPeriod,
                anchorDate = periodAnchorDate,
                onPeriodSelected = {
                    selectedPeriod = it
                    periodAnchorDate = LocalDate.now()
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent activity",
                    color = DarkText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "${entries.size} ${if (entries.size == 1) "log" else "logs"}",
                    color = MutedText,
                    fontSize = 12.sp
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (entries.isEmpty()) {
            item {
                EmptyActivityHistoryCard(onShowLog)
            }
        } else {
            items(
                items = entries,
                key = { entry -> entry.id }
            ) { entry ->
                ActivityHistoryRow(
                    entry = entry,
                    isLatest = entry.id == entries.first().id,
                    onDeleteClick = {
                        entryToDelete = entry
                    }
                )
            }
        }
    }
}

@Composable
fun ActivityMetricCard(
    modifier: Modifier,
    label: String,
    value: String,
    unit: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            BorderColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            ActivityEyebrow(label)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                color = DarkText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = unit,
                color = MutedText,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ActivityChartCard(
    entries: List<ActivityEntry>,
    selectedPeriod: ActivityChartPeriod,
    anchorDate: LocalDate,
    onPeriodSelected: (ActivityChartPeriod) -> Unit
) {
    val chartData = activityChartData(
        entries = entries,
        period = selectedPeriod,
        anchorDate = anchorDate
    )

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
            modifier = Modifier.padding(
                start = 18.dp,
                top = 20.dp,
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
                    ActivityEyebrow("Activity over time")

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = formatCalories(chartData.totalCalories),
                        color = DarkText,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.5).sp
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = periodLabel(selectedPeriod),
                        color = MutedText,
                        fontSize = 12.sp
                    )
                }

                ActivityPeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = onPeriodSelected
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            ActivityBarChart(
                labels = chartData.labels,
                values = chartData.values,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
fun ActivityPeriodSelector(
    selectedPeriod: ActivityChartPeriod,
    onPeriodSelected: (ActivityChartPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(LightCard)
            .padding(3.dp)
    ) {
        ActivityPeriodButton(
            text = "Day",
            selected = selectedPeriod == ActivityChartPeriod.DAY,
            onClick = {
                onPeriodSelected(ActivityChartPeriod.DAY)
            }
        )

        ActivityPeriodButton(
            text = "Week",
            selected = selectedPeriod == ActivityChartPeriod.WEEK,
            onClick = {
                onPeriodSelected(ActivityChartPeriod.WEEK)
            }
        )

        ActivityPeriodButton(
            text = "Month",
            selected = selectedPeriod == ActivityChartPeriod.MONTH,
            onClick = {
                onPeriodSelected(ActivityChartPeriod.MONTH)
            }
        )
    }
}

@Composable
fun ActivityPeriodButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(
                if (selected) AccentTeal else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) PrimaryTeal else MutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ActivityBarChart(
    labels: List<String>,
    values: List<Int>,
    modifier: Modifier = Modifier
) {
    val highestValue = values.maxOrNull()?.coerceAtLeast(100) ?: 100
    val chartMax = ((highestValue + 99) / 100) * 100

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 30.dp, end = 4.dp)
            ) {
                val chartWidth = size.width
                val chartHeight = size.height
                val barWidth = chartWidth / (values.size * 2f)
                val groupWidth = chartWidth / values.size

                for (index in 0..4) {
                    val y = chartHeight * index / 4f

                    drawLine(
                        color = Color(0xFFE8EFED),
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(chartWidth, y),
                        strokeWidth = 1f
                    )
                }

                values.forEachIndexed { index, value ->
                    if (value <= 0) return@forEachIndexed

                    val height = (value.toFloat() / chartMax) * chartHeight
                    val left = index * groupWidth +
                            (groupWidth - barWidth) / 2f
                    val top = chartHeight - height

                    drawRoundRect(
                        color = if (index == values.indexOf(values.maxOrNull())) {
                            PrimaryTeal
                        } else {
                            ChartTeal
                        },
                        topLeft = androidx.compose.ui.geometry.Offset(
                            left,
                            top
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            barWidth,
                            height
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                            12f,
                            12f
                        )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                for (index in 4 downTo 0) {
                    Text(
                        text = "${chartMax * index / 4}",
                        color = Color(0xFF92A19F),
                        fontSize = 9.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    color = MutedText,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun ActivityHistoryRow(
    entry: ActivityEntry,
    isLatest: Boolean,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActivityRowIcon(entry.type)

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = activityTitle(entry),
                color = DarkText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = activityDateText(entry.dateMillis),
                color = MutedText,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = activityDetailsText(entry),
                color = MutedText,
                fontSize = 12.sp
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${entry.estimatedCalories} kcal",
                color = PrimaryTeal,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            if (isLatest) {
                Text(
                    text = "Latest",
                    color = PrimaryTeal,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDeleteClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete activity",
                        tint = MutedText,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityRowIcon(type: ActivityType) {
    val icon = when (type) {
        ActivityType.WALKING -> Icons.Outlined.DirectionsWalk
        ActivityType.TENNIS -> Icons.Outlined.SportsTennis
        ActivityType.OTHER -> Icons.Outlined.FitnessCenter
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(AccentTeal),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryTeal,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun EmptyActivityHistoryCard(onShowLog: () -> Unit) {
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
            ActivityEyebrow("No activity yet")

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Start tracking movement",
                color = DarkText,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = "Log walking, tennis, or another activity to see your progress here.",
                color = MutedText,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onShowLog,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryTeal
                ),
                shape = RoundedCornerShape(13.dp)
            ) {
                Text("Log activity")
            }
        }
    }
}

@Composable
fun DeleteActivityDialog(
    entry: ActivityEntry,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete activity?",
                color = DarkText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Delete ${activityTitle(entry)} logged on ${activityDateText(entry.dateMillis)}? This cannot be undone.",
                color = MutedText
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmDelete) {
                Text(
                    text = "Delete",
                    color = Color(0xFFB3261E),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = PrimaryTeal
                )
            }
        }
    )
}

data class ActivitySummary(
    val totalMinutes: Int,
    val totalCalories: Int
)

data class ActivityChartData(
    val labels: List<String>,
    val values: List<Int>,
    val totalCalories: Int
)

fun activitySummary(
    entries: List<ActivityEntry>,
    period: ActivityChartPeriod,
    anchorDate: LocalDate = LocalDate.now()
): ActivitySummary {
    val includedEntries = entriesForPeriod(
        entries = entries,
        period = period,
        anchorDate = anchorDate
    )

    return ActivitySummary(
        totalMinutes = includedEntries.sumOf {
            it.durationMinutes ?: 0
        },
        totalCalories = includedEntries.sumOf {
            it.estimatedCalories
        }
    )
}

fun activityChartData(
    entries: List<ActivityEntry>,
    period: ActivityChartPeriod,
    anchorDate: LocalDate = LocalDate.now()
): ActivityChartData {
    return when (period) {
        ActivityChartPeriod.DAY -> {
            val hours = listOf(
                "00", "04", "08", "12", "16", "20"
            )

            val values = hours.mapIndexed { index, _ ->
                val startHour = index * 4
                val endHour = startHour + 4

                entries
                    .filter {
                        val dateTime = Instant.ofEpochMilli(it.dateMillis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()

                        dateTime.toLocalDate() == anchorDate &&
                                dateTime.hour in startHour until endHour
                    }
                    .sumOf { it.estimatedCalories }
            }

            ActivityChartData(
                labels = hours,
                values = values,
                totalCalories = values.sum()
            )
        }

        ActivityChartPeriod.WEEK -> {
            val startOfWeek = anchorDate.with(
                TemporalAdjusters.previousOrSame(
                    java.time.DayOfWeek.MONDAY
                )
            )

            val labels = listOf(
                "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
            )

            val values = (0..6).map { dayOffset ->
                val date = startOfWeek.plusDays(dayOffset.toLong())

                entries
                    .filter {
                        dateFromMillis(it.dateMillis) == date
                    }
                    .sumOf { it.estimatedCalories }
            }

            ActivityChartData(
                labels = labels,
                values = values,
                totalCalories = values.sum()
            )
        }

        ActivityChartPeriod.MONTH -> {
            val firstDay = anchorDate.withDayOfMonth(1)
            val labels = listOf(
                "W1", "W2", "W3", "W4", "W5"
            )

            val values = (0..4).map { weekIndex ->
                val weekStart = firstDay.plusDays(
                    (weekIndex * 7).toLong()
                )
                val weekEnd = weekStart.plusDays(6)

                entries
                    .filter {
                        val entryDate = dateFromMillis(it.dateMillis)

                        !entryDate.isBefore(weekStart) &&
                                !entryDate.isAfter(weekEnd) &&
                                entryDate.month == firstDay.month &&
                                entryDate.year == firstDay.year
                    }
                    .sumOf { it.estimatedCalories }
            }

            ActivityChartData(
                labels = labels,
                values = values,
                totalCalories = values.sum()
            )
        }
    }
}

fun entriesForPeriod(
    entries: List<ActivityEntry>,
    period: ActivityChartPeriod,
    anchorDate: LocalDate = LocalDate.now()
): List<ActivityEntry> {
    return when (period) {
        ActivityChartPeriod.DAY -> {
            entries.filter {
                dateFromMillis(it.dateMillis) == anchorDate
            }
        }

        ActivityChartPeriod.WEEK -> {
            val weekStart = anchorDate.with(
                TemporalAdjusters.previousOrSame(
                    java.time.DayOfWeek.MONDAY
                )
            )
            val weekEnd = weekStart.plusDays(6)

            entries.filter {
                val entryDate = dateFromMillis(it.dateMillis)

                !entryDate.isBefore(weekStart) &&
                        !entryDate.isAfter(weekEnd)
            }
        }

        ActivityChartPeriod.MONTH -> {
            val firstDay = anchorDate.withDayOfMonth(1)
            val lastDay = anchorDate.withDayOfMonth(1).plusMonths(1).minusDays(1)

            entries.filter {
                val entryDate = dateFromMillis(it.dateMillis)

                !entryDate.isBefore(firstDay) &&
                        !entryDate.isAfter(lastDay)
            }
        }
    }
}

fun dateFromMillis(dateMillis: Long): LocalDate {
    return Instant.ofEpochMilli(dateMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

fun activityTitle(entry: ActivityEntry): String {
    return when (entry.type) {
        ActivityType.WALKING -> "Walking"
        ActivityType.TENNIS -> "Tennis"
        ActivityType.OTHER -> {
            entry.activityName ?: "Other activity"
        }
    }
}

fun activityDetailsText(entry: ActivityEntry): String {
    return when (entry.type) {
        ActivityType.WALKING -> {
            "${entry.steps ?: 0} steps"
        }

        ActivityType.TENNIS -> {
            formatDuration(entry.durationMinutes ?: 0)
        }

        ActivityType.OTHER -> {
            formatDuration(entry.durationMinutes ?: 0)
        }
    }
}

fun activityDateText(dateMillis: Long): String {
    val date = dateFromMillis(dateMillis)
    val today = LocalDate.now()

    return if (date == today) {
        "Today, " + date.format(
            DateTimeFormatter.ofPattern(
                "d MMM yyyy",
                Locale.getDefault()
            )
        )
    } else {
        date.format(
            DateTimeFormatter.ofPattern(
                "d MMM yyyy",
                Locale.getDefault()
            )
        )
    }
}

fun formatDuration(minutes: Int): String {
    if (minutes <= 0) {
        return "—"
    }

    val hours = minutes / 60
    val remainingMinutes = minutes % 60

    return when {
        hours > 0 && remainingMinutes > 0 -> {
            "${hours}h ${remainingMinutes}m"
        }

        hours > 0 -> {
            "${hours}h"
        }

        else -> {
            "${remainingMinutes}m"
        }
    }
}

fun formatCalories(calories: Int): String {
    return String.format(
        Locale.getDefault(),
        "%,d kcal",
        calories
    )
}

fun periodLabel(period: ActivityChartPeriod): String {
    return when (period) {
        ActivityChartPeriod.DAY -> "Today"
        ActivityChartPeriod.WEEK -> "This week"
        ActivityChartPeriod.MONTH -> "This month"
    }
}
fun previousPeriodDate(
    anchor: LocalDate,
    period: ActivityChartPeriod
): LocalDate {
    return when (period) {
        ActivityChartPeriod.DAY -> anchor.minusDays(1)

        ActivityChartPeriod.WEEK ->
            anchor.with(
                java.time.temporal.TemporalAdjusters.previousOrSame(
                    java.time.DayOfWeek.MONDAY
                )
            ).minusWeeks(1)

        ActivityChartPeriod.MONTH ->
            anchor.withDayOfMonth(1).minusMonths(1)
    }
}

fun nextPeriodDate(
    anchor: LocalDate,
    period: ActivityChartPeriod
): LocalDate {
    return when (period) {
        ActivityChartPeriod.DAY -> anchor.plusDays(1)

        ActivityChartPeriod.WEEK ->
            anchor.with(
                java.time.temporal.TemporalAdjusters.previousOrSame(
                    java.time.DayOfWeek.MONDAY
                )
            ).plusWeeks(1)

        ActivityChartPeriod.MONTH ->
            anchor.withDayOfMonth(1).plusMonths(1)
    }
}

fun periodTitleText(
    anchor: LocalDate,
    period: ActivityChartPeriod,
    locale: Locale = Locale.getDefault()
): String {
    val formatter = when (period) {
        ActivityChartPeriod.DAY ->
            DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", locale)

        ActivityChartPeriod.WEEK ->
            DateTimeFormatter.ofPattern("'Week starting' d MMM yyyy", locale)

        ActivityChartPeriod.MONTH ->
            DateTimeFormatter.ofPattern("MMMM yyyy", locale)
    }

    val dateForTitle = when (period) {
        ActivityChartPeriod.DAY -> anchor

        ActivityChartPeriod.WEEK ->
            anchor.with(
                java.time.temporal.TemporalAdjusters.previousOrSame(
                    java.time.DayOfWeek.MONDAY
                )
            )

        ActivityChartPeriod.MONTH ->
            anchor.withDayOfMonth(1)
    }

    return dateForTitle.format(formatter)
}