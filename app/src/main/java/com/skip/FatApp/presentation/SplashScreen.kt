package com.skip.FatApp.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.skip.FatApp.R

@Composable
fun SplashScreen() {
    Image(
        painter = painterResource(id = R.drawable.splash_screen),
        contentDescription = "FatApp splash screen",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}