package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.PaymentStatus
import com.example.model.ShopInfo
import com.example.model.TableBillSummary
import com.example.model.TableOrder
import com.example.ui.theme.CheterCyan
import com.example.ui.theme.DhabaGold
import com.example.ui.theme.DhabaGreen
import com.example.ui.theme.DhabaGreenDark
import com.example.ui.theme.DhabaRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ItemizedReceiptDialog(
    billSummary: TableBillSummary,
    selectedOrder: TableOrder? = null,
    shopInfo: ShopInfo = ShopInfo(),
    onDismiss: () -> Unit,
    onPayBillClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // View mode: 0 = Consolidated Active Table Running Bill, 1 = Specific Order KOT
    var viewMode by remember { mutableStateOf(if (selectedOrder != null && billSummary.orders.size > 1) 1 else 0) }
    var currentSelectedOrder by remember(selectedOrder) { mutableStateOf(selectedOrder ?: billSummary.orders.lastOrNull()) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember { dateFormat.format(Date()) }

    // Consolidated or Single Order items computation
    val isRunningTab = viewMode == 0 || billSummary.orders.size <= 1 || currentSelectedOrder == null

    val itemsList = if (isRunningTab) {
        billSummary.consolidatedItems.map { consolidated ->
            ItemReceiptLine(
                nameHi = consolidated.menuItem.nameHi,
                nameEn = consolidated.menuItem.nameEn,
                emoji = consolidated.menuItem.emoji,
                isVeg = consolidated.menuItem.isVeg,
                quantity = consolidated.quantity,
                unitPrice = consolidated.unitPrice,
                totalPrice = consolidated.totalPrice,
                notes = consolidated.notes.filter { it.isNotBlank() }.joinToString(", "),
                kots = consolidated.kotOrderIds
            )
        }
    } else {
        currentSelectedOrder!!.items.map { cartItem ->
            ItemReceiptLine(
                nameHi = cartItem.item.nameHi,
                nameEn = cartItem.item.nameEn,
                emoji = cartItem.item.emoji,
                isVeg = cartItem.item.isVeg,
                quantity = cartItem.quantity,
                unitPrice = cartItem.item.price,
                totalPrice = cartItem.totalPrice,
                notes = cartItem.specialNote,
                kots = listOf(currentSelectedOrder!!.orderId)
            )
        }
    }

    val subtotal = if (isRunningTab) billSummary.totalSubtotal else currentSelectedOrder!!.subtotal
    val gst = if (isRunningTab) billSummary.totalGst else currentSelectedOrder!!.gst
    val tip = if (isRunningTab) billSummary.totalTips else currentSelectedOrder!!.tip
    val grandTotal = if (isRunningTab) billSummary.grandTotal else currentSelectedOrder!!.grandTotal
    val billNumber = if (isRunningTab) "TB-${billSummary.tableNumber}-${(1000..9999).random()}" else "ORD-${currentSelectedOrder!!.orderId}"

    fun buildReceiptText(): String {
        val sb = StringBuilder()
        sb.appendLine("========================================")
        sb.appendLine("       ${shopInfo.shopName.ifBlank { "CHETER RESTAURANT & LOUNGE" }}")
        if (shopInfo.address.isNotBlank()) sb.appendLine("       ${shopInfo.address}")
        if (shopInfo.phone.isNotBlank()) sb.appendLine("       Tel: ${shopInfo.phone}")
        sb.appendLine("========================================")
        sb.appendLine("Date: $formattedDate")
        sb.appendLine("Bill / Invoice #: $billNumber")
        sb.appendLine("Table: #${billSummary.tableNumber} | Customer: ${billSummary.customerName}")
        sb.appendLine("Cust ID: ${billSummary.customerId} | Phone: ${billSummary.customerPhone}")
        sb.appendLine("----------------------------------------")
        sb.appendLine(String.format("%-18s %3s %6s %7s", "ITEM", "QTY", "RATE", "TOTAL"))
        sb.appendLine("----------------------------------------")
        itemsList.forEach { line ->
            val name = if (line.nameEn.isNotBlank()) line.nameEn else line.nameHi
            val truncatedName = if (name.length > 18) name.substring(0, 15) + "..." else name
            sb.appendLine(String.format("%-18s %3d  ₹%-4d  ₹%-5d", truncatedName, line.quantity, line.unitPrice, line.totalPrice))
        }
        sb.appendLine("----------------------------------------")
        sb.appendLine(String.format("%-28s ₹%d", "Subtotal:", subtotal))
        sb.appendLine(String.format("%-28s ₹%d", "GST (5%):", gst))
        if (tip > 0) {
            sb.appendLine(String.format("%-28s ₹%d", "Tip / Gratuity:", tip))
        }
        sb.appendLine("========================================")
        sb.appendLine(String.format("%-28s ₹%d", "GRAND TOTAL:", grandTotal))
        sb.appendLine("========================================")
        sb.appendLine("Status: ${if (billSummary.isSettled) "PAID IN FULL" else "RUNNING TAB / UNPAID"}")
        sb.appendLine("Thank you for dining with us! Visit again.")
        return sb.toString()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
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
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = DhabaRed.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DhabaRed)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = "Receipt",
                                tint = DhabaRed,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "विस्तृत आइटम बिल (Itemized Bill)",
                                fontSize = 16.sp,
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
                        modifier = Modifier.testTag("close_itemized_receipt_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle between Running Tab vs KOT Rounds (if multiple orders exist)
                if (billSummary.orders.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (viewMode == 0) DhabaRed else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewMode = 0 }
                        ) {
                            Text(
                                text = "🧾 कुल चालू बिल (${billSummary.orders.size} KOTs)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (viewMode == 0) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (viewMode == 1) DhabaRed else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewMode = 1 }
                        ) {
                            Text(
                                text = "📦 KOT राउंड-वार",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (viewMode == 1) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    if (viewMode == 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            billSummary.orders.forEachIndexed { idx, ord ->
                                val isSelected = currentSelectedOrder?.orderId == ord.orderId
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) DhabaGold else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { currentSelectedOrder = ord }
                                ) {
                                    Text(
                                        text = "KOT #${idx + 1} (${ord.items.size})",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF1E273D) else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Authentic Thermal Restaurant Receipt Paper Look
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFDFE)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Receipt Header
                        Text(
                            text = shopInfo.shopName.ifBlank { "CHETER RESTAURANT & LOUNGE" },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E293B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (shopInfo.address.isNotBlank()) {
                            Text(
                                text = shopInfo.address,
                                fontSize = 10.5.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Text(
                            text = "GSTIN: 07AAACH1234F1Z8 • FSSAI: 10821001000123",
                            fontSize = 9.5.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        DashedDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Invoice details grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "टेबल / Table: #${billSummary.tableNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text(text = "ग्राहक / Guest: ${billSummary.customerName}", fontSize = 10.5.sp, color = Color(0xFF475569))
                                Text(text = "फोन: ${billSummary.customerPhone}", fontSize = 10.5.sp, color = Color(0xFF475569))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Bill: $billNumber", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DhabaRed)
                                Text(text = formattedDate, fontSize = 10.sp, color = Color(0xFF64748B))
                                Text(text = "ID: ${billSummary.customerId}", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        DashedDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Table Headers: ITEM | QTY | RATE | TOTAL
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "व्यंजन (ITEM)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), modifier = Modifier.weight(1.8f))
                            Text(text = "QTY", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), textAlign = TextAlign.Center, modifier = Modifier.weight(0.6f))
                            Text(text = "दर (RATE)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
                            Text(text = "योग (TOTAL)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Full breakdown list of all ordered dishes with quantity, unit price, and total cost per item
                        itemsList.forEach { line ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1.8f)
                                    ) {
                                        Text(text = if (line.isVeg) "🟢" else "🔴", fontSize = 9.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column {
                                            Text(
                                                text = line.nameHi,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            if (line.nameEn.isNotBlank() && line.nameEn != line.nameHi) {
                                                Text(
                                                    text = line.nameEn,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "${line.quantity}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F172A),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(0.6f)
                                    )

                                    Text(
                                        text = "₹${line.unitPrice}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569),
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(0.9f)
                                    )

                                    Text(
                                        text = "₹${line.totalPrice}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (line.notes.isNotBlank()) {
                                    Text(
                                        text = "  ↳ 📝 ${line.notes}",
                                        fontSize = 9.5.sp,
                                        color = Color(0xFFD97706),
                                        modifier = Modifier.padding(start = 14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        DashedDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Math Breakdown: Subtotal, GST, Tip, Grand Total
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "सब-टोटल (Subtotal):", fontSize = 11.sp, color = Color(0xFF475569))
                            Text(text = "₹$subtotal", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "जीएसटी (CGST 2.5% + SGST 2.5%):", fontSize = 11.sp, color = Color(0xFF475569))
                            Text(text = "₹$gst", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                        }
                        if (tip > 0) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "वेटर टिप (Waiter Tip / Gratuity):", fontSize = 11.sp, color = Color(0xFF475569))
                                Text(text = "₹$tip", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DhabaGreenDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        DashedDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Final Grand Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "कुल देय राशि (GRAND TOTAL)", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                Text(text = "सभी टैक्स सहित (Incl. all taxes)", fontSize = 9.5.sp, color = Color(0xFF64748B))
                            }
                            Text(
                                text = "₹$grandTotal",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = DhabaGreenDark
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status pill at bottom of receipt
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (billSummary.isSettled) DhabaGreen.copy(alpha = 0.15f) else Color(0xFFFEF3C7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (billSummary.isSettled) DhabaGreenDark else Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (billSummary.isSettled) "✅ भुगतान संपन्न (PAID)" else "⏳ सक्रिय रनिंग टैब (ACTIVE RUNNING TAB)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (billSummary.isSettled) DhabaGreenDark else Color(0xFFB45309)
                                )
                                Text(
                                    text = "${itemsList.sumOf { it.quantity }} आइटम कुल",
                                    fontSize = 10.5.sp,
                                    color = if (billSummary.isSettled) DhabaGreenDark else Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons: Share, Copy & Pay (if applicable)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Copy Itemized Text
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Restaurant Bill", buildReceiptText())
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "विस्तृत बिल क्लिपबोर्ड पर कॉपी हो गया!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "कॉपी करें", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    // Share Bill via WhatsApp / Intent
                    OutlinedButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, buildReceiptText())
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "बिल साझा करें (Share Bill)")
                            context.startActivity(shareIntent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = DhabaRed)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "शेयर करें", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = DhabaRed)
                    }

                    if (onPayBillClick != null && !billSummary.isSettled) {
                        Button(
                            onClick = {
                                onDismiss()
                                onPayBillClick()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DhabaGreenDark),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Text(text = "💳 बिल भरें (₹$grandTotal)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

data class ItemReceiptLine(
    val nameHi: String,
    val nameEn: String,
    val emoji: String,
    val isVeg: Boolean,
    val quantity: Int,
    val unitPrice: Int,
    val totalPrice: Int,
    val notes: String = "",
    val kots: List<String> = emptyList()
)

@Composable
fun DashedDivider(
    color: Color = Color(0xFFCBD5E1),
    thickness: Float = 1.5f
) {
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
    ) {
        val pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
            strokeWidth = thickness,
            pathEffect = pathEffect
        )
    }
}
