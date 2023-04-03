package com.example.memoria

import androidx.room.*
import org.jetbrains.annotations.NotNull
import java.util.*

@Entity(tableName = "users")
data class User(@PrimaryKey @NotNull val id: String,
                @NotNull val username: String,
                @NotNull val password: String,
                @NotNull val creation_time: String)
