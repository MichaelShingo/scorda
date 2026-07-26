package com.example.scorda.data.database

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.scorda.data.database.dao.AnnotationDao
import com.example.scorda.data.database.dao.BrushDao
import com.example.scorda.data.database.dao.ComposerDao
import com.example.scorda.data.database.dao.GenreDao
import com.example.scorda.data.database.dao.InstrumentDao
import com.example.scorda.data.database.dao.ScoreDao
import com.example.scorda.data.database.dao.SetlistDao
import com.example.scorda.data.database.dao.TagDao
import com.example.scorda.data.database.entities.AnnotationLayer
import com.example.scorda.data.database.entities.Brush
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        Stroke::class,
        Brush::class
    ],
    version = 13,
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
    abstract fun brushDao(): BrushDao

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
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    seedDefaultBrushes(database.brushDao())
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDefaultBrushes(brushDao: BrushDao) {
            val defaults = listOf(
                Brush(name = "Thin Black", color = Color.Black.toArgb(), thickness = 2f, order = 0),
                Brush(name = "Thin Red", color = Color.Red.toArgb(), thickness = 2f, order = 1),
                Brush(name = "Thin Blue", color = Color.Blue.toArgb(), thickness = 2f, order = 2),
                Brush(
                    name = "Highlighter",
                    color = Color.Yellow.copy(alpha = 0.3f).toArgb(),
                    thickness = 20f,
                    order = 3
                )
            )
            defaults.forEach { brushDao.insertBrush(it) }
        }
    }
}
