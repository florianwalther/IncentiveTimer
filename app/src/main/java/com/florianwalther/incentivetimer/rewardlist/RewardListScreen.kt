package com.florianwalther.incentivetimer.rewardlist

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.application.FullScreenDestinations
import com.florianwalther.incentivetimer.data.Reward
import com.florianwalther.incentivetimer.ui.IconKeys
import com.florianwalther.incentivetimer.ui.ListBottomPadding
import com.florianwalther.incentivetimer.ui.defaultIcon
import com.florianwalther.incentivetimer.ui.rewardIcons
import com.florianwalther.incentivetimer.ui.theme.IncentiveTimerTheme
import kotlinx.coroutines.launch

@Composable
fun RewardListScreen(
    navController: NavController,
    viewModel: RewardListViewModel = hiltViewModel()
) {
    val rewards by viewModel.rewards.observeAsState(listOf())
    ScreenContent(
        rewards = rewards,
        onAddNewRewardClicked = {
            navController.navigate(FullScreenDestinations.AddEditRewardScreen.route)
        }
    )
}

@Composable
private fun ScreenContent(
    rewards: List<Reward>,
    onAddNewRewardClicked: () -> Unit,
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
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_new_reward)
                )
            }
        },
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
                    RewardItem(reward)
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
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = {},
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = rewardIcons[reward.iconKey] ?: defaultIcon,
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
            RewardItem(Reward(IconKeys.BATH_TUB, "Title", 5))
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
                    Reward(iconKey = IconKeys.CAKE, name = "CAKE", 5),
                    Reward(iconKey = IconKeys.BATH_TUB, name = "BATH_TUB", 20),
                    Reward(iconKey = IconKeys.TV, name = "TV", 60),
                ),
                onAddNewRewardClicked = {}
            )
        }
    }
}