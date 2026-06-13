package com.optirout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OptiRoutTheme(dynamicColor = false) {
                OptiRoutApp()
            }
        }
    }
}
