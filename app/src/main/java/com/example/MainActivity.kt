package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.FarmerBillingApp
import com.example.ui.theme.FarmerBillingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.parseColor("#111827")),
            navigationBarStyle = SystemBarStyle.dark(Color.parseColor("#111827")),
        )
        setContent {
            FarmerBillingTheme {
                FarmerBillingApp()
            }
        }
    }
}
