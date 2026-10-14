package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.model.PostEntity
import com.example.data.model.PostStatus
import com.example.data.model.ThemeMode
import com.example.ui.components.PostFlowLogo
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.BottomNavItem
import com.example.ui.navigation.Screen
import com.example.ui.screens.accounts.ConnectedAccountsScreen
import com.example.ui.screens.activity.ActivityLogScreen
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.SignupScreen
import com.example.ui.screens.calendar.CalendarScreen
import com.example.ui.screens.create.CreatePostScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.notifications.NotificationCenterScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.posts.PostsScreen
import com.example.ui.screens.settings.HelpCenterScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.PostFlowTheme
import com.example.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostFlowApp(
    viewModel: PostFlowViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemDark
    }

    val activeUserId by viewModel.activeUserId.collectAsState()
    val completedOnboarding by viewModel.completedOnboarding.collectAsState()
    val unreadNotifsCount by viewModel.unreadNotificationsCount.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Navigation Stack state
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
    var selectedPostForDetail by remember { mutableStateOf<PostEntity?>(null) }
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle back button for sub-screens
    BackHandler(enabled = currentScreen != Screen.DASHBOARD && activeUserId != null) {
        currentScreen = Screen.DASHBOARD
    }

    PostFlowTheme(darkTheme = isDarkTheme) {
        // Auth / Onboarding gating
        if (activeUserId == null) {
            when (currentScreen) {
                Screen.SIGNUP -> SignupScreen(
                    viewModel = viewModel,
                    onNavigate = { currentScreen = it },
                    onSignupSuccess = { currentScreen = Screen.ONBOARDING }
                )
                Screen.FORGOT_PASSWORD -> ForgotPasswordScreen(
                    viewModel = viewModel,
                    onNavigate = { currentScreen = it }
                )
                else -> LoginScreen(
                    viewModel = viewModel,
                    onNavigate = { currentScreen = it },
                    onLoginSuccess = {
                        currentScreen = if (!completedOnboarding) Screen.ONBOARDING else Screen.DASHBOARD
                    }
                )
            }
        } else if (!completedOnboarding && currentScreen == Screen.ONBOARDING) {
            OnboardingScreen(
                viewModel = viewModel,
                onComplete = { currentScreen = Screen.DASHBOARD }
            )
        } else {
            // Main application layout
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            PostFlowLogo(size = 30, showText = true)
                        },
                        actions = {
                            // Notifications bell
                            IconButton(onClick = { currentScreen = Screen.NOTIFICATIONS }) {
                                BadgedBox(
                                    badge = {
                                        if (unreadNotifsCount > 0) {
                                            Badge { Text("$unreadNotifsCount") }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Notifications,
                                        contentDescription = "Notifications",
                                        tint = if (currentScreen == Screen.NOTIFICATIONS) IndigoPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    val isBottomBarVisible = currentScreen in listOf(
                        Screen.DASHBOARD,
                        Screen.CALENDAR,
                        Screen.CREATE,
                        Screen.POSTS,
                        Screen.SETTINGS
                    )

                    if (isBottomBarVisible) {
                        NavigationBar(
                            modifier = Modifier.navigationBarsPadding(),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            BottomNavItem.items.forEach { item ->
                                val isSelected = currentScreen == item.screen
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentScreen = item.screen },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title,
                                            tint = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = IndigoPrimary.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier.testTag("nav_item_${item.screen.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    when (currentScreen) {
                        Screen.DASHBOARD -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigate = { currentScreen = it },
                            onPreviewPost = { selectedPostForDetail = it }
                        )
                        Screen.CALENDAR -> CalendarScreen(
                            viewModel = viewModel,
                            onNavigate = { currentScreen = it },
                            onPreviewPost = { selectedPostForDetail = it }
                        )
                        Screen.CREATE -> CreatePostScreen(
                            viewModel = viewModel,
                            onFinished = { currentScreen = Screen.DASHBOARD }
                        )
                        Screen.POSTS -> PostsScreen(
                            viewModel = viewModel,
                            onNavigate = { currentScreen = it },
                            onPreviewPost = { selectedPostForDetail = it }
                        )
                        Screen.SETTINGS -> SettingsScreen(
                            viewModel = viewModel,
                            onNavigate = { currentScreen = it }
                        )
                        Screen.NOTIFICATIONS -> NotificationCenterScreen(
                            viewModel = viewModel
                        )
                        Screen.ACCOUNTS -> ConnectedAccountsScreen(
                            viewModel = viewModel
                        )
                        Screen.ACTIVITY -> ActivityLogScreen(
                            viewModel = viewModel
                        )
                        Screen.HELP -> HelpCenterScreen()
                        else -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigate = { currentScreen = it },
                            onPreviewPost = { selectedPostForDetail = it }
                        )
                    }
                }
            }

            // Post Detail Modal Bottom Sheet
            selectedPostForDetail?.let { post ->
                ModalBottomSheet(
                    onDismissRequest = { selectedPostForDetail = null },
                    sheetState = detailSheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Post Details",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatusBadge(status = post.status)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (post.mediaUrls.isNotBlank()) {
                            AsyncImage(
                                model = post.mediaUrls,
                                contentDescription = "Detail media",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.Black)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Text(
                            text = post.caption.ifBlank { "No caption provided" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (post.hashtags.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = post.hashtags,
                                style = MaterialTheme.typography.bodySmall.copy(color = IndigoPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Platform Permalink info
                        if (!post.platformPermalink.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SuccessGreen.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Official Meta Post ID: #${post.platformPostId}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen
                                        )
                                        Text(
                                            text = post.platformPermalink,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Filled.OpenInBrowser,
                                        contentDescription = "Open",
                                        tint = SuccessGreen
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Error Banner if failed
                        if (!post.errorMessage.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ErrorRed.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = ErrorRed)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = post.errorMessage,
                                        fontSize = 12.sp,
                                        color = ErrorRed
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (post.status == PostStatus.SCHEDULED || post.status == PostStatus.DRAFT) {
                                Button(
                                    onClick = {
                                        viewModel.publishNow(post.id)
                                        selectedPostForDetail = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                                ) {
                                    Icon(imageVector = Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Publish Now")
                                }
                            }

                            if (post.status == PostStatus.FAILED || post.status == PostStatus.CONNECTION_REQUIRED) {
                                Button(
                                    onClick = {
                                        viewModel.retryPost(post.id)
                                        selectedPostForDetail = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                                ) {
                                    Icon(imageVector = Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Retry Publishing")
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.duplicatePost(post.id)
                                    selectedPostForDetail = null
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Duplicate")
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.deletePost(post.id)
                                    selectedPostForDetail = null
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete", tint = ErrorRed)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
