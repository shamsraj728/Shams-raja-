package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PlatformType
import com.example.data.model.SocialAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialAccountDao {
    @Query("SELECT * FROM social_accounts WHERE userId = :userId ORDER BY platform ASC")
    fun getAccountsByUserFlow(userId: String): Flow<List<SocialAccountEntity>>

    @Query("SELECT * FROM social_accounts WHERE userId = :userId AND platform = :platform LIMIT 1")
    suspend fun getAccountByPlatform(userId: String, platform: PlatformType): SocialAccountEntity?

    @Query("SELECT * FROM social_accounts WHERE userId = :userId AND platform = 'INSTAGRAM' LIMIT 1")
    fun getInstagramAccountFlow(userId: String): Flow<SocialAccountEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: SocialAccountEntity)

    @Update
    suspend fun updateAccount(account: SocialAccountEntity)

    @Delete
    suspend fun deleteAccount(account: SocialAccountEntity)

    @Query("DELETE FROM social_accounts WHERE id = :accountId")
    suspend fun deleteAccountById(accountId: String)
}
