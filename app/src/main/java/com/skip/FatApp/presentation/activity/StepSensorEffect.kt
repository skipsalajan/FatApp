package com.skip.FatApp.presentation.activity

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.skip.FatApp.data.StepSensorHelper

@Composable
fun StepSensorEffect(
    context: Context,
    onStepCount: (Int) -> Unit
) {
    val helper = remember {
        StepSensorHelper(
            context = context,
            onStepCount = onStepCount
        )
    }

    LaunchedEffect(Unit) {
        helper.start()
    }

    DisposableEffect(Unit) {
        onDispose {
            helper.stop()
        }
    }
}