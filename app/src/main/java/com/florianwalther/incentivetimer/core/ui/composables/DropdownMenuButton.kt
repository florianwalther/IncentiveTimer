package com.florianwalther.incentivetimer.core.ui.composables

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material.*
import com.florianwalther.incentivetimer.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.florianwalther.incentivetimer.core.ui.theme.IncentiveTimerTheme

@Composable
fun DropdownMenuButton(
    @StringRes optionsLabels: List<Int>,
    selectedIndex: Int,
    onOptionSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val buttonText = if (optionsLabels.isNotEmpty()) {
        stringResource(optionsLabels[selectedIndex])
    } else {
        stringResource(R.string.drop_down_menu_button_empty_options)
    }

    Box(modifier) {
        TextButton(onClick = { expanded = !expanded }) {
            Text(buttonText, color = MaterialTheme.colors.onSurface)
            Icon(imageVector = Icons.Filled.ArrowDropDown, tint = MaterialTheme.colors.onSurface, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }) {
            optionsLabels.forEachIndexed { index, optionLabel ->
                DropdownMenuItem(
                    onClick = {
                        onOptionSelected(index)
                        expanded = false
                    }) {
                    Text(stringResource(optionLabel))
                }
            }
        }
    }
}

@Preview(
    name = "Light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ScreenContentPreview() {
    IncentiveTimerTheme {
        Surface {
            DropdownMenuButton(
                optionsLabels = listOf(R.string.last_7_days, R.string.all_time),
                selectedIndex = 1,
                onOptionSelected = {},
            )
        }
    }
}