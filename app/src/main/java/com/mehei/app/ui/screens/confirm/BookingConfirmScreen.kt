package com.mehei.app.ui.screens.confirm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mehei.app.ui.modifiers.bounceClick
import com.mehei.app.ui.theme.MeheiSuccess

@Composable
fun BookingConfirmScreen(
    bookingId: String,
    onViewBookings: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Success Icon
            val scaleState = remember { MutableTransitionState(false) }.apply { targetState = true }
            val scale by animateFloatAsState(
                targetValue = if (scaleState.targetState) 1f else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "SuccessIconScale"
            )

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .background(MeheiSuccess.copy(alpha = 0.12f), shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Booking Confirmed",
                    modifier = Modifier.size(64.dp),
                    tint = MeheiSuccess,
                )
            }

            // Success Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Booking Confirmed!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Your artist has been notified and will be at your home at the scheduled time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            // Booking ID Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Booking Reference",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = bookingId.take(8).uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // What happens next
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val stepsVisible = remember { MutableTransitionState(false) }.apply { targetState = true }
                    
                    Text(
                        text = "What happens next?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    
                    AnimatedVisibility(
                        visibleState = stepsVisible,
                        enter = fadeIn(animationSpec = tween(400, delayMillis = 300)) + 
                                slideInVertically(animationSpec = tween(400, delayMillis = 300)) { 20 }
                    ) {
                        NextStep(number = "1", text = "Artist reviews your booking details")
                    }
                    
                    AnimatedVisibility(
                        visibleState = stepsVisible,
                        enter = fadeIn(animationSpec = tween(400, delayMillis = 400)) + 
                                slideInVertically(animationSpec = tween(400, delayMillis = 400)) { 20 }
                    ) {
                        NextStep(number = "2", text = "You receive an SMS confirmation")
                    }
                    
                    AnimatedVisibility(
                        visibleState = stepsVisible,
                        enter = fadeIn(animationSpec = tween(400, delayMillis = 500)) + 
                                slideInVertically(animationSpec = tween(400, delayMillis = 500)) { 20 }
                    ) {
                        NextStep(number = "3", text = "Artist arrives at your location on time")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CTA Buttons
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick { onViewBookings() },
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("View My Bookings", fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick { onBackToHome() },
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Back to Home")
            }
        }
    }
}

@Composable
private fun NextStep(number: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(28.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
