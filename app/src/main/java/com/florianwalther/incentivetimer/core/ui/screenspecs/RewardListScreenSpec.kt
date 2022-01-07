package com.florianwalther.incentivetimer.core.ui.screenspecs

import androidx.compose.material.SnackbarResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.core.util.exhaustive
import com.florianwalther.incentivetimer.features.rewards.addeditreward.ADD_EDIT_REWARD_RESULT
import com.florianwalther.incentivetimer.features.rewards.addeditreward.RESULT_REWARD_ADDED
import com.florianwalther.incentivetimer.features.rewards.addeditreward.RESULT_REWARD_DELETE
import com.florianwalther.incentivetimer.features.rewards.addeditreward.RESULT_REWARD_UPDATED
import com.florianwalther.incentivetimer.features.rewards.rewardlist.RewardListScreenAppBar
import com.florianwalther.incentivetimer.features.rewards.rewardlist.RewardListScreenContent
import com.florianwalther.incentivetimer.features.rewards.rewardlist.RewardListViewModel
import kotlinx.coroutines.flow.collectLatest

object RewardListScreenSpec : BottomNavScreenSpec {

    override val icon: ImageVector = Icons.Outlined.Star

    override val label: Int = R.string.rewards

    override val navHostRoute: String = "reward_list"

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink {
            uriPattern = "https://www.incentivetimer.com/reward_list"
        }
    )

    @Composable
    override fun TopBar(navController: NavController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: RewardListViewModel = hiltViewModel(navBackStackEntry)
        val multiSelectionModeActive by viewModel.multiSelectionModeActive.observeAsState(false)
        val selectedItemCount by viewModel.selectedItemCount.observeAsState(0)

        RewardListScreenAppBar(
            multiSelectionModeActive = multiSelectionModeActive,
            selectedItemCount = selectedItemCount,
            actions = viewModel,
        )
    }

    @Composable
    override fun Content(navController: NavController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: RewardListViewModel = hiltViewModel(navBackStackEntry)
        val rewards by viewModel.rewards.observeAsState(listOf())
        val selectedRewards by viewModel.selectedRewards.observeAsState(listOf())
        val showDeleteAllUnlockedRewardsDialog
                by viewModel.showDeleteAllUnlockedRewardsDialog.observeAsState(false)
        val showDeleteAllSelectedRewardsDialog
                by viewModel.showDeleteAllSelectedRewardsDialog.observeAsState(false)

        val scaffoldState = rememberScaffoldState()

        val context = LocalContext.current

        LaunchedEffect(Unit) {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is RewardListViewModel.RewardListEvent.ShowUndoRewardSnackbar -> {
                        val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                            message = context.getString(R.string.reward_deleted),
                            actionLabel = context.getString(R.string.undo),
                        )
                        if (snackbarResult == SnackbarResult.ActionPerformed) {
                            viewModel.onUndoDeleteRewardConfirmed(event.reward)
                        }
                        Unit
                    }
                    is RewardListViewModel.RewardListEvent.NavigateToEditRewardScreen -> {
                        navController.navigate(AddEditRewardScreenSpec.buildRoute(event.reward.id))
                    }
                }.exhaustive
            }
        }

        val addEditRewardResult = navController.currentBackStackEntry
            ?.savedStateHandle?.getLiveData<String>(ADD_EDIT_REWARD_RESULT)?.observeAsState()

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
            showDeleteAllUnlockedRewardsDialog = showDeleteAllUnlockedRewardsDialog,
            showDeleteAllSelectedRewardsDialog = showDeleteAllSelectedRewardsDialog,
            selectedRewards = selectedRewards,
            onAddNewRewardClicked = {
                navController.navigate(AddEditRewardScreenSpec.buildRoute())
            },
            scaffoldState = scaffoldState,
            actions = viewModel,
        )
    }
}