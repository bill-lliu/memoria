package com.example.memoria

import androidx.room.*

@Dao
interface UserDao {
    @Insert
    fun insert(user: User)

    @Update
    fun update(user: User)

    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    fun findOne(username: String, password: String) : User

    @Delete
    fun delete(user: User)

}
