package com.example.memoria

import androidx.room.*

@Dao
interface PostDao {
    @Insert
    fun insert(post: Post)

    @Update
    fun update(post: Post)

    @Query("SELECT * FROM posts")
    fun loadPosts() : List<Post>

    @Delete
    fun delete(post: Post)

}
