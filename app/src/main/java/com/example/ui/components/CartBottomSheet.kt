package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CartItem
import com.example.model.LanguageMode
import com.example.ui.theme.DhabaGold
import com.example.ui.theme.DhabaGreenDark
import com.example.ui.theme.DhabaRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBottomSheet(
    tableNumber: Int,
    cartItems: List<CartItem>,
    customerName: String,
    orderNotes: String,
    languageMode: LanguageMode,
    onCustomerNameChange: (String) -> Unit,
    onOrderNotesChange: (String) -> Unit,
    onIncreaseQuantity: (Int) -> Unit,
    onDecreaseQuantity: (Int) -> Unit,
    onClearCart: () -> Unit,
    onPlaceOrder: (Int) -> Unit,
    onPayBillDirect: (Int) -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTip by remember { mutableIntStateOf(30) }

    val subtotal = cartItems.sumOf { it.totalPrice }
    val gst = (subtotal * 0.05).toInt()
    val grandTotal = subtotal + gst + selectedTip

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
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
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "Bill",
                        tint = DhabaRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "आपका ऑर्डर (Your Order)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "टेबल / Table #$tableNumber",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DhabaRed
                        )
                    }
                }

                if (cartItems.isNotEmpty()) {
                    IconButton(
                        onClick = onClearCart,
                        modifier = Modifier.testTag("clear_cart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear Cart",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (cartItems.isEmpty()) {
                // Empty Cart State
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🍽️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "आपकी थाली खाली है",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "मेनू से स्वादिष्ट खाना जोड़ें",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // List of Cart Items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(cartItems, key = { it.item.id }) { cartItem ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = cartItem.item.emoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (languageMode == LanguageMode.ENGLISH) cartItem.item.nameEn else cartItem.item.nameHi,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "₹${cartItem.item.price} x ${cartItem.quantity} = ₹${cartItem.totalPrice}",
                                            fontSize = 12.sp,
                                            color = DhabaGreenDark,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (cartItem.specialNote.isNotBlank()) {
                                            Text(
                                                text = "📝 ${cartItem.specialNote}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // Stepper
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = DhabaRed
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        IconButton(
                                            onClick = { onDecreaseQuantity(cartItem.item.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "-", tint = Color.White, modifier = Modifier.size(12.dp))
                                        }

                                        Text(
                                            text = "${cartItem.quantity}",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )

                                        IconButton(
                                            onClick = { onIncreaseQuantity(cartItem.item.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "+", tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Customer Name & Notes Input
                OutlinedTextField(
                    value = customerName,
                    onValueChange = onCustomerNameChange,
                    label = { Text("ग्राहक का नाम (Customer Name)", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cart_customer_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = orderNotes,
                    onValueChange = onOrderNotesChange,
                    label = { Text("किचन के लिए विशेष नोट (उदा: कम मिर्च, गरमा गरम)", fontSize = 11.sp) },
                    placeholder = { Text("Special cooking note for chef...", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cart_order_notes_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tip Options Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "वेटर टिप / Service Tip:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0, 20, 30, 50).forEach { tip ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedTip == tip) DhabaGold else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable { selectedTip = tip }
                                    .testTag("tip_btn_$tip")
                            ) {
                                Text(
                                    text = if (tip == 0) "कोई नहीं" else "₹$tip",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTip == tip) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTip == tip) Color.Black else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // Bill Breakdown
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "आइटम सब-टोटल (Subtotal)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "₹$subtotal", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "जीएसटी (GST 5%)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "₹$gst", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (selectedTip > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "टिप (Tip)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "₹$selectedTip", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "कुल देय राशि (Grand Total)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "₹$grandTotal", fontSize = 18.sp, fontWeight = FontWeight.Black, color = DhabaGreenDark)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Place Order Button
                Button(
                    onClick = { onPlaceOrder(selectedTip) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("place_order_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DhabaRed)
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "किचन में ऑर्डर भेजें • ₹$grandTotal",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Direct Pay Bill Button
                androidx.compose.material3.OutlinedButton(
                    onClick = { onPayBillDirect(selectedTip) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("cart_pay_bill_direct_btn"),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, DhabaGreenDark),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DhabaGreenDark)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = DhabaGreenDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "सीधे UPI / QR से बिल भुगतान करें (Pay Bill)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DhabaGreenDark
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
