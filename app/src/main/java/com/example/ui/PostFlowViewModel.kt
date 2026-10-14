package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ActivityLogEntity
import com.example.data.model.ConnectionStatus
import com.example.data.model.CropRatio
import com.example.data.model.MediaType
import com.example.data.model.NotificationEntity
import com.example.data.model.NotificationPreferences
import com.example.data.model.PlatformType
import com.example.data.model.PostEntity
import com.example.data.model.PostStatus
import com.example.data.model.ScheduleFrequency
import com.example.data.model.SocialAccountEntity
import com.example.data.model.ThemeMode
import com.example.data.model.UserEntity
import com.example.data.repository.PostFlowRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class PostFlowViewModel(application: Application) : AndroidViewModel(application) {
    val repository = PostFlowRepository(application)

    val activeUserId: StateFlow<String?> = repository.activeUserIdFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val completedOnboarding: StateFlow<Boolean> = repository.completedOnboardingFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val themeMode: StateFlow<ThemeMode> = repository.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    val isDemoMode: StateFlow<Boolean> = repository.isDemoModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val simulateFailures: StateFlow<Boolean> = repository.simulateFailuresFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val notificationPreferences: StateFlow<NotificationPreferences> = repository.notificationPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, NotificationPreferences())

    // Active User Flow
    val currentUser: StateFlow<UserEntity?> = activeUserId.flatMapLatest { userId ->
        if (userId != null) {
            repository.userDao.getFirstUserFlow()
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Posts Flow
    val allPosts: StateFlow<List<PostEntity>> = activeUserId.flatMapLatest { userId ->
        if (userId != null) repository.postDao.getAllPostsFlow(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Next scheduled post
    val nextScheduledPost: StateFlow<PostEntity?> = activeUserId.flatMapLatest { userId ->
        if (userId != null) repository.postDao.getNextScheduledPostFlow(userId) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Accounts
    val socialAccounts: StateFlow<List<SocialAccountEntity>> = activeUserId.flatMapLatest { userId ->
        if (userId != null) repository.socialAccountDao.getAccountsByUserFlow(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val instagramAccount: StateFlow<SocialAccountEntity?> = activeUserId.flatMapLatest { userId ->
        if (userId != null) repository.socialAccountDao.getInstagramAccountFlow(userId) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Notifications
    val notifications: StateFlow<List<NotificationEntity>> = activeUserId.flatMapLatest { userId ->
        if (userId != null) repository.notificationDao.getNotificationsFlow(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val unreadNotificationsCount: StateFlow<Int> = activeUserId.flatMapLatest { userId ->
        if (userId != null) repository.notificationDao.getUnreadCountFlow(userId) else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // Activity Logs
    val activityLogs: StateFlow<List<ActivityLogEntity>> = activeUserId.flatMapLatest { userId ->
        if (userId != null) repository.activityLogDao.getActivityLogsFlow(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Metrics
    val scheduledCount: StateFlow<Int> = allPosts.combine(flowOf(Unit)) { posts, _ ->
        posts.count { it.status == PostStatus.SCHEDULED }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val publishedCount: StateFlow<Int> = allPosts.combine(flowOf(Unit)) { posts, _ ->
        posts.count { it.status == PostStatus.PUBLISHED }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val draftsCount: StateFlow<Int> = allPosts.combine(flowOf(Unit)) { posts, _ ->
        posts.count { it.status == PostStatus.DRAFT }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val failedCount: StateFlow<Int> = allPosts.combine(flowOf(Unit)) { posts, _ ->
        posts.count { it.status == PostStatus.FAILED || it.status == PostStatus.CONNECTION_REQUIRED }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // UI feedback message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        // Start background publishing watcher loop
        startBackgroundPublishingLoop()
    }

    private fun startBackgroundPublishingLoop() {
        viewModelScope.launch {
            while (isActive) {
                delay(8000) // check every 8 seconds
                try {
                    val currentUserId = activeUserId.value
                    if (currentUserId != null) {
                        repository.publishingEngine.checkDueScheduledPosts(
                            currentTime = System.currentTimeMillis(),
                            simulateFailures = simulateFailures.value
                        )
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    // Auth actions
    fun signUp(username: String, email: String, pass: String, fullName: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.signUp(username, email, pass, fullName)
            result.fold(
                onSuccess = { onResult(true, null) },
                onFailure = { onResult(false, it.message) }
            )
        }
    }

    fun logIn(identifier: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.logIn(identifier, pass)
            result.fold(
                onSuccess = { onResult(true, null) },
                onFailure = { onResult(false, it.message) }
            )
        }
    }

    fun logInWithGoogle(email: String, displayName: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.logInWithGoogle(email, displayName)
            result.fold(
                onSuccess = { onResult(true, null) },
                onFailure = { onResult(false, it.message) }
            )
        }
    }

    fun logOut() {
        viewModelScope.launch {
            repository.logOut()
        }
    }

    fun resetPassword(email: String, newPass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.resetPassword(email, newPass)
            result.fold(
                onSuccess = { onResult(true, null) },
                onFailure = { onResult(false, it.message) }
            )
        }
    }

    fun updateProfile(username: String, email: String, fullName: String, bio: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateProfile(user.id, username, email, fullName, bio)
            showMessage("Profile updated successfully")
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.setCompletedOnboarding(true)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setDemoMode(isDemo: Boolean) {
        viewModelScope.launch {
            repository.setDemoMode(isDemo)
        }
    }

    fun setSimulateFailures(simulate: Boolean) {
        viewModelScope.launch {
            repository.setSimulateFailures(simulate)
            showMessage(if (simulate) "Failure simulation enabled" else "Normal publishing enabled")
        }
    }

    fun updateNotificationPreferences(
        whatsapp: Boolean,
        phone: String,
        email: Boolean,
        daily: Boolean
    ) {
        viewModelScope.launch {
            repository.updateNotificationPreferences(whatsapp, phone, email, daily)
            showMessage("Notification preferences saved")
        }
    }

    // Social Account Management
    fun toggleAccountConnection(account: SocialAccountEntity) {
        viewModelScope.launch {
            val newStatus = if (account.status == ConnectionStatus.CONNECTED) {
                ConnectionStatus.DISCONNECTED
            } else {
                ConnectionStatus.CONNECTED
            }
            val updated = account.copy(
                status = newStatus,
                tokenExpiry = if (newStatus == ConnectionStatus.CONNECTED) System.currentTimeMillis() + 60L * 24 * 3600 * 1000 else 0L
            )
            repository.socialAccountDao.updateAccount(updated)
            showMessage(
                if (newStatus == ConnectionStatus.CONNECTED)
                    "${account.accountName} connected successfully"
                else
                    "${account.accountName} disconnected"
            )
        }
    }

    fun reconnectAccount(account: SocialAccountEntity) {
        viewModelScope.launch {
            val updated = account.copy(
                status = ConnectionStatus.CONNECTED,
                tokenExpiry = System.currentTimeMillis() + 60L * 24 * 3600 * 1000
            )
            repository.socialAccountDao.updateAccount(updated)
            showMessage("Official Meta OAuth token refreshed for ${account.accountHandle}")
        }
    }

    // Post actions
    fun createOrUpdatePost(
        postId: String?,
        caption: String,
        hashtags: String,
        location: String,
        mediaUrl: String,
        mediaType: MediaType,
        cropRatio: CropRatio,
        rotationDegrees: Int,
        publishNow: Boolean,
        scheduledTimestamp: Long,
        frequency: ScheduleFrequency,
        onComplete: () -> Unit
    ) {
        val user = currentUser.value ?: return
        val igAcc = instagramAccount.value
        val accHandle = igAcc?.accountHandle ?: "@user_creator"
        val accId = igAcc?.id ?: ""

        viewModelScope.launch {
            val post = PostEntity(
                id = postId ?: UUID.randomUUID().toString(),
                userId = user.id,
                caption = caption,
                hashtags = hashtags,
                location = location,
                mediaUrls = mediaUrl,
                mediaType = mediaType,
                cropRatio = cropRatio,
                rotationDegrees = rotationDegrees,
                accountId = accId,
                accountHandle = accHandle,
                platform = "Instagram",
                status = if (publishNow) PostStatus.PREPARING else (if (scheduledTimestamp > 0) PostStatus.SCHEDULED else PostStatus.DRAFT),
                scheduledTime = if (publishNow) System.currentTimeMillis() else scheduledTimestamp,
                scheduleFrequency = frequency,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            repository.savePost(post)

            if (publishNow) {
                showMessage("Publishing to Instagram...")
                launch {
                    val success = repository.publishNow(post.id)
                    if (success) {
                        showMessage("Post published to Instagram successfully!")
                    } else {
                        showMessage("Publishing failed. Check Notification Center.")
                    }
                }
            } else if (scheduledTimestamp > 0) {
                showMessage("Post scheduled for ${post.accountHandle}")
            } else {
                showMessage("Post saved as draft")
            }

            onComplete()
        }
    }

    fun publishNow(postId: String) {
        viewModelScope.launch {
            showMessage("Publishing post...")
            val success = repository.publishNow(postId)
            if (success) {
                showMessage("Post published successfully!")
            } else {
                showMessage("Publishing failed. View details in Posts or Notifications.")
            }
        }
    }

    fun retryPost(postId: String) {
        viewModelScope.launch {
            showMessage("Retrying publish to Instagram...")
            val success = repository.retryFailedPost(postId)
            if (success) {
                showMessage("Post retry published successfully!")
            } else {
                showMessage("Retry failed. Check account connection.")
            }
        }
    }

    fun duplicatePost(postId: String) {
        viewModelScope.launch {
            val copy = repository.duplicatePost(postId)
            if (copy != null) {
                showMessage("Post duplicated as Draft")
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
            showMessage("Post removed")
        }
    }

    // Notifications actions
    fun markAllNotificationsRead() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.notificationDao.markAllAsRead(user.id)
            showMessage("All notifications marked as read")
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.notificationDao.deleteNotificationById(id)
        }
    }

    fun clearAllNotifications() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.notificationDao.clearAllNotifications(user.id)
            showMessage("Notification center cleared")
        }
    }
}
