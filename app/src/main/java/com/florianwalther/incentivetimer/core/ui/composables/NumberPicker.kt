package com.florianwalther.incentivetimer.core.ui.composables

import android.widget.NumberPicker
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun NumberPicker(
    minValue: Int,
    maxValue: Int,
    value: Int,
    modifier: Modifier = Modifier,
    onValueChanged: (newVal: Int) -> Unit,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            NumberPicker(context).apply {
                this.minValue = minValue
                this.maxValue = maxValue
                this.value = value
                this.setOnValueChangedListener { _, _, newVal ->
                    onValueChanged(newVal)
                }
            }
        }
    )
}