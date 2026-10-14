package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.PostFlowDatabase
import com.example.data.model.ActivityLogEntity
import com.example.data.model.ConnectionStatus
import com.example.data.model.CropRatio
import com.example.data.model.LogLevel
import com.example.data.model.MediaType
import com.example.data.model.NotificationEntity
import com.example.data.model.NotificationPreferences
import com.example.data.model.NotificationType
import com.example.data.model.PlatformType
import com.example.data.model.PostEntity
import com.example.data.model.PostStatus
import com.example.data.model.ScheduleFrequency
import com.example.data.model.SocialAccountEntity
import com.example.data.model.ThemeMode
import com.example.data.model.UserEntity
import com.example.service.NotificationDispatcher
import com.example.service.PublishingEngine
import com.example.service.SecurityUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "postflow_settings")

class PostFlowRepository(private val context: Context) {
    private val db = PostFlowDatabase.getDatabase(context)
    val userDao = db.userDao()
    val postDao = db.postDao()
    val socialAccountDao = db.socialAccountDao()
    val notificationDao = db.notificationDao()
    val activityLogDao = db.activityLogDao()

    val notificationDispatcher = NotificationDispatcher(context, notificationDao)
    val publishingEngine = PublishingEngine(postDao, socialAccountDao, activityLogDao, notificationDispatcher)

    // DataStore preference keys
    private object PreferencesKeys {
        val ACTIVE_USER_ID = stringPreferencesKey("active_user_id")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val COMPLETED_ONBOARDING = booleanPreferencesKey("completed_onboarding")
        val IS_DEMO_MODE = booleanPreferencesKey("is_demo_mode")
        val SIMULATE_FAILURES = booleanPreferencesKey("simulate_failures")
        val WHATSAPP_NOTIF = booleanPreferencesKey("whatsapp_notif")
        val WHATSAPP_PHONE = stringPreferencesKey("whatsapp_phone")
        val EMAIL_NOTIF = booleanPreferencesKey("email_notif")
        val DAILY_SUMMARY = booleanPreferencesKey("daily_summary")
    }

