package com.florianwalther.incentivetimer.features.addeditreward

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.core.ui.IconKey
import com.florianwalther.incentivetimer.core.ui.composables.ITIconButton
import com.florianwalther.incentivetimer.core.ui.defaultRewardIconKey
import com.florianwalther.incentivetimer.core.ui.theme.IncentiveTimerTheme
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment

@Composable
fun AddEditRewardScreenAppBar(
    isEditMode: Boolean,
    onCloseClicked: () -> Unit,
    actions: AddEditRewardScreenActions
) {
    val appBarTitle =
        stringResource(if (isEditMode) R.string.edit_reward else R.string.add_reward)
    TopAppBar(
        title = {
            Text(appBarTitle)
        },
        navigationIcon = {
            IconButton(onClick = onCloseClicked) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.close)
                )
            }
        },
        actions = {
            if (isEditMode) {
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
                            actions.onDeleteRewardClicked()
                        }) {
                            Text(stringResource(R.string.delete_reward))
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun AddEditRewardScreenContent(
    isEditMode: Boolean,
    rewardNameInput: String,
    rewardNameInputIsError: Boolean,
    chanceInPercentInput: Int,
    rewardIconKeySelection: IconKey,
    showRewardIconSelectionDialog: Boolean,
    showDeleteRewardConfirmationDialog: Boolean,
    actions: AddEditRewardScreenActions,
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = actions::onSaveClicked,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = stringResource(R.string.save_reward)
                )
            }
        },
    ) {
        Column(Modifier.padding(16.dp)) {
            val focusRequester = remember { FocusRequester()}
            TextField(
                value = rewardNameInput,
                onValueChange = actions::onRewardNameInputChanged,
                label = { Text(stringResource(R.string.reward_name)) },
                singleLine = true,
                isError = rewardNameInputIsError,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
            if (!isEditMode) {
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
            if (rewardNameInputIsError) {
                Text(
                    stringResource(R.string.field_cant_be_blank),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.error,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.chance) + ": $chanceInPercentInput%")
            Slider(
                value = chanceInPercentInput.toFloat() / 100,
                onValueChange = { chanceAsFloat ->
                    actions.onChanceInPercentInputChanged((chanceAsFloat * 100).toInt())
                }
            )
            Spacer(Modifier.height(16.dp))
            ITIconButton(
                onClick = actions::onRewardIconButtonClicked,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = rewardIconKeySelection.rewardIcon,
                    contentDescription = stringResource(R.string.select_icon),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }
        }
    }

    if (showRewardIconSelectionDialog) {
        RewardIconSelectionDialog(
            onDismissRequest = actions::onRewardIconDialogDismissed,
            onIconSelected = actions::onRewardIconSelected
        )
    }

    if (showDeleteRewardConfirmationDialog) {
        AlertDialog(
            onDismissRequest = actions::onDeleteRewardDialogDismissed,
            title = {
                Text(stringResource(R.string.confirm_deletion))
            },
            text = {
                Text(stringResource(R.string.confirm_reward_deletion_text))
            },
            confirmButton = {
                TextButton(onClick = actions::onDeleteRewardConfirmed) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = actions::onDeleteRewardDialogDismissed) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun RewardIconSelectionDialog(
    onDismissRequest: () -> Unit,
    onIconSelected: (iconKey: IconKey) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            FlowRow(Modifier.fillMaxWidth(), mainAxisAlignment = MainAxisAlignment.Center) {
                IconKey.values().forEach { iconKey ->
                    IconButton(onClick = {
                        onIconSelected(iconKey)
                        onDismissRequest()
                    }) {
                        Icon(
                            imageVector = iconKey.rewardIcon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(8.dp)
                        )
                    }
                }
            }
        },
        buttons = {
            TextButton(
                onClick = onDismissRequest, modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
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
            AddEditRewardScreenContent(
                isEditMode = false,
                rewardNameInput = "Example reward",
                rewardNameInputIsError = false,
                chanceInPercentInput = 10,
                showRewardIconSelectionDialog = false,
                showDeleteRewardConfirmationDialog = false,
                rewardIconKeySelection = defaultRewardIconKey,
                actions = object : AddEditRewardScreenActions {
                    override fun onRewardNameInputChanged(input: String) {}
                    override fun onChanceInPercentInputChanged(input: Int) {}
                    override fun onRewardIconButtonClicked() {}
                    override fun onRewardIconSelected(iconKey: IconKey) {}
                    override fun onRewardIconDialogDismissed() {}
                    override fun onSaveClicked() {}
                    override fun onDeleteRewardClicked() {}
                    override fun onDeleteRewardConfirmed() {}
                    override fun onDeleteRewardDialogDismissed() {}
                }
            )
        }
    }
}