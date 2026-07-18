package com.mehei.app.ui.screens.explore

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mehei.app.domain.model.Artist
import com.mehei.app.domain.model.ArtistTier
import com.mehei.app.domain.model.SortOption
import com.mehei.app.ui.components.ShimmerArtistList
import com.mehei.app.ui.modifiers.bounceClick
import com.mehei.app.ui.theme.MeheiFlashBadge
import com.mehei.app.ui.theme.MeheiHennaBrown
import com.mehei.app.ui.theme.MeheiSuccess
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    state: ExploreState,
    onEvent: (ExploreEvent) -> Unit,
    onArtistClick: (String) -> Unit,
    onRequestArtistClick: () -> Unit,
    onProfileClick: () -> Unit,
    onFavoritesClick: () -> Unit = {},
    onFlashDealsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLocating by remember { mutableStateOf(false) }
    var showLocationBottomSheet by remember { mutableStateOf(false) }
    var showCityPickerDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.any { it.value }
        if (granted) {
            isLocating = true
            coroutineScope.launch {
                // Fetch both live lat/lng and city
                val location = com.mehei.app.util.LocationHelper.getCurrentLocation(context)
                if (location != null) {
                    onEvent(ExploreEvent.UpdateLocation(location.latitude, location.longitude))
                }
                val city = com.mehei.app.util.LocationHelper.getCurrentCity(context)
                isLocating = false
                if (city != null) {
                    onEvent(ExploreEvent.SelectCity(city))
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (com.mehei.app.util.LocationHelper.hasLocationPermission(context)) {
            // Get live location for distance calculation
            val location = com.mehei.app.util.LocationHelper.getCurrentLocation(context)
            if (location != null) {
                onEvent(ExploreEvent.UpdateLocation(location.latitude, location.longitude))
            }
            // Get city name for display
            val city = com.mehei.app.util.LocationHelper.getCurrentCity(context)
            if (city != null) {
                onEvent(ExploreEvent.SelectCity(city))
            }
        } else {
            showLocationBottomSheet = true
        }
    }

    // --- Location Bottom Sheet ---
    if (showLocationBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLocationBottomSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "Set Location to Find Artists",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "MEHEI needs location access to search for mehendi artists in your area and estimate booking rates accurately.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        showLocationBottomSheet = false
                        locationPermissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .bounceClick(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Use Current Location", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        showLocationBottomSheet = false
                        showCityPickerDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .bounceClick(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Select City Manually", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // --- City Picker Dialog ---
    if (showCityPickerDialog) {
        AlertDialog(
            onDismissRequest = { showCityPickerDialog = false },
            title = { Text("Select City Manually") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val cities = listOf("Mumbai", "Pune", "Delhi", "Bangalore")
                    cities.forEach { city ->
                        Surface(
                            onClick = {
                                onEvent(ExploreEvent.SelectCity(city))
                                showCityPickerDialog = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            color = if (state.selectedCity.equals(city, ignoreCase = true)) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        ) {
                            Text(
                                text = city,
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityPickerDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // --- Main Screen ---
    Scaffold(
        topBar = {
            MeheiTopBar(
                selectedCity = state.selectedCity,
                isLocating = isLocating,
                onLocationClick = {
                    if (com.mehei.app.util.LocationHelper.hasLocationPermission(context)) {
                        isLocating = true
                        coroutineScope.launch {
                            val location = com.mehei.app.util.LocationHelper.getCurrentLocation(context)
                            if (location != null) {
                                onEvent(ExploreEvent.UpdateLocation(location.latitude, location.longitude))
                            }
                            val city = com.mehei.app.util.LocationHelper.getCurrentCity(context)
                            isLocating = false
                            if (city != null) {
                                onEvent(ExploreEvent.SelectCity(city))
                            }
                        }
                    } else {
                        showLocationBottomSheet = true
                    }
                },
                onProfileClick = onProfileClick,
                onFavoritesClick = onFavoritesClick,
                onFlashDealsClick = onFlashDealsClick,
                userName = state.userName
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onRequestArtistClick,
                icon = { Icon(Icons.Filled.FlashOn, contentDescription = "On-Demand") },
                text = { Text("Request Now") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Search Bar
            MeheiSearchBar(
                query = state.searchQuery,
                onQueryChange = { onEvent(ExploreEvent.Search(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // Discover Styles Carousel
            DiscoverStylesCarousel(
                onStyleSelect = { onEvent(ExploreEvent.Search(it)) }
            )

            // Filter Chips + Sort
            FilterChipsRow(
                selectedTier = state.filterTier,
                showFlashOnly = state.showFlashSlotsOnly,
                sortBy = state.sortBy,
                onTierSelected = { onEvent(ExploreEvent.FilterByTier(it)) },
                onFlashToggle = { onEvent(ExploreEvent.ToggleFlashSlots) },
                onSortChange = { onEvent(ExploreEvent.SetSort(it)) },
            )

            // Artist List
            if (state.isLoading) {
                ShimmerArtistList(
                    count = 4,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                )
            } else if (state.artists.isEmpty()) {
                EmptyState(
                    locationDenied = state.locationDenied,
                    onClearFilters = { onEvent(ExploreEvent.ClearFilters) },
                    onChangeLocation = { showCityPickerDialog = true },
                    onEnableLocation = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        count = state.artists.size,
                        key = { state.artists[it].id },
                    ) { index ->
                        val artist = state.artists[index]
                        // Staggered entrance animation
                        val visibleState = remember {
                            MutableTransitionState(false).apply {
                                targetState = true
                            }
                        }
                        
                        AnimatedVisibility(
                            visibleState = visibleState,
                            enter = fadeIn(
                                animationSpec = tween(
                                    durationMillis = 400,
                                    delayMillis = index * 50 // Stagger delay
                                )
                            ) + slideInVertically(
                                initialOffsetY = { 50 },
                                animationSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                )
                            )
                        ) {
                            ArtistCard(
                                artist = artist,
                                onClick = { onArtistClick(artist.id) },
                                onFavoriteClick = { onEvent(ExploreEvent.ToggleFavorite(artist.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Top App Bar — Branded MEHEI header with pulsing location dot
// ═══════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeheiTopBar(
    selectedCity: String,
    isLocating: Boolean,
    onLocationClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onFlashDealsClick: () -> Unit = {},
    userName: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "location_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    TopAppBar(
        title = {
            Column {
                Text(
                    text = userName?.takeIf { it.isNotBlank() }?.let { "Hi, $it" } ?: "MEHEI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLocationClick() }
                ) {
                    // Pulsing location dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                MeheiSuccess.copy(alpha = pulseAlpha)
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    if (isLocating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = selectedCity,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Change location",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onFlashDealsClick) {
                Icon(
                    imageVector = Icons.Filled.FlashOn,
                    contentDescription = "Flash Deals",
                    tint = MeheiFlashBadge, // use the yellow color
                )
            }
            IconButton(onClick = onFavoritesClick) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Saved Artists",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onProfileClick) {
                // Profile avatar placeholder
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary,
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonOutline,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

// ═══════════════════════════════════════════════════════════════
// Premium Search Bar — Filled background, trailing mic icon
// ═══════════════════════════════════════════════════════════════
@Composable
private fun MeheiSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                "Search artists, events, styles...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = "Voice search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
        ),
    )
}

// ═══════════════════════════════════════════════════════════════
// Discover Styles Carousel — Horizontal scrolling style pills
// ═══════════════════════════════════════════════════════════════
@Composable
private fun DiscoverStylesCarousel(
    onStyleSelect: (String) -> Unit
) {
    val styles = listOf(
        "✨ Bridal",
        "🌙 Arabic",
        "◎ Minimalist",
        "🎨 Portrait",
        "🌿 Indo-Western"
    )
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "DISCOVER STYLES",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            items(styles) { style ->
                Surface(
                    onClick = { onStyleSelect(style.substringAfter(" ")) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.bounceClick()
                ) {
                    Text(
                        text = style,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Filter Chips Row — With icons, sort dropdown
// ═══════════════════════════════════════════════════════════════
@Composable
private fun FilterChipsRow(
    selectedTier: ArtistTier?,
    showFlashOnly: Boolean,
    sortBy: SortOption,
    onTierSelected: (ArtistTier?) -> Unit,
    onFlashToggle: () -> Unit,
    onSortChange: (SortOption) -> Unit,
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            contentPadding = PaddingValues(start = 16.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            // Flash Slots chip
            item {
                FilterChip(
                    selected = showFlashOnly,
                    onClick = {},
                    modifier = Modifier.bounceClick { onFlashToggle() },
                    label = { Text("Flash Deals") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.FlashOn,
                            contentDescription = "Flash deals",
                            modifier = Modifier.size(16.dp),
                            tint = if (showFlashOnly) MaterialTheme.colorScheme.onPrimary else MeheiFlashBadge,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }

            // All chip
            item {
                FilterChip(
                    selected = selectedTier == null,
                    onClick = {},
                    modifier = Modifier.bounceClick { onTierSelected(null) },
                    label = { Text("All") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Palette,
                            contentDescription = "All artists",
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }

            // Tier chips with icons
            val tiers = listOf(
                Triple(ArtistTier.MASTER, "Master", Icons.Outlined.WorkspacePremium),
                Triple(ArtistTier.APPRENTICE, "Apprentice", Icons.Filled.Star),
                Triple(ArtistTier.ASSOCIATE, "Associate", Icons.Filled.PersonOutline),
            )
            items(tiers.size) { index ->
                val (tier, label, icon) = tiers[index]
                FilterChip(
                    selected = selectedTier == tier,
                    onClick = {},
                    modifier = Modifier.bounceClick { onTierSelected(tier) },
                    label = { Text(label) },
                    leadingIcon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }

        // Sort dropdown
        Box {
            IconButton(onClick = { showSortMenu = true }) {
                Icon(
                    imageVector = Icons.Filled.SwapVert,
                    contentDescription = "Sort artists",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                val options = listOf(
                    SortOption.NEAREST to "Nearest",
                    SortOption.RATING to "Rating",
                    SortOption.PRICE_LOW to "Price: Low → High",
                    SortOption.PRICE_HIGH to "Price: High → Low",
                )
                options.forEach { (option, label) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (sortBy == option) FontWeight.Bold else FontWeight.Normal,
                                color = if (sortBy == option) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onSortChange(option)
                            showSortMenu = false
                        }
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Artist Card — Enriched with distance, availability, premium feel
// ═══════════════════════════════════════════════════════════════
@Composable
private fun ArtistCard(
    artist: Artist,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .bounceClick { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(56.dp)
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
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = artist.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (artist.tier == ArtistTier.MASTER) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Outlined.WorkspacePremium,
                                    contentDescription = "Master Artist",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating",
                                modifier = Modifier.size(14.dp),
                                tint = MeheiFlashBadge,
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${artist.rating}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = " · ${artist.totalReviews} reviews",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = " · ${artist.experienceYears}yr exp",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Tier badge
                        val tierLabel = when (artist.tier) {
                            ArtistTier.MASTER -> "Master Artist"
                            ArtistTier.APPRENTICE -> "Trained Apprentice"
                            ArtistTier.ASSOCIATE -> "Associate"
                        }
                        Text(
                            text = tierLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MeheiHennaBrown,
                        )
                    }
                }
                
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (artist.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (artist.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (artist.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Flash slot + Availability badges row
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (artist.hasFlashSlots) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MeheiFlashBadge.copy(alpha = 0.15f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FlashOn,
                                contentDescription = "Flash deal available",
                                modifier = Modifier.size(12.dp),
                                tint = MeheiFlashBadge,
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Flash",
                                style = MaterialTheme.typography.labelSmall,
                                color = MeheiFlashBadge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                // Availability chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MeheiSuccess.copy(alpha = 0.12f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MeheiSuccess)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Available Today",
                            style = MaterialTheme.typography.labelSmall,
                            color = MeheiSuccess,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                // Distance badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Distance",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        val distanceText = if (artist.distanceKm != null) {
                            String.format(java.util.Locale.US, "%.1f km", artist.distanceKm)
                        } else {
                            "Nearby"
                        }
                        Text(
                            text = distanceText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bio
            Text(
                text = artist.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Specialties chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(artist.specialties.take(4)) { eventType ->
                    val label = eventType.name.replace('_', ' ')
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        modifier = Modifier.height(32.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price range + Book Now
            val minRate = artist.rateCards.minOfOrNull { it.pricePerHand } ?: 0
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Starting from",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹$minRate / hand",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MeheiSuccess,
                    )
                }
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                ) {
                    Text("Book Now", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Enhanced Empty State — Themed illustration, multiple CTAs
// ═══════════════════════════════════════════════════════════════
@Composable
private fun EmptyState(
    locationDenied: Boolean = false,
    onClearFilters: () -> Unit,
    onChangeLocation: () -> Unit = {},
    onEnableLocation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "empty_float_offset"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Themed illustration — henna cone searching
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            )
                        )
                    )
                    .graphicsLayer { translationY = floatOffset },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "No artists found",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "No artists nearby yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Try expanding your search area or browse all artists",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Primary CTA
            Button(
                onClick = onClearFilters,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .bounceClick(),
            ) {
                Text("Browse All Artists", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary CTA
            OutlinedButton(
                onClick = onChangeLocation,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .bounceClick(),
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Change Location", fontWeight = FontWeight.SemiBold)
            }

            // Tertiary — only if location is denied
            if (locationDenied) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onEnableLocation,
                    modifier = Modifier.bounceClick()
                ) {
                    Text(
                        "Enable Location Services",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
