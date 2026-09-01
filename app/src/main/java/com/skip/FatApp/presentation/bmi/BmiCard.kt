package com.skip.FatApp.presentation.bmi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skip.FatApp.domain.BmiCalculator
import com.skip.FatApp.domain.BmiCategory
import com.skip.FatApp.presentation.UserProfile
import com.skip.FatApp.presentation.UserProfileManager

private val DarkText = Color(0xFF19312F)
private val PrimaryTeal = Color(0xFF19766C)
private val AccentTeal = Color(0xFFDCEEE9)
private val BorderColor = Color(0xFFDCE7E4)
private val MutedText = Color(0xFF6B7D7A)

@Composable
fun BmiCard(
    latestWeightKg: Double?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profileManager = remember {
        UserProfileManager(context.applicationContext)
    }
    val profile = profileManager.loadProfile()

    val heightCm = profile.heightCm

    val bmiResult = if (
        latestWeightKg != null &&
        heightCm != null &&
        heightCm > 0
    ) {
        BmiCalculator.calculate(
            weightKg = latestWeightKg,
            heightCm = heightCm
        )
    } else {
        null
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            BorderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        when {
            latestWeightKg == null -> {
                EmptyBmiContent(
                    message = "Save a weight to calculate your BMI."
                )
            }

            heightCm == null || heightCm <= 0 -> {
                EmptyBmiContent(
                    message = "Add your height in Settings to calculate BMI."
                )
            }

            bmiResult != null -> {
                BmiContent(
                    bmi = bmiResult.value,
                    category = bmiResult.category,
                    profile = profile
                )
            }
        }
    }
}

@Composable
private fun EmptyBmiContent(
    message: String
) {
    Row(
        modifier = Modifier.padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BmiIcon()

        Spacer(modifier = Modifier.width(13.dp))

        Column {
            BmiEyebrow("Your body metric")

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Body mass index",
                color = DarkText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = message,
                color = MutedText,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun BmiContent(
    bmi: Double,
    category: BmiCategory,
    profile: UserProfile
) {
    val heightText = "${profile.heightCm?.toInt()} cm"

    Column(
        modifier = Modifier.padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            BmiIcon()

            Spacer(modifier = Modifier.width(13.dp))

            Column {
                BmiEyebrow("Your body metric")

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Body mass index",
                    color = DarkText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Based on your latest weight and height of $heightText",
                    color = MutedText,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = BmiCalculator.format(bmi),
                color = DarkText,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-2).sp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = category.label,
                color = bmiCategoryColor(category),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 7.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        BmiSpectrumBar(bmi = bmi)

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Healthy range",
                color = MutedText,
                fontSize = 12.sp
            )

            Text(
                text = "18.5 – 24.9",
                color = DarkText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (profile.name.isNotBlank()) {
                "Profile: ${profile.name}, $heightText"
            } else {
                "Profile: $heightText"
            },
            color = MutedText,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun BmiIcon() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(AccentTeal),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.MonitorHeart,
            contentDescription = null,
            tint = PrimaryTeal,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun BmiSpectrumBar(bmi: Double) {
    val markerFraction = bmiMarkerFraction(bmi)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
    ) {
        val markerSize = 18.dp
        val usableWidth = maxWidth - markerSize
        val markerOffset = usableWidth * markerFraction

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .height(14.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Box(
                modifier = Modifier
                    .weight(18.5f)
                    .height(14.dp)
                    .background(Color(0xFFE6D6B7))
            )

            Box(
                modifier = Modifier
                    .weight(6.5f)
                    .height(14.dp)
                    .background(Color(0xFFB4D6CE))
            )

            Box(
                modifier = Modifier
                    .weight(5f)
                    .height(14.dp)
                    .background(Color(0xFFE2C98C))
            )

            Box(
                modifier = Modifier
                    .weight(5f)
                    .height(14.dp)
                    .background(Color(0xFFE1B58B))
            )

            Box(
                modifier = Modifier
                    .weight(5f)
                    .height(14.dp)
                    .background(Color(0xFFD99183))
            )

            Box(
                modifier = Modifier
                    .weight(10f)
                    .height(14.dp)
                    .background(Color(0xFFC97B7B))
            )
        }

        Box(
            modifier = Modifier
                .offset(x = markerOffset)
                .align(Alignment.CenterStart)
                .size(markerSize)
                .clip(RoundedCornerShape(50))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(PrimaryTeal)
            )
        }
    }
}

private fun bmiMarkerFraction(bmi: Double): Float {
    val limitedBmi = bmi.coerceIn(10.0, 50.0)

    val underweightWidth = 18.5
    val healthyWidth = 6.5
    val overweightWidth = 5.0
    val obesityClassIWidth = 5.0
    val obesityClassIIWidth = 5.0
    val obesityClassIIIWidth = 10.0

    val totalWidth =
        underweightWidth +
                healthyWidth +
                overweightWidth +
                obesityClassIWidth +
                obesityClassIIWidth +
                obesityClassIIIWidth

    val position = when {
        limitedBmi < 18.5 -> {
            limitedBmi
        }

        limitedBmi < 25.0 -> {
            underweightWidth + (limitedBmi - 18.5)
        }

        limitedBmi < 30.0 -> {
            underweightWidth +
                    healthyWidth +
                    (limitedBmi - 25.0)
        }

        limitedBmi < 35.0 -> {
            underweightWidth +
                    healthyWidth +
                    overweightWidth +
                    (limitedBmi - 30.0)
        }

        limitedBmi < 40.0 -> {
            underweightWidth +
                    healthyWidth +
                    overweightWidth +
                    obesityClassIWidth +
                    (limitedBmi - 35.0)
        }

        else -> {
            underweightWidth +
                    healthyWidth +
                    overweightWidth +
                    obesityClassIWidth +
                    obesityClassIIWidth +
                    (limitedBmi - 40.0)
        }
    }

    return (position / totalWidth)
        .toFloat()
        .coerceIn(0f, 1f)
}

private fun bmiCategoryColor(
    category: BmiCategory
): Color {
    return when (category) {
        BmiCategory.UNDERWEIGHT -> Color(0xFF8A642D)
        BmiCategory.HEALTHY_WEIGHT -> PrimaryTeal
        BmiCategory.OVERWEIGHT -> Color(0xFF9B6E1E)
        BmiCategory.OBESITY_CLASS_I -> Color(0xFFB45D34)
        BmiCategory.OBESITY_CLASS_II -> Color(0xFFC44D43)
        BmiCategory.OBESITY_CLASS_III -> Color(0xFFA53838)
    }
}

@Composable
private fun BmiEyebrow(text: String) {
    Text(
        text = text.uppercase(),
        color = MutedText,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.2.sp
    )
}