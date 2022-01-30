package com.florianwalther.incentivetimer.core.ui.composables

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.florianwalther.incentivetimer.R

@Composable
fun AppInstructionsDialog(
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = { Text(stringResource(R.string.app_instructions_text)) },
        title = { Text(stringResource(R.string.instructions)) },

        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}