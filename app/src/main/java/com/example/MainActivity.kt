package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.ProjectRepository
import com.example.ui.EditorScreen
import com.example.ui.EditorViewModel
import com.example.ui.EditorViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup local SQLite database elements
        val database = AppDatabase.getDatabase(this)
        val repository = ProjectRepository(database.projectDao())

        // Initialise modern editor state viewModel
        val viewModel: EditorViewModel by viewModels {
            EditorViewModelFactory(application, repository)
        }

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EditorScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
