package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LanguageMode
import com.example.model.MenuCategory
import com.example.model.MenuItem
import com.example.ui.components.AddDishDialog
import com.example.ui.components.CartBottomSheet
import com.example.ui.components.CategoryBar
import com.example.ui.components.DhabaHeader
import com.example.ui.components.DishReadyAlertDialog
import com.example.ui.components.DynamicTableBillDialog
import com.example.ui.components.ItemDetailDialog
import com.example.ui.components.MenuItemCard
import com.example.ui.components.OrderStatusDialog
import com.example.ui.components.PaymentSuccessDialog
import com.example.ui.components.TableQrDialog
import com.example.ui.theme.CheterCyan
import com.example.ui.theme.DhabaGold
import com.example.ui.theme.DhabaGreen
import com.example.ui.theme.DhabaGreenDark
import com.example.ui.theme.DhabaRed
import com.example.ui.theme.DhabaRedDark
import com.example.viewmodel.MenuViewModel

@Composable
fun MenuScreen(
    viewModel: MenuViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredItems by viewModel.filteredItems.collectAsState()
    val listState = rememberLazyListState()

    val totalCartCount = uiState.cartItems.values.sumOf { it.quantity }
    val totalCartPrice = uiState.cartItems.values.sumOf { it.totalPrice }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            // Floating Cart Bar when items are added
            AnimatedVisibility(
                visible = totalCartCount > 0,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("floating_cart_bar")
                        .clickable { viewModel.setCartOpen(true) },
                    shape = RoundedCornerShape(20.dp),
                    color = DhabaRedDark,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.25f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = "Cart",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "$totalCartCount व्यंजन जोड़े गए (Items)",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "कुल राशि: ₹$totalCartPrice + टैक्स",
                                    color = DhabaGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "ऑर्डर देखें",
                                color = DhabaRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = DhabaRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = if (totalCartCount > 0) 90.dp else 24.dp)
        ) {
            // Header Section
            item(key = "header") {
                DhabaHeader(
                    tableNumber = uiState.selectedTableNumber,
                    languageMode = uiState.languageMode,
                    activeOrder = uiState.activeOrder,
                    cartCount = totalCartCount,
                    onLanguageToggle = {
                        val nextMode = when (uiState.languageMode) {
                            LanguageMode.BOTH -> LanguageMode.HINDI
                            LanguageMode.HINDI -> LanguageMode.ENGLISH
                            LanguageMode.ENGLISH -> LanguageMode.BOTH
                        }
                        viewModel.setLanguageMode(nextMode)
                    },
                    onTableSelectorClick = { viewModel.setTableQrOpen(true) },
                    onTableSelect = { viewModel.setTableNumber(it) },
                    onPayBillClick = { viewModel.setPayBillDialogOpen(true) },
                    onAddDishClick = { viewModel.setAddDishOpen(true) },
                    onActiveOrderClick = { viewModel.setOrderStatusOpen(true) },
                    onCartClick = { viewModel.setCartOpen(true) },
                    onSellerDashboardClick = { viewModel.setAppMode(com.example.model.AppMode.SELLER_DASHBOARD) }
                )
            }

            // Customer Blocked / Fraud Protection Alert Banner
            if (uiState.isCurrentCustomerBlocked) {
                item(key = "blocked_customer_banner") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("blocked_customer_banner"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DhabaRed.copy(alpha = 0.12f)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, DhabaRed)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🚨", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "खाता ब्लॉक है • Account Blocked (Unpaid Bill)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DhabaRed
                                )
                                Text(
                                    text = "User ID: ${uiState.currentCustomerId} • Phone: ${uiState.currentCustomerPhone} को अनपेड बिल के कारण ब्लॉक किया गया है।",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Live Waiter Arriving for Cash Banner
            if (uiState.isCashRequestedForTable.contains(uiState.selectedTableNumber)) {
                item(key = "waiter_cash_arrival_banner") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { viewModel.setPayBillDialogOpen(true) }
                            .testTag("waiter_cash_arrival_banner"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DhabaGreenDark),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "🏃‍♂️", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Waiter is arriving at Table #${uiState.selectedTableNumber} to collect cash.",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "कृपया नकद तैयार रखें • टैप करके बिल देखें",
                                        fontSize = 10.5.sp,
                                        color = DhabaGold
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Quick Table Selector Strip (1-tap direct table number choice 1 to 100)
            item(key = "table_selector_strip") {
                val tableList = remember { List(100) { it + 1 } }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🪑 टेबल:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (tableNum in tableList) {
                                val isSelected = uiState.selectedTableNumber == tableNum
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) CheterCyan else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) CheterCyan else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier
                                        .testTag("menu_table_chip_$tableNum")
                                        .clickable { viewModel.setTableNumber(tableNum) }
                                ) {
                                    Text(
                                        text = "T-$tableNum",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search & Category Bar Section
            item(key = "category_bar") {
                CategoryBar(
                    selectedCategory = uiState.selectedCategory,
                    categories = MenuCategory.entries.toList(),
                    searchQuery = uiState.searchQuery,
                    vegOnly = uiState.vegOnlyFilter,
                    sortOption = uiState.sortOption,
                    languageMode = uiState.languageMode,
                    onCategorySelect = { viewModel.selectCategory(it) },
                    onSearchChange = { viewModel.updateSearchQuery(it) },
                    onVegOnlyToggle = { viewModel.toggleVegOnlyFilter() },
                    onSortOptionChange = { viewModel.setSortOption(it) }
                )
            }

            // Item count & category header
            item(key = "category_title_strip") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.selectedCategory.emoji} ${uiState.selectedCategory.titleHi} (${filteredItems.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "ताज़ा बनाया गया • Fresh Prepared",
                        fontSize = 11.sp,
                        color = DhabaGreenDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Empty state if no items found
            if (filteredItems.isEmpty()) {
                item(key = "empty_menu") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🔍", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "कोई व्यंजन नहीं मिला",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "कृपया अन्य नाम खोजें या फ़िल्टर रीसेट करें",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Menu Items List
                items(filteredItems, key = { it.id }) { item ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                        MenuItemCard(
                            item = item,
                            cartItem = uiState.cartItems[item.id],
                            isFavorite = uiState.favoriteItemIds.contains(item.id),
                            languageMode = uiState.languageMode,
                            onItemClick = { viewModel.showItemDetail(item) },
                            onAddToCart = { viewModel.addToCart(item) },
                            onRemoveFromCart = { viewModel.removeFromCart(item.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(item.id) }
                        )
                    }
                }
            }

            // Footer note
            item(key = "footer") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "👑 रॉयल ढाबा & रेस्टोरेंट • Royal Dhaba",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Powered by Digital QR Menu App",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }

    // Item Detail Dialog
    uiState.selectedItemForDetail?.let { item ->
        ItemDetailDialog(
            item = item,
            cartItem = uiState.cartItems[item.id],
            languageMode = uiState.languageMode,
            onDismiss = { viewModel.showItemDetail(null) },
            onAddToCart = { qty, note ->
                repeat(qty) {
                    viewModel.addToCart(item, note)
                }
            }
        )
    }

    // Table QR & Switcher Dialog
    if (uiState.isTableQrOpen) {
        TableQrDialog(
            currentTableNumber = uiState.selectedTableNumber,
            shopInfo = uiState.shopInfo,
            onTableSelected = { viewModel.setTableNumber(it) },
            onDismiss = { viewModel.setTableQrOpen(false) }
        )
    }

    // Add Dish Dialog
    if (uiState.isAddDishOpen) {
        AddDishDialog(
            onDismiss = { viewModel.setAddDishOpen(false) },
            onAddDish = { nameHi, nameEn, category, price, descHi, descEn, isVeg, spicy, emo, prepTime ->
                viewModel.addNewDish(nameHi, nameEn, category, price, descHi, descEn, isVeg, spicy, emo, prepTime)
            }
        )
    }

    // Cart Bottom Sheet
    if (uiState.isCartOpen) {
        CartBottomSheet(
            tableNumber = uiState.selectedTableNumber,
            cartItems = uiState.cartItems.values.toList(),
            customerName = uiState.customerName,
            orderNotes = uiState.orderNotes,
            languageMode = uiState.languageMode,
            onCustomerNameChange = { viewModel.setCustomerName(it) },
            onOrderNotesChange = { viewModel.updateOrderNotes(it) },
            onIncreaseQuantity = { itemId ->
                val item = uiState.allItems.find { it.id == itemId }
                if (item != null) viewModel.addToCart(item)
            },
            onDecreaseQuantity = { itemId -> viewModel.removeFromCart(itemId) },
            onClearCart = { viewModel.clearCart() },
            onPlaceOrder = { tip -> viewModel.placeOrder(tip) },
            onPayBillDirect = { tip ->
                viewModel.placeOrder(tip)
                viewModel.setPayBillDialogOpen(true)
            },
            onDismiss = { viewModel.setCartOpen(false) }
        )
    }

    // Order Status Dialog (Supports Multiple Active Orders per Table & Live Dish Timers)
    if (uiState.isOrderStatusOpen) {
        val activeOrdersForTable = uiState.tableOrdersMap[uiState.selectedTableNumber]
            ?: (uiState.activeOrder?.let { listOf(it) } ?: emptyList())
        if (activeOrdersForTable.isNotEmpty()) {
            OrderStatusDialog(
                orders = activeOrdersForTable,
                currentTableNumber = uiState.selectedTableNumber,
                onDismiss = { viewModel.setOrderStatusOpen(false) },
                onPayBillClick = { viewModel.setPayBillDialogOpen(true) }
            )
        }
    }

    // Dish Ready Alert Popup Dialog (Green Celebration Alert Notification)
    uiState.dishReadyAlert?.let { alert ->
        DishReadyAlertDialog(
            alert = alert,
            onDismiss = { viewModel.dismissDishReadyAlert() }
        )
    }

    // Dynamic Table Bill Dialog (Live item breakdown, tax calculation & UPI / Cash options)
    if (uiState.isPayBillDialogOpen) {
        val currentSummary = viewModel.getTableBillSummary(uiState.selectedTableNumber)
        DynamicTableBillDialog(
            billSummary = currentSummary,
            shopInfo = uiState.shopInfo,
            onPayOnlineUpi = { viewModel.payBillOnlineUpi(uiState.selectedTableNumber) },
            onRequestCash = { viewModel.requestCashPayment(uiState.selectedTableNumber) },
            onSelectTable = { viewModel.setTableNumber(it) },
            onDismiss = { viewModel.setPayBillDialogOpen(false) }
        )
    }

    // Digital Payment Receipt & Success Dialog
    uiState.paymentSuccessOrder?.let { paidOrder ->
        PaymentSuccessDialog(
            order = paidOrder,
            onDismiss = { viewModel.dismissPaymentSuccess() }
        )
    }
}
