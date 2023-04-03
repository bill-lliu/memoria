package com.example.memoria

import androidx.room.*
import org.jetbrains.annotations.NotNull
import java.util.*

@Entity(tableName = "users")
data class User(@PrimaryKey @NotNull val id: String,
                @NotNull val username: String,
                @NotNull val password: String,
                @NotNull val creation_time: String)
@Dao
interface UserDAO {
    @Insert
    fun insert(user: User)

    @Update
    fun update(user: User)

    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    fun findOne(username: String, password: String) : User

    @Delete
    fun delete(user: User)

}

@Database(entities = [User::class], version = 1)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao() : UserDAO
}