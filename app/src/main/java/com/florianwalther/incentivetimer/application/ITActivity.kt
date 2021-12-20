package com.florianwalther.incentivetimer.application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.addeditreward.AddEditRewardScreen
import com.florianwalther.incentivetimer.rewardlist.RewardListScreen
import com.florianwalther.incentivetimer.timer.TimerScreen
import com.florianwalther.incentivetimer.ui.theme.IncentiveTimerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IncentiveTimerTheme {
                ScreenContent()
            }
        }
    }
}

@Composable
private fun ScreenContent() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavDestinations.forEach { bottomNavDestination ->
                    BottomNavigationItem(
                        icon = {
                            Icon(
                                bottomNavDestination.icon,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(stringResource(bottomNavDestination.label))
                        },
                        alwaysShowLabel = false,
                        selected = currentDestination?.hierarchy?.any { it.route == bottomNavDestination.route } == true,
                        onClick = {
                            navController.navigate(bottomNavDestination.route) {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = bottomNavDestinations[0].route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(BottomNavDestinations.TimerScreen.route) {
                TimerScreen(navController)
            }
            composable(BottomNavDestinations.RewardListScreen.route) {
                RewardListScreen(navController)
            }
            composable(FullScreenDestinations.AddEditRewardScreen.route) {
                AddEditRewardScreen(navController)
            }
        }
    }
}


val bottomNavDestinations = listOf<BottomNavDestinations>(
    BottomNavDestinations.TimerScreen,
    BottomNavDestinations.RewardListScreen
)

sealed class BottomNavDestinations(
    val route: String,
    val icon: ImageVector,
    @StringRes val label: Int
) {
    object TimerScreen : BottomNavDestinations(route ="timer", Icons.Outlined.Timer, R.string.timer)
    object RewardListScreen :
        BottomNavDestinations(route ="reward_list", Icons.Outlined.List, R.string.reward_list)
}

sealed class FullScreenDestinations(
    val route: String,
) {
    object AddEditRewardScreen : FullScreenDestinations(route = "add_edit_reward")
}

@Preview(showBackground = true)
@Composable
fun ScreenContentPreview() {
    IncentiveTimerTheme {
        ScreenContent()
    }
}