package com.nidcard.app.data.dao

import androidx.room.*
import com.nidcard.app.data.entity.NIDCard
import kotlinx.coroutines.flow.Flow

@Dao
interface NIDCardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nidCard: NIDCard): Long

    @Update
    suspend fun update(nidCard: NIDCard)

    @Delete
    suspend fun delete(nidCard: NIDCard)

    @Query("DELETE FROM nid_cards WHERE nid = :nid")
    suspend fun deleteByNid(nid: String)

    @Query("DELETE FROM nid_cards")
    suspend fun deleteAll()

    @Query("SELECT * FROM nid_cards WHERE nid = :nid LIMIT 1")
    suspend fun getByNid(nid: String): NIDCard?

    @Query("SELECT * FROM nid_cards WHERE pin = :pin LIMIT 1")
    suspend fun getByPin(pin: String): NIDCard?

    @Query("SELECT * FROM nid_cards WHERE nid = :nid OR pin = :nid LIMIT 1")
    suspend fun searchByNidOrPin(nid: String): NIDCard?

    @Query("SELECT * FROM nid_cards ORDER BY createdAt DESC")
    fun getAllCards(): Flow<List<NIDCard>>

    @Query("SELECT * FROM nid_cards ORDER BY createdAt DESC")
    suspend fun getAllCardsList(): List<NIDCard>

    @Query("SELECT COUNT(*) FROM nid_cards")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM nid_cards WHERE createdAt LIKE :todayPattern")
    suspend fun getTodayCount(todayPattern: String): Int

    @Query("SELECT * FROM nid_cards WHERE nameBn LIKE '%' || :query || '%' OR nameEn LIKE '%' || :query || '%' OR nid LIKE '%' || :query || '%' OR pin LIKE '%' || :query || '%' OR father LIKE '%' || :query || '%' OR mother LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%'")
    fun searchCards(query: String): Flow<List<NIDCard>>

    @Query("SELECT * FROM nid_cards WHERE id = :id")
    suspend fun getById(id: Long): NIDCard?

    @Query("DELETE FROM nid_cards WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
