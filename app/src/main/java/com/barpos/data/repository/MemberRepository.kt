package com.barpos.data.repository

import com.barpos.data.database.dao.MemberDao
import com.barpos.data.database.entity.Member
import kotlinx.coroutines.flow.Flow

class MemberRepository(private val memberDao: MemberDao) {
    val activeMembers: Flow<List<Member>> = memberDao.getActiveMembers()
    val allMembers: Flow<List<Member>> = memberDao.getAllMembers()

    suspend fun getById(id: Long): Member? = memberDao.getById(id)
    suspend fun insert(member: Member): Long = memberDao.insert(member)
    suspend fun update(member: Member) = memberDao.update(member)
    suspend fun subtractBalance(memberId: Long, amount: Double) = memberDao.subtractBalance(memberId, amount)
    suspend fun resetBalance(memberId: Long) = memberDao.resetBalance(memberId)
    fun searchMembers(query: String): Flow<List<Member>> = memberDao.searchMembers(query)
}
