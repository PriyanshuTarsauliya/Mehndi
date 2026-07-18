package com.mehei.app.ui.screens.checkout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mehei.app.domain.model.PaymentMethod
import com.mehei.app.domain.model.RefundPolicy
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                ) {
                    // Final total breakdown
                    val finalAmount = state.depositAmount - state.couponDiscount + state.tipAmount
                    if (state.couponDiscount > 0 || state.tipAmount > 0) {
                        Column(
                            modifier = Modifier.padding(bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Deposit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${state.depositAmount}", style = MaterialTheme.typography.bodySmall)
                            }
                            if (state.couponDiscount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Coupon discount", style = MaterialTheme.typography.bodySmall, color = MeheiSuccess)
                                    Text("-₹${state.couponDiscount}", style = MaterialTheme.typography.bodySmall, color = MeheiSuccess)
                                }
                            }
                            if (state.tipAmount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tip for artist", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("+₹${state.tipAmount}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val animatedDeposit by animateIntAsState(
                            targetValue = finalAmount.coerceAtLeast(0),
                            animationSpec = tween(durationMillis = 500),
                            label = "DepositAnimation"
                        )
                        
                        Column {
                            Text(
                                text = "Total Due",
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

            // ═══════════════════════════════════════════
            // Payment Method Selector
            // ═══════════════════════════════════════════
            SectionTitle("Payment Method")
            PaymentMethodSelector(
                selectedMethod = state.selectedPaymentMethod,
                onMethodSelect = { onEvent(CheckoutEvent.PaymentMethodChanged(it)) }
            )

            // ═══════════════════════════════════════════
            // Coupon Code
            // ═══════════════════════════════════════════
            CouponSection(
                couponCode = state.couponCode,
                couponDiscount = state.couponDiscount,
                isCouponApplied = state.isCouponApplied,
                couponError = state.couponError,
                onCouponChanged = { onEvent(CheckoutEvent.CouponChanged(it)) },
                onApplyCoupon = { onEvent(CheckoutEvent.ApplyCoupon) },
                onRemoveCoupon = { onEvent(CheckoutEvent.RemoveCoupon) },
            )

            // ═══════════════════════════════════════════
            // Tip Section
            // ═══════════════════════════════════════════
            TipSection(
                selectedTip = state.tipAmount,
                onTipChanged = { onEvent(CheckoutEvent.TipChanged(it)) },
            )

            // ═══════════════════════════════════════════
            // Refund & Cancellation Policy
            // ═══════════════════════════════════════════
            RefundPolicyCard(
                depositAmount = state.depositAmount,
            )

            // Secure Payment Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Verified,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MeheiSuccess
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Secured by Razorpay · 256-bit SSL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Payment Method Selector
// ═══════════════════════════════════════════════════════════════
@Composable
private fun PaymentMethodSelector(
    selectedMethod: PaymentMethod,
    onMethodSelect: (PaymentMethod) -> Unit,
) {
    val methods = listOf(
        Triple(PaymentMethod.UPI, "UPI / Google Pay", Icons.Filled.QrCode2),
        Triple(PaymentMethod.CREDIT_CARD, "Credit Card", Icons.Filled.CreditCard),
        Triple(PaymentMethod.DEBIT_CARD, "Debit Card", Icons.Filled.CreditCard),
        Triple(PaymentMethod.NET_BANKING, "Net Banking", Icons.Filled.AccountBalance),
        Triple(PaymentMethod.WALLET, "Wallet", Icons.Filled.Wallet),
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        methods.forEach { (method, label, icon) ->
            PaymentMethodChip(
                icon = icon,
                label = label,
                isSelected = selectedMethod == method,
                onClick = { onMethodSelect(method) }
            )
        }
    }
}

@Composable
private fun PaymentMethodChip(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Coupon Section
// ═══════════════════════════════════════════════════════════════
@Composable
private fun CouponSection(
    couponCode: String,
    couponDiscount: Int,
    isCouponApplied: Boolean,
    couponError: String?,
    onCouponChanged: (String) -> Unit,
    onApplyCoupon: () -> Unit,
    onRemoveCoupon: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Have a coupon?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (isCouponApplied) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MeheiSuccess.copy(alpha = 0.1f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MeheiSuccess, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            couponCode.uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MeheiSuccess
                        )
                        Text(
                            "Saving ₹$couponDiscount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MeheiSuccess
                        )
                    }
                    TextButton(onClick = onRemoveCoupon) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    OutlinedTextField(
                        value = couponCode,
                        onValueChange = onCouponChanged,
                        placeholder = { Text("Enter code") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        isError = couponError != null,
                        supportingText = couponError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                    )
                    Button(
                        onClick = onApplyCoupon,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp),
                        enabled = couponCode.isNotBlank()
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Tip Section
// ═══════════════════════════════════════════════════════════════
@Composable
private fun TipSection(
    selectedTip: Int,
    onTipChanged: (Int) -> Unit,
) {
    val tipOptions = listOf(0, 50, 100, 200, 500)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Money, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Tip your artist",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                "100% of the tip goes directly to the artist.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tipOptions.forEach { amount ->
                    val isSelected = selectedTip == amount
                    Surface(
                        onClick = { onTipChanged(amount) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (amount == 0) "None" else "₹$amount",
                            modifier = Modifier.padding(vertical = 10.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Refund Policy Card
// ═══════════════════════════════════════════════════════════════
@Composable
private fun RefundPolicyCard(
    depositAmount: Int,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Policy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Refund & Cancellation Policy",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Always-visible summary
            Text(
                text = "30% deposit (₹$depositAmount) secures your booking. Free cancellation > 48 hrs before session.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Expandable detailed policy
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider()
                    RefundPolicy.policyItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = item.icon,
                                modifier = Modifier.width(28.dp)
                            )
                            Column {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    // Refund timeline visual
                    HorizontalDivider()
                    Text(
                        "Refund Timeline",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    RefundTimelineRow("> 48 hrs", "100%", "₹$depositAmount", MeheiSuccess)
                    RefundTimelineRow("24–48 hrs", "50%", "₹${depositAmount / 2}", Color(0xFFF59E0B))
                    RefundTimelineRow("< 24 hrs", "0%", "₹0", MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun RefundTimelineRow(
    window: String,
    percent: String,
    amount: String,
    color: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(window, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(
            percent,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            amount,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
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
