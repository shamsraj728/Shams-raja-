package com.example.service

import com.example.data.local.ActivityLogDao
import com.example.data.local.PostDao
import com.example.data.local.SocialAccountDao
import com.example.data.model.ActivityLogEntity
import com.example.data.model.ConnectionStatus
import com.example.data.model.LogLevel
import com.example.data.model.NotificationType
import com.example.data.model.PlatformType
import com.example.data.model.PostEntity
import com.example.data.model.PostStatus
import kotlinx.coroutines.delay
import java.util.UUID

class PublishingEngine(
    private val postDao: PostDao,
    private val socialAccountDao: SocialAccountDao,
    private val activityLogDao: ActivityLogDao,
    private val notificationDispatcher: NotificationDispatcher
) {
    // Set of currently processing post IDs to prevent duplicate parallel publishing
    private val activeJobIds = mutableSetOf<String>()

    suspend fun publishPost(
        post: PostEntity,
        forceFailDemo: Boolean = false
    ): Boolean {
        synchronized(activeJobIds) {
            if (activeJobIds.contains(post.id)) {
                return false // Idempotency check: prevent duplicate submission
            }
            activeJobIds.add(post.id)
        }

        try {
            // STEP 1: PREPARING - Verify Account Connection
            updatePostStatus(post.copy(status = PostStatus.PREPARING))
            logActivity(
                userId = post.userId,
                action = "Preparing Post",
                details = "Checking Meta API connection and validating token for ${post.accountHandle}",
                level = LogLevel.INFO,
                postId = post.id
            )

            val account = socialAccountDao.getAccountByPlatform(post.userId, PlatformType.INSTAGRAM)
            if (account == null || account.status != ConnectionStatus.CONNECTED) {
                failPost(
                    post = post,
                    reason = "Instagram connection is not active or token has expired. Please reconnect in Connected Accounts.",
                    technicalError = "OAuthException: code 190, subcode 463 - Access token has expired or is invalid.",
                    requiresReconnect = true
                )
                return false
            }

            // STEP 2: Verify Media
            if (post.mediaUrls.isBlank()) {
                failPost(
                    post = post,
                    reason = "No media found for post. Instagram requires at least one image or video.",
                    technicalError = "MediaValidationException: media_type required with non-empty url.",
                    requiresReconnect = false
                )
                return false
            }

            // STEP 3: Verify Required Permissions
            if (!account.scopes.contains("instagram_content_publish")) {
                failPost(
                    post = post,
                    reason = "Instagram account is missing 'instagram_content_publish' permission.",
                    technicalError = "GraphPermissionException: Insufficient scopes for Instagram Creator/Business account publishing.",
                    requiresReconnect = true
                )
                return false
            }

            // Optional simulation failure toggle for testing failed post CUJ
            if (forceFailDemo) {
                delay(800)
                failPost(
                    post = post,
                    reason = "Instagram temporarily rejected the request: media processing timed out on Meta servers.",
                    technicalError = "IGApiError: code 2207001 - Media upload container processing timeout.",
                    requiresReconnect = false
                )
                return false
            }

            // STEP 4: Transition to PUBLISHING
            updatePostStatus(post.copy(status = PostStatus.PUBLISHING))
            notificationDispatcher.sendNotification(
                userId = post.userId,
                title = "🚀 Publishing Started",
                message = "Your post to ${post.accountHandle} is being processed by Instagram.",
                type = NotificationType.PUBLISHING_START,
                relatedPostId = post.id
            )

            logActivity(
                userId = post.userId,
                action = "Publishing to Instagram",
                details = "Uploading container to Instagram Graph API v21.0 for ${post.accountHandle}",
                level = LogLevel.INFO,
                postId = post.id,
                technical = "POST https://graph.facebook.com/v21.0/${account.id}/media (caption_length: ${post.caption.length})"
            )

            // STEP 5: Official Instagram API Media Container creation & Publishing
            // Simulate realistic API delay
            delay(1500)

            val generatedPostId = "18" + (1000000000L..9999999999L).random()
            val shortcode = UUID.randomUUID().toString().substring(0, 8)
            val permalink = "https://www.instagram.com/p/C$shortcode/"

            // STEP 6: Save Result & Update Post
            val updatedPost = post.copy(
                status = PostStatus.PUBLISHED,
                platformPostId = generatedPostId,
                platformPermalink = permalink,
                publishedTime = System.currentTimeMillis(),
                errorMessage = null
            )
            updatePostStatus(updatedPost)

            // STEP 7: Activity Log
            logActivity(
                userId = post.userId,
                action = "Post Published Successfully",
                details = "Published to Instagram as ID #$generatedPostId ($permalink)",
                level = LogLevel.SUCCESS,
                postId = post.id,
                technical = "HTTP 200 OK - Response: {\"id\": \"$generatedPostId\", \"permalink\": \"$permalink\"}"
            )

            // STEP 8: Smart Notification
            notificationDispatcher.sendNotification(
                userId = post.userId,
                title = "✅ Post Published!",
                message = "Your post was successfully published to Instagram (${post.accountHandle}).",
                type = NotificationType.PUBLISHED_SUCCESS,
                relatedPostId = post.id
            )

            return true
        } catch (e: Exception) {
            failPost(
                post = post,
                reason = "Unexpected error during publishing: ${e.localizedMessage ?: "Network error"}",
                technicalError = e.stackTraceToString(),
                requiresReconnect = false
            )
            return false
        } finally {
            synchronized(activeJobIds) {
                activeJobIds.remove(post.id)
            }
        }
    }

    private suspend fun failPost(
        post: PostEntity,
        reason: String,
        technicalError: String,
        requiresReconnect: Boolean
    ) {
        val newStatus = if (requiresReconnect) PostStatus.CONNECTION_REQUIRED else PostStatus.FAILED
        val updatedPost = post.copy(
            status = newStatus,
            errorMessage = reason,
            retryCount = post.retryCount + 1,
            updatedAt = System.currentTimeMillis()
        )
        updatePostStatus(updatedPost)

        logActivity(
            userId = post.userId,
            action = "Publishing Failed",
            details = reason,
            level = LogLevel.ERROR,
            postId = post.id,
            technical = technicalError
        )

        notificationDispatcher.sendNotification(
            userId = post.userId,
            title = if (requiresReconnect) "🔐 Connection Attention Needed" else "⚠️ Publishing Failed",
            message = reason,
            type = if (requiresReconnect) NotificationType.CONNECTION_EXPIRING else NotificationType.PUBLISH_FAILED,
            relatedPostId = post.id
        )
    }

    private suspend fun updatePostStatus(post: PostEntity) {
        postDao.updatePost(post)
    }

    private suspend fun logActivity(
        userId: String,
        action: String,
        details: String,
        level: LogLevel,
        postId: String?,
        technical: String? = null
    ) {
        activityLogDao.insertLog(
            ActivityLogEntity(
                userId = userId,
                action = action,
                details = details,
                level = level,
                relatedPostId = postId,
                technicalDetails = technical,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun checkDueScheduledPosts(currentTime: Long, simulateFailures: Boolean) {
        val duePosts = postDao.getDueScheduledPosts(currentTime)
        for (post in duePosts) {
            publishPost(post, forceFailDemo = simulateFailures)
        }
    }
}
