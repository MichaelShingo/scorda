package com.example.scorda.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.scorda.data.database.dao.AnnotationDao
import com.example.scorda.data.database.dao.ComposerDao
import com.example.scorda.data.database.dao.GenreDao
import com.example.scorda.data.database.dao.InstrumentDao
import com.example.scorda.data.database.dao.ScoreDao
import com.example.scorda.data.database.dao.SetlistDao
import com.example.scorda.data.database.dao.TagDao
import com.example.scorda.data.database.entities.AnnotationLayer
import com.example.scorda.data.database.entities.Composer
import com.example.scorda.data.database.entities.Genre
import com.example.scorda.data.database.entities.Instrument
import com.example.scorda.data.database.entities.Score
import com.example.scorda.data.database.entities.ScoreGenreCrossRef
import com.example.scorda.data.database.entities.ScoreInstrumentCrossRef
import com.example.scorda.data.database.entities.ScoreSetlistCrossRef
import com.example.scorda.data.database.entities.ScoreTagCrossRef
import com.example.scorda.data.database.entities.Setlist
import com.example.scorda.data.database.entities.Stroke
import com.example.scorda.data.database.entities.Tag

@Database(
    entities = [
        Score::class,
        Composer::class,
        Genre::class,
        Instrument::class,
        ScoreGenreCrossRef::class,
        ScoreInstrumentCrossRef::class,
        Setlist::class,
        ScoreSetlistCrossRef::class,
        Tag::class,
        ScoreTagCrossRef::class,
        AnnotationLayer::class,
        Stroke::class
    ],
    version = 16,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao
    abstract fun composerDao(): ComposerDao
    abstract fun genreDao(): GenreDao
    abstract fun instrumentDao(): InstrumentDao
    abstract fun setlistDao(): SetlistDao
    abstract fun tagDao(): TagDao
    abstract fun annotationDao(): AnnotationDao

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