    val activeUserIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.ACTIVE_USER_ID]
    }

    val completedOnboardingFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.COMPLETED_ONBOARDING] ?: false
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[PreferencesKeys.THEME_MODE]) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    val isDemoModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.IS_DEMO_MODE] ?: true
    }

    val simulateFailuresFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SIMULATE_FAILURES] ?: false
    }

    val notificationPreferencesFlow: Flow<NotificationPreferences> = context.dataStore.data.map { prefs ->
        NotificationPreferences(
            whatsappEnabled = prefs[PreferencesKeys.WHATSAPP_NOTIF] ?: false,
            whatsappPhoneNumber = prefs[PreferencesKeys.WHATSAPP_PHONE] ?: "",
            emailNotificationsEnabled = prefs[PreferencesKeys.EMAIL_NOTIF] ?: true,
            dailySummaryEnabled = prefs[PreferencesKeys.DAILY_SUMMARY] ?: true
        )
    }

    suspend fun setActiveUserId(userId: String?) {
        context.dataStore.edit { prefs ->
            if (userId != null) {
                prefs[PreferencesKeys.ACTIVE_USER_ID] = userId
            } else {
                prefs.remove(PreferencesKeys.ACTIVE_USER_ID)
            }
        }
    }

    suspend fun setCompletedOnboarding(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.COMPLETED_ONBOARDING] = completed
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setDemoMode(isDemo: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_DEMO_MODE] = isDemo
        }
    }

    suspend fun setSimulateFailures(simulate: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SIMULATE_FAILURES] = simulate
        }
    }

    suspend fun updateNotificationPreferences(
        whatsappEnabled: Boolean,
        whatsappPhone: String,
        emailEnabled: Boolean,
        dailySummary: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.WHATSAPP_NOTIF] = whatsappEnabled
            prefs[PreferencesKeys.WHATSAPP_PHONE] = whatsappPhone
            prefs[PreferencesKeys.EMAIL_NOTIF] = emailEnabled
            prefs[PreferencesKeys.DAILY_SUMMARY] = dailySummary
        }
    }

    // --- Authentication Operations ---
    suspend fun signUp(
        username: String,
        email: String,
        password: String,
        fullName: String = ""
    ): Result<UserEntity> {
        val cleanEmail = email.trim().lowercase()
        val cleanUsername = username.trim()

        if (!SecurityUtils.isValidEmail(cleanEmail)) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (!SecurityUtils.isValidUsername(cleanUsername)) {
            return Result.failure(IllegalArgumentException("Username must be at least 3 characters (letters, numbers, underscores)."))
        }
        val (isValidPass, passError) = SecurityUtils.validatePasswordStrength(password)
        if (!isValidPass) {
            return Result.failure(IllegalArgumentException(passError))
        }

        if (userDao.getUserByEmail(cleanEmail) != null) {
            return Result.failure(IllegalArgumentException("An account with this email already exists."))
        }
        if (userDao.getUserByUsername(cleanUsername) != null) {
            return Result.failure(IllegalArgumentException("This username is already taken."))
        }

        val newUser = UserEntity(
            username = cleanUsername,
            email = cleanEmail,
            fullName = if (fullName.isNotBlank()) fullName else cleanUsername.replaceFirstChar { it.uppercase() },
            passwordHash = SecurityUtils.hashPassword(password)
        )
        userDao.insertUser(newUser)
        setActiveUserId(newUser.id)

        // Seed initial connected accounts and sample scheduled post
        seedInitialUserData(newUser)

        return Result.success(newUser)
    }

    suspend fun logIn(identifier: String, password: String): Result<UserEntity> {
        val cleanIdentifier = identifier.trim().lowercase()
        val user = userDao.getUserByIdentifier(cleanIdentifier)
            ?: return Result.failure(IllegalArgumentException("No user found with '$cleanIdentifier'."))

        val inputHash = SecurityUtils.hashPassword(password)
        if (user.passwordHash != inputHash) {
            return Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }

        setActiveUserId(user.id)
        return Result.success(user)
    }

    suspend fun logInWithGoogle(email: String, displayName: String): Result<UserEntity> {
        val cleanEmail = email.trim().lowercase()
        var user = userDao.getUserByEmail(cleanEmail)
        if (user == null) {
            val username = cleanEmail.substringBefore("@").replace(".", "_") + "_" + (100..999).random()
            user = UserEntity(
                username = username,
                email = cleanEmail,
                fullName = displayName,
                passwordHash = SecurityUtils.hashPassword(UUID.randomUUID().toString())
            )
            userDao.insertUser(user)
            seedInitialUserData(user)
        }
        setActiveUserId(user.id)
        return Result.success(user)
    }

    suspend fun logOut() {
        setActiveUserId(null)
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<Unit> {
        val cleanEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("No account registered with $cleanEmail."))

        val (isValidPass, passError) = SecurityUtils.validatePasswordStrength(newPassword)
        if (!isValidPass) return Result.failure(IllegalArgumentException(passError))

        val updated = user.copy(passwordHash = SecurityUtils.hashPassword(newPassword))
        userDao.updateUser(updated)
        return Result.success(Unit)
    }

    suspend fun updateProfile(
        userId: String,
        newUsername: String,
        newEmail: String,
        newFullName: String,
        newBio: String
    ): Result<UserEntity> {
        val user = userDao.getUserById(userId)
            ?: return Result.failure(IllegalArgumentException("User not found."))

        val updated = user.copy(
            username = newUsername.trim(),
            email = newEmail.trim().lowercase(),
            fullName = newFullName.trim(),
            bio = newBio.trim()
        )
        userDao.updateUser(updated)
        return Result.success(updated)
    }

    // --- Seed Data for Instant Usability ---
    suspend fun seedInitialUserData(user: UserEntity) {
        // Connect default Instagram creator account (in Demo/Official Ready mode)
        val igAccount = SocialAccountEntity(
            userId = user.id,
            platform = PlatformType.INSTAGRAM,
            accountName = "${user.fullName} Studio",
            accountHandle = "@${user.username}_official",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            status = ConnectionStatus.CONNECTED,
            accessToken = "IGQVJ_DEMO_TOKEN_" + UUID.randomUUID().toString().take(12),
            tokenExpiry = System.currentTimeMillis() + 60L * 24 * 3600 * 1000,
            isDemoAccount = true
        )
        socialAccountDao.insertAccount(igAccount)

        val fbAccount = SocialAccountEntity(
            userId = user.id,
            platform = PlatformType.FACEBOOK_PAGE,
            accountName = "${user.fullName} Page",
            accountHandle = "@${user.username}page",
            status = ConnectionStatus.DISCONNECTED,
            isDemoAccount = true
        )
        socialAccountDao.insertAccount(fbAccount)

        // Seed 1 scheduled post for today/tomorrow
        val futureTime = System.currentTimeMillis() + (3 * 3600 * 1000) // 3 hours from now
        val samplePost = PostEntity(
            userId = user.id,
            caption = "Exciting launch day! ✨ Effortlessly scheduled and managed with PostFlow. Create, Schedule, and Publish with total peace of mind.",
            hashtags = "#PostFlow #ContentCreator #SocialMediaManager #Productivity #Reels",
            location = "San Francisco, California",
            mediaUrls = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800",
            mediaType = MediaType.IMAGE,
            cropRatio = CropRatio.SQUARE,
            accountId = igAccount.id,
            accountHandle = igAccount.accountHandle,
            platform = "Instagram",
            status = PostStatus.SCHEDULED,
            scheduledTime = futureTime,
            scheduleFrequency = ScheduleFrequency.ONCE
        )
        postDao.insertPost(samplePost)

        // Seed initial notifications & activity
        notificationDispatcher.sendNotification(
            userId = user.id,
            title = "🎉 Welcome to PostFlow!",
            message = "Your social command center is ready. Instagram account connected.",
            type = NotificationType.SYSTEM_UPDATE
        )

        notificationDispatcher.sendNotification(
            userId = user.id,
            title = "⏰ Post Scheduled",
            message = "Your next post to ${igAccount.accountHandle} is scheduled for today.",
            type = NotificationType.SCHEDULED,
            relatedPostId = samplePost.id
        )

        activityLogDao.insertLog(
            ActivityLogEntity(
                userId = user.id,
                action = "Account Initialized",
                details = "PostFlow profile and Instagram Creator connection configured.",
                level = LogLevel.SUCCESS
            )
        )
    }

    // --- Post CRUD Operations ---
    suspend fun savePost(post: PostEntity) {
        postDao.insertPost(post)
        if (post.status == PostStatus.SCHEDULED) {
            notificationDispatcher.sendNotification(
                userId = post.userId,
                title = "⏰ Post Scheduled",
                message = "Post scheduled for ${post.accountHandle}.",
                type = NotificationType.SCHEDULED,
                relatedPostId = post.id
            )
            activityLogDao.insertLog(
                ActivityLogEntity(
                    userId = post.userId,
                    action = "Post Scheduled",
                    details = "Scheduled for publish at ${post.scheduledTime}",
                    level = LogLevel.INFO,
                    relatedPostId = post.id
                )
            )
        }
    }

    suspend fun deletePost(postId: String) {
        postDao.deletePostById(postId)
    }

    suspend fun duplicatePost(postId: String): PostEntity? {
        val original = postDao.getPostById(postId) ?: return null
        val copy = original.copy(
            id = UUID.randomUUID().toString(),
            status = PostStatus.DRAFT,
            platformPostId = null,
            platformPermalink = null,
            errorMessage = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        postDao.insertPost(copy)
        return copy
    }

    suspend fun publishNow(postId: String): Boolean {
        val post = postDao.getPostById(postId) ?: return false
        val simulateFailures = simulateFailuresFlow.first()
        return publishingEngine.publishPost(post, forceFailDemo = simulateFailures)
    }

    suspend fun retryFailedPost(postId: String): Boolean {
        val post = postDao.getPostById(postId) ?: return false
        return publishingEngine.publishPost(post, forceFailDemo = false)
    }
}
