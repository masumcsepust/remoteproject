package com.harf.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.harf.ai.ui.LocalStrings
import com.harf.ai.ui.Strings
import com.harf.ai.ui.screens.HomeScreen
import com.harf.ai.ui.screens.HujurScreen
import com.harf.ai.ui.screens.LanguagePickerScreen
import com.harf.ai.ui.screens.MyReviewsScreen
import com.harf.ai.ui.screens.PracticeScreen
import com.harf.ai.ui.stringsFor
import com.harf.ai.ui.theme.HarfTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = (application as HarfApp).settings
        setContent {
            HarfTheme {
                val language by settings.language.collectAsStateWithLifecycle()
                val current = language
                if (current == null) {
                    LanguagePickerScreen(onPick = settings::setLanguage)
                } else {
                    CompositionLocalProvider(LocalStrings provides stringsFor(current)) {
                        HarfNavHost()
                    }
                }
            }
        }
    }
}

private enum class Tab(val route: String, val icon: ImageVector, val label: (Strings) -> String) {
    Learn("learn", Icons.AutoMirrored.Filled.MenuBook, { it.tabLearn }),
    Reviews("reviews", Icons.Filled.RateReview, { it.tabReviews }),
    Hujur("hujur", Icons.Filled.School, { it.tabHujur }),
}

@Composable
private fun HarfNavHost() {
    val s = LocalStrings.current
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route

    Scaffold(
        bottomBar = {
            if (Tab.entries.any { it.route == route }) {
                NavigationBar {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label(s)) },
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(nav, startDestination = Tab.Learn.route, modifier = Modifier.padding(padding)) {
            composable(Tab.Learn.route) {
                HomeScreen(onLetterClick = { nav.navigate("practice/${it.key}") })
            }
            composable(Tab.Reviews.route) { MyReviewsScreen() }
            composable(Tab.Hujur.route) { HujurScreen() }
            composable(
                "practice/{letterKey}",
                arguments = listOf(navArgument("letterKey") { type = NavType.StringType }),
            ) {
                PracticeScreen(
                    letterKey = it.arguments!!.getString("letterKey")!!,
                    onBack = { nav.popBackStack() },
                )
            }
        }
    }
}
