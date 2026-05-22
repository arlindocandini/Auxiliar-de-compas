package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AppDatabase
import com.example.data.repository.ShoppingRepository
import com.example.ui.screens.ShoppingAssistantApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ShoppingViewModel
import com.example.ui.viewmodel.ShoppingViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = AppDatabase.getDatabase(applicationContext)
    val repository = ShoppingRepository(database.shoppingDao())

    setContent {
      MyApplicationTheme {
        val viewModel: ShoppingViewModel = viewModel(
          factory = ShoppingViewModelFactory(repository)
        )
        ShoppingAssistantApp(viewModel = viewModel)
      }
    }
  }
}
