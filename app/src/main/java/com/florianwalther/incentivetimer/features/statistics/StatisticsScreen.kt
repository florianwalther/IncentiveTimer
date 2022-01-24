package com.florianwalther.incentivetimer.features.statistics

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.florianwalther.incentivetimer.R
import com.florianwalther.incentivetimer.core.ui.composables.DropdownMenuButton
import com.florianwalther.incentivetimer.core.ui.composables.SimpleConfirmationDialog
import com.florianwalther.incentivetimer.core.ui.theme.IncentiveTimerTheme
import com.florianwalther.incentivetimer.core.util.formatMinutesToTimeString
import com.florianwalther.incentivetimer.features.statistics.model.DailyPomodoroStatistic
import com.florianwalther.incentivetimer.features.statistics.model.StatisticsScreenState
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.Utils
import logcat.logcat
import java.text.DateFormat
import java.util.*

@Composable
fun StatisticsScreenAppBar(
    actions: StatisticsActions,
) {
    TopAppBar(
        title = {
            Text(stringResource(R.string.statistics))
        },
        actions = {
            Box {
                var expanded by remember { mutableStateOf(false) }
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
                        actions.onResetPomodoroStatisticsClicked()
                    }) {
                        Text(stringResource(R.string.reset_statistics))
                    }
                }
            }
        }
    )
}

@Composable
fun StatisticsScreenContent(
    screenState: StatisticsScreenState,
    actions: StatisticsActions,
    resetBarChartZoom: Boolean,
    onBarChartZoomReset: () -> Unit,
) {
    Column {
        if (screenState.dailyPomodoroStatisticsInTimeframe.isNotEmpty()) {
            Column(Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)) {
                Row(
                    Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.pomodoro_time_completed),
                        Modifier.weight(1f)
                    )
                    DropdownMenuButton(
                        optionsLabels = StatisticsGranularity.values().map { it.readableName }
                            .toList(),
                        selectedIndex = screenState.selectedStatisticsGranularityIndex,
                        onOptionSelected = { selectedIndex ->

                            actions.onPomodoroMinutesCompletedGranularitySelected(
                                StatisticsGranularity.values()[selectedIndex]
                            )
                        },
                    )
                }
                val hourAbbreviation = stringResource(R.string.hours_abbreviation)
                Text(
                    stringResource(R.string.total) + ": ${
                        formatMinutesToTimeString(
                            screenState.pomodoroMinutesCompletedInTimeframe,
                            showHoursWithoutSeconds = true
                        )
                    } $hourAbbreviation"
                )
            }
        }

        PomodoroStatisticsBarChart(
            dailyPomodoroStatistics = screenState.dailyPomodoroStatisticsInTimeframe,
            resetZoom = resetBarChartZoom,
            onZoomReset = onBarChartZoomReset,
        )
    }

    if (screenState.showResetPomodoroStatisticsDialog) {
        SimpleConfirmationDialog(
            title = R.string.reset_statistics,
            text = R.string.reset_pomodoro_statistics_confirmation_message,
            dismissAction = actions::onResetPomodoroStatisticsDialogDismissed,
            confirmAction = actions::onResetPomodoroStatisticsConfirmed
        )
    }
}

