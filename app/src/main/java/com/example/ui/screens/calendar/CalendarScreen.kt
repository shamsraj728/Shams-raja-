package com.example.ui.screens.calendar

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PostEntity
import com.example.ui.PostFlowViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PostCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.IndigoPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: PostFlowViewModel,
    onNavigate: (Screen) -> Unit,
    onPreviewPost: (PostEntity) -> Unit
) {
    val posts by viewModel.allPosts.collectAsState()
    var selectedCalendarView by remember { mutableIntStateOf(0) } // 0: Month, 1: Week, 2: Day

    val calendar = remember { Calendar.getInstance() }
    var currentMonthOffset by remember { mutableIntStateOf(0) }
    var selectedDayTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }

    // Calculate dates for current month/week
    val displayCalendar = remember(currentMonthOffset) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, currentMonthOffset)
        cal
    }

    val monthName = remember(displayCalendar) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(displayCalendar.time)
    }

    // Days in current display
    val daysList = remember(displayCalendar) {
        val list = mutableListOf<Date>()
        val cal = displayCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, i)
            list.add(cal.time)
        }
        list
    }

    // Filter posts for currently selected day
    val selectedDayPosts = remember(posts, selectedDayTimestamp) {
        val targetCal = Calendar.getInstance().apply { timeInMillis = selectedDayTimestamp }
        posts.filter { p ->
            val pCal = Calendar.getInstance().apply {
                timeInMillis = if (p.scheduledTime > 0) p.scheduledTime else p.createdAt
            }
            pCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
                    pCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        text = "Content Calendar",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Plan and organize your publication pipeline",
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
                    Text("Schedule", fontSize = 12.sp)
                }
            }
        }

        // View Tabs (Month, Week, Day)
        item {
            TabRow(selectedTabIndex = selectedCalendarView) {
                Tab(
                    selected = selectedCalendarView == 0,
                    onClick = { selectedCalendarView = 0 },
                    text = { Text("Month", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedCalendarView == 1,
                    onClick = { selectedCalendarView = 1 },
                    text = { Text("Week", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedCalendarView == 2,
                    onClick = { selectedCalendarView = 2 },
                    text = { Text("Day", fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        // Month Selector Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = monthName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row {
                            IconButton(onClick = { currentMonthOffset-- }) {
                                Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = "Previous Month")
                            }
                            IconButton(onClick = { currentMonthOffset++ }) {
                                Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = "Next Month")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Days horizontal selector
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(daysList) { date ->
                            val isSelected = remember(date, selectedDayTimestamp) {
                                val cal1 = Calendar.getInstance().apply { time = date }
                                val cal2 = Calendar.getInstance().apply { timeInMillis = selectedDayTimestamp }
                                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                                        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
                            }

                            val hasPost = remember(date, posts) {
                                val cal = Calendar.getInstance().apply { time = date }
                                posts.any { p ->
                                    val pCal = Calendar.getInstance().apply {
                                        timeInMillis = if (p.scheduledTime > 0) p.scheduledTime else p.createdAt
                                    }
                                    pCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                                            pCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
                                }
                            }

                            val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(date)
                            val dayNumber = SimpleDateFormat("d", Locale.getDefault()).format(date)

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .width(52.dp)
                                    .clickable { selectedDayTimestamp = date.time }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = dayOfWeek,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = dayNumber,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (hasPost) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else IndigoPrimary)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(5.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Day's Posts Header
        item {
            val selectedDateText = remember(selectedDayTimestamp) {
                SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date(selectedDayTimestamp))
            }
            Text(
                text = "Posts for $selectedDateText (${selectedDayPosts.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (selectedDayPosts.isEmpty()) {
            item {
                EmptyStateView(
                    title = "No Posts for this Day",
                    description = "Keep your feed consistent. Tap below to schedule a photo or video post for this date.",
                    icon = Icons.Filled.EventNote,
                    actionLabel = "Schedule Post Here",
                    onActionClick = { onNavigate(Screen.CREATE) }
                )
            }
        } else {
            items(selectedDayPosts, key = { it.id }) { post ->
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
