package com.example.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndigoPrimary

data class FaqItem(val question: String, val answer: String)

@Composable
fun HelpCenterScreen() {
    val faqs = remember {
        listOf(
            FaqItem(
                question = "How does PostFlow publish to Instagram automatically?",
                answer = "PostFlow connects to the official Meta Graph API v21.0 using OAuth 2.0. When your scheduled time arrives, our background worker creates an official media container on Meta's servers, verifies readiness, and issues the publish command without requiring your phone to be active or unlocked."
            ),
            FaqItem(
                question = "What account type is required for Instagram?",
                answer = "Official Instagram Content Publishing requires an Instagram Creator or Instagram Business account linked to a Facebook Page, in accordance with Meta developer guidelines. Personal accounts are not permitted by Meta for automated API publishing."
            ),
            FaqItem(
                question = "What media formats and sizes are supported?",
                answer = "Photos: JPG, JPEG, and PNG up to 8MB. Recommended aspect ratios are 1:1 (Square), 4:5 (Portrait), and 1.91:1 (Landscape). Videos: MP4/MOV encoded with H.264/AAC, frame rate 23-60 FPS, up to 100MB in size."
            ),
            FaqItem(
                question = "What happens if a post fails to publish?",
                answer = "PostFlow never silently drops a failed post. The post is preserved with its full caption, hashtags, and media. An error diagnosis is logged, a notification is sent immediately, and a 1-tap 'Retry' button is provided in both the Posts list and Notification Center."
            ),
            FaqItem(
                question = "How do WhatsApp and Email notifications work?",
                answer = "PostFlow supports Meta Cloud API WhatsApp webhooks and transactional email alerts. You can configure in Settings whether you want alerts for scheduled posts, successful publishing, or failure alerts requiring attention."
            ),
            FaqItem(
                question = "Can I test without a live Meta Developer App?",
                answer = "Yes! PostFlow comes with an active Demo Mode built right in. You can simulate account connections, scheduling, successful publishes, and even failure scenarios using the 'Simulate Publishing Failures' toggle in Connected Accounts."
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Help,
                    contentDescription = null,
                    tint = IndigoPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Column {
                    Text(
                        text = "Help Center & FAQs",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Guides, API compliance, and troubleshooting",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(faqs) { item ->
            FaqAccordionCard(item = item)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun FaqAccordionCard(item: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.question,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = IndigoPrimary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = item.answer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
