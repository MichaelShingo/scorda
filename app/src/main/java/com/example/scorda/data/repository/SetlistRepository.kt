package com.example.scorda.data.repository

import com.example.scorda.data.database.AppDatabase
import com.example.scorda.data.database.entities.Setlist
import com.example.scorda.data.database.relations.SetlistWithDetails
import kotlinx.coroutines.flow.Flow

class SetlistRepository(
    private val db: AppDatabase
) {
    val setlistDao = db.setlistDao()

    fun observeSetlists(): Flow<List<Setlist>> = setlistDao.getAllSetlists()

    fun observeSetlist(id: Long): Flow<SetlistWithDetails> = setlistDao.getSetlist(id)

    fun searchSetlists(query: String): Flow<List<Setlist>> = setlistDao.searchSetlists(query)

    suspend fun insertSetlist(setlist: Setlist) = setlistDao.insert(setlist)

    suspend fun updateSetlist(setlist: Setlist) {
        val updatedSetlist = setlist.copy(updatedAt = System.currentTimeMillis())
        setlistDao.update(updatedSetlist)
    }

    suspend fun deleteSetlist(setlist: Setlist) = setlistDao.delete(setlist)


}