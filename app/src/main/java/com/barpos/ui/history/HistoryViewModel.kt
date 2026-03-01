package com.barpos.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.barpos.BarApplication
import com.barpos.data.database.entity.Member
import com.barpos.data.database.entity.Transaction
import com.barpos.data.database.entity.TransactionItem
import com.barpos.data.repository.MemberRepository
import com.barpos.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TransactionDisplay(
    val transaction: Transaction,
    val memberName: String,
    val items: List<TransactionItem> = emptyList()
)

class HistoryViewModel(
    private val transactionRepository: TransactionRepository,
    private val memberRepository: MemberRepository
) : ViewModel() {

    private val _members = MutableStateFlow<Map<Long, String>>(emptyMap())

    val transactions = transactionRepository.allTransactions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allMembers = memberRepository.allMembers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _filterMemberId = MutableStateFlow<Long?>(null)
    val filterMemberId: StateFlow<Long?> = _filterMemberId.asStateFlow()

    private val _selectedTransactionItems = MutableStateFlow<List<TransactionItem>>(emptyList())
    val selectedTransactionItems: StateFlow<List<TransactionItem>> = _selectedTransactionItems.asStateFlow()

    private val _selectedTransactionId = MutableStateFlow<Long?>(null)
    val selectedTransactionId: StateFlow<Long?> = _selectedTransactionId.asStateFlow()

    init {
        viewModelScope.launch {
            memberRepository.allMembers.collect { members ->
                _members.value = members.associate { it.id to it.name }
            }
        }
    }

    val displayTransactions: StateFlow<List<TransactionDisplay>> = combine(
        transactions, _members, _filterMemberId
    ) { txs, members, filterId ->
        txs
            .filter { filterId == null || it.memberId == filterId }
            .map { tx ->
                TransactionDisplay(
                    transaction = tx,
                    memberName = members[tx.memberId] ?: "Ukendt"
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilterMember(memberId: Long?) {
        _filterMemberId.value = memberId
    }

    fun selectTransaction(transactionId: Long) {
        if (_selectedTransactionId.value == transactionId) {
            _selectedTransactionId.value = null
            _selectedTransactionItems.value = emptyList()
        } else {
            _selectedTransactionId.value = transactionId
            viewModelScope.launch {
                _selectedTransactionItems.value =
                    transactionRepository.getTransactionItems(transactionId)
            }
        }
    }

    class Factory(private val application: BarApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(
                application.transactionRepository,
                application.memberRepository
            ) as T
        }
    }
}
