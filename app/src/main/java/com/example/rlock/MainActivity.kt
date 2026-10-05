package com.example.rlock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rlock.data.InMemoryTemplateRepository
import com.example.rlock.ui.RLockApp
import com.example.rlock.ui.RLockViewModel
import com.example.rlock.ui.theme.RLockTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = InMemoryTemplateRepository()

        setContent {
            RLockTheme {
                val viewModel: RLockViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return RLockViewModel(repository) as T
                        }
                    }
                )

                RLockApp(viewModel = viewModel)
            }
        }
    }
}
