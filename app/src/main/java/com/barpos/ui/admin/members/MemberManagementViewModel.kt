package com.barpos.ui.admin.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.barpos.BarApplication
import com.barpos.data.database.entity.Member
import com.barpos.data.repository.MemberRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MemberManagementViewModel(
    private val memberRepository: MemberRepository
) : ViewModel() {

    val members = memberRepository.allMembers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun addMember(name: String, certificateId: String?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            memberRepository.insert(
                Member(
                    name = name.trim(),
                    certificateId = certificateId?.trim()?.ifBlank { null }
                )
            )
        }
    }

    fun updateMember(member: Member) {
        viewModelScope.launch {
            memberRepository.update(member)
        }
    }

    fun toggleMemberActive(member: Member) {
        viewModelScope.launch {
            memberRepository.update(member.copy(isActive = !member.isActive))
        }
    }

    class Factory(private val application: BarApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MemberManagementViewModel(application.memberRepository) as T
        }
    }
}
