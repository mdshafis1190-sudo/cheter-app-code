package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.R
import com.example.model.LanguageMode
import com.example.model.TableOrder
import com.example.ui.theme.CheterCyan
import com.example.ui.theme.CheterPurple
import com.example.ui.theme.DhabaGreenDark
import com.example.ui.theme.CheterViolet
import com.example.ui.theme.DhabaGold
import com.example.ui.theme.DhabaGreen
import com.example.ui.theme.DhabaRed

@Composable
fun DhabaHeader(
    tableNumber: Int,
    languageMode: LanguageMode,
    activeOrder: TableOrder?,
    cartCount: Int,
    onLanguageToggle: () -> Unit,
    onTableSelectorClick: () -> Unit = {},
    onTableSelect: (Int) -> Unit = {},
    onPayBillClick: () -> Unit = {},
    onAddDishClick: () -> Unit,
    onActiveOrderClick: () -> Unit,
    onCartClick: () -> Unit,
    onSellerDashboardClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isTableDropdownExpanded by remember { mutableStateOf(false) }
    var selectedZoneIndex by remember { mutableStateOf(0) }
    val all100Tables = remember { List(100) { it + 1 } }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "order_pulse"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF0B0F19),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            // Hero Dining Background Image
            Image(
                painter = painterResource(id = R.drawable.img_restaurant_hero_1787202152649),
                contentDescription = "CHETER Hero",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Neon Dark Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0B0F19).copy(alpha = 0.85f),
                                Color(0xFF131A2A).copy(alpha = 0.88f),
                                Color(0xFF0B0F19).copy(alpha = 0.98f)
                            )
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar with Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Table Badge / Dropdown Selector
                    Box {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF1E273D).copy(alpha = 0.95f),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, CheterCyan),
                            modifier = Modifier
                                .testTag("table_selector_badge")
                                .clickable { isTableDropdownExpanded = !isTableDropdownExpanded }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DinnerDining,
                                    contentDescription = "Table",
                                    tint = CheterCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Table #$tableNumber",
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Table",
                                    tint = CheterCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // 100 Tables Dropdown Menu
                        DropdownMenu(
                            expanded = isTableDropdownExpanded,
                            onDismissRequest = { isTableDropdownExpanded = false },
                            modifier = Modifier
                                .width(280.dp)
                                .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
                                .border(1.dp, CheterCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                // Header in Dropdown
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🪑 टेबल चुनें (1-100)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = CheterCyan.copy(alpha = 0.2f),
                                        modifier = Modifier.clickable {
                                            isTableDropdownExpanded = false
                                            onTableSelectorClick()
                                        }
                                    ) {
                                        Text(
                                            text = "QR देखें 🔍",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CheterCyan,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Quick Range Selector Tabs
                                val zones = listOf("All (1-100)", "1-25", "26-50", "51-75", "76-100")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    zones.forEachIndexed { index, zoneTitle ->
                                        val isZoneActive = selectedZoneIndex == index
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isZoneActive) CheterCyan else Color(0xFF1E293B),
                                            modifier = Modifier.clickable { selectedZoneIndex = index }
                                        ) {
                                            Text(
                                                text = zoneTitle,
                                                fontSize = 10.sp,
                                                fontWeight = if (isZoneActive) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isZoneActive) Color.Black else Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Color(0xFF334155), thickness = 0.8.dp)
                                Spacer(modifier = Modifier.height(6.dp))

                                val displayedTables = when (selectedZoneIndex) {
                                    1 -> all100Tables.subList(0, 25)
                                    2 -> all100Tables.subList(25, 50)
                                    3 -> all100Tables.subList(50, 75)
                                    4 -> all100Tables.subList(75, 100)
                                    else -> all100Tables
                                }

                                // Scrollable Grid of All 100 Tables (using non-subcompose chunked rows)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 190.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    displayedTables.chunked(5).forEach { rowTables ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            rowTables.forEach { tNum ->
                                                val isCurrent = tNum == tableNumber
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isCurrent) CheterCyan else Color(0xFF1E293B),
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        0.8.dp,
                                                        if (isCurrent) CheterCyan else Color(0xFF334155)
                                                    ),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("dropdown_table_chip_$tNum")
                                                        .clickable {
                                                            onTableSelect(tNum)
                                                            onTableSelectorClick()
                                                            isTableDropdownExpanded = false
                                                        }
                                                ) {
                                                    Box(
                                                        modifier = Modifier.padding(vertical = 6.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "T-$tNum",
                                                            fontSize = 11.sp,
                                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isCurrent) Color.Black else Color.White
                                                        )
                                                    }
                                                }
                                            }
                                            if (rowTables.size < 5) {
                                                repeat(5 - rowTables.size) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Top Action Icons (Language, Seller Panel, Add Item, Pay Bill, Cart)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Seller Dashboard Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = DhabaRed.copy(alpha = 0.85f),
                            modifier = Modifier
                                .testTag("header_seller_panel_button")
                                .clickable { onSellerDashboardClick() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = "Seller",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "दुकानदार",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Language Mode Toggle Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1E273D).copy(alpha = 0.85f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CheterPurple.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .testTag("language_toggle_button")
                                .clickable { onLanguageToggle() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Language",
                                    tint = CheterCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (languageMode) {
                                        LanguageMode.BOTH -> "हिन्दी + EN"
                                        LanguageMode.HINDI -> "हिन्दी"
                                        LanguageMode.ENGLISH -> "English"
                                    },
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Add Dish Button
                        IconButton(
                            onClick = onAddDishClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("add_dish_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF1E273D).copy(alpha = 0.85f), CircleShape)
                                    .border(1.dp, CheterCyan.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Dish",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Pay Bill Direct Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = DhabaGreenDark,
                            modifier = Modifier
                                .testTag("header_pay_bill_btn")
                                .clickable { onPayBillClick() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = "Pay Bill",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "बिल / Pay",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Cart Button with badge
                        if (cartCount > 0) {
                            IconButton(
                                onClick = onCartClick,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("header_cart_button")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(DhabaRed, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingBag,
                                        contentDescription = "Cart",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Middle Restaurant Title with CHETER Neon Logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Glowing C Neon Logo Icon
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, CheterCyan.copy(alpha = 0.8f)),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.cheter_app_logo_1787300662184),
                            contentDescription = "CHETER Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "CHETER",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "चेतर • डिजिटल रेस्टोरेंट & लाउंज",
                            color = CheterCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = "🍽️ टेबल चुनें और ताज़ा खाना ऑर्डर करें • Select Table & Order Instantly",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Bottom Strip with Active Order Pill or Tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeOrder != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DhabaGreen,
                            modifier = Modifier
                                .scale(pulseScale)
                                .testTag("active_order_status_pill")
                                .clickable { onActiveOrderClick() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Live Order",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ऑर्डर #${activeOrder.orderId}: ${activeOrder.status.titleHi}",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "✨ 100% ताज़ा खाना • तेज़ सेवा",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Rating & Fast Delivery Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E273D).copy(alpha = 0.8f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DhabaGold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "⭐ 4.9 (1.2k+ समीक्षा)",
                            color = DhabaGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

