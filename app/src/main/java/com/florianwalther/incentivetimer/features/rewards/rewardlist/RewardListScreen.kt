package com.florianwalther.incentivetimer.features.rewards.rewardlist

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.core.ui.IconKey
import com.florianwalther.incentivetimer.core.ui.ListBottomPadding
import com.florianwalther.incentivetimer.core.ui.composables.SimpleConfirmationDialog
import com.florianwalther.incentivetimer.core.ui.theme.ITBlue
import com.florianwalther.incentivetimer.core.ui.theme.IncentiveTimerTheme
import com.florianwalther.incentivetimer.core.ui.theme.PrimaryLightAlpha
import com.florianwalther.incentivetimer.data.db.Reward
import com.florianwalther.incentivetimer.features.rewards.rewardlist.model.RewardListScreenState
import kotlinx.coroutines.launch

@Composable
fun RewardListScreenAppBar(
    screenState: RewardListScreenState,
    actions: RewardListActions,
) {
    TopAppBar(
        title = {
            if (screenState.multiSelectionModeActive) {
                Text(stringResource(R.string.selected_placeholder, screenState.selectedItemCount))
            } else {
                Text(stringResource(R.string.rewards))
            }
        },
        actions = {
            if (screenState.multiSelectionModeActive) {
                IconButton(onClick = actions::onDeleteAllSelectedItemsClicked) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_all_selected_items)
                    )
                }
            } else {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.open_menu)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(onClick = {
                            expanded = false
                            actions.onDeleteAllUnlockedRewardsClicked()
                        }) {
                            Text(stringResource(R.string.delete_all_unlocked_rewards))
                        }
                    }
                }
            }
        },
        navigationIcon = if (screenState.multiSelectionModeActive) {
            {
                IconButton(onClick = actions::onCancelMultiSelectionModeClicked) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.close)
                    )
                }
            }
        } else {
            null
        }
    )
}

@Composable
fun RewardListScreenContent(
    screenState: RewardListScreenState,
    onAddNewRewardClicked: () -> Unit,
    actions: RewardListActions,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
) {
    Scaffold(
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

        Box(Modifier.fillMaxSize()) {
            val rewards = screenState.rewards
            if (rewards.isEmpty()) {
                Text(
                    stringResource(R.string.reward_list_empty_text),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.body2,
                )
            } else {
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
                    items(screenState.rewards, key = { it.id }) { reward ->
                        val selected = screenState.selectedRewardIds.contains(reward.id)
                        val dismissState = rememberDismissState(
                            confirmStateChange = { dismissValue ->
                                if (dismissValue == DismissValue.DismissedToEnd || dismissValue == DismissValue.DismissedToStart) {
                                    actions.onRewardSwiped(reward)
                                }
                                true
                            })
                        if (reward.isUnlocked) {
                            SwipeToDismiss(
                                state = dismissState,
                                background = {},
                                modifier = Modifier.animateItemPlacement(),
                            ) {
                                RewardItem(
                                    reward = reward,
                                    selected = selected,
                                    onItemClicked = actions::onRewardClicked,
                                    onItemLongClicked = actions::onRewardLongClicked,
                                )
                            }
                        } else {
                            RewardItem(
                                reward = reward,
                                selected = selected,
                                onItemClicked = actions::onRewardClicked,
                                onItemLongClicked = actions::onRewardLongClicked,
                                modifier = Modifier.animateItemPlacement(),
                            )
                        }
                    }
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

    if (screenState.showDeleteAllUnlockedRewardsDialog) {
        SimpleConfirmationDialog(
            title = R.string.delete_all_unlocked_rewards,
            text = R.string.delete_all_unlocked_rewards_confirmation_text,
            dismissAction = actions::onDeleteAllUnlockedRewardsDialogDismissed,
            confirmAction = actions::onDeleteAllUnlockedRewardsConfirmed
        )
    }

    if (screenState.showDeleteAllSelectedRewardsDialog) {
        SimpleConfirmationDialog(
            title = R.string.delete_rewards,
            text = R.string.delete_all_selected_rewards_confirmation_text,
            dismissAction = actions::onDeleteAllSelectedRewardsDialogDismissed,
            confirmAction = actions::onDeleteAllSelectedRewardsConfirmed
        )
    }
}

@Composable
private fun RewardItem(
    reward: Reward,
    selected: Boolean,
    onItemClicked: (Reward) -> Unit,
    onItemLongClicked: (Reward) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = {
                    onItemClicked(reward)
                },
                onLongClick = {
                    onItemLongClicked(reward)
                }
            ),
        backgroundColor = if (selected) ITBlue.copy(alpha = PrimaryLightAlpha) else MaterialTheme.colors.surface
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = reward.iconKey.rewardIcon,
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .size(64.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = reward.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.chance) + ": ${reward.chanceInPercent}%",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (reward.isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.reward_unlocked),
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colors.primary,
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
            RewardItem(
                Reward("Title mmmmmmmmmmmmmmmmmmmmm", 5, IconKey.BATH_TUB),
                onItemClicked = {},
                onItemLongClicked = {},
                selected = false,
            )
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
private fun RewardItemUnlockedPreview() {
    IncentiveTimerTheme {
        Surface {
            RewardItem(
                Reward(
                    "Title mmmmmmmmmmmmmmmmmmmmmmmmmmmm",
                    5,
                    IconKey.BATH_TUB,
                    isUnlocked = true
                ),
                onItemClicked = {},
                onItemLongClicked = {},
                selected = true,
            )
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
            RewardListScreenContent(

                screenState = RewardListScreenState(
                    rewards = listOf(
                        Reward(name = "CAKE", 5, iconKey = IconKey.CAKE),
                        Reward(name = "BATH_TUB", 20, iconKey = IconKey.BATH_TUB),
                        Reward(name = "TV", 60, iconKey = IconKey.TV),
                    ),
                    selectedRewardIds = listOf(2L),
                    selectedItemCount = 1,
                    multiSelectionModeActive = true,
                    showDeleteAllSelectedRewardsDialog = false,
                    showDeleteAllUnlockedRewardsDialog = false,
                ),
                onAddNewRewardClicked = {},
                actions = object : RewardListActions {
                    override fun onDeleteAllUnlockedRewardsClicked() {}
                    override fun onDeleteAllUnlockedRewardsConfirmed() {}
                    override fun onDeleteAllUnlockedRewardsDialogDismissed() {}
                    override fun onRewardClicked(reward: Reward) {}
                    override fun onRewardLongClicked(reward: Reward) {}
                    override fun onCancelMultiSelectionModeClicked() {}
                    override fun onRewardSwiped(reward: Reward) {}
                    override fun onUndoDeleteRewardConfirmed(reward: Reward) {}
                    override fun onDeleteAllSelectedRewardsConfirmed() {}
                    override fun onDeleteAllSelectedRewardsDialogDismissed() {}
                    override fun onDeleteAllSelectedItemsClicked() {}
                },
            )
        }
    }
}