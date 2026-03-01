package com.barpos.ui.admin.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.barpos.BarApplication
import com.barpos.data.database.entity.*
import com.barpos.data.repository.TransactionRepository
import com.barpos.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class StatsPeriod(val label: String, val days: Int) {
    TODAY("I dag", 0),
    WEEK("Sidste 7 dage", 7),
    MONTH("Denne måned", 30),
    ALL("Alt tid", 3650)
}

data class StatsData(
    val totalRevenue: Double = 0.0,
    val topProducts: List<ProductSalesData> = emptyList(),
    val salesByCategory: List<CategorySalesData> = emptyList(),
    val salesByMember: List<MemberSalesData> = emptyList(),
    val dailyRevenue: List<DailyRevenueData> = emptyList()
)

class StatsViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(StatsPeriod.WEEK)
    val selectedPeriod: StateFlow<StatsPeriod> = _selectedPeriod.asStateFlow()

    private val _statsData = MutableStateFlow(StatsData())
    val statsData: StateFlow<StatsData> = _statsData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadStats()
    }

    fun selectPeriod(period: StatsPeriod) {
        _selectedPeriod.value = period
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            val period = _selectedPeriod.value
            val startDate = when (period) {
                StatsPeriod.TODAY -> DateUtils.startOfDay()
                StatsPeriod.MONTH -> DateUtils.startOfMonth()
                else -> DateUtils.daysAgo(period.days)
            }
            val endDate = DateUtils.endOfDay()

            val totalRevenue = transactionRepository.getTotalRevenue(startDate, endDate)
            val topProducts = transactionRepository.getTopProducts(startDate, endDate)
            val salesByCategory = transactionRepository.getSalesByCategory(startDate, endDate)
            val salesByMember = transactionRepository.getSalesByMember(startDate, endDate)
            val dailyRevenue = transactionRepository.getDailyRevenue(startDate, endDate)

            _statsData.value = StatsData(
                totalRevenue = totalRevenue,
                topProducts = topProducts,
                salesByCategory = salesByCategory,
                salesByMember = salesByMember,
                dailyRevenue = dailyRevenue
            )
            _isLoading.value = false
        }
    }

    class Factory(private val application: BarApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatsViewModel(application.transactionRepository) as T
        }
    }
}
