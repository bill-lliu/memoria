package com.example.memoria

import androidx.room.*

@Dao
interface UserDao {
    @Insert
    fun insert(user: User) : Long

    @Update
    fun update(user: User)

    @Query("SELECT * FROM users WHERE username = :username")
    fun findOne(username: String) : User

    @Delete
    fun delete(user: User)

}
