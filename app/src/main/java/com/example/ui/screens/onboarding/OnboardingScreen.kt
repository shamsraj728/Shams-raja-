package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PostFlowViewModel
import com.example.ui.components.PostFlowLogo
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.VioletSecondary

data class OnboardingStep(
    val stepNumber: Int,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val isOptional: Boolean = false
)

@Composable
fun OnboardingScreen(
    viewModel: PostFlowViewModel,
    onComplete: () -> Unit
) {
    val steps = remember {
        listOf(
            OnboardingStep(1, "Welcome to PostFlow", "Your social media scheduling command center. Create, schedule, and publish without the hassle.", Icons.Filled.Send),
            OnboardingStep(2, "Explain What It Does", "Upload photos & videos, preview authentic Instagram feeds, set custom schedules, and enjoy automated publishing.", Icons.Filled.CalendarMonth),
            OnboardingStep(3, "Account Configured", "Your local secure account session is ready with end-to-end credential isolation.", Icons.Filled.CheckCircle),
            OnboardingStep(4, "Connect Instagram", "Official Meta Graph API OAuth connection for seamless Creator/Business account publishing.", Icons.Filled.Link),
            OnboardingStep(5, "Enable Notifications", "Get real-time updates when publishing starts, succeeds, or requires your attention.", Icons.Filled.Notifications),
            OnboardingStep(6, "Optional: WhatsApp Alerts", "Receive instant WhatsApp messages through official Meta Cloud API integrations.", Icons.Filled.Phone, isOptional = true),
            OnboardingStep(7, "Setup Complete", "Everything is aligned. Ready to transform your social media workflow.", Icons.Filled.RocketLaunch),
            OnboardingStep(8, "Open Dashboard", "Enter PostFlow and start managing your content pipeline.", Icons.Filled.Schedule)
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[currentStepIndex]
    var whatsAppPhone by remember { mutableStateOf("") }
    var enableWhatsApp by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PostFlowLogo(size = 32, showText = false)
            if (currentStepIndex < steps.size - 1) {
                TextButton(
                    onClick = {
                        viewModel.completeOnboarding()
                        onComplete()
                    }
                ) {
                    Text("Skip to Dashboard", color = IndigoPrimary, fontSize = 13.sp)
                }
            }
        }

        // Center Content Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(IndigoPrimary.copy(alpha = 0.15f), VioletSecondary.copy(alpha = 0.15f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentStep.icon,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = IndigoPrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Step ${currentStep.stepNumber} of ${steps.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentStep.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentStep.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                // Optional WhatsApp input step
                if (currentStep.stepNumber == 6) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable WhatsApp", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Switch(checked = enableWhatsApp, onCheckedChange = { enableWhatsApp = it })
                    }
                    if (enableWhatsApp) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = whatsAppPhone,
                            onValueChange = { whatsAppPhone = it },
                            label = { Text("Phone Number") },
                            placeholder = { Text("+1 415 555 0199") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        // Progress indicators & Action Button
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentStepIndex) 20.dp else 8.dp, 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (index == currentStepIndex) IndigoPrimary else Color.LightGray.copy(alpha = 0.5f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (currentStepIndex < steps.size - 1) {
                        if (currentStep.stepNumber == 6 && enableWhatsApp && whatsAppPhone.isNotBlank()) {
                            viewModel.updateNotificationPreferences(true, whatsAppPhone, true, true)
                        }
                        currentStepIndex++
                    } else {
                        viewModel.completeOnboarding()
                        onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_next_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text(
                    text = if (currentStepIndex == steps.size - 1) "Enter Dashboard" else "Next Step",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}
