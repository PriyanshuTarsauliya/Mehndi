package com.mehei.app.ui.screens.checkout

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mehei.app.ui.modifiers.bounceClick
import com.mehei.app.ui.theme.MeheiSuccess
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    state: CheckoutState,
    effects: SharedFlow<CheckoutEffect>,
    onEvent: (CheckoutEvent) -> Unit,
    onPaymentSuccess: (bookingId: String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val activity = androidx.compose.ui.platform.LocalContext.current as android.app.Activity

    LaunchedEffect(effects) {
        effects.collectLatest { effect ->
            when (effect) {
                is CheckoutEffect.NavigateToConfirmation -> onPaymentSuccess(effect.bookingId)
                is CheckoutEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is CheckoutEffect.LaunchRazorpay -> {
                    try {
                        val checkout = com.razorpay.Checkout()
                        checkout.setKeyID("rzp_test_YOUR_KEY_HERE")
                        
                        val options = org.json.JSONObject()
                        options.put("name", "MEHEI")
                        options.put("description", "Booking Deposit")
                        options.put("currency", "INR")
                        options.put("amount", effect.amountPaise)
                        
                        checkout.open(activity, options)
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Error initializing Razorpay: ${e.message}")
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Booking") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
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
                    val animatedDeposit by animateIntAsState(
                        targetValue = state.depositAmount.toInt(),
                        animationSpec = tween(durationMillis = 500),
                        label = "DepositAnimation"
                    )
                    
                    Column {
                        Text(
                            text = "Deposit Due",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "₹$animatedDeposit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MeheiSuccess,
                        )
                    }
                    Button(
                        onClick = { },
                        enabled = !state.isProcessing,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .bounceClick {
                                if (!state.isProcessing) onEvent(CheckoutEvent.ConfirmPayment)
                            },
                        contentPadding = PaddingValues(horizontal = 32.dp),
                    ) {
                        if (state.isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Pay & Book",
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
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Summary Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Booking Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Artist ID: ${state.artistId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Schedule Section
            SectionTitle("When is the event?")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = state.selectedDate,
                    onValueChange = { onEvent(CheckoutEvent.DateChanged(it)) },
                    label = { Text("Date") },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                    value = state.selectedTime,
                    onValueChange = { onEvent(CheckoutEvent.TimeChanged(it)) },
                    label = { Text("Time") },
                    leadingIcon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                )
            }

            // Location Section
            SectionTitle("Where should the artist go?")
            OutlinedTextField(
                value = state.address,
                onValueChange = { onEvent(CheckoutEvent.AddressChanged(it)) },
                label = { Text("Full Address") },
                leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                isError = state.error != null,
            )

            OutlinedTextField(
                value = state.landmark,
                onValueChange = { onEvent(CheckoutEvent.LandmarkChanged(it)) },
                label = { Text("Landmark (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )

            // Policy Section
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cancellation Policy",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The 30% deposit (₹${state.depositAmount}) is non-refundable. Cancellations made less than 48 hours before the event may incur a 50% charge of the total estimate.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

