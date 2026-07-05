package com.example.scorda.data.repository

import com.example.scorda.data.database.AppDatabase
import com.example.scorda.data.database.entities.Instrument
import kotlinx.coroutines.flow.Flow

class InstrumentRepository(
    private val db: AppDatabase
) {
    private val instrumentDao = db.instrumentDao()

    fun observeInstruments(): Flow<List<Instrument>> = instrumentDao.getAllInstruments()

    fun observeInstrument(id: Long): Flow<Instrument?> = instrumentDao.getInstrumentById(id)

    fun searchInstruments(query: String): Flow<List<Instrument>> =
        instrumentDao.searchInstruments(query)

    suspend fun insertInstrument(instrument: Instrument) = instrumentDao.insert(instrument)

    suspend fun updateInstrument(instrument: Instrument) {
        val updatedInstrument = instrument.copy(updatedAt = System.currentTimeMillis())
        instrumentDao.update(updatedInstrument)
    }

    suspend fun deleteInstrument(instrument: Instrument) = instrumentDao.delete(instrument)
}