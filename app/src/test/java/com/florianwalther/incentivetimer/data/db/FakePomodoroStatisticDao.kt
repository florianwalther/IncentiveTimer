package com.florianwalther.incentivetimer.data.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakePomodoroStatisticDao(
    pomodoroStatistics: LinkedHashMap<Long, PomodoroStatistic> = LinkedHashMap()
) : PomodoroStatisticDao {

    private val pomodoroStatistics =
        MutableStateFlow<Map<Long, PomodoroStatistic>>(pomodoroStatistics)

    override fun getAllPomodoroStatistics(): Flow<List<PomodoroStatistic>> =
        pomodoroStatistics.map { it.values.toList() }

    override suspend fun insertPomodoroStatistic(pomodoroStatistic: PomodoroStatistic) {
        val pomodoroStatisticWithId = if (pomodoroStatistic.id > 0) {
            pomodoroStatistic
        } else {
            val newId = getCurrentHighestPomodoroStatisticId() + 1
            pomodoroStatistic.copy(id = newId)
        }

        updatePomodoroStatisticsMap {
            this[pomodoroStatisticWithId.id] = pomodoroStatisticWithId
        }
    }

    override suspend fun deleteAllPomodoroStatistics() {
        updatePomodoroStatisticsMap {
            this.clear()
        }
    }

    private fun updatePomodoroStatisticsMap(block: MutableMap<Long, PomodoroStatistic>.() -> Unit) {
        pomodoroStatistics.value = pomodoroStatistics.value.toMutableMap().apply(block).toMap()
    }

    private fun getCurrentHighestPomodoroStatisticId(): Long =
        pomodoroStatistics.value.keys.lastOrNull() ?: 0L
}