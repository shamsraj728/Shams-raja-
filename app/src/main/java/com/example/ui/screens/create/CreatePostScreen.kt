package com.example.ui.screens.create

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CropRatio
import com.example.data.model.MediaType
import com.example.data.model.ScheduleFrequency
import com.example.service.SecurityUtils
import com.example.ui.PostFlowViewModel
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.VioletSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    viewModel: PostFlowViewModel,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val igAccount by viewModel.instagramAccount.collectAsState()

    var caption by remember { mutableStateOf("") }
    var hashtags by remember { mutableStateOf("#PostFlow #ContentCreator #GrowYourBrand") }
    var location by remember { mutableStateOf("") }
    var mediaType by remember { mutableStateOf(MediaType.IMAGE) }
    var cropRatio by remember { mutableStateOf(CropRatio.SQUARE) }
    var rotationDegrees by remember { mutableIntStateOf(0) }

    // Pre-curated high quality photos and demo video library for instant smooth creation
    val curatedImages = remember {
        listOf(
            "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800",
            "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=800",
            "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800",
            "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=800",
            "https://images.unsplash.com/photo-1551836022-d5d88e9218df?w=800"
        )
    }

    var selectedMediaUrl by remember { mutableStateOf(curatedImages[0]) }
    var showCustomUrlDialog by remember { mutableStateOf(false) }
    var customUrlInput by remember { mutableStateOf("") }

    // Scheduling states
    var scheduleOption by remember { mutableIntStateOf(0) } // 0: Now, 1: Later (Schedule), 2: Draft
    var scheduledTimeOffsetHours by remember { mutableIntStateOf(2) } // default 2 hours ahead
    var scheduleFrequency by remember { mutableStateOf(ScheduleFrequency.ONCE) }

    // Preview Bottom Sheet State
    var showLivePreviewModal by remember { mutableStateOf(false) }
    val previewSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Validation
    val mediaValidation = remember(selectedMediaUrl, mediaType) {
        SecurityUtils.validateMedia(selectedMediaUrl, isVideo = mediaType == MediaType.VIDEO)
    }

    val characterLimit = 2200 // Instagram limit

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Create New Post",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Prepare, style, and schedule your social content",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // MEDIA PREVIEW & TOOLS CARD
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Media Asset",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row {
                        IconButton(
                            onClick = { rotationDegrees = (rotationDegrees + 90) % 360 }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RotateRight,
                                contentDescription = "Rotate media",
                                tint = IndigoPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Media canvas preview with chosen aspect ratio and rotation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(cropRatio.ratio)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = selectedMediaUrl,
                        contentDescription = "Selected post media",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(rotationDegrees.toFloat())
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Aspect Ratio Selector Chips
                Text(
                    text = "Aspect Ratio (Crop)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CropRatio.values().forEach { ratio ->
                        FilterChip(
                            selected = cropRatio == ratio,
                            onClick = { cropRatio = ratio },
                            label = { Text(ratio.label, fontSize = 11.sp) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Media presets row
                Text(
                    text = "Select Media Asset",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    curatedImages.forEachIndexed { index, url ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (selectedMediaUrl == url) 2.dp else 0.dp,
                                    color = if (selectedMediaUrl == url) IndigoPrimary else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedMediaUrl = url }
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Preset $index",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mediaValidation.message,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (mediaValidation.isValid) MaterialTheme.colorScheme.onSurfaceVariant else Color.Red
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CAPTION & HASHTAG TOOLS CARD
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Caption",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${caption.length} / $characterLimit",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (caption.length > characterLimit) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    placeholder = { Text("Write an engaging caption for your followers...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("caption_input_field"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Caption Action Buttons (Copy, Clear)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Post Caption", "$caption\n\n$hashtags"))
                            viewModel.showMessage("Caption copied to clipboard")
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { caption = "" },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hashtags
                Text(
                    text = "Hashtags",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = hashtags,
                    onValueChange = { hashtags = it },
                    placeholder = { Text("#hashtags") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Hashtag suggestion chips
                Text(
                    text = "Tap to append suggested tags:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val suggestions = listOf("#marketing", "#growth", "#photography", "#viral", "#instadaily", "#explorepage")
                    suggestions.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = IndigoPrimary.copy(alpha = 0.1f),
                            modifier = Modifier.clickable {
                                if (!hashtags.contains(tag)) {
                                    hashtags = if (hashtags.isBlank()) tag else "$hashtags $tag"
                                }
                            }
                        ) {
                            Text(
                                text = tag,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = IndigoPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Location Tag
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Add Location (Optional)") },
                    placeholder = { Text("e.g. San Francisco, California") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.LocationOn, contentDescription = null, tint = IndigoPrimary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PUBLISHING / SCHEDULING CARD
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Publishing Schedule",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                TabRow(selectedTabIndex = scheduleOption) {
                    Tab(
                        selected = scheduleOption == 0,
                        onClick = { scheduleOption = 0 },
                        text = { Text("Publish Now", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = scheduleOption == 1,
                        onClick = { scheduleOption = 1 },
                        text = { Text("Schedule", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = scheduleOption == 2,
                        onClick = { scheduleOption = 2 },
                        text = { Text("Draft", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                AnimatedVisibility(visible = scheduleOption == 1) {
                    Column {
                        Text(
                            text = "Schedule Time:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(1 to "In 1 hr", 3 to "In 3 hrs", 12 to "In 12 hrs", 24 to "Tomorrow").forEach { (hours, label) ->
                                FilterChip(
                                    selected = scheduledTimeOffsetHours == hours,
                                    onClick = { scheduledTimeOffsetHours = hours },
                                    label = { Text(label, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        val targetTime = System.currentTimeMillis() + (scheduledTimeOffsetHours * 3600 * 1000L)
                        val formattedTarget = SimpleDateFormat("EEEE, MMM d 'at' h:mm a", Locale.getDefault()).format(Date(targetTime))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Will publish on: $formattedTarget (Local Time)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = IndigoPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Recurrence Frequency:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ScheduleFrequency.values().forEach { freq ->
                                FilterChip(
                                    selected = scheduleFrequency == freq,
                                    onClick = { scheduleFrequency = freq },
                                    label = { Text(freq.label, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LIVE PREVIEW MODAL BUTTON & SUBMIT ACTION BUTTONS
        OutlinedButton(
            onClick = { showLivePreviewModal = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Filled.Visibility, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Show Instagram Post Preview", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val isPublishNow = scheduleOption == 0
                val targetSchedule = if (scheduleOption == 1) {
                    System.currentTimeMillis() + (scheduledTimeOffsetHours * 3600 * 1000L)
                } else 0L

                viewModel.createOrUpdatePost(
                    postId = null,
                    caption = caption,
                    hashtags = hashtags,
                    location = location,
                    mediaUrl = selectedMediaUrl,
                    mediaType = mediaType,
                    cropRatio = cropRatio,
                    rotationDegrees = rotationDegrees,
                    publishNow = isPublishNow,
                    scheduledTimestamp = targetSchedule,
                    frequency = scheduleFrequency,
                    onComplete = onFinished
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_post_action_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
        ) {
            val label = when (scheduleOption) {
                0 -> "🚀 Publish Now to Instagram"
                1 -> "⏰ Schedule Post"
                else -> "💾 Save as Draft"
            }
            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(90.dp)) // padding for bottom bar
    }

    // Live Instagram Post Preview Bottom Sheet Modal
    if (showLivePreviewModal) {
        ModalBottomSheet(
            onDismissRequest = { showLivePreviewModal = false },
            sheetState = previewSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Live Post Preview",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Exact rendering for ${igAccount?.accountHandle ?: "@instagram"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Realistic Instagram card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        // Instagram Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(IndigoPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("IG", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = igAccount?.accountHandle ?: "@your_account",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    if (location.isNotBlank()) {
                                        Text(text = location, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            Icon(imageVector = Icons.Filled.MoreHoriz, contentDescription = null)
                        }

                        // Media
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(cropRatio.ratio)
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = selectedMediaUrl,
                                contentDescription = "Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(rotationDegrees.toFloat())
                            )
                        }

                        // Actions & Caption
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Icon(imageVector = Icons.Filled.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(22.dp))
                                    Icon(imageVector = Icons.Filled.Send, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                                }
                                Icon(imageVector = Icons.Filled.Schedule, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(20.dp))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${igAccount?.accountHandle ?: "@your_account"}  ${caption.ifBlank { "Exciting update coming soon!" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (hashtags.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = hashtags,
                                    style = MaterialTheme.typography.bodySmall.copy(color = IndigoPrimary)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { showLivePreviewModal = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Close Preview")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
