package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import com.example.model.OrderStatus
import com.example.model.TableOrder
import kotlinx.coroutines.delay
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import com.example.model.BlockedCustomer
import com.example.model.CashPaymentAlert
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AppMode
import com.example.model.MenuCategory
import com.example.model.MenuItem
import com.example.model.SpicyLevel
import com.example.model.TableBillSummary
import com.example.ui.components.AddDishDialog
import com.example.ui.components.ItemizedReceiptDialog
import com.example.ui.components.OptimizedDishImage
import com.example.ui.components.ShopAuthDialog
import com.example.ui.theme.CheterCyan
import com.example.ui.theme.CheterPurple
import com.example.ui.theme.DhabaGold
import com.example.ui.theme.DhabaGreen
import com.example.ui.theme.DhabaGreenDark
import com.example.ui.theme.DhabaRed
import com.example.ui.theme.DhabaRedDark
import com.example.viewmodel.MenuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    viewModel: MenuViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var dashboardTab by remember { mutableStateOf(0) } // 0: Add Item Form, 1: Manage Menu, 2: Shop QR Code

    // Add Dish Form state matching user's HTML specification:
    var formShopId by remember { mutableStateOf(uiState.shopInfo.shopId) }
    var formItemName by remember { mutableStateOf("") }
    var formItemNameEn by remember { mutableStateOf("") }
    var formItemPrice by remember { mutableStateOf("") }
    var formMediaUrl by remember { mutableStateOf("") }
    var formMediaType by remember { mutableStateOf("image") } // "image" or "video"
    var formCategory by remember { mutableStateOf(MenuCategory.MAIN) }
    var formCategoryDropdownOpen by remember { mutableStateOf(false) }
    var formMediaTypeDropdownOpen by remember { mutableStateOf(false) }
    var formIsVeg by remember { mutableStateOf(true) }
    var formSpicyLevel by remember { mutableStateOf(SpicyLevel.MEDIUM) }
    var formEmoji by remember { mutableStateOf("🍛") }
    var formPrepTimeMin by remember { mutableStateOf("15") }

    // Add/Edit Dish Modal State for Admin Panel
    var isAddEditModalOpen by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MenuItem?>(null) }
    var receiptBillSummaryModal by remember { mutableStateOf<TableBillSummary?>(null) }

    // Category filter for menu management tab
    var selectedCategoryFilter by remember { mutableStateOf(MenuCategory.ALL) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(DhabaRed, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "दुकानदार पैनल (Seller Dashboard)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${uiState.shopInfo.shopName} • ID: ${uiState.shopInfo.shopId}",
                                fontSize = 11.5.sp,
                                color = DhabaGreenDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    // Switch to Customer View Button
                    Button(
                        onClick = { viewModel.setAppMode(AppMode.CUSTOMER_MENU) },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("switch_to_customer_view_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F3542)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF2ED573), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "ग्राहक मेनू", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Auth Account Button
                    IconButton(
                        onClick = { viewModel.setAuthDialogOpen(true) },
                        modifier = Modifier.testTag("seller_auth_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.shopOwner.isAuthenticated) Icons.Default.CheckCircle else Icons.Default.AccountCircle,
                            contentDescription = "Account",
                            tint = if (uiState.shopOwner.isAuthenticated) DhabaGreenDark else DhabaRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Realtime Incoming Order Alert Banner (Supabase Realtime Live Push)
            val newOrder = uiState.newRealtimeOrderNotification
            if (newOrder != null) {
                Surface(
                    color = Color(0xFF0F172A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("admin_realtime_new_order_banner"),
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 6.dp,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, DhabaGreenDark)
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
                            Surface(
                                shape = CircleShape,
                                color = DhabaGreenDark,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "⚡ नया लाइव ऑर्डर! टेबल T-${newOrder.tableNumber}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = DhabaGreenDark
                                    ) {
                                        Text("₹${newOrder.grandTotal}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                                    }
                                }
                                Text(
                                    text = "${newOrder.customerName} • ${newOrder.items.size} आयटम • ${newOrder.items.joinToString(", ") { "${it.item.nameHi} (x${it.quantity})" }.take(40)}...",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = {
                                    dashboardTab = 0
                                    viewModel.dismissNewOrderNotification()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DhabaGreenDark),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp).testTag("accept_realtime_order_btn")
                            ) {
                                Text("👨‍🍳 किचन में देखें", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { viewModel.dismissNewOrderNotification() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Dismiss", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Live Cash Alert Sticky Ticker for Waiter/Admin
            if (uiState.activeCashAlerts.isNotEmpty()) {
                val firstAlert = uiState.activeCashAlerts.first()
                Surface(
                    color = DhabaGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dashboardTab = 1 }
                        .testTag("admin_live_cash_alert_ticker")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFF1E273D), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "🚨 टेबल #${firstAlert.tableNumber} पर नकद संग्रह अलर्ट (₹${firstAlert.totalAmount})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF1E273D)
                                )
                                Text(
                                    text = "${firstAlert.customerName} (${firstAlert.customerPhone}) • ID: ${firstAlert.customerId}",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF2C3E50)
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.markCashReceived(firstAlert.alertId) },
                            colors = ButtonDefaults.buttonColors(containerColor = DhabaGreenDark),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("ticker_mark_cash_received_${firstAlert.alertId}")
                        ) {
                            Text("✅ Cash Received", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            val totalActiveOrdersCount = uiState.tableOrdersMap.values.flatten().count { it.status != OrderStatus.SERVED }

            // Dashboard Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = dashboardTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = DhabaRed,
                edgePadding = 12.dp
            ) {
                Tab(
                    selected = dashboardTab == 0,
                    onClick = { dashboardTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.OutdoorGrill, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("👨‍🍳 किचन & ETA ($totalActiveOrdersCount)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = dashboardTab == 1,
                    onClick = { dashboardTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("💵 कैश अलर्ट & बिल (${uiState.activeCashAlerts.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = dashboardTab == 2,
                    onClick = { dashboardTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("🛡️ फ्रॉड / बैन (${uiState.blockedCustomers.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = dashboardTab == 3,
                    onClick = { dashboardTab = 3 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RestaurantMenu, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("मेनू सूची (${uiState.allItems.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = dashboardTab == 4,
                    onClick = { dashboardTab = 4 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("सामान जोड़ें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = dashboardTab == 5,
                    onClick = { dashboardTab = 5 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("QR कोड", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = dashboardTab == 6,
                    onClick = { dashboardTab = 6 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DinnerDining, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("⚡ टेबल्स T-1..100 (${uiState.restaurantTables.count { it.isOccupied }}/100)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = dashboardTab == 7,
                    onClick = { dashboardTab = 7 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("⚡ Supabase DB", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            when (dashboardTab) {
                0 -> {
                    // ================= TAB 0: LIVE KITCHEN & ETA ORDERS =================
                    KitchenOrdersTabContent(viewModel = viewModel)
                }

                6 -> {
                    // ================= TAB 6: 100 RESTAURANT TABLES GRID (Supabase Synced) =================
                    TablesManagerTabContent(viewModel = viewModel)
                }

                7 -> {
                    // ================= TAB 7: SUPABASE DATABASE ENGINE CONFIGURATION =================
                    SupabaseSettingsTabContent(viewModel = viewModel)
                }

                4 -> {
                    // ================= TAB 4: ADD ITEM FORM (HTML Spec) =================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestaurantMenu,
                                        contentDescription = null,
                                        tint = DhabaRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "दुकानदार पैनल (Add Item Form)",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "Firebase Database से कनेक्टेड - लाइव मेनू अपडेट",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Shop ID Input with quick generator
                                OutlinedTextField(
                                    value = formShopId,
                                    onValueChange = {
                                        formShopId = it
                                        viewModel.setShopId(it)
                                    },
                                    label = { Text("आपकी शॉप ID (उदा: shop_101)") },
                                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = DhabaRed) },
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            val generated = "shop_" + (100..999).random()
                                            formShopId = generated
                                            viewModel.setShopId(generated)
                                            Toast.makeText(context, "नई शॉप ID सेट: $generated", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Generate New ID", tint = DhabaGold)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("dashboard_shop_id_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Item Name Hindi
                                OutlinedTextField(
                                    value = formItemName,
                                    onValueChange = { formItemName = it },
                                    label = { Text("सामान/डिश का नाम हिंदी में (उदा: दाल मखनी)") },
                                    leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null, tint = DhabaRed) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("dashboard_item_name_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Item Name English
                                OutlinedTextField(
                                    value = formItemNameEn,
                                    onValueChange = { formItemNameEn = it },
                                    label = { Text("English Name (e.g. Dal Makhani Special)") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("dashboard_item_name_en_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Category Dropdown & Price Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Category Dropdown
                                    Box(modifier = Modifier.weight(1.3f)) {
                                        OutlinedTextField(
                                            value = "${formCategory.emoji} ${formCategory.titleHi}",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("कैटेगरी (Category)") },
                                            trailingIcon = {
                                                IconButton(onClick = { formCategoryDropdownOpen = true }) {
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Category")
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { formCategoryDropdownOpen = true }
                                                .testTag("dashboard_category_dropdown_button"),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        DropdownMenu(
                                            expanded = formCategoryDropdownOpen,
                                            onDismissRequest = { formCategoryDropdownOpen = false }
                                        ) {
                                            MenuCategory.entries.filter { it != MenuCategory.ALL }.forEach { cat ->
                                                DropdownMenuItem(
                                                    text = { Text("${cat.emoji} ${cat.titleHi} (${cat.titleEn})", fontSize = 13.sp) },
                                                    onClick = {
                                                        formCategory = cat
                                                        formCategoryDropdownOpen = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Price Input
                                    OutlinedTextField(
                                        value = formItemPrice,
                                        onValueChange = { formItemPrice = it },
                                        label = { Text("कीमत ₹ (Price)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("dashboard_item_price_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Media URL & Media Type Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Media URL
                                    OutlinedTextField(
                                        value = formMediaUrl,
                                        onValueChange = { formMediaUrl = it },
                                        label = { Text("फ़ोटो/वीडियो Link (Media URL)") },
                                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = DhabaRed) },
                                        modifier = Modifier
                                            .weight(1.4f)
                                            .testTag("dashboard_media_url_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )

                                    // Media Type Selector (image / video)
                                    Box(modifier = Modifier.weight(0.9f)) {
                                        OutlinedTextField(
                                            value = if (formMediaType == "video") "🎥 Video" else "📸 Photo",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Type") },
                                            trailingIcon = {
                                                IconButton(onClick = { formMediaTypeDropdownOpen = true }) {
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { formMediaTypeDropdownOpen = true }
                                                .testTag("dashboard_media_type_dropdown"),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        DropdownMenu(
                                            expanded = formMediaTypeDropdownOpen,
                                            onDismissRequest = { formMediaTypeDropdownOpen = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("📸 Photo (Image)", fontSize = 13.sp) },
                                                onClick = {
                                                    formMediaType = "image"
                                                    formMediaTypeDropdownOpen = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("🎥 Video (MP4/Clip)", fontSize = 13.sp) },
                                                onClick = {
                                                    formMediaType = "video"
                                                    formMediaTypeDropdownOpen = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Preparation Time in Minutes Input Field
                                OutlinedTextField(
                                    value = formPrepTimeMin,
                                    onValueChange = { formPrepTimeMin = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Preparation Time in Minutes (तैयारी का समय मिनटों में)") },
                                    leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, tint = DhabaRed) },
                                    placeholder = { Text("15") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("dashboard_item_prep_time_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Quick preset buttons for prep time
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(3, 5, 10, 15, 20, 30).forEach { mins ->
                                        val isSelected = formPrepTimeMin.toIntOrNull() == mins
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) DhabaRed else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { formPrepTimeMin = mins.toString() }
                                        ) {
                                            Text(
                                                text = "$mins m",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Veg Toggle & Emoji Chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        listOf("🍲", "🍛", "🥣", "🫓", "🥟", "🍢", "🍚", "🥤", "🍨").forEach { emo ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (formEmoji == emo) DhabaRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.clickable { formEmoji = emo }
                                            ) {
                                                Text(text = emo, fontSize = 18.sp, modifier = Modifier.padding(4.dp))
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = if (formIsVeg) "शाकाहारी 🌱" else "Non-Veg 🍗", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Switch(
                                            checked = formIsVeg,
                                            onCheckedChange = { formIsVeg = it },
                                            modifier = Modifier.testTag("dashboard_veg_switch")
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Main Add to Menu Button
                                Button(
                                    onClick = {
                                        val price = formItemPrice.toIntOrNull() ?: 150
                                        val prepMinutes = formPrepTimeMin.toIntOrNull()?.coerceAtLeast(1) ?: 15
                                        val finalNameHi = if (formItemName.isNotBlank()) formItemName else (if (formItemNameEn.isNotBlank()) formItemNameEn else "नया व्यंजन")
                                        val finalNameEn = if (formItemNameEn.isNotBlank()) formItemNameEn else finalNameHi

                                        viewModel.addNewDishFromSeller(
                                            shopId = formShopId.ifBlank { "shop_101" },
                                            nameHi = finalNameHi,
                                            nameEn = finalNameEn,
                                            category = formCategory,
                                            price = price,
                                            mediaUrl = formMediaUrl,
                                            mediaType = formMediaType,
                                            isVeg = formIsVeg,
                                            spicyLevel = formSpicyLevel,
                                            emoji = formEmoji,
                                            prepTimeMin = prepMinutes
                                        )

                                        Toast.makeText(
                                            context,
                                            "सफलतापूर्वक Supabase में जुड़ा!\nदुकान: ${formShopId.ifBlank { "shop_101" }}\nसामान: $finalNameHi - ₹$price (${prepMinutes} min)",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        // Reset fields
                                        formItemName = ""
                                        formItemNameEn = ""
                                        formItemPrice = ""
                                        formMediaUrl = ""
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("dashboard_add_dish_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DhabaRed)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "मेनू में जोड़ें (Add to Firebase Menu)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // ================= TAB 3: ORGANIZE MENU BY CATEGORIES =================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Category Dropdown Selection & Filter Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "श्रेणी अनुसार मेनू (Category Filter):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = {
                                        editingItem = null
                                        isAddEditModalOpen = true
                                    },
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("admin_add_dish_modal_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DhabaRed),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ डिश जोड़ें", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Category Dropdown Picker
                                Box {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = DhabaRed.copy(alpha = 0.12f),
                                        modifier = Modifier
                                            .clickable { isCategoryDropdownExpanded = true }
                                            .testTag("category_organizer_dropdown")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${selectedCategoryFilter.emoji} ${selectedCategoryFilter.titleHi}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DhabaRed
                                            )
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = DhabaRed)
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = isCategoryDropdownExpanded,
                                        onDismissRequest = { isCategoryDropdownExpanded = false }
                                    ) {
                                        MenuCategory.entries.forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text("${cat.emoji} ${cat.titleHi} (${cat.titleEn})", fontSize = 13.sp) },
                                                onClick = {
                                                    selectedCategoryFilter = cat
                                                    isCategoryDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Chips Row for quick switching
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MenuCategory.entries.forEach { cat ->
                                val isSelected = selectedCategoryFilter == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategoryFilter = cat },
                                    label = { Text("${cat.emoji} ${cat.titleHi}", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DhabaRed,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Filtered Items List for Seller Management
                        val displayedItems = if (selectedCategoryFilter == MenuCategory.ALL) {
                            uiState.allItems
                        } else {
                            uiState.allItems.filter { it.category == selectedCategoryFilter }
                        }

                        if (displayedItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "इस श्रेणी में कोई व्यंजन नहीं है।\nनया सामान जोड़ें!",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(displayedItems, key = { it.id }) { item ->
                                    SellerItemRow(
                                        item = item,
                                        onEdit = {
                                            editingItem = item
                                            isAddEditModalOpen = true
                                        },
                                        onToggleAvailability = { viewModel.toggleItemAvailability(item.id) },
                                        onDelete = {
                                            viewModel.deleteDish(item.id)
                                            Toast.makeText(context, "${item.nameHi} मेनू से हटा दिया गया", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // ================= TAB 1: CASH ALERTS & LIVE TABLE BILLING =================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section Header: Live Cash Alerts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFD35400), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "लाइव नकद संग्रह अलर्ट (Cash Alerts)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = if (uiState.activeCashAlerts.isNotEmpty()) DhabaGold else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "${uiState.activeCashAlerts.size} सक्रिय",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.activeCashAlerts.isNotEmpty()) Color(0xFF1E273D) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (uiState.activeCashAlerts.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "✅", fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "कोई लंबित नकद अनुरोध नहीं है",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "जब कोई ग्राहक 'Pay via Cash' चुनेगा, यहाँ वेटर के लिए तुरंत अलर्ट दिखाई देगा।",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            uiState.activeCashAlerts.forEach { alert ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("cash_alert_card_${alert.alertId}"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = DhabaGold.copy(alpha = 0.2f)),
                                    border = androidx.compose.foundation.BorderStroke(2.dp, DhabaGold)
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
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = DhabaRed
                                            ) {
                                                Text(
                                                    text = "टेबल #${alert.tableNumber}",
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }

                                            Text(
                                                text = "नकद राशि: ₹${alert.totalAmount}",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Black,
                                                color = DhabaGreenDark
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "👤 ${alert.customerName} (ID: ${alert.customerId})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "📱 ${alert.customerPhone}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2C3E50)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    viewModel.markCashReceived(alert.alertId)
                                                    Toast.makeText(context, "टेबल #${alert.tableNumber} से ₹${alert.totalAmount} नकद प्राप्त हुआ दर्ज कर लिया गया!", Toast.LENGTH_LONG).show()
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                                    .testTag("mark_cash_received_${alert.alertId}"),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = DhabaGreenDark)
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("✅ Cash Received (नकद मिला)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }

                                            Button(
                                                onClick = {
                                                    viewModel.markOrderUnpaidAndBan(alert.tableNumber, reason = "Left table without paying cash")
                                                    Toast.makeText(context, "टेबल #${alert.tableNumber} के ग्राहक (${alert.customerId}) को फ्रॉड के तहत ब्लॉक किया गया", Toast.LENGTH_LONG).show()
                                                },
                                                modifier = Modifier
                                                    .height(44.dp)
                                                    .testTag("mark_unpaid_alert_${alert.alertId}"),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = DhabaRed)
                                            ) {
                                                Icon(Icons.Default.Block, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Mark Unpaid", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Section 2: All Table Live Bills
                        Text(
                            text = "📋 सभी टेबल बिल एवं स्थिति (Live Tables)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val tablesToDisplay = remember(uiState.tableOrdersMap, uiState.isCashRequestedForTable) {
                            val activeTbls = (uiState.tableOrdersMap.keys + uiState.isCashRequestedForTable).filter { it in 1..100 }
                            if (activeTbls.isNotEmpty()) (activeTbls + (1..12)).distinct().sorted() else (1..25).toList()
                        }

                        for (tNum in tablesToDisplay) {
                            val billSummary = viewModel.getTableBillSummary(tNum)
                            val hasOrders = billSummary.orders.isNotEmpty()

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_table_card_$tNum"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (billSummary.isCashRequested) DhabaGold.copy(alpha = 0.15f)
                                    else if (billSummary.isFraudUnpaid) DhabaRed.copy(alpha = 0.12f)
                                    else if (billSummary.isSettled) DhabaGreenDark.copy(alpha = 0.08f)
                                    else MaterialTheme.colorScheme.surface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (billSummary.isCashRequested) DhabaGold
                                    else if (billSummary.isFraudUnpaid) DhabaRed
                                    else if (billSummary.isSettled) DhabaGreenDark
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = DhabaRed
                                            ) {
                                                Text(
                                                    text = "T$tNum",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = billSummary.customerName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when {
                                                billSummary.isFraudUnpaid -> DhabaRed
                                                billSummary.isCashRequested -> DhabaGold
                                                billSummary.isSettled -> DhabaGreenDark
                                                hasOrders -> Color(0xFFE67E22)
                                                else -> Color(0xFF95A5A6)
                                            }
                                        ) {
                                            Text(
                                                text = when {
                                                    billSummary.isFraudUnpaid -> "🚨 Unpaid / Banned"
                                                    billSummary.isCashRequested -> "💵 Cash Requested"
                                                    billSummary.isSettled -> "✅ Paid"
                                                    hasOrders -> "⏳ Bill Unpaid"
                                                    else -> "खाली (Empty)"
                                                },
                                                color = if (billSummary.isCashRequested) Color(0xFF1E273D) else Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (hasOrders) {
                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Itemized breakdown table
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                .padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = "📋 विस्तृत व्यंजन सूची (${billSummary.orders.size} KOTs):",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DhabaRed
                                            )
                                            billSummary.consolidatedItems.forEach { cItem ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "${if (cItem.menuItem.isVeg) "🟢" else "🔴"} ${cItem.menuItem.nameHi}",
                                                        fontSize = 11.5.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "${cItem.quantity} × ₹${cItem.unitPrice} = ₹${cItem.totalPrice}",
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Subtotal, GST & Grand Total summary
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "सब-टोटल: ₹${billSummary.totalSubtotal} | GST (5%): ₹${billSummary.totalGst}",
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "कुल: ₹${billSummary.grandTotal}",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Black,
                                                color = DhabaGreenDark
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // View Itemized Receipt Button
                                        OutlinedButton(
                                            onClick = { receiptBillSummaryModal = billSummary },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(34.dp)
                                                .testTag("admin_table_view_receipt_btn_$tNum"),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp), tint = DhabaRed)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("🧾 विस्तृत रसीद (Itemized Bill)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DhabaRed)
                                        }

                                        if (!billSummary.isSettled && !billSummary.isFraudUnpaid) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        viewModel.payBill(tNum, PaymentMethod.CASH)
                                                        Toast.makeText(context, "टेबल #$tNum का बिल ₹${billSummary.grandTotal} चुकता कर दिया गया", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.weight(1f).height(36.dp).testTag("table_mark_paid_btn_$tNum"),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = DhabaGreenDark),
                                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                                ) {
                                                    Text("✅ Mark Paid", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.markOrderUnpaidAndBan(tNum)
                                                        Toast.makeText(context, "टेबल #$tNum ग्राहक को अनपेड फ्रॉड के लिए ब्लॉक किया गया", Toast.LENGTH_LONG).show()
                                                    },
                                                    modifier = Modifier.weight(1f).height(36.dp).testTag("table_mark_unpaid_ban_btn_$tNum"),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = DhabaRed),
                                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                                ) {
                                                    Text("🚨 Mark Unpaid (Ban)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // ================= TAB 2: FRAUD PROTECTION & BLOCKED CUSTOMERS =================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Fraud Protection Banner Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DhabaRed.copy(alpha = 0.1f)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, DhabaRed)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = DhabaRed, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "धोखाधड़ी सुरक्षा प्रणाली (Fraud Protection)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DhabaRed
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "यदि कोई ग्राहक बिना बिल चुकाए टेबल छोड़ देता है, तो 'Mark Unpaid' दबाने पर उनकी User ID और Phone Number तुरंत ब्लैकलिस्ट हो जाती है और अगली बार वे ऑर्डर नहीं कर पाते।",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Blocked Customers Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🚫 ब्लॉक किए गए ग्राहक (${uiState.blockedCustomers.size})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (uiState.blockedCustomers.isNotEmpty()) {
                                Text(
                                    text = "कुल नुकसान: ₹${uiState.blockedCustomers.sumOf { it.unpaidAmount }}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DhabaRed
                                )
                            }
                        }

                        if (uiState.blockedCustomers.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "🛡️", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "कोई ब्लैकलिस्टेड ग्राहक नहीं है",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "जब किसी टेबल को 'Mark Unpaid' किया जाएगा, ग्राहक का विवरण यहाँ सुरक्षित रूप से स्टोर होगा।",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            uiState.blockedCustomers.forEach { blocked ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("blocked_customer_card_${blocked.customerId}"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = DhabaRed.copy(alpha = 0.08f)),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, DhabaRed)
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
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Block, contentDescription = null, tint = DhabaRed, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "ब्लॉक: ${blocked.customerName}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp,
                                                    color = DhabaRed
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = DhabaRed
                                            ) {
                                                Text(
                                                    text = "₹${blocked.unpaidAmount} बकाया",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "🆔 User ID: ${blocked.customerId}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "📱 Phone: ${blocked.phone}", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                                        Text(text = "🪑 टेबल #${blocked.tableNumber} • ऑर्डर #${blocked.orderId}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "कारण: ${blocked.reason}", fontSize = 11.sp, color = DhabaRed)

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Button(
                                            onClick = {
                                                viewModel.unblockCustomer(blocked.customerId)
                                                Toast.makeText(context, "${blocked.customerId} को अनब्लॉक कर दिया गया", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(38.dp)
                                                .testTag("unblock_customer_btn_${blocked.customerId}"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = DhabaGreenDark)
                                        ) {
                                            Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("🔓 अनब्लॉक करें (Unblock Customer)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                5 -> {
                    // ================= TAB 5: UNIFIED SINGLE QR & DYNAMIC MERCHANT CONFIGURATION =================
                    var merchantShopName by remember(uiState.shopInfo.shopName) { mutableStateOf(uiState.shopInfo.shopName) }
                    var merchantUpiId by remember(uiState.shopInfo.upiId) { mutableStateOf(uiState.shopInfo.upiId) }
                    var merchantCustomQrUrl by remember(uiState.shopInfo.customUpiQrUrl) { mutableStateOf(uiState.shopInfo.customUpiQrUrl) }
                    var merchantTotalTables by remember(uiState.shopInfo.totalTables) { mutableStateOf(uiState.shopInfo.totalTables.toString()) }
                    var merchantPhone by remember(uiState.shopInfo.phone) { mutableStateOf(uiState.shopInfo.phone) }
                    var merchantAddress by remember(uiState.shopInfo.address) { mutableStateOf(uiState.shopInfo.address) }
                    var selectedTableForQr by remember { mutableStateOf(1) }
                    var customHotelIdInput by remember(uiState.shopInfo.hotelId) { mutableStateOf(uiState.shopInfo.hotelId.ifBlank { "hotel1" }) }
                    var showAllTablesQrSheet by remember { mutableStateOf(false) }
                    var isSavedSuccess by remember { mutableStateOf(false) }

                    val activeHotelId = uiState.shopInfo.hotelId.ifBlank { "hotel1" }
                    val tableRestaurantUrl = "https://cheter.app/?hotel=$activeHotelId&table=$selectedTableForQr"
                    val tableQrApiUrl = "https://api.qrserver.com/v1/create-qr-code/?data=${android.net.Uri.encode(tableRestaurantUrl)}&size=350x350"

                    val masterRestaurantUrl = "https://cheter.app/?hotel=$activeHotelId"
                    val masterQrApiUrl = "https://api.qrserver.com/v1/create-qr-code/?data=${android.net.Uri.encode(masterRestaurantUrl)}&size=350x350"

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section 0: Multi-Restaurant SaaS Hotel Switcher
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = DhabaRed,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "🏨 मल्टी-रेस्टोरेंट होटल सेलेक्टर (SaaS Hotel ID)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = "प्रत्येक होटल का मेनू, ऑर्डर व लाइव किचन अलग और सुरक्षित (Isolated) है।",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )

                                // Preset Hotel Chips
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("hotel1", "hotel2", "hotel3", "dhabaz_express").forEach { hId ->
                                        val isCurrent = activeHotelId.equals(hId, ignoreCase = true)
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isCurrent) DhabaRed else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.clickable {
                                                customHotelIdInput = hId
                                                viewModel.switchHotel(hId)
                                                Toast.makeText(context, "होटल स्विच: $hId", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (isCurrent) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text(
                                                    text = hId,
                                                    color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = customHotelIdInput,
                                        onValueChange = { customHotelIdInput = it },
                                        label = { Text("कस्टम होटल ID (Hotel ID)") },
                                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Button(
                                        onClick = {
                                            if (customHotelIdInput.isNotBlank()) {
                                                viewModel.switchHotel(customHotelIdInput.trim())
                                                Toast.makeText(context, "होटल लोड: ${customHotelIdInput.trim()}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = DhabaRedDark)
                                    ) {
                                        Text("स्विच करें", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Section 1: Table-Specific QR Generator (T-1 to T-100)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = DhabaGreenDark.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.QrCode2,
                                            contentDescription = null,
                                            tint = DhabaGreenDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "🎯 डायनामिक टेबल QR कोड (Tables T-1 to T-100)",
                                            color = DhabaGreenDark,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "टेबल T-$selectedTableForQr का डिजिटल QR स्टैंड",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "होटल: $activeHotelId  •  टेबल संख्या: $selectedTableForQr",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DhabaRed
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Table Number Fast Picker (1 to 100)
                                Text(
                                    text = "टेबल चुनें (Select Table 1-100):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    (1..100).forEach { tbl ->
                                        val isSel = selectedTableForQr == tbl
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSel) DhabaRed else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.clickable { selectedTableForQr = tbl }
                                        ) {
                                            Text(
                                                text = "T-$tbl",
                                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Table QR Graphic Standee
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(2.5.dp, DhabaGreenDark.copy(alpha = 0.5f)),
                                    shadowElevation = 4.dp,
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = DhabaGreenDark
                                        ) {
                                            Text(
                                                text = "🪑 TABLE / टेबल #$selectedTableForQr",
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Box(
                                            modifier = Modifier.size(190.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = tableQrApiUrl,
                                                contentDescription = "Table $selectedTableForQr QR Code",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Fit
                                            )

                                            Surface(
                                                shape = CircleShape,
                                                color = DhabaRed,
                                                shadowElevation = 2.dp,
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text("🍽️", fontSize = 16.sp)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = tableRestaurantUrl,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF57606F)
                                        )
                                        Text(
                                            text = "स्कैन करते ही टेबल $selectedTableForQr ऑटो-सेलेक्ट होगी",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DhabaGreenDark
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Print/Download & Share Table QR Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            Toast.makeText(
                                                context,
                                                "टेबल T-$selectedTableForQr का QR स्टैंड प्रिंट/डाउनलोड हो गया!\nहोटल: $activeHotelId\nलिंक: $tableRestaurantUrl",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = DhabaGreenDark)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("प्रिंट / डाउनलोड T-$selectedTableForQr", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Table QR URL", tableRestaurantUrl)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "टेबल $selectedTableForQr का लिंक कॉपी हो गया!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("लिंक कॉपी करें", fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Batch Sheet Button for All 100 Tables
                                Button(
                                    onClick = { showAllTablesQrSheet = !showAllTablesQrSheet },
                                    modifier = Modifier.fillMaxWidth().height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DhabaGold)
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (showAllTablesQrSheet) "प्रिंट शीट बंद करें" else "🖨️ सभी 100 टेबल्स QR प्रिंट शीट (Print All T-1 to T-100)",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        fontSize = 12.5.sp
                                    )
                                }

                                if (showAllTablesQrSheet) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "📋 होटल '${activeHotelId}' - सभी 100 टेबल्स की सूची (T-1 से T-100):",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            (1..10).forEach { batch ->
                                                val start = (batch - 1) * 10 + 1
                                                val end = batch * 10
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(text = "टेबल्स T-$start से T-$end", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                    Button(
                                                        onClick = {
                                                            Toast.makeText(context, "T-$start से T-$end के QR स्टैंड डाउनलोड हो गए!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = DhabaRed),
                                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                    ) {
                                                        Text("प्रिंट बैच", fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section 2: Unified Single Restaurant Master QR Stand (Entrance / Reception)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = DhabaGold.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.QrCode2,
                                            contentDescription = null,
                                            tint = Color(0xFFD35400),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "मास्टर होटल QR (General Entry QR)",
                                            color = Color(0xFFD35400),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = uiState.shopInfo.shopName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "होटल ID: $activeHotelId  •  टेबल्स: 1 से ${uiState.shopInfo.totalTables}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DhabaGreenDark
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(2.dp, DhabaRed.copy(alpha = 0.4f)),
                                    shadowElevation = 3.dp,
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier.size(170.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = masterQrApiUrl,
                                                contentDescription = "Master Hotel QR Code",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = masterRestaurantUrl,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF57606F)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        Toast.makeText(
                                            context,
                                            "मास्टर QR स्टैंड डाउनलोड हो गया!\nहोटल: $activeHotelId",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DhabaRed)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("मास्टर QR स्टैंड डाउनलोड करें", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        // Section 2: Dynamic Merchant Profile & Payment Configuration
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("merchant_configuration_card"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Payments,
                                            contentDescription = null,
                                            tint = DhabaGreenDark,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "डायनामिक मर्चेंट सेटिंग्स (Payment Config)",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Text(
                                    text = "यहाँ सेट किया गया UPI ID और QR कोड ग्राहकों को बिल भुगतान पेज पर लाइव दिखेगा।",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )

                                // Restaurant Name Input
                                OutlinedTextField(
                                    value = merchantShopName,
                                    onValueChange = { merchantShopName = it },
                                    label = { Text("रेस्टोरेंट का नाम (Restaurant Name)") },
                                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("merchant_shop_name_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                // Merchant UPI ID / VPA Input
                                OutlinedTextField(
                                    value = merchantUpiId,
                                    onValueChange = { merchantUpiId = it },
                                    label = { Text("मर्चेंट UPI ID / VPA (Merchant UPI)") },
                                    placeholder = { Text("e.g. yourbusiness@okhdfcbank") },
                                    leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = DhabaGreenDark) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("merchant_upi_id_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                // Quick UPI Suffix Chips
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("@okhdfcbank", "@okaxis", "@paytm", "@ybl", "@upi", "@icici").forEach { suffix ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.clickable {
                                                val clean = merchantUpiId.substringBefore("@")
                                                merchantUpiId = "${clean.ifBlank { "merchant" }}$suffix"
                                            }
                                        ) {
                                            Text(
                                                text = suffix,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                // Custom UPI QR Asset URL Input (Optional)
                                OutlinedTextField(
                                    value = merchantCustomQrUrl,
                                    onValueChange = { merchantCustomQrUrl = it },
                                    label = { Text("कस्टम मर्चेंट QR इमेज URL (वैकल्पिक)") },
                                    placeholder = { Text("https://example.com/merchant_qr.png") },
                                    leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("merchant_custom_qr_url_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                // Total Tables (1-100) & Phone Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = merchantTotalTables,
                                        onValueChange = { input ->
                                            if (input.isEmpty() || input.all { it.isDigit() }) {
                                                merchantTotalTables = input
                                            }
                                        },
                                        label = { Text("कुल टेबल्स (1-100)") },
                                        leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("merchant_total_tables_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = merchantPhone,
                                        onValueChange = { merchantPhone = it },
                                        label = { Text("फ़ोन नंबर") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .testTag("merchant_phone_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )
                                }

                                // Address Input
                                OutlinedTextField(
                                    value = merchantAddress,
                                    onValueChange = { merchantAddress = it },
                                    label = { Text("रेस्टोरेंट का पता (Address)") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("merchant_address_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Save & Sync Button
                                Button(
                                    onClick = {
                                        val totalT = merchantTotalTables.toIntOrNull()?.coerceIn(1, 100) ?: 100
                                        viewModel.updateMerchantSettings(
                                            shopName = merchantShopName,
                                            upiId = merchantUpiId,
                                            customUpiQrUrl = merchantCustomQrUrl,
                                            phone = merchantPhone,
                                            address = merchantAddress,
                                            totalTables = totalT
                                        )
                                        isSavedSuccess = true
                                        Toast.makeText(
                                            context,
                                            "मर्चेंट सेटिंग्स सफलतापूर्वक अपडेट हो गई!\nUPI ID: ${merchantUpiId.ifBlank { "cheter.dine@okhdfcbank" }}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("save_merchant_settings_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DhabaGreenDark)
                                ) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "मर्चेंट सेटिंग्स सेव व सिंक करें (Save & Sync)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }

                                if (isSavedSuccess) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = DhabaGreenDark.copy(alpha = 0.1f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = DhabaGreenDark,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "लाइव अपडेट सक्रिय: सभी ग्राहक अब UPI ID: '${merchantUpiId}' पर भुगतान करेंगे।",
                                                fontSize = 11.5.sp,
                                                color = DhabaGreenDark,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Section 3: Live Customer Payment QR Preview
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📱 ग्राहक भुगतान स्क्रीन प्रीव्यू (Customer Bill Preview)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "जब ग्राहक किसी भी टेबल (1-100) से बिल भरेंगे, तो उन्हें यह UPI विवरण दिखेगा:",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "प्राप्तकर्ता: ${uiState.shopInfo.shopName}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "UPI VPA: ${uiState.shopInfo.upiId.ifBlank { "cheter.dine@okhdfcbank" }}",
                                                fontSize = 12.sp,
                                                color = DhabaGreenDark,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = DhabaGold.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "₹ लाइव बिल",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFD35400),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Section 4: Owner Security & Session Management
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("seller_account_security_card"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = DhabaRed,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = "🔐 ओनर खाता व सुरक्षा (Account & Security)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (uiState.shopOwner.isAuthenticated) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.fillMaxWidth()
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
                                                        text = "प्रमाणीकृत ओनर: ${uiState.shopOwner.displayName.ifBlank { "दुकानदार" }}",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "📱 फोन: ${if (uiState.shopOwner.phone.isNotBlank()) "+91 ${uiState.shopOwner.phone}" else "9876543210"}",
                                                        fontSize = 12.5.sp,
                                                        color = DhabaGreenDark,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = DhabaGreenDark.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = "✅ सत्र सक्रिय",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = DhabaGreenDark,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "दुकान ID: ${uiState.shopOwner.shopId} • सुरक्षा: फोन + पासवर्ड प्रमाणीकरण",
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Spacer(modifier = Modifier.height(14.dp))

                                            // Explicit Logout Option
                                            Button(
                                                onClick = {
                                                    viewModel.signOut(context)
                                                    Toast.makeText(context, "ओनर सत्र सफलतापूर्वक समाप्त (Logged out)", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(44.dp)
                                                    .testTag("settings_logout_button"),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = DhabaRed)
                                            ) {
                                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "🚪 सत्र से लॉगआउट करें (Logout Session)",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "वर्तमान में कोई ओनर सत्र सक्रिय नहीं है।",
                                                fontSize = 12.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Button(
                                                onClick = { viewModel.setAuthDialogOpen(true) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(42.dp)
                                                    .testTag("settings_open_login_button"),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = DhabaGreenDark)
                                            ) {
                                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("🔑 ओनर लॉगिन करें (Login with Phone & Password)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
    }

    // Shop Owner Auth Dialog
    if (uiState.isAuthDialogOpen) {
        ShopAuthDialog(
            currentUser = uiState.shopOwner,
            isLoading = uiState.isAuthLoading,
            authErrorMessage = uiState.authErrorMessage,
            lockoutRemainingSeconds = uiState.lockoutRemainingSeconds,
            failedAttemptsCount = uiState.failedAttemptsCount,
            onDismiss = { viewModel.setAuthDialogOpen(false) },
            onSignInPhone = { phone, pass -> viewModel.signInPhone(context, phone, pass) },
            onSignUpPhone = { phone, pass, sName, oName -> viewModel.signUpPhone(context, phone, pass, sName, oName) },
            onClearError = { viewModel.clearAuthError() },
            onQuickDemoLogin = { viewModel.quickDemoLogin(context) },
            onSignOut = { ctx -> viewModel.signOut(ctx) }
        )
    }

    // Admin Panel Add / Edit Dish Modal with Custom Prep Time input for Supabase
    if (isAddEditModalOpen) {
        AddDishDialog(
            initialItem = editingItem,
            onDismiss = {
                isAddEditModalOpen = false
                editingItem = null
            },
            onSaveDish = { id, nameHi, nameEn, category, price, descHi, descEn, isVeg, spicyLevel, emoji, prepTimeMin ->
                val shopId = formShopId.ifBlank { "cheter_101" }
                if (id != null) {
                    val current = editingItem ?: MenuItem(id = id, nameHi = nameHi, nameEn = nameEn, category = category, price = price)
                    val updated = current.copy(
                        id = id,
                        nameHi = nameHi,
                        nameEn = nameEn,
                        category = category,
                        price = price,
                        descHi = descHi,
                        descEn = descEn,
                        isVeg = isVeg,
                        spicyLevel = spicyLevel,
                        emoji = emoji,
                        prepTimeMin = prepTimeMin
                    )
                    viewModel.editDishFromSeller(shopId, updated)
                    Toast.makeText(context, "${nameHi} की तैयारी का समय ($prepTimeMin min) Supabase 'menu_items' (prep_time) में अपडेट हुआ!", Toast.LENGTH_LONG).show()
                } else {
                    viewModel.addNewDishFromSeller(
                        shopId = shopId,
                        nameHi = nameHi,
                        nameEn = nameEn,
                        category = category,
                        price = price,
                        mediaUrl = "",
                        mediaType = "image",
                        isVeg = isVeg,
                        spicyLevel = spicyLevel,
                        emoji = emoji,
                        prepTimeMin = prepTimeMin
                    )
                    Toast.makeText(context, "${nameHi} Supabase 'menu_items' (prep_time: $prepTimeMin min) में सुरक्षित हुआ!", Toast.LENGTH_LONG).show()
                }
                isAddEditModalOpen = false
                editingItem = null
            }
        )
    }

    // Itemized Receipt Modal Dialog for Restaurant Owner
    receiptBillSummaryModal?.let { summary ->
        ItemizedReceiptDialog(
            billSummary = summary,
            shopInfo = uiState.shopInfo,
            onDismiss = { receiptBillSummaryModal = null },
            onPayBillClick = {
                viewModel.payBill(summary.tableNumber, com.example.model.PaymentMethod.CASH)
                receiptBillSummaryModal = null
                Toast.makeText(context, "टेबल #${summary.tableNumber} का बिल चुकता कर दिया गया", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun SellerItemRow(
    item: MenuItem,
    onEdit: () -> Unit,
    onToggleAvailability: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("seller_item_row_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Media Photo / Video preview or Emoji
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                OptimizedDishImage(
                    imageUrl = item.mediaUrl,
                    fallbackEmoji = item.emoji,
                    contentDescription = item.nameHi,
                    targetWidthPx = 180,
                    targetHeightPx = 180,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (item.mediaType == "video" && item.mediaUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleFilled,
                            contentDescription = "Video",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.nameHi,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.mediaType == "video") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFF4757).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "VIDEO",
                                color = Color(0xFFFF4757),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${item.category.emoji} ${item.category.titleHi}",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Custom prep time badge from Supabase prep_time column
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CheterCyan.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = CheterCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${item.prepTimeMin} min",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = CheterCyan
                            )
                        }
                    }
                }

                Text(
                    text = "₹${item.price}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DhabaGreenDark
                )
            }

            // In-stock Switch, Edit & Delete buttons
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (item.isAvailable) "उपलब्ध" else "खत्म",
                        fontSize = 11.sp,
                        color = if (item.isAvailable) DhabaGreenDark else Color(0xFFE74C3C),
                        fontWeight = FontWeight.Bold
                    )
                    Switch(
                        checked = item.isAvailable,
                        onCheckedChange = { onToggleAvailability() },
                        modifier = Modifier.testTag("toggle_avail_${item.id}")
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Edit Dish button (opens Add/Edit Dish modal with custom prep time field)
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("edit_item_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Dish",
                            tint = DhabaGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("delete_item_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFE74C3C),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KitchenOrdersTabContent(
    viewModel: MenuViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Live clock ticker
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    val allOrders = uiState.tableOrdersMap.values.flatten().sortedByDescending { it.orderTimestamp }
    val activeOrders = allOrders.filter { it.status != OrderStatus.SERVED }
    val servedOrders = allOrders.filter { it.status == OrderStatus.SERVED }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Summary Header Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DhabaRed.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔥 सक्रिय ऑर्डर", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DhabaRed)
                    Text("${activeOrders.size}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = DhabaRed)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DhabaGold.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val hurryCount = activeOrders.count {
                        val remSecs = ((it.estimatedReadyTimestamp - currentTime) / 1000).toInt()
                        remSecs < 300
                    }
                    Text("⚠️ <5 मिनट बाकी", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD35400))
                    Text("$hurryCount", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFFD35400))
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DhabaGreenDark.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅ सर्व किए गए", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DhabaGreenDark)
                    Text("${servedOrders.size}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = DhabaGreenDark)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "👨‍🍳 लाइव किचन ऑर्डर्स & काउंटडाउन टाइमर (Kitchen Queue)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (activeOrders.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎉", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("सभी ऑर्डर पूरे हो चुके हैं!", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("किचन में कोई लंबित ऑर्डर नहीं है", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            activeOrders.forEach { order ->
                val totalDurationMs = (order.estimatedPrepTimeMinutes * 60 * 1000L).coerceAtLeast(1000L)
                val remainingMs = (order.estimatedReadyTimestamp - currentTime).coerceAtLeast(0L)
                val remainingSeconds = (remainingMs / 1000).toInt()
                val remMins = remainingSeconds / 60
                val remSecs = remainingSeconds % 60
                val isUnder5Min = remainingSeconds < 300
                val elapsedMs = (currentTime - order.orderTimestamp).coerceAtLeast(0L)
                val progressFraction = (elapsedMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("kitchen_order_card_${order.orderId}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnder5Min) DhabaRed.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isUnder5Min) 2.dp else 1.dp,
                        if (isUnder5Min) DhabaRed else CheterCyan.copy(alpha = 0.4f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Header row: Table & Status Tag
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isUnder5Min) DhabaRed else DhabaGold
                                ) {
                                    Text(
                                        text = "टेबल #${order.tableNumber}",
                                        color = if (isUnder5Min) Color.White else Color(0xFF1E273D),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ऑर्डर #${order.orderId}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DhabaRed
                                )
                            }

                            // Status Tag
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (order.status) {
                                    OrderStatus.RECEIVED -> Color(0xFFE67E22)
                                    OrderStatus.PREPARING -> DhabaRed
                                    OrderStatus.READY_TO_SERVE -> Color(0xFF2980B9)
                                    OrderStatus.SERVED -> DhabaGreenDark
                                }
                            ) {
                                Text(
                                    text = order.status.titleHi,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Customer info
                        Text(
                            text = "👤 ग्राहक: ${order.customerName} (${order.customerPhone})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live ETA Countdown Banner inside Kitchen Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUnder5Min) Color(0xFF7F1D1D) else Color(0xFF0F172A)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
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
                                            tint = if (isUnder5Min) Color(0xFFFCA5A5) else CheterCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isUnder5Min) "🔥 अत्यावश्यक (Less than 5 min!)" else "अनुमानित तैयारी समय (Live ETA)",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUnder5Min) Color(0xFFFCA5A5) else CheterCyan
                                        )
                                    }

                                    Text(
                                        text = String.format("%02d:%02d शेष", remMins, remSecs),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isUnder5Min) Color(0xFFF87171) else Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(2.5.dp)),
                                    color = if (isUnder5Min) Color(0xFFEF4444) else CheterCyan,
                                    trackColor = Color(0xFF334155)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dishes list
                        Text(
                            text = "🍲 व्यंजन सूची:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        order.items.forEach { cartItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• ${cartItem.item.emoji} ${cartItem.item.nameHi} (${cartItem.item.nameEn})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "x${cartItem.quantity}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DhabaRed
                                )
                            }
                        }

                        if (order.specialInstructions.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "📝 विशेष निर्देश: ${order.specialInstructions}",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons for Waiter & Kitchen
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (order.status == OrderStatus.RECEIVED) {
                                Button(
                                    onClick = {
                                        viewModel.updateOrderStatus(order.orderId, order.tableNumber, OrderStatus.PREPARING)
                                        Toast.makeText(context, "ऑर्डर #${order.orderId} पकाया जा रहा है", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f).height(40.dp).testTag("start_cooking_${order.orderId}"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DhabaRed)
                                ) {
                                    Text("👨‍🍳 पकाना शुरू", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            if (order.status == OrderStatus.PREPARING) {
                                Button(
                                    onClick = {
                                        viewModel.updateOrderStatus(order.orderId, order.tableNumber, OrderStatus.READY_TO_SERVE)
                                        Toast.makeText(context, "ऑर्डर #${order.orderId} परोसने के लिए तैयार है", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f).height(40.dp).testTag("ready_to_serve_${order.orderId}"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2980B9))
                                ) {
                                    Text("🔔 तैयार है", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            // Waiter Action: "Mark as Served"
                            Button(
                                onClick = {
                                    viewModel.markOrderAsServed(order.orderId, order.tableNumber)
                                    Toast.makeText(context, "टेबल #${order.tableNumber} पर भोजन परोसा गया! टाइमर रुक गया।", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(40.dp)
                                    .testTag("waiter_mark_served_${order.orderId}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DhabaGreenDark)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("✅ Mark as Served", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Served Orders List (Historical)
        if (servedOrders.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "✅ परोसे गए ऑर्डर (Served History - ${servedOrders.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DhabaGreenDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            servedOrders.take(5).forEach { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("टेबल #${order.tableNumber} • ऑर्डर #${order.orderId}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${order.customerName} • ${order.items.joinToString { "${it.item.nameHi} x${it.quantity}" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DhabaGreenDark.copy(alpha = 0.15f)
                        ) {
                            Text("✅ SERVED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DhabaGreenDark, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 6: 100 RESTAURANT TABLES GRID (SUPABASE POSTGRESQL REALTIME SYNC)
// =========================================================================
@Composable
fun TablesManagerTabContent(viewModel: MenuViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var tableFilter by remember { mutableStateOf("ALL") } // ALL, OCCUPIED, FREE
    var searchQuery by remember { mutableStateOf("") }

    val allTables = uiState.restaurantTables
    val occupiedCount = allTables.count { it.isOccupied }
    val freeCount = allTables.size - occupiedCount
    val totalLiveBillOnTables = allTables.filter { it.isOccupied }.sumOf { it.activeOrderTotal }

    val filteredTables = remember(allTables, tableFilter, searchQuery) {
        var list = allTables
        if (tableFilter == "OCCUPIED") {
            list = list.filter { it.isOccupied }
        } else if (tableFilter == "FREE") {
            list = list.filter { !it.isOccupied }
        }
        if (searchQuery.isNotBlank()) {
            val num = searchQuery.toIntOrNull()
            list = if (num != null) {
                list.filter { it.tableNumber == num }
            } else {
                list.filter { it.tableCode.contains(searchQuery, ignoreCase = true) || it.activeCustomerName?.contains(searchQuery, ignoreCase = true) == true }
            }
        }
        list
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Live Supabase Metrics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DinnerDining, contentDescription = null, tint = DhabaRed, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("100 टेबल्स लाइव प्रबंधन (T-1 to T-100)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DhabaGreenDark.copy(alpha = 0.15f)
                        ) {
                            Text("⚡ Supabase PostgreSQL", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = DhabaGreenDark, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4 Stat Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Total
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("कुल टेबल्स", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("100", fontSize = 18.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        // Occupied
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = DhabaRed.copy(alpha = 0.12f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔴 भरे हुए", fontSize = 10.5.sp, color = DhabaRed, fontWeight = FontWeight.Bold)
                                Text("$occupiedCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = DhabaRed)
                            }
                        }
                        // Free
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = DhabaGreenDark.copy(alpha = 0.12f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🟢 खाली", fontSize = 10.5.sp, color = DhabaGreenDark, fontWeight = FontWeight.Bold)
                                Text("$freeCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = DhabaGreenDark)
                            }
                        }
                        // Active Sales
                        Surface(
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(12.dp),
                            color = DhabaGold.copy(alpha = 0.2f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("लाइव बिल", fontSize = 10.5.sp, color = Color(0xFFD35400), fontWeight = FontWeight.Bold)
                                Text("₹$totalLiveBillOnTables", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFD35400))
                            }
                        }
                    }
                }
            }
        }

        // Filter and Search Controls
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("टेबल नं खोजें (1-100)...", fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("table_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                FilterChip(
                    selected = tableFilter == "ALL",
                    onClick = { tableFilter = "ALL" },
                    label = { Text("सभी (100)", fontSize = 11.5.sp) }
                )
                FilterChip(
                    selected = tableFilter == "OCCUPIED",
                    onClick = { tableFilter = "OCCUPIED" },
                    label = { Text("🔴 भरे ($occupiedCount)", fontSize = 11.5.sp) }
                )
                FilterChip(
                    selected = tableFilter == "FREE",
                    onClick = { tableFilter = "FREE" },
                    label = { Text("🟢 खाली ($freeCount)", fontSize = 11.5.sp) }
                )
            }
        }

        // Table Items List
        items(filteredTables, key = { it.tableNumber }) { table ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("table_card_${table.tableNumber}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (table.isOccupied) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (table.isOccupied) 3.dp else 1.dp),
                border = if (table.isOccupied) androidx.compose.foundation.BorderStroke(1.5.dp, DhabaRed.copy(alpha = 0.5f)) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (table.isOccupied) DhabaRed else DhabaGreenDark,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = table.tableCode,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "टेबल #${table.tableNumber}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (table.isOccupied) DhabaRed.copy(alpha = 0.15f) else DhabaGreenDark.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (table.isOccupied) "🔴 OCCUPIED" else "🟢 AVAILABLE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (table.isOccupied) DhabaRed else DhabaGreenDark,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            Text(
                                text = "क्षमता: ${table.capacity} सीट्स ${if (table.isOccupied) "• ${table.activeCustomerName ?: "ग्राहक"} (₹${table.activeOrderTotal})" else "• तैयार"}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (table.isOccupied && table.activeOrderId != null) {
                                Text(
                                    text = "ऑर्डर ID: ${table.activeOrderId}",
                                    fontSize = 10.5.sp,
                                    color = DhabaRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Action Button
                    if (table.isOccupied) {
                        Button(
                            onClick = {
                                viewModel.freeTable(table.tableNumber)
                                Toast.makeText(context, "टेबल #${table.tableNumber} खाली कर दी गई", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DhabaGreenDark),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("free_table_btn_${table.tableNumber}")
                        ) {
                            Text("✅ खाली करें", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                viewModel.occupyTable(table.tableNumber, "Guest T-${table.tableNumber}", 0)
                                Toast.makeText(context, "टेबल #${table.tableNumber} बुक की गई", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("occupy_table_btn_${table.tableNumber}")
                        ) {
                            Text("🪑 बैठाएं", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 7: SUPABASE RELATIONAL DATABASE & AUTH CONFIGURATION
// =========================================================================
@Composable
fun SupabaseSettingsTabContent(viewModel: MenuViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var projectUrl by remember { mutableStateOf(uiState.supabaseProjectUrl) }
    var anonKey by remember { mutableStateOf(com.example.supabase.SupabaseConfig.DEFAULT_ANON_KEY) }
    var isSaved by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Supabase Banner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = DhabaGreenDark,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("⚡ Supabase PostgreSQL", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                Text("Primary Relational Database Engine", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DhabaGreenDark
                        ) {
                            Text("🟢 ACTIVE & SYNCED", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Supabase PostgreSQL डेटाबेस 100 टेबल्स, मेनू आइटम्स और रीयल-टाइम ऑर्डर सिंक के लिए पूरी तरह एकीकृत है। एडमिन डैशबोर्ड बिना रीफ़्रेश किए नए ऑर्डर तुरंत प्राप्त करता है।",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Schema Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("📊 PostgreSQL डेटाबेस स्कीमा (Relational Tables)", fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    val schemaItems = listOf(
                        Triple("restaurant_tables", "100 टेबल्स (T-1..T-100) लाइव ऑक्यूपेंसी व बिल", "table_number, table_code, is_occupied, capacity"),
                        Triple("orders", "लाइव ग्राहक ऑर्डर, ETA टाइमर व पेमेंट स्टेटस", "id, shop_id, table_number, grand_total, status"),
                        Triple("menu_items", "व्यंजन कैटलॉग, मूल्य, श्रेणी व शाकाहारी फ़िल्टर", "id, name_hi, name_en, price, category, is_veg"),
                        Triple("restaurant_owners", "ओनर ऑथेंटिकेशन (फ़ोन + SHA-256 सुरक्षित पासवर्ड)", "phone, password_hash, salt, failed_attempts, is_locked")
                    )

                    schemaItems.forEach { (table, desc, cols) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("🗄️ $table", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = DhabaRed)
                                    Text("PostgreSQL", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("कॉलम्स: $cols", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Supabase Connection Configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("⚙️ Supabase प्रोजेक्ट कॉन्फ़िगरेशन", fontSize = 14.5.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = projectUrl,
                        onValueChange = { projectUrl = it },
                        label = { Text("Supabase Project URL") },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("supabase_url_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = anonKey,
                        onValueChange = { anonKey = it },
                        label = { Text("Supabase Anon Public API Key") },
                        leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("supabase_anon_key_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            viewModel.updateSupabaseConfig(context, projectUrl, anonKey)
                            isSaved = true
                            Toast.makeText(context, "Supabase कॉन्फ़िगरेशन सहेजा गया और रीकनेक्ट किया गया!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("save_supabase_config_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DhabaGreenDark)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("💾 सहेजें व रीकनेक्ट करें (Save & Reconnect)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    if (isSaved) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DhabaGreenDark.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✅ Supabase PostgreSQL से सफलतापूर्वक कनेक्टेड। 3-सेकंड रीयल-टाइम पोलिंग सक्रिय है।",
                                fontSize = 11.sp,
                                color = DhabaGreenDark,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
