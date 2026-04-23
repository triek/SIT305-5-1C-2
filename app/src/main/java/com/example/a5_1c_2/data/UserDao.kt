package com.example.a5_1c_2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    @Query("UPDATE users SET lastPlayedUrl = :videoUrl WHERE id = :userId")
    suspend fun updateLastPlayedUrl(userId: Long, videoUrl: String)

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): User?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addPlaylistItem(item: PlaylistItem): Long

    @Query("SELECT * FROM playlist_items WHERE userId = :userId ORDER BY addedAtMillis DESC")
    suspend fun getPlaylistItemsForUser(userId: Long): List<PlaylistItem>
}
