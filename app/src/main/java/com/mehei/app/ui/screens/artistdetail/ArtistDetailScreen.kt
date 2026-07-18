package com.mehei.app.ui.screens.artistdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mehei.app.domain.model.Artist
import com.mehei.app.domain.model.Complexity
import com.mehei.app.domain.model.FlashSlot
import com.mehei.app.domain.model.Review
import com.mehei.app.ui.modifiers.bounceClick
import com.mehei.app.ui.theme.MeheiFlashBadge
import com.mehei.app.ui.theme.MeheiHennaBrown
import com.mehei.app.ui.theme.MeheiSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    state: ArtistDetailState,
    onBookClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onFavoriteClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val artist = state.artist
    val reviews = state.reviews
    val flashSlots = state.flashSlots

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(artist?.name ?: "Artist") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (artist != null) {
                        IconButton(onClick = { onFavoriteClick(artist.id) }) {
                            Icon(
                                imageVector = if (artist.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (artist.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (artist != null) {
                Surface(
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val minRate = artist.rateCards.minOfOrNull { it.pricePerHand } ?: 0
                        Column {
                            Text(
                                text = "From ₹$minRate/hand",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MeheiSuccess,
                            )
                            Text(
                                text = "Scope-based pricing",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Button(
                            onClick = { },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .bounceClick { onBookClick(artist.id) },
                            contentPadding = PaddingValues(horizontal = 32.dp),
                        ) {
                            Text(
                                "Book Now",
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
        if (artist == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Artist not found")
            }
            return@Scaffold
        }

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Parallax Hero Profile
            Box(
                modifier = Modifier.graphicsLayer {
                    // Translate down by half the scroll offset to create parallax
                    translationY = scrollState.value * 0.5f
                    // Slight fade out as it scrolls up
                    alpha = 1f - (scrollState.value / 600f).coerceIn(0f, 1f)
                }
            ) {
                ProfileHero(artist = artist)
            }

            // Stats Row
            StatsRow(artist = artist)

            HorizontalDivider()

            // Setup staggered animation states
            val bioVisible = remember { MutableTransitionState(false).apply { targetState = true } }
            val pricingVisible = remember { MutableTransitionState(false).apply { targetState = true } }
            val flashVisible = remember { MutableTransitionState(false).apply { targetState = true } }
            val reviewsVisible = remember { MutableTransitionState(false).apply { targetState = true } }

            // Bio
            AnimatedVisibility(
                visibleState = bioVisible,
                enter = fadeIn(animationSpec = tween(400, delayMillis = 100)) + 
                        slideInVertically(animationSpec = tween(400, delayMillis = 100)) { 50 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = artist.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider()
                }
            }

            // Rate Cards
            AnimatedVisibility(
                visibleState = pricingVisible,
                enter = fadeIn(animationSpec = tween(400, delayMillis = 200)) + 
                        slideInVertically(animationSpec = tween(400, delayMillis = 200)) { 50 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text(
                        text = "Pricing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    RateCardsSection(artist = artist)
                }
            }

            // Flash Slots
            if (flashSlots.isNotEmpty()) {
                AnimatedVisibility(
                    visibleState = flashVisible,
                    enter = fadeIn(animationSpec = tween(400, delayMillis = 300)) + 
                            slideInVertically(animationSpec = tween(400, delayMillis = 300)) { 50 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        HorizontalDivider()
                        FlashSlotsSection(slots = flashSlots)
                    }
                }
            }

            // Reviews
            if (reviews.isNotEmpty()) {
                AnimatedVisibility(
                    visibleState = reviewsVisible,
                    enter = fadeIn(animationSpec = tween(400, delayMillis = 400)) + 
                            slideInVertically(animationSpec = tween(400, delayMillis = 400)) { 50 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        HorizontalDivider()
                        ReviewsSection(reviews = reviews)
                    }
                }
            }

            // Bottom padding for bottom bar
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun ProfileHero(artist: Artist) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary,
                        )
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = artist.name.take(1),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (artist.tier == com.mehei.app.domain.model.ArtistTier.MASTER) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.Outlined.WorkspacePremium,
                        contentDescription = "Master Artist",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MeheiFlashBadge,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${artist.rating}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = " (${artist.totalReviews} reviews)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Specialty chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                items(artist.specialties) { eventType ->
                    val label = eventType.name.replace('_', ' ')
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        },
                        modifier = Modifier.height(28.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(artist: Artist) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatItem(
            icon = Icons.Outlined.Schedule,
            value = "${artist.experienceYears} yrs",
            label = "Experience",
        )
        StatItem(
            icon = Icons.Outlined.EmojiEvents,
            value = artist.tier.name.lowercase().replaceFirstChar { it.uppercase() },
            label = "Tier",
        )
        StatItem(
            icon = Icons.Outlined.Spa,
            value = "${artist.specialties.size}",
            label = "Specialties",
        )
    }
}

@Composable
private fun StatItem(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RateCardsSection(artist: Artist) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        artist.rateCards.forEach { rateCard ->
            val (label, desc) = when (rateCard.complexity) {
                Complexity.SIMPLE -> "Simple" to "Dots, stripes, mandalas"
                Complexity.TRADITIONAL -> "Traditional" to "Dense patterns, Arabic"
                Complexity.PORTRAIT -> "Portrait" to "Intricate figurines"
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick { },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${rateCard.pricePerHand}/hand",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MeheiSuccess,
                        )
                        Text(
                            text = "+ ₹${rateCard.hourlyRate}/hr",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashSlotsSection(slots: List<FlashSlot>) {

    Text(
        text = "⚡ Flash Slots Available",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )

    slots.forEach { slot ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick { },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MeheiFlashBadge.copy(alpha = 0.08f),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = slot.date,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "${slot.startTime} – ${slot.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MeheiFlashBadge.copy(alpha = 0.2f),
                ) {
                    Text(
                        text = "${slot.discountPercent}% OFF",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MeheiFlashBadge,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewsSection(reviews: List<Review>) {
    Text(
        text = "Party Reviews",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )

    reviews.forEach { review ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick { },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            ),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = review.customerName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MeheiFlashBadge,
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "${review.rating}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                val eventLabel = review.eventType.name.replace('_', ' ')
                    .lowercase()
                    .replaceFirstChar { it.uppercase() }
                Text(
                    text = eventLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MeheiHennaBrown,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
