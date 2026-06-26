package com.workoutjournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.workoutjournal.ui.navigation.AppNavigation
import com.workoutjournal.ui.theme.WorkoutJournalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkoutJournalTheme {
                AppNavigation()
            }
        }
    }
}
