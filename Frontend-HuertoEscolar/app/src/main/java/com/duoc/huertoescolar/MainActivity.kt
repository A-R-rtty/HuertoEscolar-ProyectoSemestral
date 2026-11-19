// MainActivity.kt
package com.duoc.huertoescolar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.huertoescolar.data.local.HuertoDatabase
import com.duoc.huertoescolar.data.repository.HuertoRepository
import com.duoc.huertoescolar.ui.navigation.AppNavigation
import com.duoc.huertoescolar.ui.theme.HuertoEscolarTheme
import com.duoc.huertoescolar.ui.viewmodel.HuertoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = HuertoDatabase.getDatabase(applicationContext)
        val repository = HuertoRepository(database)

        setContent {
            HuertoEscolarTheme {
                val huertoViewModel: HuertoViewModel = viewModel(
                    factory = HuertoViewModel.Factory(repository)
                )
                AppNavigation(viewModel = huertoViewModel)
            }
        }
    }
}