package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ConnectionStatus
import com.example.data.model.PostEntity
import com.example.ui.PostFlowViewModel
import com.example.ui.components.DemoModeBanner
import com.example.ui.components.PostCard
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.VioletSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: PostFlowViewModel,
    onNavigate: (Screen) -> Unit,
    onPreviewPost: (PostEntity) -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val isDemo by viewModel.isDemoMode.collectAsState()
    val igAccount by viewModel.instagramAccount.collectAsState()
    val scheduledCount by viewModel.scheduledCount.collectAsState()
    val publishedCount by viewModel.publishedCount.collectAsState()
    val draftsCount by viewModel.draftsCount.collectAsState()
    val failedCount by viewModel.failedCount.collectAsState()
    val nextPost by viewModel.nextScheduledPost.collectAsState()
    val recentPosts by viewModel.allPosts.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            DemoModeBanner(isDemoMode = isDemo)
        }

        // Welcome & Connected Account Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Hello, ${user?.fullName?.ifBlank { user?.username } ?: "Creator"} 👋",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your social media automation is active and running.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Account Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(Screen.ACCOUNTS) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCAF45))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "IG",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = igAccount?.accountHandle ?: "@instagram_account",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (igAccount?.status == ConnectionStatus.CONNECTED) "Official Meta API Connected" else "Connection Required",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (igAccount?.status == ConnectionStatus.CONNECTED) SuccessGreen else ErrorRed
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Filled.Link,
                                contentDescription = "Manage account",
                                tint = IndigoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Metrics Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Scheduled",
                        count = scheduledCount,
                        icon = Icons.Filled.Schedule,
                        accentColor = IndigoPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.POSTS) }
                    )
                    StatCard(
                        title = "Published",
                        count = publishedCount,
                        icon = Icons.Filled.CheckCircle,
                        accentColor = SuccessGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.POSTS) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Drafts",
                        count = draftsCount,
                        icon = Icons.Filled.Folder,
                        accentColor = SlateMuted,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.POSTS) }
                    )
                    StatCard(
                        title = "Failed",
                        count = failedCount,
                        icon = Icons.Filled.Error,
                        accentColor = ErrorRed,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.POSTS) }
                    )
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onNavigate(Screen.CREATE) },
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("dashboard_create_post_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Create Post", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onNavigate(Screen.CALENDAR) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_calendar_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Calendar", fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = { onNavigate(Screen.ACCOUNTS) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_accounts_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Accounts", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Next Scheduled Post Highlight
        item {
            Column {
                Text(
                    text = "Next Scheduled Post",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (nextPost != null) {
                    val p = nextPost!!
                    val dateFormatted = remember(p.scheduledTime) {
                        SimpleDateFormat("EEE, MMM d 'at' h:mm a", Locale.getDefault()).format(Date(p.scheduledTime))
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPreviewPost(p) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = p.platform,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = IndigoPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = " • $dateFormatted",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusBadge(status = p.status)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (p.mediaUrls.isNotBlank()) {
                                    AsyncImage(
                                        model = p.mediaUrls,
                                        contentDescription = "Next post media",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Text(
                                    text = p.caption.ifBlank { "No caption" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.publishNow(p.id) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Publish Now", fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onPreviewPost(p) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                                ) {
                                    Text("View Details", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No posts currently scheduled",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onNavigate(Screen.CREATE) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                            ) {
                                Text("Schedule First Post")
                            }
                        }
                    }
                }
            }
        }

        // Recent Activity / Posts Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Posts",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { onNavigate(Screen.POSTS) }) {
                    Text("View All", color = IndigoPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Recent posts list (first 3)
        items(recentPosts.take(3), key = { it.id }) { post ->
            PostCard(
                post = post,
                onPreviewClick = { onPreviewPost(post) },
                onPublishNow = { viewModel.publishNow(post.id) },
                onRetry = { viewModel.retryPost(post.id) },
                onDuplicate = { viewModel.duplicatePost(post.id) },
                onDelete = { viewModel.deletePost(post.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // padding for bottom bar
        }
    }
}
