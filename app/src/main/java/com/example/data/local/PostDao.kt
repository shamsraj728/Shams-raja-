package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PostEntity
import com.example.data.model.PostStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllPostsFlow(userId: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE userId = :userId AND status = :status ORDER BY scheduledTime ASC")
    fun getPostsByStatusFlow(userId: String, status: PostStatus): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :postId LIMIT 1")
    suspend fun getPostById(postId: String): PostEntity?

    @Query("SELECT * FROM posts WHERE id = :postId LIMIT 1")
    fun getPostByIdFlow(postId: String): Flow<PostEntity?>

    @Query("SELECT * FROM posts WHERE userId = :userId AND status = 'SCHEDULED' ORDER BY scheduledTime ASC LIMIT 1")
    fun getNextScheduledPostFlow(userId: String): Flow<PostEntity?>

    @Query("SELECT * FROM posts WHERE status = 'SCHEDULED' AND scheduledTime <= :currentTime")
    suspend fun getDueScheduledPosts(currentTime: Long): List<PostEntity>

    @Query("SELECT COUNT(*) FROM posts WHERE userId = :userId AND status = :status")
    fun getCountByStatusFlow(userId: String, status: PostStatus): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Delete
    suspend fun deletePost(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePostById(postId: String)
}
