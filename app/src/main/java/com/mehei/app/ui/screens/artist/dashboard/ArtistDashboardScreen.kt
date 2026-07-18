package com.mehei.app.ui.screens.artist.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mehei.app.domain.model.Booking
import com.mehei.app.ui.components.ShimmerBookingList
import com.mehei.app.ui.modifiers.bounceClick

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: ArtistDashboardViewModel = hiltViewModel(),
    onBookingClick: (String) -> Unit = {},
    onSwitchToCustomer: () -> Unit = {},
    onPortfolioClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("Artist Dashboard", fontWeight = FontWeight.Bold)
                            Text(
                                text = "Manage your bookings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onSwitchToCustomer) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Customer Mode")
                        }
                    },
                    actions = {
                        IconButton(onClick = onPortfolioClick) {
                            Icon(Icons.Outlined.PhotoLibrary, contentDescription = "My Portfolio", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
                
                // Uber-like Online Toggle
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Status", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (state.isOnline) "Online - Ready for Requests" else "Offline",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (state.isOnline) com.mehei.app.ui.theme.MeheiSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.isOnline,
                            onCheckedChange = { viewModel.onEvent(ArtistDashboardEvent.ToggleOnlineStatus(it)) }
                        )
                    }
                }

                PrimaryTabRow(
                    selectedTabIndex = state.selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                ) {
                    Tab(
                        selected = state.selectedTab == 0,
                        onClick = { viewModel.onEvent(ArtistDashboardEvent.TabSelected(0)) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("New")
                                if (state.pendingBookings.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Badge { Text("${state.pendingBookings.size}") }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = state.selectedTab == 1,
                        onClick = { viewModel.onEvent(ArtistDashboardEvent.TabSelected(1)) },
                        text = { Text("Upcoming") }
                    )
                    Tab(
                        selected = state.selectedTab == 2,
                        onClick = { viewModel.onEvent(ArtistDashboardEvent.TabSelected(2)) },
                        text = { Text("Past") }
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (state.isLoading) {
            ShimmerBookingList(
                count = 3,
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
            )
        } else {
            // AnimatedContent for smooth tab transitions
            AnimatedContent(
                targetState = state.selectedTab,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(250)) +
                            slideInHorizontally(
                                animationSpec = tween(250),
                                initialOffsetX = { if (targetState > initialState) it / 4 else -it / 4 }
                            )).togetherWith(
                        fadeOut(animationSpec = tween(200)) +
                                slideOutHorizontally(
                                    animationSpec = tween(200),
                                    targetOffsetX = { if (targetState > initialState) -it / 4 else it / 4 }
                                )
                    )
                },
                label = "dashboard_tab_content"
            ) { tabIndex ->
                val bookingsToShow = when (tabIndex) {
                    0 -> state.pendingBookings
                    1 -> state.upcomingBookings
                    else -> state.pastBookings
                }

                if (bookingsToShow.isEmpty()) {
                    DashboardEmptyState(
                        tabIndex = tabIndex,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(bookingsToShow, key = { it.id }) { booking ->
                            ArtistBookingCard(
                                booking = booking,
                                onActionClick = { onBookingClick(booking.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    // Incoming Request Overlay
    state.incomingRequest?.let { request ->
        var timeRemaining by remember { mutableStateOf(15) }
        
        LaunchedEffect(request.id) {
            while (timeRemaining > 0) {
                kotlinx.coroutines.delay(1000)
                timeRemaining--
            }
            if (timeRemaining == 0) {
                viewModel.onEvent(ArtistDashboardEvent.DeclineRequest)
            }
        }
        
        AlertDialog(
            onDismissRequest = { /* Cannot dismiss without action */ },
            title = {
                Text(
                    text = "New Booking Request!",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Time to accept: $timeRemaining seconds", color = com.mehei.app.ui.theme.MeheiError, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Customer: ${request.customerName}", fontWeight = FontWeight.Medium)
                    Text("Hands: ${request.numHands} | Type: ${request.eventType.name}")
                    Text("Complexity: ${request.complexity.name}")
                    Text("Est. Earnings: ₹${request.estimatedPrice}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onEvent(ArtistDashboardEvent.AcceptRequest) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Accept Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(ArtistDashboardEvent.DeclineRequest) }) {
                    Text("Decline")
                }
            }
        )
    }
}

@Composable
private fun DashboardEmptyState(tabIndex: Int, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "dash_empty_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dash_empty_offset"
    )

    val (emoji, title, subtitle) = when (tabIndex) {
        0 -> Triple("📬", "No new requests", "When customers book you, their requests will appear here")
        1 -> Triple("📅", "No upcoming bookings", "Accept new requests to see your upcoming schedule")
        else -> Triple("📝", "No past bookings", "Your completed and cancelled bookings will show up here")
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.graphicsLayer { translationY = floatOffset }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ArtistBookingCard(
    booking: Booking,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .bounceClick { onActionClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = booking.status.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "Payout: ₹${booking.totalPrice - booking.deposit}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Customer: ${booking.customerId}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${booking.bookingDate} at ${booking.startTime} (${booking.eventType.name})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Customer Location",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("View Request")
                }
            }
        }
    }
}
