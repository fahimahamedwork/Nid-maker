package com.nidcard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nidcard.app.ui.navigation.AppNavigation
import com.nidcard.app.ui.theme.NIDCardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NIDCardTheme {
                AppNavigation()
            }
        }
    }
}