@Composable
private fun PomodoroStatisticsBarChart(
    dailyPomodoroStatistics: List<DailyPomodoroStatistic>,
    modifier: Modifier = Modifier,
    resetZoom: Boolean,
    onZoomReset: () -> Unit,
) {
    val colorPrimary = MaterialTheme.colors.primary.toArgb()
    val noDataText = stringResource(R.string.no_data_text_daily_pomodoro_statistics)
    val onSurfaceTextColor = MaterialTheme.colors.onSurface.toArgb()
    val textSize = 12f

    val label = stringResource(R.string.pomodoro_minutes_completed)
    val minAbbreviation = stringResource(R.string.minutes_abbreviation)
    val hourAbbreviation = stringResource(R.string.hours_abbreviation)

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            BarChart(context)
        },
        update = { barChart ->
            var barData: BarData? = null // null necessary to show empty view text

            val dataSetFormatter = object : ValueFormatter() {
                override fun getBarLabel(barEntry: BarEntry?): String {
                    val minutes = barEntry?.y?.toInt() ?: 0
                    return if (minutes < 60) {
                        "$minutes $minAbbreviation"
                    } else {
                        formatMinutesToTimeString(
                            minutes,
                            showHoursWithoutSeconds = true
                        ) + " " + hourAbbreviation
                    }
                }
            }

            val xAxisFormatter = IndexAxisValueFormatter(
                dailyPomodoroStatistics.map { statistic ->
                    DateFormat.getDateInstance(DateFormat.SHORT).format(statistic.dateWithoutTime)
                }
            )

            val leftAxisFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    val yValue = value.toInt()
                    return if (yValue == 0) {
                        ""
                    } else {
                        "$yValue $minAbbreviation"
                    }
                }
            }

            if (dailyPomodoroStatistics.isNotEmpty()) {
                val entries = dailyPomodoroStatistics.mapIndexed { index, dailyStatistic ->
                    BarEntry(
                        index.toFloat(),
                        dailyStatistic.totalPomodoroDurationInMinutes.toFloat(),
                    )
                }
                val dataSet = BarDataSet(entries, label).apply {
                    color = colorPrimary
                    valueFormatter = dataSetFormatter
                }
                barData = BarData(dataSet).apply {
                    setValueTextSize(textSize)
                    setValueTextColor(onSurfaceTextColor)
                }
            }

            barChart.apply {
                xAxis.apply {
                    granularity = 1f
                    isGranularityEnabled = true
                    setTextSize(textSize)
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    textColor = onSurfaceTextColor
                    valueFormatter = xAxisFormatter
                }
                axisLeft.apply {
                    setTextSize(textSize)
                    textColor = onSurfaceTextColor
                    axisMinimum = 0f
                    granularity = 1f
                    isGranularityEnabled = true
                    valueFormatter = leftAxisFormatter
                }
                axisRight.apply {
                    axisMinimum = 0f
                    setDrawLabels(false)
                    setDrawGridLines(false)
                }

                data = barData
                setNoDataText(noDataText)
                setNoDataTextColor(onSurfaceTextColor)
                getPaint(Chart.PAINT_INFO).textSize = Utils.convertDpToPixel(14f)
                legend.textColor = onSurfaceTextColor
                setMaxVisibleValueCount(12)
                isHighlightPerTapEnabled = false
                isHighlightPerDragEnabled = false
                extraBottomOffset = 8f
                if (resetZoom) {
                    logcat("StatisticsScreen") { "reset zoom" }
                    this.fitScreen()
                    onZoomReset()
                }
                invalidate()
            }
        }
    )
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
            StatisticsScreenContent(
                screenState = StatisticsScreenState(
                    listOf(
                        DailyPomodoroStatistic(Date(1), 1 * 60_000),
                        DailyPomodoroStatistic(Date(2), 2 * 60_000),
                        DailyPomodoroStatistic(Date(3), 3 * 60_000),
                        DailyPomodoroStatistic(Date(4), 4 * 60_000),
                        DailyPomodoroStatistic(Date(5), 5 * 60_000),
                    ),
                    selectedStatisticsGranularityIndex = 1,
                    showResetPomodoroStatisticsDialog = false,
                    pomodoroMinutesCompletedInTimeframe = 13
                ),
                actions = object : StatisticsActions {
                    override fun onPomodoroMinutesCompletedGranularitySelected(
                        granularity: StatisticsGranularity
                    ) {
                    }

                    override fun onResetPomodoroStatisticsClicked() {}
                    override fun onResetPomodoroStatisticsConfirmed() {}
                    override fun onResetPomodoroStatisticsDialogDismissed() {}
                },
                resetBarChartZoom = false,
                onBarChartZoomReset = {},
            )
        }
    }
}