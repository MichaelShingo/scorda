package com.example.scorda.data.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.scorda.data.database.dao.ComposerDao
import com.example.scorda.data.database.dao.GenreDao
import com.example.scorda.data.database.dao.InstrumentDao
import com.example.scorda.data.database.dao.ScoreDao
import com.example.scorda.data.database.entities.Composer
import com.example.scorda.data.database.entities.Genre
import com.example.scorda.data.database.entities.Instrument
import com.example.scorda.data.database.entities.Score
import com.example.scorda.data.database.entities.ScoreGenreCrossRef
import com.example.scorda.data.database.entities.ScoreInstrumentCrossRef

@Database(
    entities = [
        Score::class,
        Composer::class,
        Genre::class,
        Instrument::class,
        ScoreGenreCrossRef::class,
        ScoreInstrumentCrossRef::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao
    abstract fun composerDao(): ComposerDao
    abstract fun genreDao(): GenreDao
    abstract fun instrumentDao(): InstrumentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scorda_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}