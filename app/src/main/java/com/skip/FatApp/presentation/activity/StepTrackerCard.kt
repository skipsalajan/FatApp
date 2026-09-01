package com.skip.FatApp.presentation.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat

private val DarkText = Color(0xFF19312F)
private val PrimaryTeal = Color(0xFF19766C)
private val AccentTeal = Color(0xFFDCEEE9)
private val BorderColor = Color(0xFFDCE7E4)
private val MutedText = Color(0xFF6B7D7A)

@Composable
fun StepTrackerCard(
    modifier: Modifier = Modifier,
    todaySteps: Int,
    label: String = "Live steps today"
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = BorderColor
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepTrackerIcon()

            Spacer(modifier = Modifier.width(13.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "TODAY'S STEPS",
                    color = MutedText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = NumberFormat.getIntegerInstance().format(todaySteps),
                    color = DarkText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = label,
                    color = MutedText,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun StepTrackerIcon() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(AccentTeal),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.DirectionsWalk,
            contentDescription = null,
            tint = PrimaryTeal,
            modifier = Modifier.size(22.dp)
        )
    }
}