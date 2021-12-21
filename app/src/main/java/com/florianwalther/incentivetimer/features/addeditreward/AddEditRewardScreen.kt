package com.florianwalther.incentivetimer.features.addeditreward

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.core.ui.IconKey
import com.florianwalther.incentivetimer.core.ui.composables.ITIconButton
import com.florianwalther.incentivetimer.core.ui.defaultRewardIconKey
import com.florianwalther.incentivetimer.core.ui.theme.IncentiveTimerTheme
import com.florianwalther.incentivetimer.core.util.exhaustive
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import kotlinx.coroutines.flow.collect

interface AddEditRewardScreenActions {
    fun onRewardNameInputChanged(input: String)
    fun onChanceInPercentInputChanged(input: Int)
    fun onRewardIconButtonClicked()
    fun onRewardIconSelected(iconKey: IconKey)
    fun onRewardIconDialogDismissRequest()
    fun onSaveClicked()
}

@Composable
fun AddEditRewardScreen(
    navController: NavController,
) {
    val viewModel: AddEditRewardViewModel = hiltViewModel()
    val isEditMode = viewModel.isEditMode
    val rewardNameInput by viewModel.rewardNameInput.observeAsState("")
    val rewardNameInputIsError by viewModel.rewardNameInputIsError.observeAsState(false)
    val chanceInPercentInput by viewModel.chanceInPercentInput.observeAsState(10)
    val rewardIconKeySelection
            by viewModel.rewardIconKeySelection.observeAsState(defaultRewardIconKey)
    val showRewardIconSelectionDialog
            by viewModel.showRewardIconSelectionDialog.observeAsState(false)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AddEditRewardViewModel.AddEditRewardEvent.RewardCreated -> {
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        ADD_EDIT_REWARD_RESULT, RESULT_REWARD_ADDED
                    )
                    navController.popBackStack()
                }
                AddEditRewardViewModel.AddEditRewardEvent.RewardUpdated -> {
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        ADD_EDIT_REWARD_RESULT, RESULT_REWARD_UPDATED
                    )
                    navController.popBackStack()
                }
            }.exhaustive
        }
    }

    ScreenContent(
        isEditMode = isEditMode,
        rewardNameInput = rewardNameInput,
        rewardNameInputIsError = rewardNameInputIsError,
        chanceInPercentInput = chanceInPercentInput,
        rewardIconKeySelection = rewardIconKeySelection,
        showRewardIconSelectionDialog = showRewardIconSelectionDialog,
        actions = viewModel,
        onCloseClicked = { navController.popBackStack() },
    )
}

@Composable
private fun ScreenContent(
    isEditMode: Boolean,
    rewardNameInput: String,
    rewardNameInputIsError: Boolean,
    chanceInPercentInput: Int,
    rewardIconKeySelection: IconKey,
    showRewardIconSelectionDialog: Boolean,
    actions: AddEditRewardScreenActions,
    onCloseClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
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
            )
        },
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
            TextField(
                value = rewardNameInput,
                onValueChange = actions::onRewardNameInputChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.reward_name)) },
                singleLine = true,
                isError = rewardNameInputIsError,
            )
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
            onDismissRequest = actions::onRewardIconDialogDismissRequest,
            onIconSelected = actions::onRewardIconSelected
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
            ScreenContent(
                isEditMode = false,
                rewardNameInput = "Example reward",
                rewardNameInputIsError = false,
                chanceInPercentInput = 10,
                onCloseClicked = {},
                showRewardIconSelectionDialog = false,
                rewardIconKeySelection = defaultRewardIconKey,
                actions = object : AddEditRewardScreenActions {
                    override fun onRewardNameInputChanged(input: String) {}
                    override fun onChanceInPercentInputChanged(input: Int) {}
                    override fun onRewardIconButtonClicked() {}
                    override fun onRewardIconSelected(iconKey: IconKey) {}
                    override fun onRewardIconDialogDismissRequest() {}
                    override fun onSaveClicked() {}
                },
            )
        }
    }
}