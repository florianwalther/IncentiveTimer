package com.florianwalther.incentivetimer.core.ui.composables

import androidx.annotation.StringRes
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.florianwalther.incentivetimer.R

@Composable
fun SimpleConfirmationDialog(
    @StringRes title: Int,
    @StringRes text: Int,
    dismissAction: () -> Unit,
    confirmAction: () -> Unit,
    @StringRes confirmButtonText: Int = R.string.confirm,
) {
    AlertDialog(
        onDismissRequest = dismissAction,
        text = { Text(stringResource(text)) },
        title = { Text(stringResource(title)) },
        dismissButton = {
            TextButton(onClick = dismissAction) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = confirmAction) {
                Text(stringResource(confirmButtonText))
            }
        }
    )
}