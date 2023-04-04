package com.example.memoria

import androidx.room.*

@Entity(tableName = "users")
data class User(@PrimaryKey(autoGenerate = true) val id: Int?,
                val username: String,
                val password: String,
                val creation_time: String)
