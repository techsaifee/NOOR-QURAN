package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QuranReaderScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.NoorQuranTheme
import com.example.ui.viewmodel.QuranViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val quranViewModel: QuranViewModel = viewModel()

            // Observe settings to dynamically rebuild the Theme and colors!
            val themeMode by quranViewModel.themeMode.collectAsState()
            val accentColor by quranViewModel.accentColor.collectAsState()

            // Coroutine background timer to track active reading duration (Daily Reading Time)
            val coroutineScope = rememberCoroutineScope()
            DisposableEffect(Unit) {
                var isRunning = true
                val job = coroutineScope.launch {
                    while (isRunning) {
                        delay(5000) // update every 5 seconds to minimize DB writes
                        quranViewModel.addReadingTime(5)
                    }
                }
                onDispose {
                    isRunning = false
                    job.cancel()
                }
            }

            NoorQuranTheme(
                themeMode = themeMode,
                accentColor = accentColor
            ) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("home") {
                        HomeScreen(
                            viewModel = quranViewModel,
                            onNavigateToReader = { id, name, count ->
                                navController.navigate("reader/$id/$name/$count")
                            },
                            onNavigateToSettings = {
                                navController.navigate("settings")
                            }
                        )
                    }

                    composable(
                        route = "reader/{chapterId}/{chapterName}/{versesCount}",
                        arguments = listOf(
                            navArgument("chapterId") { type = NavType.IntType },
                            navArgument("chapterName") { type = NavType.StringType },
                            navArgument("versesCount") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val chapterId = backStackEntry.arguments?.getInt("chapterId") ?: 1
                        val chapterName = backStackEntry.arguments?.getString("chapterName") ?: "Al-Fatihah"
                        val versesCount = backStackEntry.arguments?.getInt("versesCount") ?: 7

                        QuranReaderScreen(
                            viewModel = quranViewModel,
                            chapterId = chapterId,
                            chapterName = chapterName,
                            versesCount = versesCount,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            viewModel = quranViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
