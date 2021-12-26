package com.florianwalther.incentivetimer.core.ui.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.florianwalther.incentivetimer.core.ui.theme.IncentiveTimerTheme
import com.florianwalther.incentivetimer.core.ui.theme.PrimaryLightAlpha

@Composable
fun RoundedCornerCircularProgressIndicatorWithBackground(
    /*@FloatRange(from = 0.0, to = 1.0)*/
    progress: Float,
    modifier: Modifier = Modifier,
    foregroundColor: Color = MaterialTheme.colors.primary,
    backgroundColor: Color = MaterialTheme.colors.primary.copy(alpha = PrimaryLightAlpha),
    strokeWidth: Dp = ProgressIndicatorDefaults.StrokeWidth
) {
    Box(modifier) {
        RoundedCornerCircularProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxSize(),
            color = foregroundColor,
            strokeWidth = strokeWidth,
        )
        RoundedCornerCircularProgressIndicator(
            progress = 1f,
            modifier = Modifier.fillMaxSize(),
            color = backgroundColor,
            strokeWidth = strokeWidth,
        )
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
            RoundedCornerCircularProgressIndicatorWithBackground(progress = .6f)
        }
    }
}