package com.example.memoria

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

@Database(
    entities = [User::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getUserDao() : UserDao
    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }

        private fun buildDatabase(appContext: Context): AppDatabase {
            val filesDir = appContext.getExternalFilesDir(null)
            val dataDir = File(filesDir, "data")
            if (!dataDir.exists())
                dataDir.mkdir()

            val builder =
                Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    File(dataDir, "MemoriaDB.db").toString()
                ).allowMainThreadQueries()

            return builder.build()
        }
    }

}


