package com.florianwalther.incentivetimer.core.ui.screenspecs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.features.settings.SettingsScreenAppBar
import com.florianwalther.incentivetimer.features.settings.SettingsScreenContent
import com.florianwalther.incentivetimer.features.settings.SettingsViewModel
import com.florianwalther.incentivetimer.features.settings.model.SettingsScreenState

object SettingsScreenSpec : BottomNavScreenSpec {

    override val navHostRoute: String = "settings"

    override val icon: ImageVector = Icons.Outlined.Settings

    override val label: Int = R.string.settings

    @Composable
    override fun TopBar(navController: NavController, navBackStackEntry: NavBackStackEntry) {
        SettingsScreenAppBar()
    }

    @Composable
    override fun Content(navController: NavController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: SettingsViewModel = hiltViewModel(navBackStackEntry)
        val screenState by viewModel.screenState.observeAsState(SettingsScreenState.initialState)
        SettingsScreenContent(
            actions = viewModel,
            screenState = screenState,
        )
    }
}