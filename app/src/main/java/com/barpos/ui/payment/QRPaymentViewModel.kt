package com.barpos.ui.payment

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.barpos.BarApplication
import com.barpos.data.database.entity.Member
import com.barpos.data.database.entity.AdminSettings
import com.barpos.data.database.entity.Transaction
import com.barpos.data.database.entity.TransactionType
import com.barpos.data.repository.MemberRepository
import com.barpos.data.repository.SettingsRepository
import com.barpos.data.repository.TransactionRepository
import com.barpos.util.QRCodeGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class QRPaymentViewModel(
    private val memberRepository: MemberRepository,
    private val transactionRepository: TransactionRepository,
    private val settingsRepository: SettingsRepository,
    private val memberId: Long
) : ViewModel() {

    private val _member = MutableStateFlow<Member?>(null)
    val member: StateFlow<Member?> = _member.asStateFlow()

    private val _settings = MutableStateFlow<AdminSettings?>(null)
    val settings: StateFlow<AdminSettings?> = _settings.asStateFlow()

    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap.asStateFlow()

    private val _paymentAmount = MutableStateFlow(0.0)
    val paymentAmount: StateFlow<Double> = _paymentAmount.asStateFlow()

    init {
        viewModelScope.launch {
            val m = memberRepository.getById(memberId)
            _member.value = m
            val s = settingsRepository.getSettingsSync()
            _settings.value = s

            if (m != null && s != null && m.balance < 0) {
                val amount = abs(m.balance)
                _paymentAmount.value = amount
                _qrBitmap.value = QRCodeGenerator.generateMobilePayQR(
                    phoneNumber = s.mobilepayNumber,
                    amount = amount,
                    comment = s.barName,
                    size = 512
                )
            }
        }
    }

    fun confirmPayment(onSuccess: () -> Unit) {
        val m = _member.value ?: return
        viewModelScope.launch {
            val paymentTransaction = Transaction(
                memberId = m.id,
                type = TransactionType.PAYMENT,
                totalAmount = abs(m.balance)
            )
            transactionRepository.insertPayment(paymentTransaction)
            memberRepository.resetBalance(m.id)
            onSuccess()
        }
    }

    class Factory(
        private val application: BarApplication,
        private val memberId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QRPaymentViewModel(
                application.memberRepository,
                application.transactionRepository,
                application.settingsRepository,
                memberId
            ) as T
        }
    }
}
