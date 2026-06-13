package com.optirout.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.optirout.R
import com.optirout.viewmodel.RouteState
import com.optirout.viewmodel.RouteViewModel

@Composable
fun LoadingScreen(
    viewModel: RouteViewModel,
    onNavigateToMap: () -> Unit,
    onError: () -> Unit,
) {
    val routeState by viewModel.routeState.collectAsState()

    LaunchedEffect(routeState) {
        when (routeState) {
            is RouteState.Success -> onNavigateToMap()
            is RouteState.Error -> onError()
            else -> Unit
        }
    }

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.animation_loading)
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(200.dp),
            )
        }
    }
}
