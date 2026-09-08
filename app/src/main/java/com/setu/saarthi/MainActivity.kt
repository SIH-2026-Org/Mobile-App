package com.setu.saarthi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.setu.saarthi.ui.navigation.SaarthiNavGraph
import com.setu.saarthi.ui.theme.SaarthiSetuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SaarthiSetuTheme {
                SaarthiNavGraph()
            }
        }
    }
}
