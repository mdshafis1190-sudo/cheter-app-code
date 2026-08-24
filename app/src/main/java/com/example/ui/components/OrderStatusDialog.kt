package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CartItem
import com.example.model.OrderStatus
import com.example.model.TableBillSummary
import com.example.model.TableOrder
import com.example.ui.theme.CheterCyan
import com.example.ui.theme.DhabaGold
import com.example.ui.theme.DhabaGreen
import com.example.ui.theme.DhabaGreenDark
import com.example.ui.theme.DhabaRed
import kotlinx.coroutines.delay

@Composable
fun OrderStatusDialog(
    orders: List<TableOrder>,
    currentTableNumber: Int,
    onDismiss: () -> Unit,
    onPayBillClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (orders.isEmpty()) {
        onDismiss()
        return
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val safeSelectedIndex = selectedTabIndex.coerceIn(0, (orders.size - 1).coerceAtLeast(0))
    val selectedOrder = orders[safeSelectedIndex]
    var showItemizedReceiptModal by remember { mutableStateOf(false) }

    // Table Bill Summary for full running bill consolidation
    val tableBillSummary = remember(orders, currentTableNumber) {
        TableBillSummary(
            tableNumber = currentTableNumber,
            orders = orders,
            customerName = orders.firstOrNull()?.customerName ?: "Customer",
            customerPhone = orders.firstOrNull()?.customerPhone ?: "9876543210",
            customerId = orders.firstOrNull()?.customerId ?: "CUST-4102",
            totalItemsCount = orders.sumOf { ord -> ord.items.sumOf { it.quantity } },
            totalSubtotal = orders.sumOf { it.subtotal },
            totalGst = orders.sumOf { it.gst },
            totalTips = orders.sumOf { it.tip },
            grandTotal = orders.sumOf { it.grandTotal },
            isSettled = orders.all { it.paymentStatus == com.example.model.PaymentStatus.PAID }
        )
    }

    // Live Clock Ticker for real-time countdown
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    val totalActiveAmount = orders.sumOf { it.grandTotal }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .height(680.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "लाइव ऑर्डर ट्रैकिंग (Live Order Status)",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "टेबल #$currentTableNumber • कुल ${orders.size} सक्रिय ऑर्डर",
                            fontSize = 12.sp,
                            color = DhabaRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_order_status_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Multi-Order Tab Selector (if multiple orders exist from the same table)
                if (orders.size > 1) {
                    ScrollableTabRow(
                        selectedTabIndex = safeSelectedIndex,
                        edgePadding = 0.dp,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = DhabaRed,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[safeSelectedIndex]),
                                color = DhabaRed,
                                height = 3.dp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        orders.forEachIndexed { index, ord ->
                            val isSelected = index == safeSelectedIndex
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "ऑर्डर #${ord.orderId}",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) DhabaRed else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when (ord.status) {
                                                OrderStatus.RECEIVED -> DhabaGold.copy(alpha = 0.2f)
                                                OrderStatus.PREPARING -> DhabaRed.copy(alpha = 0.2f)
                                                OrderStatus.READY_TO_SERVE -> Color(0xFF3498DB).copy(alpha = 0.2f)
                                                OrderStatus.SERVED -> DhabaGreen.copy(alpha = 0.2f)
                                            }
                                        ) {
                                            Text(
                                                text = ord.status.titleHi,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (ord.status) {
                                                    OrderStatus.RECEIVED -> Color(0xFFD35400)
                                                    OrderStatus.PREPARING -> DhabaRed
                                                    OrderStatus.READY_TO_SERVE -> Color(0xFF2980B9)
                                                    OrderStatus.SERVED -> DhabaGreenDark
                                                },
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Scrollable Content for Selected Order
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        SingleOrderTrackingCard(
                            order = selectedOrder,
                            currentTime = currentTime
                        )
                    }

                    // Item-wise Live Countdown Timers Section
                    item {
                        ItemWiseDishTimersSection(
                            order = selectedOrder,
                            currentTime = currentTime
                        )
                    }

                    // Order Summary & Instructions with Itemized Breakdown
                    item {
                        OrderBillSummaryCard(
                            order = selectedOrder,
                            billSummary = tableBillSummary,
                            onViewReceipt = { showItemizedReceiptModal = true }
                        )
                    }
                }

                if (showItemizedReceiptModal) {
                    ItemizedReceiptDialog(
                        billSummary = tableBillSummary,
                        selectedOrder = selectedOrder,
                        onDismiss = { showItemizedReceiptModal = false },
                        onPayBillClick = {
                            showItemizedReceiptModal = false
                            onDismiss()
                            onPayBillClick()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onDismiss()
                            onPayBillClick()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("order_status_pay_bill_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DhabaGreenDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (orders.size > 1) "सभी बिल ₹$totalActiveAmount दें" else "💳 बिल भुगतान (₹${selectedOrder.grandTotal})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    androidx.compose.material3.OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(0.9f)
                            .height(48.dp)
                            .testTag("dismiss_order_status_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "मेनू पर जाएं", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// Backward-compatible overload for single order
@Composable
fun OrderStatusDialog(
    order: TableOrder,
    onDismiss: () -> Unit,
    onPayBillClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    OrderStatusDialog(
        orders = listOf(order),
        currentTableNumber = order.tableNumber,
        onDismiss = onDismiss,
        onPayBillClick = onPayBillClick,
        modifier = modifier
    )
}

@Composable
private fun SingleOrderTrackingCard(
    order: TableOrder,
    currentTime: Long
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "status_pulse"
    )

    val isServed = order.status == OrderStatus.SERVED
    val isReady = order.status == OrderStatus.READY_TO_SERVE
    val totalDurationMs = (order.estimatedPrepTimeMinutes * 60 * 1000L).coerceAtLeast(1000L)
    val remainingMs = (order.estimatedReadyTimestamp - currentTime).coerceAtLeast(0L)
    val remainingSeconds = (remainingMs / 1000).toInt()
    val remMins = remainingSeconds / 60
    val remSecs = remainingSeconds % 60
    val isUnder5Min = remainingSeconds < 300 && !isServed && !isReady
    val elapsedMs = (currentTime - order.orderTimestamp).coerceAtLeast(0L)
    val progressFraction = (elapsedMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (order.status) {
                OrderStatus.RECEIVED -> DhabaGold.copy(alpha = 0.12f)
                OrderStatus.PREPARING -> DhabaRed.copy(alpha = 0.10f)
                OrderStatus.READY_TO_SERVE -> Color(0xFF3498DB).copy(alpha = 0.12f)
                OrderStatus.SERVED -> DhabaGreen.copy(alpha = 0.15f)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            when (order.status) {
                OrderStatus.RECEIVED -> DhabaGold
                OrderStatus.PREPARING -> DhabaRed
                OrderStatus.READY_TO_SERVE -> Color(0xFF3498DB)
                OrderStatus.SERVED -> DhabaGreenDark
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Icon & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = when (order.status) {
                            OrderStatus.RECEIVED -> "📋"
                            OrderStatus.PREPARING -> "👨‍🍳"
                            OrderStatus.READY_TO_SERVE -> "🔔"
                            OrderStatus.SERVED -> "🎉"
                        },
                        fontSize = 28.sp,
                        modifier = Modifier.scale(pulseScale)
                    )
                    Column {
                        Text(
                            text = order.status.titleHi,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when (order.status) {
                                OrderStatus.RECEIVED -> Color(0xFFD35400)
                                OrderStatus.PREPARING -> DhabaRed
                                OrderStatus.READY_TO_SERVE -> Color(0xFF2980B9)
                                OrderStatus.SERVED -> DhabaGreenDark
                            }
                        )
                        Text(
                            text = order.status.titleEn,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (order.status) {
                        OrderStatus.RECEIVED -> Color(0xFFD35400)
                        OrderStatus.PREPARING -> DhabaRed
                        OrderStatus.READY_TO_SERVE -> Color(0xFF2980B9)
                        OrderStatus.SERVED -> DhabaGreenDark
                    }
                ) {
                    Text(
                        text = "ऑर्डर #${order.orderId}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Overall Estimated Countdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isServed || isReady) DhabaGreenDark.copy(alpha = 0.12f)
                    else if (isUnder5Min) DhabaRed.copy(alpha = 0.15f)
                    else Color(0xFF1E293B)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (isServed || isReady) DhabaGreenDark else if (isUnder5Min) DhabaRed else CheterCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isServed) "भोजन परोसा गया (Served)" else if (isReady) "व्यंजन तैयार है (Ready to Serve)" else "कुल अनुमानित समय (Total Prep ETA)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isServed || isReady) DhabaGreenDark else if (isUnder5Min) DhabaRed else Color.White
                            )
                        }

                        if (!isServed && !isReady) {
                            Text(
                                text = String.format("%02d:%02d शेष", remMins, remSecs),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isUnder5Min) DhabaRed else CheterCyan
                            )
                        }
                    }

                    if (!isServed && !isReady) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isUnder5Min) DhabaRed else CheterCyan,
                            trackColor = Color(0xFF334155)
                        )
                    } else if (isReady) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🔔 भोजन तैयार है और वेटर द्वारा टेबल पर लाया जा रहा है!",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = DhabaGreenDark,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Step Progress (1 to 4)
            val steps = OrderStatus.entries.toList()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, step ->
                    val isDone = order.status.stepIndex >= step.stepIndex
                    val isCurrent = order.status.stepIndex == step.stepIndex

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = if (isDone) DhabaGreenDark else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (step) {
                                OrderStatus.RECEIVED -> "प्राप्त"
                                OrderStatus.PREPARING -> "तैयारी"
                                OrderStatus.READY_TO_SERVE -> "तैयार"
                                OrderStatus.SERVED -> "परोसा"
                            },
                            fontSize = 9.5.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) DhabaRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemWiseDishTimersSection(
    order: TableOrder,
    currentTime: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = DhabaRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "डिश-वार तैयारी टाइमर (Dish Live Timers)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DhabaRed.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${order.items.size} आइटम",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = DhabaRed,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Item rows with individual countdown timers
            order.items.forEach { cartItem ->
                ItemDishTimerRow(
                    cartItem = cartItem,
                    orderTimestamp = order.orderTimestamp,
                    orderStatus = order.status,
                    currentTime = currentTime
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun ItemDishTimerRow(
    cartItem: CartItem,
    orderTimestamp: Long,
    orderStatus: OrderStatus,
    currentTime: Long
) {
    val prepMin = cartItem.item.prepTimeMin
    val itemDurationMs = (prepMin * 60 * 1000L).coerceAtLeast(1000L)
    val itemReadyTimestamp = orderTimestamp + itemDurationMs
    val remainingMs = (itemReadyTimestamp - currentTime).coerceAtLeast(0L)
    val remainingSeconds = (remainingMs / 1000).toInt()
    val remMins = remainingSeconds / 60
    val remSecs = remainingSeconds % 60

    val isOrderFinished = orderStatus == OrderStatus.SERVED || orderStatus == OrderStatus.READY_TO_SERVE
    val isDishReady = isOrderFinished || remainingSeconds <= 0
    val elapsedMs = (currentTime - orderTimestamp).coerceAtLeast(0L)
    val progressFraction = (elapsedMs.toFloat() / itemDurationMs.toFloat()).coerceIn(0f, 1f)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDishReady) DhabaGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDishReady) DhabaGreenDark.copy(alpha = 0.4f) else Color.LightGray.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = cartItem.item.emoji, fontSize = 20.sp)
                    Column {
                        Text(
                            text = "${cartItem.item.nameHi} x${cartItem.quantity}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "तैयारी समय: ${cartItem.item.prepTimeMin} मिनट • ₹${cartItem.totalPrice}",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Live Badge Status
                if (isDishReady) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DhabaGreenDark
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "व्यंजन तैयार है",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (remainingSeconds < 180) DhabaRed else Color(0xFF1E293B)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (remainingSeconds < 180) Color.White else CheterCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%02d:%02d शेष", remMins, remSecs),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingSeconds < 180) Color.White else CheterCyan
                            )
                        }
                    }
                }
            }

            if (!isDishReady) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (remainingSeconds < 180) DhabaRed else DhabaGold,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OrderBillSummaryCard(
    order: TableOrder,
    billSummary: TableBillSummary? = null,
    onViewReceipt: (() -> Unit)? = null
) {
    var showConsolidatedTab by remember { mutableStateOf(false) }
    val hasMultipleKots = (billSummary?.orders?.size ?: 1) > 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🧾 विस्तृत बिल विवरण (Itemized Bill):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (showConsolidatedTab && hasMultipleKots) "टेबल रनिंग बिल (${billSummary?.orders?.size} KOTs)" else "ऑर्डर #${order.orderId}",
                        fontSize = 11.sp,
                        color = DhabaRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (hasMultipleKots && billSummary != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DhabaGold.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DhabaGold),
                        modifier = Modifier.clickable { showConsolidatedTab = !showConsolidatedTab }
                    ) {
                        Text(
                            text = if (showConsolidatedTab) "📦 केवल यह KOT" else "🧾 कुल रनिंग टैब",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E273D),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(6.dp))

            // Column Header: ITEM | RATE BREAKDOWN | TOTAL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "व्यंजन (Item)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "मात्रा × दर = योग", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (showConsolidatedTab && billSummary != null) {
                // Consolidated list of all items across multiple KOTs
                billSummary.consolidatedItems.forEach { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Text(text = if (item.menuItem.isVeg) "🟢" else "🔴", fontSize = 9.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = item.menuItem.nameHi,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (item.menuItem.nameEn.isNotBlank() && item.menuItem.nameEn != item.menuItem.nameHi) {
                                        Text(
                                            text = item.menuItem.nameEn,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${item.quantity} × ₹${item.unitPrice} = ₹${item.totalPrice}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                // Single current order breakdown (Quantity x Unit Rate = Total Cost per item)
                order.items.forEach { cartItem ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Text(text = if (cartItem.item.isVeg) "🟢" else "🔴", fontSize = 9.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = cartItem.item.nameHi,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (cartItem.item.nameEn.isNotBlank() && cartItem.item.nameEn != cartItem.item.nameHi) {
                                        Text(
                                            text = cartItem.item.nameEn,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${cartItem.quantity} × ₹${cartItem.item.price} = ₹${cartItem.totalPrice}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (cartItem.specialNote.isNotBlank()) {
                            Text(
                                text = "  ↳ 📝 ${cartItem.specialNote}",
                                fontSize = 9.5.sp,
                                color = Color(0xFFD97706),
                                modifier = Modifier.padding(start = 14.dp)
                            )
                        }
                    }
                }
            }

            if (order.specialInstructions.isNotBlank() && !showConsolidatedTab) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📝 विशेष निर्देश: ${order.specialInstructions}",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(6.dp))

            val currentSubtotal = if (showConsolidatedTab && billSummary != null) billSummary.totalSubtotal else order.subtotal
            val currentGst = if (showConsolidatedTab && billSummary != null) billSummary.totalGst else order.gst
            val currentTip = if (showConsolidatedTab && billSummary != null) billSummary.totalTips else order.tip
            val currentGrandTotal = if (showConsolidatedTab && billSummary != null) billSummary.grandTotal else order.grandTotal

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "सबटोटल (Subtotal):", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "₹$currentSubtotal", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "जीएसटी (5% GST):", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "₹$currentGst", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
            }
            if (currentTip > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "वेटर टिप (Tip):", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "₹$currentTip", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = DhabaGreenDark)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "कुल राशि (Grand Total):", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "₹$currentGrandTotal",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = DhabaGreenDark
                )
            }

            if (onViewReceipt != null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onViewReceipt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .testTag("view_itemized_receipt_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(15.dp), tint = DhabaRed)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "🖨️ विस्तृत टैक्स इनवॉइस / रसीद देखें", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DhabaRed)
                }
            }
        }
    }
}
