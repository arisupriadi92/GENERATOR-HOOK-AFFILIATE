package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.HookRepository
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HookViewModel
import com.example.ui.viewmodel.HookViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize local Room Database
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = HookRepository(database.hookDao(), database.popularHookDao())
        
        // Instantiating HookViewModel via standard Factory pattern
        val viewModel = ViewModelProvider(
            this, 
            HookViewModelFactory(repository)
        )[HookViewModel::class.java]

        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                HomeScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
