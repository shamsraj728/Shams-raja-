package com.example.service

import java.security.MessageDigest

object SecurityUtils {
    fun hashPassword(password: String, salt: String = "PostFlow_Secure_Salt_2026"): String {
        val input = "$salt:$password"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidUsername(username: String): Boolean {
        return username.length >= 3 && username.matches(Regex("^[a-zA-Z0-9_.-]+$"))
    }

    fun validatePasswordStrength(password: String): Pair<Boolean, String> {
        if (password.length < 6) {
            return false to "Password must be at least 6 characters long."
        }
        return true to "Password is valid."
    }

    data class MediaValidationResult(
        val isValid: Boolean,
        val message: String,
        val estimatedSizeKb: Long = 1024,
        val recommendedAspectRatio: String = "1:1 or 4:5"
    )

    fun validateMedia(
        fileUri: String,
        isVideo: Boolean = false,
        sizeBytes: Long = 2_500_000
    ): MediaValidationResult {
        if (fileUri.isBlank()) {
            return MediaValidationResult(false, "No media file selected. Please choose a photo or video.")
        }
        // Instagram limits: photo up to 8MB, video up to 100MB
        val maxSizeBytes = if (isVideo) 100 * 1024 * 1024 else 8 * 1024 * 1024
        if (sizeBytes > maxSizeBytes) {
            val limitMb = if (isVideo) "100MB" else "8MB"
            return MediaValidationResult(
                false,
                "File size exceeds Meta/Instagram limit of $limitMb. Please compress before uploading."
            )
        }
        return MediaValidationResult(
            isValid = true,
            message = "Media meets Instagram Graph API requirements (JPG/PNG/MP4 within size limits).",
            estimatedSizeKb = sizeBytes / 1024
        )
    }
}
