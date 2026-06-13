package com.optirout

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.optirout.ui.navigation.OptiRoutNavGraph

@Composable
fun OptiRoutApp() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        val navController = rememberNavController()
        OptiRoutNavGraph(navController = navController)
    }
}
