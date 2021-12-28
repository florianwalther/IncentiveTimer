package com.florianwalther.incentivetimer.core.ui.screenspecs

import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.features.rewards.addeditreward.ADD_EDIT_REWARD_RESULT
import com.florianwalther.incentivetimer.features.rewards.addeditreward.RESULT_REWARD_ADDED
import com.florianwalther.incentivetimer.features.rewards.addeditreward.RESULT_REWARD_DELETE
import com.florianwalther.incentivetimer.features.rewards.addeditreward.RESULT_REWARD_UPDATED
import com.florianwalther.incentivetimer.features.rewards.rewardlist.RewardListScreenAppBar
import com.florianwalther.incentivetimer.features.rewards.rewardlist.RewardListScreenContent
import com.florianwalther.incentivetimer.features.rewards.rewardlist.RewardListViewModel

object RewardListScreenSpec : ScreenSpec {
    override val navHostRoute: String = "rewardList"

    @Composable
    override fun TopBar(navController: NavController, navBackStackEntry: NavBackStackEntry) {
        RewardListScreenAppBar()
    }

    @Composable
    override fun Content(navController: NavController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: RewardListViewModel = hiltViewModel(navBackStackEntry)
        val rewards by viewModel.rewards.observeAsState(listOf())

        val addEditRewardResult = navController.currentBackStackEntry
            ?.savedStateHandle?.getLiveData<String>(ADD_EDIT_REWARD_RESULT)?.observeAsState()

        val scaffoldState = rememberScaffoldState()

        val context = LocalContext.current

        LaunchedEffect(key1 = addEditRewardResult) {
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>(
                ADD_EDIT_REWARD_RESULT
            )
            addEditRewardResult?.value?.let { addEditRewardResult ->
                when (addEditRewardResult) {
                    RESULT_REWARD_ADDED -> {
                        scaffoldState.snackbarHostState.showSnackbar(context.getString(R.string.reward_added))
                    }
                    RESULT_REWARD_UPDATED -> {
                        scaffoldState.snackbarHostState.showSnackbar(context.getString(R.string.reward_updated))
                    }
                    RESULT_REWARD_DELETE -> {
                        scaffoldState.snackbarHostState.showSnackbar(context.getString(R.string.reward_deleted))
                    }
                }
            }
        }

        RewardListScreenContent(
            rewards = rewards,
            onAddNewRewardClicked = {
                navController.navigate(AddEditRewardScreenSpec.buildRoute())
            },
            onRewardItemClicked = { id ->
                navController.navigate(AddEditRewardScreenSpec.buildRoute(id))
            },
            scaffoldState = scaffoldState
        )
    }
}