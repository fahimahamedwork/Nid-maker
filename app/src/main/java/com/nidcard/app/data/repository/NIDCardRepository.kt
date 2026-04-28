package com.nidcard.app.data.repository

import com.nidcard.app.data.dao.NIDCardDao
import com.nidcard.app.data.entity.NIDCard
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class NIDCardRepository(private val dao: NIDCardDao) {

    suspend fun insert(card: NIDCard): Long = dao.insert(card)

    suspend fun update(card: NIDCard) = dao.update(card)

    suspend fun delete(card: NIDCard) = dao.delete(card)

    suspend fun deleteByNid(nid: String) = dao.deleteByNid(nid)

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun deleteByIds(ids: List<Long>) = dao.deleteByIds(ids)

    suspend fun getByNid(nid: String): NIDCard? = dao.getByNid(nid)

    suspend fun getByPin(pin: String): NIDCard? = dao.getByPin(pin)

    suspend fun searchByNidOrPin(query: String): NIDCard? = dao.searchByNidOrPin(query)

    fun getAllCards(): Flow<List<NIDCard>> = dao.getAllCards()

    suspend fun getAllCardsList(): List<NIDCard> = dao.getAllCardsList()

    fun getTotalCount(): Flow<Int> = dao.getTotalCount()

    suspend fun getTodayCount(): Int {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return dao.getTodayCount("$today%")
    }

    fun searchCards(query: String): Flow<List<NIDCard>> = dao.searchCards(query)

    suspend fun getById(id: Long): NIDCard? = dao.getById(id)
}
