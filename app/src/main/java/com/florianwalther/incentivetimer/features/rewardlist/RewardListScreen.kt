package com.florianwalther.incentivetimer.features.rewardlist

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.application.FullScreenDestinations
import com.florianwalther.incentivetimer.data.Reward
import com.florianwalther.incentivetimer.core.ui.IconKey
import com.florianwalther.incentivetimer.core.ui.ListBottomPadding
import com.florianwalther.incentivetimer.core.ui.theme.IncentiveTimerTheme
import com.florianwalther.incentivetimer.features.addeditreward.ADD_EDIT_REWARD_RESULT
import com.florianwalther.incentivetimer.features.addeditreward.ARG_REWARD_ID
import com.florianwalther.incentivetimer.features.addeditreward.RESULT_REWARD_ADDED
import com.florianwalther.incentivetimer.features.addeditreward.RESULT_REWARD_UPDATED
import kotlinx.coroutines.launch

@Composable
fun RewardListScreen(
    navController: NavController,
    viewModel: RewardListViewModel = hiltViewModel()
) {
    val rewards by viewModel.rewards.observeAsState(listOf())

    val addEditRewardResult = navController.currentBackStackEntry
        ?.savedStateHandle?.getLiveData<String>(ADD_EDIT_REWARD_RESULT)?.observeAsState()

    val scaffoldState = rememberScaffoldState()

    val context = LocalContext.current

    LaunchedEffect(key1 = addEditRewardResult) {
        navController.currentBackStackEntry?.savedStateHandle?.remove<String>(ADD_EDIT_REWARD_RESULT)
        addEditRewardResult?.value?.let { addEditRewardResult ->
            when (addEditRewardResult) {
                RESULT_REWARD_ADDED -> {
                    scaffoldState.snackbarHostState.showSnackbar(context.getString(R.string.reward_added))
                }
                RESULT_REWARD_UPDATED -> {
                    scaffoldState.snackbarHostState.showSnackbar(context.getString(R.string.reward_updated))
                }
            }
        }
    }

    ScreenContent(
        rewards = rewards,
        onAddNewRewardClicked = {
            navController.navigate(FullScreenDestinations.AddEditRewardScreen.route)
        },
        onRewardItemClicked = { id ->
            navController.navigate(FullScreenDestinations.AddEditRewardScreen.route + "?$ARG_REWARD_ID=$id")
        },
        scaffoldState = scaffoldState
    )
}

@Composable
private fun ScreenContent(
    rewards: List<Reward>,
    onRewardItemClicked: (Long) -> Unit,
    onAddNewRewardClicked: () -> Unit,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
) {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(stringResource(R.string.reward_list))
            })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewRewardClicked,
                modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_new_reward)
                )
            }
        },
        scaffoldState = scaffoldState,
    ) {
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        Box {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = 8.dp,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = ListBottomPadding
                ),
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(rewards) { reward ->
                    RewardItem(reward, onItemClicked = { id ->
                        onRewardItemClicked(id)
                    })
                }
            }
            AnimatedVisibility(
                visible = listState.firstVisibleItemIndex > 3,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    backgroundColor = Color.LightGray,
                    contentColor = Color.Black,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandLess,
                        contentDescription = stringResource(R.string.scroll_to_top),
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardItem(
    reward: Reward,
    onItemClicked: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = { onItemClicked(reward.id) },
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = reward.iconKey.rewardIcon,
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .size(64.dp)
                    .fillMaxWidth()
            )
            Column() {
                Text(
                    text = reward.name,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.chance) + ": ${reward.chanceInPercent}%",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(
    name = "Light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Preview(
    name = "Dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun RewardItemPreview() {
    IncentiveTimerTheme {
        Surface {
            RewardItem(Reward("Title", 5, IconKey.BATH_TUB), onItemClicked = {})
        }
    }
}

@Preview(
    name = "Light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Preview(
    name = "Dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun ScreenContentPreview() {
    IncentiveTimerTheme {
        Surface {
            ScreenContent(
                listOf(
                    Reward(name = "CAKE", 5, iconKey = IconKey.CAKE),
                    Reward(name = "BATH_TUB", 20, iconKey = IconKey.BATH_TUB),
                    Reward(name = "TV", 60, iconKey = IconKey.TV),
                ),
                onAddNewRewardClicked = {},
                onRewardItemClicked = {},
            )
        }
    }
}