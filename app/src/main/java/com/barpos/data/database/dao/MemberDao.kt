package com.barpos.data.database.dao

import androidx.room.*
import com.barpos.data.database.entity.Member
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members WHERE isActive = 1 ORDER BY name")
    fun getActiveMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members ORDER BY name")
    fun getAllMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getById(id: Long): Member?

    @Insert
    suspend fun insert(member: Member): Long

    @Update
    suspend fun update(member: Member)

    @Query("UPDATE members SET balance = balance - :amount WHERE id = :memberId")
    suspend fun subtractBalance(memberId: Long, amount: Double)

    @Query("UPDATE members SET balance = 0 WHERE id = :memberId")
    suspend fun resetBalance(memberId: Long)

    @Query("SELECT * FROM members WHERE name LIKE '%' || :query || '%' AND isActive = 1 ORDER BY name")
    fun searchMembers(query: String): Flow<List<Member>>
}
