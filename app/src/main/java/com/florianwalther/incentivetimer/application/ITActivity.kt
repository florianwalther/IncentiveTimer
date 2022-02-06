package com.florianwalther.incentivetimer.application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.florianwalther.incentivetimer.core.ui.composables.AppInstructionsDialog
import com.florianwalther.incentivetimer.core.ui.screenspecs.*
import com.florianwalther.incentivetimer.core.ui.theme.IncentiveTimerTheme
import com.florianwalther.incentivetimer.data.datastore.ThemeSelection
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ITActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val activityViewModel: ITActivityViewModel = hiltViewModel()
            val appPreferences by activityViewModel.appPreferences.observeAsState()

            appPreferences?.let { appPreferences ->
                val showAppInstructionsDialog = !appPreferences.appInstructionsDialogShown
                val darkTheme = when (appPreferences.selectedTheme) {
                    ThemeSelection.SYSTEM -> isSystemInDarkTheme()
                    ThemeSelection.LIGHT -> false
                    ThemeSelection.DARK -> true
                }
                IncentiveTimerTheme(
                    darkTheme = darkTheme
                ) {
                    ScreenContent(
                        showAppInstructionsDialog = showAppInstructionsDialog,
                        onAppInstructionsDialogDismissed = activityViewModel::onAppInstructionsDialogDismissed
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenContent(
    showAppInstructionsDialog: Boolean,
    onAppInstructionsDialogDismissed: () -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val screenSpec = ScreenSpec.allScreens[currentDestination?.route]

    Scaffold(
        topBar = {
            val navBackStackEntry = navBackStackEntry
            if (navBackStackEntry != null) {
                screenSpec?.TopBar(navController, navBackStackEntry)
            }
        },
        bottomBar = {
            val hideBottomBar = navBackStackEntry?.arguments?.getBoolean(ARG_HIDE_BOTTOM_BAR)

            if (hideBottomBar == null || !hideBottomBar) {
                BottomNavigation {
                    BottomNavScreenSpec.screens.forEach { bottomNavDestination ->
                        BottomNavigationItem(
                            icon = {
                                Icon(bottomNavDestination.icon, contentDescription = null)
                            },
                            label = {
                                Text(stringResource(bottomNavDestination.label))
                            },
                            alwaysShowLabel = false,
                            selected = currentDestination?.hierarchy?.any { it.route == bottomNavDestination.navHostRoute } == true,
                            onClick = {
                                navController.navigate(bottomNavDestination.navHostRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavScreenSpec.screens[0].navHostRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            ScreenSpec.allScreens.values.forEach { screen ->
                composable(
                    route = screen.navHostRoute,
                    arguments = screen.arguments,
                    deepLinks = screen.deepLinks,
                ) { navBackStackEntry ->
                    screen.Content(
                        navController = navController,
                        navBackStackEntry = navBackStackEntry
                    )
                }
            }
        }
    }

    if (showAppInstructionsDialog) {
        AppInstructionsDialog(onDismissRequest = onAppInstructionsDialogDismissed)
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenContentPreview() {
    IncentiveTimerTheme {
        ScreenContent(
            showAppInstructionsDialog = false,
            onAppInstructionsDialogDismissed = {}
        )
    }
}

const val ARG_HIDE_BOTTOM_BAR = "ARG_HIDE_BOTTOM_BAR"