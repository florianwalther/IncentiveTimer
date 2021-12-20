package com.florianwalther.incentivetimer.addeditreward

import android.content.res.Configuration
import android.system.Os.close
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.ui.theme.IncentiveTimerTheme
import kotlinx.coroutines.flow.collect

@Composable
fun AddEditRewardScreen(
    navController: NavController,
) {
    val viewModel: AddEditRewardViewModel = hiltViewModel()
    val isEditMode = viewModel.isEditMode
    val rewardNameInput by viewModel.rewardNameInput.observeAsState("")
    val chanceInPercentInput by viewModel.chanceInPercentInput.observeAsState(10)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AddEditRewardViewModel.AddEditRewardEvent.RewardCreated -> navController.popBackStack()
            }
        }
    }

    ScreenContent(
        isEditMode = isEditMode,
        rewardNameInput = rewardNameInput,
        onRewardNameInputChanged = viewModel::onRewardNameInputChanged,
        chanceInPercentInput = chanceInPercentInput,
        onChanceInputChanged = viewModel::onChanceInPercentInputChanged,
        onCloseClicked = { navController.popBackStack() },
        onSaveClicked = viewModel::onSaveClicked,
    )
}

@Composable
private fun ScreenContent(
    isEditMode: Boolean,
    rewardNameInput: String,
    onRewardNameInputChanged: (input: String) -> Unit,
    chanceInPercentInput: Int,
    onChanceInputChanged: (input: Int) -> Unit,
    onSaveClicked: () -> Unit,
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
                onClick = onSaveClicked,
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
                onValueChange = onRewardNameInputChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.reward_name)) },
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.chance) + ": $chanceInPercentInput%")
            Slider(
                value = chanceInPercentInput.toFloat() / 100,
                onValueChange = { chanceAsFloat ->
                    onChanceInputChanged((chanceAsFloat * 100).toInt())
                }
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
            ScreenContent(
                isEditMode = false,
                rewardNameInput = "Example reward",
                onRewardNameInputChanged = {},
                chanceInPercentInput = 10,
                onChanceInputChanged = {},
                onSaveClicked = {},
                onCloseClicked = {},
            )
        }
    }
}