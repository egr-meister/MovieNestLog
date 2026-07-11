package com.movienest.log.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movienest.log.data.repository.MovieNestRepository
import com.movienest.log.ui.state.StatisticsUiState
import com.movienest.log.util.DateUtils
import com.movienest.log.util.StatisticsUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class StatisticsViewModel(private val repository: MovieNestRepository) : ViewModel() {

    private val selectedMonth = MutableStateFlow(DateUtils.currentMonthKey())

    val uiState: StateFlow<StatisticsUiState> =
        combine(repository.entriesFlow, selectedMonth) { entries, monthKey ->
            // Guard against an invalid month key.
            val safeKey = DateUtils.parseMonthKeyOrNull(monthKey)
                ?.let { DateUtils.monthKeyFrom(it) } ?: DateUtils.currentMonthKey()
            StatisticsUiState(
                loading = false,
                monthKey = safeKey,
                monthLabel = DateUtils.monthDisplayLabel(safeKey),
                stats = StatisticsUtils.compute(entries, safeKey)
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState())

    fun previousMonth() { selectedMonth.value = DateUtils.previousMonthKey(selectedMonth.value) }
    fun nextMonth() { selectedMonth.value = DateUtils.nextMonthKey(selectedMonth.value) }
    fun currentMonth() { selectedMonth.value = DateUtils.currentMonthKey() }
}
