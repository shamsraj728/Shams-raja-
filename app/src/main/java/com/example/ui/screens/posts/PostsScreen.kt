package com.example.ui.screens.posts

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PostEntity
import com.example.data.model.PostStatus
import com.example.ui.PostFlowViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PostCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.IndigoPrimary

@Composable
fun PostsScreen(
    viewModel: PostFlowViewModel,
    onNavigate: (Screen) -> Unit,
    onPreviewPost: (PostEntity) -> Unit
) {
    val posts by viewModel.allPosts.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<PostStatus?>(null) }

    val filteredPosts = remember(posts, searchQuery, selectedFilter) {
        posts.filter { post ->
            val matchesSearch = searchQuery.isBlank() ||
                    post.caption.contains(searchQuery, ignoreCase = true) ||
                    post.hashtags.contains(searchQuery, ignoreCase = true) ||
                    post.accountHandle.contains(searchQuery, ignoreCase = true)

            val matchesStatus = selectedFilter == null || post.status == selectedFilter

            matchesSearch && matchesStatus
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Posts & Content",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${posts.size} total posts in repository",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { onNavigate(Screen.CREATE) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", fontSize = 12.sp)
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by caption, tags, account...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = IndigoPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Status Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All (${posts.size})", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp)
                )
                FilterChip(
                    selected = selectedFilter == PostStatus.SCHEDULED,
                    onClick = { selectedFilter = PostStatus.SCHEDULED },
                    label = { Text("Scheduled (${posts.count { it.status == PostStatus.SCHEDULED }})", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp)
                )
                FilterChip(
                    selected = selectedFilter == PostStatus.PUBLISHED,
                    onClick = { selectedFilter = PostStatus.PUBLISHED },
                    label = { Text("Published (${posts.count { it.status == PostStatus.PUBLISHED }})", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp)
                )
                FilterChip(
                    selected = selectedFilter == PostStatus.DRAFT,
                    onClick = { selectedFilter = PostStatus.DRAFT },
                    label = { Text("Drafts (${posts.count { it.status == PostStatus.DRAFT }})", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp)
                )
                FilterChip(
                    selected = selectedFilter == PostStatus.FAILED || selectedFilter == PostStatus.CONNECTION_REQUIRED,
                    onClick = { selectedFilter = PostStatus.FAILED },
                    label = { Text("Failed (${posts.count { it.status == PostStatus.FAILED || it.status == PostStatus.CONNECTION_REQUIRED }})", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Posts List
        if (filteredPosts.isEmpty()) {
            item {
                EmptyStateView(
                    title = if (searchQuery.isNotBlank() || selectedFilter != null) "No Matching Posts" else "No Posts Yet",
                    description = if (searchQuery.isNotBlank() || selectedFilter != null) "Try adjusting your search query or status filter." else "Your social publishing calendar is ready for content.",
                    icon = Icons.Filled.Folder,
                    actionLabel = "Create a Post",
                    onActionClick = { onNavigate(Screen.CREATE) }
                )
            }
        } else {
            items(filteredPosts, key = { it.id }) { post ->
                PostCard(
                    post = post,
                    onPreviewClick = { onPreviewPost(post) },
                    onPublishNow = { viewModel.publishNow(post.id) },
                    onRetry = { viewModel.retryPost(post.id) },
                    onDuplicate = { viewModel.duplicatePost(post.id) },
                    onDelete = { viewModel.deletePost(post.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
