package com.mehei.app.ui.screens.booking

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.mehei.app.domain.model.Complexity
import com.mehei.app.domain.model.EventType
import com.mehei.app.ui.theme.MeheiFlashBadge
import com.mehei.app.ui.theme.MeheiHennaBrown
import com.mehei.app.ui.theme.MeheiSuccess
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingCalcScreen(
    state: BookingCalcState,
    onEvent: (BookingCalcEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val artist = state.artist
    var isFindingArtist by remember { mutableStateOf(false) }

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
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        },
        bottomBar = {
            if (artist != null) {
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
                        // Deposit note
                        Text(
                            text = "30% non-refundable deposit secures your slot. Remaining balance due at the session.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                        Button(
                            onClick = { 
                                // In Uber-like flow, this broadcasts the request
                                isFindingArtist = true
                                // We simulate finding an artist for 3 seconds then confirming
                                // onEvent(BookingCalcEvent.ConfirmBooking) 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(8.dp), // Sharper, premium corner
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = "REQUEST ARTIST NOW · ₹${state.priceEstimate?.deposit ?: 0} DEPOSIT",
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
            // Uber-like flow: Even without a specific artist, we could show standard rates.
            // For now, we still wait for the ViewModel to load the 'standard' rates.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // Add a Finding Artist dialog state
        if (isFindingArtist) {
            AlertDialog(
                onDismissRequest = { isFindingArtist = false },
                title = { Text("Finding nearby artists...") },
                text = { 
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Broadcasting your request to available artists in your area. Please wait...", textAlign = TextAlign.Center)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { isFindingArtist = false }) {
                        Text("Cancel Request")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp), // Wider spacing for "Sleek" aesthetic
        ) {
            // Artist Header is hidden in On-Demand mode until a match is found
            // ArtistHeader(
            //     name = artist.name,
            //     rating = artist.rating,
            //     tier = artist.tier.name,
            // )

            // Large Prominent Price Display
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Estimated Total",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "₹${state.priceEstimate?.total ?: 0}",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Hands Counter
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle("How many hands?")
                HandsCounter(
                    count = state.numHands,
                    onCountChange = { onEvent(BookingCalcEvent.SetHands(it)) },
                )
            }

            // Duration Slider
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle("Estimated duration")
                DurationSlider(
                    hours = state.estimatedHours,
                    onHoursChange = { onEvent(BookingCalcEvent.SetHours(it)) },
                )
            }

            // Complexity Selector
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle("Design complexity")
                ComplexitySelector(
                    selected = state.selectedComplexity,
                    onSelect = { onEvent(BookingCalcEvent.SetComplexity(it)) },
                )
            }

            // Event Type
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle("Event type")
                EventTypeSelector(
                    selected = state.selectedEventType,
                    onSelect = { onEvent(BookingCalcEvent.SetEventType(it)) },
                )
            }

            // Organic Henna toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Organic henna paste",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Artist-supplied chemical-free henna (+₹200)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.includeOrganic,
                    onCheckedChange = { onEvent(BookingCalcEvent.ToggleOrganicHenna(it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            // Price Breakdown
            state.priceEstimate?.let { estimate ->
                PriceBreakdown(estimate = estimate)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ArtistHeader(name: String, rating: Float, tier: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
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
                text = name.take(1),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MeheiFlashBadge,
                )
                Spacer(Modifier.width(2.dp))
                Text("$rating", style = MaterialTheme.typography.bodySmall)
                Text(
                    " · $tier",
                    style = MaterialTheme.typography.bodySmall,
                    color = MeheiHennaBrown,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun HandsCounter(
    count: Int,
    onCountChange: (Int) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FilledTonalButton(
            onClick = { onCountChange(count - 1) },
            enabled = count > 1,
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
        ) {
            Text("−", style = MaterialTheme.typography.headlineSmall)
        }

        AnimatedContent(
            targetState = count,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "hands-counter",
        ) { targetCount ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(min = 60.dp),
            ) {
                Text(
                    text = "$targetCount",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = if (targetCount == 1) "hand" else "hands",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        FilledTonalButton(
            onClick = { onCountChange(count + 1) },
            enabled = count < 40,
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
        ) {
            Text("+", style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Quick presets
        val presets = listOf(2 to "Solo", 4 to "Duo", 8 to "Small", 12 to "Party")
        presets.forEach { (value, label) ->
            AssistChip(
                onClick = { onCountChange(value) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.height(32.dp),
            )
        }
    }
}

@Composable
private fun ComplexitySelector(
    selected: Complexity,
    onSelect: (Complexity) -> Unit,
) {
    val options = listOf(
        Triple(Complexity.SIMPLE, "Simple", "Dots, stripes, mandalas · 15-20 min/hand"),
        Triple(Complexity.TRADITIONAL, "Traditional", "Dense patterns, Arabic · 30-45 min/hand"),
        Triple(Complexity.PORTRAIT, "Portrait", "Intricate figurines · 45-90 min/hand"),
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (complexity, label, description) ->
            val isSelected = selected == complexity
            Card(
                onClick = { onSelect(complexity) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                ),
                border = if (isSelected) {
                    CardDefaults.outlinedCardBorder()
                } else null,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelect(complexity) },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = description,
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
private fun DurationSlider(
    hours: Float,
    onHoursChange: (Float) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "30 min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${hours}h",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "8 hours",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = hours,
            onValueChange = { onHoursChange((it * 2).roundToInt() / 2f) }, // 0.5h steps
            valueRange = 0.5f..8f,
            steps = 15,
        )
    }
}

@Composable
private fun EventTypeSelector(
    selected: EventType,
    onSelect: (EventType) -> Unit,
) {
    val eventTypes = listOf(
        EventType.KARVA_CHAUTH to "Karva Chauth",
        EventType.BABY_SHOWER to "Baby Shower",
        EventType.TEEJ to "Teej",
        EventType.ENGAGEMENT to "Engagement",
        EventType.FESTIVAL to "Festival",
        EventType.PARTY to "Party",
        EventType.HALDI to "Haldi",
        EventType.MEHNDI_NIGHT to "Mehndi Night",
        EventType.CORPORATE to "Corporate",
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        eventTypes.forEach { (type, label) ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelect(type) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun PriceBreakdown(
    estimate: com.mehei.app.domain.model.PriceEstimate,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Price Breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            PriceRow("Hands cost", "₹${estimate.handsCost}")
            PriceRow("Duration cost", "₹${estimate.hoursCost}")

            if (estimate.materialFee > 0) {
                PriceRow("Organic henna", "₹${estimate.materialFee}")
            }

            if (estimate.discount > 0) {
                PriceRow(
                    label = "Flash discount",
                    value = "−₹${estimate.discount}",
                    valueColor = MeheiSuccess,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "₹${estimate.total}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Deposit (30%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "₹${estimate.deposit}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun PriceRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor,
        )
    }
}
