package com.example.memoria

import android.graphics.Bitmap
import androidx.room.*

@Entity(tableName = "posts")
data class Post(@PrimaryKey(autoGenerate = true) val id: Int?,
                val title: String,
                val description: String,
                val creation_time: String,
                val tags: String,
                var picturePath: String
)
