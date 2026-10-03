package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainAppScreen
import com.example.ui.theme.MedCareTheme
import com.example.viewmodel.MedCareViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedCareTheme {
                val medCareViewModel: MedCareViewModel = viewModel()
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainAppScreen(viewModel = medCareViewModel)
                }
            }
        }
    }
}
