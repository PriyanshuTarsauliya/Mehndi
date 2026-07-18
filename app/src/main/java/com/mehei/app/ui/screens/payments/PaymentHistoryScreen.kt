package com.mehei.app.ui.screens.payments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mehei.app.domain.model.Payment
import com.mehei.app.domain.model.PaymentStatus
import com.mehei.app.domain.model.PaymentType
import com.mehei.app.domain.model.RefundStatus
import com.mehei.app.ui.theme.MeheiSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    state: PaymentHistoryState,
    onRequestRefund: (paymentId: String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.payments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "💸",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No payments yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Your payment history will appear here once you make your first booking.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        } else {
            // Summary card
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Stats header
                item {
                    PaymentStatsCard(payments = state.payments)
                }

                items(state.payments) { payment ->
                    PaymentCard(
                        payment = payment,
                        onRequestRefund = { onRequestRefund(payment.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentStatsCard(payments: List<Payment>) {
    val totalPaid = payments
        .filter { it.status == PaymentStatus.SUCCESS && it.type != PaymentType.REFUND }
        .sumOf { it.amount }
    val totalRefunded = payments
        .filter { it.refundAmount > 0 }
        .sumOf { it.refundAmount }
    val totalTips = payments
        .filter { it.type == PaymentType.TIP && it.status == PaymentStatus.SUCCESS }
        .sumOf { it.amount }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Total Paid", "₹$totalPaid", MeheiSuccess)
            StatItem("Refunded", "₹$totalRefunded", MaterialTheme.colorScheme.error)
            StatItem("Tips Given", "₹$totalTips", MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PaymentCard(
    payment: Payment,
    onRequestRefund: () -> Unit,
) {
    var showDetails by remember { mutableStateOf(false) }

    Card(
        onClick = { showDetails = !showDetails },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(statusColor(payment.status).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusIcon(payment.status),
                        contentDescription = null,
                        tint = statusColor(payment.status),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = paymentTypeLabel(payment.type),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = payment.createdAt.ifBlank { "Just now" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    val prefix = if (payment.type == PaymentType.REFUND) "+" else "-"
                    Text(
                        text = "${prefix}₹${payment.amount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (payment.type == PaymentType.REFUND) MeheiSuccess else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = payment.status.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor(payment.status),
                    )
                }
            }

            // Expandable details
            AnimatedVisibility(
                visible = showDetails,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider()
                    Spacer(Modifier.height(4.dp))
                    
                    DetailRow("Payment ID", payment.razorpayPaymentId ?: "N/A")
                    DetailRow("Method", payment.method.name.replace("_", " "))
                    DetailRow("Booking", payment.bookingId.take(8).uppercase())
                    
                    if (payment.refundAmount > 0) {
                        DetailRow("Refund Amount", "₹${payment.refundAmount}")
                        DetailRow("Refund Status", payment.refundStatus.name.replace("_", " "))
                    }

                    // Refund Request Button
                    if (payment.status == PaymentStatus.SUCCESS &&
                        payment.type == PaymentType.DEPOSIT &&
                        payment.refundStatus == RefundStatus.NONE
                    ) {
                        Spacer(Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = onRequestRefund,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Request Refund")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun statusColor(status: PaymentStatus): Color {
    return when (status) {
        PaymentStatus.SUCCESS -> MeheiSuccess
        PaymentStatus.FAILED -> MaterialTheme.colorScheme.error
        PaymentStatus.PENDING, PaymentStatus.PROCESSING -> Color(0xFFF59E0B)
        PaymentStatus.REFUNDED -> Color(0xFF3B82F6)
        PaymentStatus.PARTIALLY_REFUNDED -> Color(0xFF8B5CF6)
    }
}

@Composable
private fun statusIcon(status: PaymentStatus) = when (status) {
    PaymentStatus.SUCCESS -> Icons.Filled.CheckCircle
    PaymentStatus.FAILED -> Icons.Filled.Error
    PaymentStatus.PENDING, PaymentStatus.PROCESSING -> Icons.Filled.HourglassBottom
    PaymentStatus.REFUNDED -> Icons.Filled.ArrowDownward
    PaymentStatus.PARTIALLY_REFUNDED -> Icons.Filled.ArrowUpward
}

private fun paymentTypeLabel(type: PaymentType) = when (type) {
    PaymentType.DEPOSIT -> "Booking Deposit"
    PaymentType.REMAINING -> "Session Balance"
    PaymentType.TIP -> "Artist Tip"
    PaymentType.REFUND -> "Refund"
}

data class PaymentHistoryState(
    val isLoading: Boolean = false,
    val payments: List<Payment> = emptyList(),
)
