package com.mehei.app.ui.screens.request

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestScreen(
    state: RequestState,
    onEvent: (RequestEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Mehendi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 24.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Estimated Time of Arrival: ${state.estimatedEtaMinutes} mins",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                    Button(
                        onClick = { onEvent(RequestEvent.OnRequestArtistClick) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "REQUEST ARTIST · ₹${state.estimatedPrice}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Mock Map Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                MockMapCanvas(
                    nearbyArtistsCount = state.nearbyArtistsCount
                )
            }
            
            // Details Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .padding(innerPadding) // padding applies after the map to avoid clipping top bar entirely
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Text(
                    text = "Customize Request",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Add your custom selectors here similar to BookingCalcScreen
                // (Omitted the actual UI components for brevity, would reuse from BookingCalcScreen)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Complexity", style = MaterialTheme.typography.titleMedium)
                    Text(state.selectedComplexity.name, color = MaterialTheme.colorScheme.primary)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hands", style = MaterialTheme.typography.titleMedium)
                    Text("${state.selectedNumHands}", color = MaterialTheme.colorScheme.primary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Event Type", style = MaterialTheme.typography.titleMedium)
                    Text(state.selectedEventType.name, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun MockMapCanvas(nearbyArtistsCount: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "map-pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse-radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse-alpha"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val artistColor = MaterialTheme.colorScheme.tertiary
    
    // Generate random static positions for nearby artists
    val randomArtists = remember(nearbyArtistsCount) {
        List(nearbyArtistsCount) {
            Offset(
                x = Random.nextFloat() * 600 - 300,
                y = Random.nextFloat() * 600 - 300
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        
        // Draw grid lines to simulate a map background
        val gridSize = 100f
        for (i in 0..(size.width / gridSize).toInt()) {
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(i * gridSize, 0f),
                end = Offset(i * gridSize, size.height),
                strokeWidth = 2f
            )
        }
        for (i in 0..(size.height / gridSize).toInt()) {
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(0f, i * gridSize),
                end = Offset(size.width, i * gridSize),
                strokeWidth = 2f
            )
        }

        // Draw pulsing circle around user
        drawCircle(
            color = primaryColor.copy(alpha = pulseAlpha * 0.3f),
            radius = pulseRadius,
            center = center,
        )
        drawCircle(
            color = primaryColor.copy(alpha = pulseAlpha * 0.8f),
            radius = pulseRadius,
            center = center,
            style = Stroke(width = 4f)
        )

        // Draw user location
        drawCircle(
            color = primaryColor,
            radius = 24f,
            center = center,
        )
        drawCircle(
            color = Color.White,
            radius = 12f,
            center = center,
        )

        // Draw nearby artists
        randomArtists.forEach { offset ->
            val artistPos = Offset(center.x + offset.x, center.y + offset.y)
            drawCircle(
                color = artistColor,
                radius = 16f,
                center = artistPos
            )
        }
    }
}
