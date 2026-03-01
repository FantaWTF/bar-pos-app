package com.barpos.ui.admin.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.barpos.BarApplication
import com.barpos.data.database.entity.AdminSettings
import com.barpos.data.database.entity.Transaction
import com.barpos.data.database.entity.TransactionItem
import com.barpos.data.repository.MemberRepository
import com.barpos.data.repository.SettingsRepository
import com.barpos.data.repository.TransactionRepository
import com.barpos.util.CsvExporter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val transactionRepository: TransactionRepository,
    private val memberRepository: MemberRepository
) : ViewModel() {

    val settings = settingsRepository.settings.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    fun updateBarName(name: String) {
        val current = settings.value ?: return
        viewModelScope.launch {
            settingsRepository.updateSettings(current.copy(barName = name))
        }
    }

    fun updateMobilePayNumber(number: String) {
        val current = settings.value ?: return
        viewModelScope.launch {
            settingsRepository.updateSettings(current.copy(mobilepayNumber = number))
        }
    }

    fun updatePin(newPin: String) {
        val current = settings.value ?: return
        if (newPin.length < 4) return
        viewModelScope.launch {
            settingsRepository.updateSettings(current.copy(adminPin = newPin))
            _saveSuccess.value = true
        }
    }

    fun clearSaveSuccess() {
        _saveSuccess.value = false
    }

    fun exportCsv(context: Context) {
        viewModelScope.launch {
            val transactions = mutableListOf<Transaction>()
            val memberNames = mutableMapOf<Long, String>()
            val transactionItems = mutableMapOf<Long, List<TransactionItem>>()

            transactionRepository.allTransactions.first().let { txs ->
                transactions.addAll(txs)
                for (tx in txs) {
                    if (!memberNames.containsKey(tx.memberId)) {
                        val member = memberRepository.getById(tx.memberId)
                        memberNames[tx.memberId] = member?.name ?: "Ukendt"
                    }
                    transactionItems[tx.id] = transactionRepository.getTransactionItems(tx.id)
                }
            }

            val file = CsvExporter.exportTransactions(
                context, transactions, transactionItems, memberNames
            )
            CsvExporter.shareFile(context, file)
        }
    }

    class Factory(private val application: BarApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                application.settingsRepository,
                application.transactionRepository,
                application.memberRepository
            ) as T
        }
    }
}
