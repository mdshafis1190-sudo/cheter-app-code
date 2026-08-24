package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.ShopInfo
import com.example.model.TableBillSummary
import com.example.ui.theme.CheterCyan
import com.example.ui.theme.CheterPurple
import com.example.ui.theme.DhabaGold
import com.example.ui.theme.DhabaGreen
import com.example.ui.theme.DhabaGreenDark
import com.example.ui.theme.DhabaRed

@Composable
fun DynamicTableBillDialog(
    billSummary: TableBillSummary,
    shopInfo: ShopInfo = ShopInfo(),
    onPayOnlineUpi: () -> Unit,
    onRequestCash: () -> Unit,
    onDismiss: () -> Unit,
    onSelectTable: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.UPI) }

    val upiVpa = shopInfo.upiId.ifBlank { "cheter.dine@okhdfcbank" }
    val merchantName = shopInfo.shopName.ifBlank { "CHETER Royal Dhaba" }
    val upiPayload = "upi://pay?pa=$upiVpa&pn=${Uri.encode(merchantName)}&am=${billSummary.grandTotal}&cu=INR&tn=Table_${billSummary.tableNumber}_Bill"
    val dynamicQrApiUrl = if (shopInfo.customUpiQrUrl.isNotBlank()) {
        shopInfo.customUpiQrUrl
    } else {
        "https://api.qrserver.com/v1/create-qr-code/?data=${Uri.encode(upiPayload)}&size=300x300&color=0B0F19"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = DhabaRed.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DhabaRed)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = "Bill",
                                tint = DhabaRed,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "सक्रिय टेबल बिल (Table Bill)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "टेबल #${billSummary.tableNumber} • ${billSummary.customerName}",
                                fontSize = 12.sp,
                                color = DhabaRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_pay_bill_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Table Selector Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "टेबल बदलें (1-100):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val allBillTables = remember { List(100) { it + 1 } }
                    for (tableNum in allBillTables) {
                        val isSelected = tableNum == billSummary.tableNumber
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) DhabaRed else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { onSelectTable(tableNum) }
                                .testTag("select_table_pill_$tableNum")
                        ) {
                            Text(
                                text = "T$tableNum",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Customer ID & Phone Info Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F2F6),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👤 ID: ${billSummary.customerId}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2F3542)
                        )
                        Text(
                            text = "📱 Phone: ${billSummary.customerPhone}",
                            fontSize = 11.sp,
                            color = Color(0xFF57606F),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                var showItemizedReceiptModal by remember { mutableStateOf(false) }

                // Bill Breakdown Card with Detailed Itemized Summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "विस्तृत आइटम बिल (Itemized Bill)",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${billSummary.orders.size} KOT ऑर्डर • ${billSummary.totalItemsCount} आइटम",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (billSummary.isSettled) DhabaGreenDark else if (billSummary.isCashRequested) DhabaGold else if (billSummary.isFraudUnpaid) DhabaRed else Color(0xFFE67E22)
                            ) {
                                Text(
                                    text = if (billSummary.isSettled) "✅ Paid" else if (billSummary.isCashRequested) "💵 Cash Req." else if (billSummary.isFraudUnpaid) "🚨 Marked Unpaid" else "⏳ Unpaid Tab",
                                    color = if (billSummary.isCashRequested) Color(0xFF1E273D) else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Table Column Headers: ITEM | QTY & RATE | TOTAL
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "व्यंजन (Item)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "दर (Qty x Rate = Total)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // List of items with full itemized breakdown (Quantity, unit price, total cost per item)
                        billSummary.consolidatedItems.forEach { consolidated ->
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
                                        Text(
                                            text = if (consolidated.menuItem.isVeg) "🟢" else "🔴",
                                            fontSize = 9.5.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column {
                                            Text(
                                                text = consolidated.menuItem.nameHi,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (consolidated.menuItem.nameEn.isNotBlank() && consolidated.menuItem.nameEn != consolidated.menuItem.nameHi) {
                                                Text(
                                                    text = consolidated.menuItem.nameEn,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${consolidated.quantity} × ₹${consolidated.unitPrice} = ₹${consolidated.totalPrice}",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                if (consolidated.notes.any { it.isNotBlank() }) {
                                    Text(
                                        text = "  ↳ 📝 ${consolidated.notes.filter { it.isNotBlank() }.joinToString()}",
                                        fontSize = 9.5.sp,
                                        color = Color(0xFFD97706),
                                        modifier = Modifier.padding(start = 14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Math breakdown
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "सब-टोटल (Subtotal):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "₹${billSummary.totalSubtotal}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "जीएसटी (5% GST):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "₹${billSummary.totalGst}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            if (billSummary.totalTips > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "वेटर टिप (Tip):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "₹${billSummary.totalTips}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DhabaGreenDark)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "कुल देय राशि (Grand Total):", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "₹${billSummary.grandTotal}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DhabaGreenDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Button to view & print full authentic receipt
                        OutlinedButton(
                            onClick = { showItemizedReceiptModal = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("view_thermal_receipt_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp), tint = DhabaRed)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "🖨️ विस्तृत टैक्स इनवॉइस / रसीद देखें", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = DhabaRed)
                        }
                    }
                }

                if (showItemizedReceiptModal) {
                    ItemizedReceiptDialog(
                        billSummary = billSummary,
                        shopInfo = shopInfo,
                        onDismiss = { showItemizedReceiptModal = false },
                        onPayBillClick = if (!billSummary.isSettled) onPayOnlineUpi else null
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Method Selector Tabs (ONLY 2 SIMPLE OPTIONS: Online UPI vs Pay via Cash)
                Text(
                    text = "💳 भुगतान का तरीका चुनें (Payment Method):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Online Payment (UPI)
                    val isUpiSelected = selectedPaymentMethod == PaymentMethod.UPI
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isUpiSelected) DhabaRed else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isUpiSelected) DhabaRed else Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPaymentMethod = PaymentMethod.UPI }
                            .testTag("payment_method_UPI")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "📱", fontSize = 22.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Online Payment (UPI)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUpiSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "QR & UPI Apps",
                                fontSize = 9.5.sp,
                                color = if (isUpiSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // 2. Pay via Cash
                    val isCashSelected = selectedPaymentMethod == PaymentMethod.CASH
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCashSelected) DhabaGold else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isCashSelected) DhabaGold else Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPaymentMethod = PaymentMethod.CASH }
                            .testTag("payment_method_CASH")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "💵", fontSize = 22.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pay via Cash",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCashSelected) Color(0xFF1E273D) else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Collect at Table",
                                fontSize = 9.5.sp,
                                color = if (isCashSelected) Color(0xFF1E273D).copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Container
                when (selectedPaymentMethod) {
                    PaymentMethod.UPI -> {
                        // Dynamic UPI QR Display & App Launcher
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📱 Scan & Pay with any UPI App",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E273D)
                                )
                                Text(
                                    text = "Google Pay • PhonePe • Paytm • BHIM • CRED",
                                    fontSize = 11.sp,
                                    color = Color(0xFF5F6368)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // QR Code Box
                                Box(
                                    modifier = Modifier
                                        .size(175.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(2.dp, DhabaRed, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = dynamicQrApiUrl,
                                        contentDescription = "Dynamic UPI QR Code",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "UPI VPA: $upiVpa",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF333333)
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", upiVpa))
                                            Toast.makeText(context, "UPI ID कॉपी हो गया!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy UPI ID", modifier = Modifier.size(14.dp), tint = DhabaRed)
                                    }
                                }

                                Text(
                                    text = "देय राशि: ₹${billSummary.grandTotal}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DhabaGreenDark
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // 2. Prominent Share Payment Link & QR Section
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFEBF3FC),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBED8F9)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Share Payment Link button
                                            Button(
                                                onClick = {
                                                    sharePaymentDetails(
                                                        context = context,
                                                        billSummary = billSummary,
                                                        upiPayload = upiPayload,
                                                        dynamicQrApiUrl = dynamicQrApiUrl,
                                                        upiVpa = upiVpa
                                                    )
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(38.dp)
                                                    .testTag("share_payment_link_button"),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                            ) {
                                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Share Payment Link",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }

                                            // WhatsApp Quick Share
                                            Button(
                                                onClick = {
                                                    sharePaymentDetails(
                                                        context = context,
                                                        billSummary = billSummary,
                                                        upiPayload = upiPayload,
                                                        dynamicQrApiUrl = dynamicQrApiUrl,
                                                        upiVpa = upiVpa,
                                                        specificPackage = "com.whatsapp"
                                                    )
                                                },
                                                modifier = Modifier
                                                    .height(38.dp)
                                                    .testTag("share_whatsapp_button"),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                                            ) {
                                                Text(text = "💬 WhatsApp", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Share link or QR with friends at table to split/pay via any app",
                                            fontSize = 9.5.sp,
                                            color = Color(0xFF4A6B82),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // 1. UPI Intent Direct App Launchers
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "⚡ Direct App Pay (इस फ़ोन में खोलें):",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF333333),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )

                                    // App launcher grid / row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // GPay
                                        UpiAppButton(
                                            appName = "GPay",
                                            badgeColor = Color(0xFF4285F4),
                                            textColor = Color.White,
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                launchUpiApp(
                                                    context = context,
                                                    packageName = "com.google.android.apps.nbu.paisa.user",
                                                    appName = "Google Pay",
                                                    uriString = upiPayload,
                                                    grandTotal = billSummary.grandTotal
                                                )
                                            }
                                        )

                                        // PhonePe
                                        UpiAppButton(
                                            appName = "PhonePe",
                                            badgeColor = Color(0xFF5F259F),
                                            textColor = Color.White,
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                launchUpiApp(
                                                    context = context,
                                                    packageName = "com.phonepe.app",
                                                    appName = "PhonePe",
                                                    uriString = upiPayload,
                                                    grandTotal = billSummary.grandTotal
                                                )
                                            }
                                        )

                                        // Paytm
                                        UpiAppButton(
                                            appName = "Paytm",
                                            badgeColor = Color(0xFF002970),
                                            textColor = Color.White,
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                launchUpiApp(
                                                    context = context,
                                                    packageName = "net.one97.paytm",
                                                    appName = "Paytm",
                                                    uriString = upiPayload,
                                                    grandTotal = billSummary.grandTotal
                                                )
                                            }
                                        )

                                        // BHIM
                                        UpiAppButton(
                                            appName = "BHIM",
                                            badgeColor = Color(0xFF005B94),
                                            textColor = Color.White,
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                launchUpiApp(
                                                    context = context,
                                                    packageName = "in.org.npci.upiapp",
                                                    appName = "BHIM",
                                                    uriString = upiPayload,
                                                    grandTotal = billSummary.grandTotal
                                                )
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Any / Other UPI Apps Button
                                    OutlinedButton(
                                        onClick = {
                                            launchUpiApp(
                                                context = context,
                                                packageName = null,
                                                appName = "Any UPI App",
                                                uriString = upiPayload,
                                                grandTotal = billSummary.grandTotal
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(34.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB0BEC5)),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = "🚀 Open Any Installed UPI App (Cred / Amazon / etc.)",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF37474F)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Online Payment Confirm Button
                        Button(
                            onClick = onPayOnlineUpi,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("confirm_pay_upi_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DhabaGreenDark)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "✅ ऑनलाइन भुगतान पूर्ण करें • ₹${billSummary.grandTotal}",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    PaymentMethod.CASH -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DhabaGold.copy(alpha = 0.15f)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, DhabaGold)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(DhabaGold.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "💵", fontSize = 24.sp)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (billSummary.isCashRequested) {
                                    // Active Waiter Alert State
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = DhabaGreenDark,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = DhabaGold, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Waiter is arriving at Table #${billSummary.tableNumber} to collect cash.",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "कृपया ₹${billSummary.grandTotal} नकद तैयार रखें। वेटर सिस्टम में 'Cash Received' दर्ज करेगा।",
                                                fontSize = 11.5.sp,
                                                color = Color.White.copy(alpha = 0.9f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "नकद भुगतान (Pay via Cash)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E273D)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "वेटर आपकी टेबल #${billSummary.tableNumber} पर ₹${billSummary.grandTotal} नकद लेने आएगा।",
                                        fontSize = 12.sp,
                                        color = Color(0xFF4A4A4A),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = onRequestCash,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("request_cash_collection_btn"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD35400))
                                    ) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "वेटर को कैश लेने के लिए बुलाएं (Request Cash)",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpiAppButton(
    appName: String,
    badgeColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(36.dp)
            .clickable(onClick = onClick)
            .testTag("upi_app_button_${appName.lowercase()}"),
        shape = RoundedCornerShape(8.dp),
        color = badgeColor,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = appName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun launchUpiApp(
    context: Context,
    packageName: String?,
    appName: String,
    uriString: String,
    grandTotal: Int
) {
    try {
        val uri = Uri.parse(uriString)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (!packageName.isNullOrBlank()) {
            intent.setPackage(packageName)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            // Fallback: try opening with generic chooser if specific package isn't installed
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
            val chooser = Intent.createChooser(fallbackIntent, "Pay ₹$grandTotal via $appName / UPI")
            context.startActivity(chooser)
        } catch (ex: Exception) {
            Toast.makeText(
                context,
                "$appName is not installed. You can scan the QR code or copy the UPI ID.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

private fun sharePaymentDetails(
    context: Context,
    billSummary: TableBillSummary,
    upiPayload: String,
    dynamicQrApiUrl: String,
    upiVpa: String,
    specificPackage: String? = null
) {
    val shareText = """
        🍽️ *CHETER - Restaurant & Lounge*
        📋 *Table #${billSummary.tableNumber} Order Bill*
        ━━━━━━━━━━━━━━━━━━━━
        👤 Guest: ${billSummary.customerName.ifBlank { "Table #${billSummary.tableNumber}" }}
        💰 *Total Amount to Pay: ₹${billSummary.grandTotal}*
        ━━━━━━━━━━━━━━━━━━━━

        👉 *Click to Pay via any UPI App (GPay / PhonePe / Paytm):*
        $upiPayload

        🆔 *UPI ID:* $upiVpa
        🖼️ *Scan Dynamic QR Code:*
        $dynamicQrApiUrl

        Thank you for dining at CHETER! ✨
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "CHETER Restaurant Table #${billSummary.tableNumber} Bill - ₹${billSummary.grandTotal}")
        type = "text/plain"
        if (!specificPackage.isNullOrBlank()) {
            setPackage(specificPackage)
        }
    }

    try {
        if (!specificPackage.isNullOrBlank()) {
            context.startActivity(sendIntent)
        } else {
            context.startActivity(Intent.createChooser(sendIntent, "Share Payment Link & QR via..."))
        }
    } catch (e: Exception) {
        if (!specificPackage.isNullOrBlank()) {
            try {
                sendIntent.setPackage(null)
                context.startActivity(Intent.createChooser(sendIntent, "Share Payment Link via..."))
            } catch (ex: Exception) {
                Toast.makeText(context, "App not found to share. Please use standard share.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Could not open share menu", Toast.LENGTH_SHORT).show()
        }
    }
}
