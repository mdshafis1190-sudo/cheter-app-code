package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirebaseMenuService
import com.example.data.SampleMenuData
import com.example.model.AppMode
import com.example.model.BlockedCustomer
import com.example.model.CartItem
import com.example.model.CashPaymentAlert
import com.example.model.DatabaseEngine
import com.example.model.LanguageMode
import com.example.model.MenuCategory
import com.example.model.MenuItem
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.QrScanResult
import com.example.model.RestaurantTable
import com.example.model.ScannedCodeType
import com.example.model.ShopInfo
import com.example.model.ShopOwnerUser
import com.example.model.SpicyLevel
import com.example.model.TableBillSummary
import com.example.model.TableOrder
import com.example.supabase.SupabaseConfig
import com.example.supabase.SupabaseMenuService
import com.example.supabase.SupabaseRealtimeEvent
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption(val titleHi: String, val titleEn: String) {
    POPULARITY("लोकप्रियता (Popularity)", "Popularity"),
    PRICE_LOW_HIGH("कीमत: कम से ज्यादा", "Price: Low to High"),
    PRICE_HIGH_LOW("कीमत: ज्यादा से कम", "Price: High to Low"),
    RATING("रेटिंग (Rating)", "Top Rated")
}

data class MenuUiState(
    val allItems: List<MenuItem> = SampleMenuData.initialMenuItems,
    val selectedCategory: MenuCategory = MenuCategory.ALL,
    val searchQuery: String = "",
    val vegOnlyFilter: Boolean = false,
    val sortOption: SortOption = SortOption.POPULARITY,
    val languageMode: LanguageMode = LanguageMode.BOTH,
    val selectedTableNumber: Int = 4,
    val cartItems: Map<Int, CartItem> = emptyMap(),
    val activeOrder: TableOrder? = null,
    val tableOrdersMap: Map<Int, List<TableOrder>> = emptyMap(),
    val orderHistory: List<TableOrder> = emptyList(),
    val favoriteItemIds: Set<Int> = setOf(1, 2, 4, 8),
    val selectedItemForDetail: MenuItem? = null,
    val isCartOpen: Boolean = false,
    val isTableQrOpen: Boolean = false,
    val isQrScannerOpen: Boolean = false,
    val isPayBillDialogOpen: Boolean = false,
    val isAddDishOpen: Boolean = false,
    val isOrderStatusOpen: Boolean = false,
    val lastScannedResult: QrScanResult? = null,
    val paymentSuccessOrder: TableOrder? = null,
    val customerName: String = "Guest Table 4",
    val currentCustomerId: String = "CUST-4102",
    val currentCustomerPhone: String = "9876543210",
    val isCurrentCustomerBlocked: Boolean = false,
    val blockedCustomerNotice: BlockedCustomer? = null,
    val activeCashAlerts: List<CashPaymentAlert> = emptyList(),
    val blockedCustomers: List<BlockedCustomer> = emptyList(),
    val isCashRequestedForTable: Set<Int> = emptySet(),
    val orderNotes: String = "",
    val appMode: AppMode = AppMode.CUSTOMER_MENU,
    val shopInfo: ShopInfo = ShopInfo(),
    val shopOwner: ShopOwnerUser = ShopOwnerUser(),
    val isAuthDialogOpen: Boolean = false,
    val isAuthLoading: Boolean = false,
    val authErrorMessage: String? = null,
    val lockoutRemainingSeconds: Long = 0L,
    val failedAttemptsCount: Int = 0,
    val isCloudConnected: Boolean = true,
    val databaseEngine: DatabaseEngine = DatabaseEngine.SUPABASE_POSTGRESQL,
    val supabaseProjectUrl: String = SupabaseConfig.DEFAULT_PROJECT_URL,
    val restaurantTables: List<RestaurantTable> = emptyList(),
    val newRealtimeOrderNotification: TableOrder? = null,
    val dishReadyAlert: com.example.model.DishReadyAlert? = null
)

class MenuViewModel : ViewModel() {

    private val firebaseService = FirebaseMenuService()
    val supabaseService = SupabaseMenuService(null)

    private var activeTableListenerRegistration: ListenerRegistration? = null
    private var kitchenOrdersListenerRegistration: ListenerRegistration? = null
    private var cashAlertsListenerRegistration: ListenerRegistration? = null
    private var blockedUsersListenerRegistration: ListenerRegistration? = null
    private var shopInfoListenerRegistration: ListenerRegistration? = null

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState

    init {
        // Initial sample active order for Table #4 to showcase live billing
        val sampleTable4Cart = listOf(
            CartItem(
                item = SampleMenuData.initialMenuItems[0], // Paneer Butter Masala
                quantity = 2,
                specialNote = "कम मक्खन, मध्यम तीखा"
            ),
            CartItem(
                item = SampleMenuData.initialMenuItems[2], // Butter Naan
                quantity = 4,
                specialNote = "गरमा गरम क्रिस्पी"
            ),
            CartItem(
                item = SampleMenuData.initialMenuItems[3], // Dal Makhani
                quantity = 1,
                specialNote = ""
            )
        )
        val sampleSubtotal = sampleTable4Cart.sumOf { it.totalPrice }
        val sampleGst = (sampleSubtotal * 0.05).toInt()
        val sampleTip = 30
        val sampleOrder = TableOrder(
            orderId = "RD-4102",
            tableNumber = 4,
            customerName = "Guest Table 4",
            items = sampleTable4Cart,
            specialInstructions = "कृप्या खाना एक साथ लाएं",
            subtotal = sampleSubtotal,
            gst = sampleGst,
            tip = sampleTip,
            grandTotal = sampleSubtotal + sampleGst + sampleTip,
            status = OrderStatus.SERVED,
            paymentStatus = PaymentStatus.UNPAID
        )

        val initialTableMap = mapOf(4 to listOf(sampleOrder))

        // Pre-populate 100 restaurant tables (T-1 to T-100)
        val initialTables = (1..100).map { num ->
            RestaurantTable(
                tableNumber = num,
                tableCode = "T-$num",
                capacity = if (num % 5 == 0) 8 else if (num % 2 == 0) 6 else 4,
                isOccupied = (num == 4 || num == 12),
                activeOrderId = if (num == 4) "RD-4102" else if (num == 12) "RD-4103" else null,
                activeCustomerName = if (num == 4) "Guest Table 4" else if (num == 12) "Amit Patel" else null,
                activeOrderTotal = if (num == 4) 860 else if (num == 12) 540 else 0,
                lastOccupiedTime = if (num == 4 || num == 12) System.currentTimeMillis() - 15 * 60 * 1000L else 0L
            )
        }

        // Check current authenticated user on startup
        val existingUser = firebaseService.getCurrentUser()
        if (existingUser != null) {
            _uiState.value = _uiState.value.copy(
                shopOwner = existingUser,
                activeOrder = sampleOrder,
                tableOrdersMap = initialTableMap,
                restaurantTables = initialTables,
                shopInfo = _uiState.value.shopInfo.copy(
                    shopId = existingUser.shopId,
                    shopName = existingUser.shopName,
                    ownerEmail = existingUser.email,
                    menuBaseUrl = "https://cheter.app?shopId=${existingUser.shopId}",
                    qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?data=https://cheter.app?shopId=${existingUser.shopId}"
                )
            )
        } else {
            _uiState.value = _uiState.value.copy(
                activeOrder = sampleOrder,
                tableOrdersMap = initialTableMap,
                restaurantTables = initialTables
            )
        }

        // Start online real-time targeted session listeners & Supabase Realtime
        startRealtimeListeners()
        startSupabaseRealtime(_uiState.value.shopInfo.hotelId.ifBlank { "hotel1" })
        loadSupabaseData(_uiState.value.shopInfo.hotelId.ifBlank { "hotel1" })
    }

    private fun loadSupabaseData(hotelId: String) {
        viewModelScope.launch {
            try {
                val cleanHotelId = hotelId.ifBlank { "hotel1" }
                val menuRes = supabaseService.getMenuItems(cleanHotelId)
                if (menuRes.isSuccess && menuRes.getOrNull()?.isNotEmpty() == true) {
                    _uiState.value = _uiState.value.copy(allItems = menuRes.getOrNull()!!)
                }
                val tables = supabaseService.getTables(cleanHotelId)
                if (tables.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(restaurantTables = tables)
                }
                // Fetch restaurant/owner settings from Supabase
                val restaurantRes = supabaseService.getRestaurantSettings(cleanHotelId)
                if (restaurantRes.isSuccess && restaurantRes.getOrNull() != null) {
                    val restaurant = restaurantRes.getOrNull()!!
                    val curShop = _uiState.value.shopInfo
                    _uiState.value = _uiState.value.copy(
                        shopInfo = curShop.copy(
                            shopName = if (restaurant.name.isNotBlank()) restaurant.name else curShop.shopName,
                            upiId = if (restaurant.upiId.isNotBlank()) restaurant.upiId else curShop.upiId,
                            customUpiQrUrl = if (restaurant.customUpiQrUrl.isNotBlank()) restaurant.customUpiQrUrl else curShop.customUpiQrUrl,
                            phone = if (restaurant.phone.isNotBlank()) restaurant.phone else curShop.phone,
                            address = if (restaurant.address.isNotBlank()) restaurant.address else curShop.address,
                            totalTables = if (restaurant.totalTables > 0) restaurant.totalTables else curShop.totalTables
                        )
                    )
                }
            } catch (_: Exception) {}
        }
    }

    private fun startSupabaseRealtime(hotelId: String) {
        val cleanHotelId = hotelId.ifBlank { "hotel1" }
        val flow = supabaseService.initRealtime(viewModelScope, cleanHotelId) { _uiState.value.allItems }
        if (flow != null) {
            viewModelScope.launch {
                flow.collect { event ->
                    when (event) {
                        is SupabaseRealtimeEvent.NewOrderReceived -> {
                            val newOrd = event.order
                            val currentMap = _uiState.value.tableOrdersMap.toMutableMap()
                            val list = (currentMap[newOrd.tableNumber] ?: emptyList()) + newOrd
                            currentMap[newOrd.tableNumber] = list

                            // Mark table occupied in local table list
                            val currentTables = _uiState.value.restaurantTables.map { tbl ->
                                if (tbl.tableNumber == newOrd.tableNumber) {
                                    tbl.copy(
                                        isOccupied = true,
                                        activeOrderId = newOrd.orderId,
                                        activeCustomerName = newOrd.customerName,
                                        activeOrderTotal = newOrd.grandTotal
                                    )
                                } else tbl
                            }

                            _uiState.value = _uiState.value.copy(
                                tableOrdersMap = currentMap,
                                restaurantTables = currentTables,
                                newRealtimeOrderNotification = newOrd
                            )
                        }
                        is SupabaseRealtimeEvent.OrdersSync -> {
                            val syncOrders = event.orders
                            if (syncOrders.isNotEmpty()) {
                                val currentMap = _uiState.value.tableOrdersMap.toMutableMap()
                                val grouped = syncOrders.groupBy { it.tableNumber }
                                grouped.forEach { (tbl, list) ->
                                    currentMap[tbl] = list
                                }
                                _uiState.value = _uiState.value.copy(tableOrdersMap = currentMap)
                            }
                        }
                        is SupabaseRealtimeEvent.TablesSync -> {
                            val syncTables = event.tables
                            if (syncTables.isNotEmpty()) {
                                _uiState.value = _uiState.value.copy(restaurantTables = syncTables)
                            }
                        }
                        is SupabaseRealtimeEvent.ConnectionStateChanged -> {
                            _uiState.value = _uiState.value.copy(isCloudConnected = event.isConnected)
                        }
                        is SupabaseRealtimeEvent.OrderUpdated -> {
                            val upd = event.order
                            val currentMap = _uiState.value.tableOrdersMap.toMutableMap()
                            val list = currentMap[upd.tableNumber]?.map { if (it.orderId == upd.orderId) upd else it } ?: listOf(upd)
                            currentMap[upd.tableNumber] = list
                            _uiState.value = _uiState.value.copy(tableOrdersMap = currentMap)
                        }
                    }
                }
            }
        }
    }

    private fun startRealtimeListeners() {
        val shopId = _uiState.value.shopInfo.shopId.ifBlank { "shop_101" }
        attachActiveTableListener(_uiState.value.selectedTableNumber, shopId)
        attachKitchenOrdersListener(shopId)
        attachCashAlertsListener(shopId)
        attachBlockedUsersListener(shopId)
        attachShopInfoListener(shopId)
    }

    private fun attachShopInfoListener(shopId: String) {
        shopInfoListenerRegistration?.remove()
        val cleanShopId = shopId.ifBlank { "shop_101" }
        shopInfoListenerRegistration = firebaseService.observeShopInfo(cleanShopId) { updatedShop ->
            _uiState.value = _uiState.value.copy(shopInfo = updatedShop)
        }
    }

    fun attachActiveTableListener(tableNumber: Int, shopId: String = _uiState.value.shopInfo.shopId) {
        activeTableListenerRegistration?.remove()
        val cleanShopId = shopId.ifBlank { "shop_101" }
        activeTableListenerRegistration = firebaseService.observeActiveTableOrders(cleanShopId, tableNumber) { onlineOrders ->
            if (onlineOrders.isNotEmpty()) {
                val currentMap = _uiState.value.tableOrdersMap.toMutableMap()
                currentMap[tableNumber] = onlineOrders
                val latestActive = onlineOrders.find { it.status != OrderStatus.SERVED || it.paymentStatus != PaymentStatus.PAID } ?: onlineOrders.lastOrNull()
                _uiState.value = _uiState.value.copy(
                    tableOrdersMap = currentMap,
                    activeOrder = if (_uiState.value.selectedTableNumber == tableNumber && latestActive != null) latestActive else _uiState.value.activeOrder
                )
            }
        }
    }

    private fun attachKitchenOrdersListener(shopId: String) {
        kitchenOrdersListenerRegistration?.remove()
        val cleanShopId = shopId.ifBlank { "shop_101" }
        kitchenOrdersListenerRegistration = firebaseService.observeKitchenActiveOrders(cleanShopId) { onlineOrders ->
            if (onlineOrders.isNotEmpty()) {
                val currentMap = _uiState.value.tableOrdersMap.toMutableMap()
                val grouped = onlineOrders.groupBy { it.tableNumber }
                grouped.forEach { (tbl, list) ->
                    currentMap[tbl] = list
                }
                _uiState.value = _uiState.value.copy(tableOrdersMap = currentMap)
            }
        }
    }

    private fun attachCashAlertsListener(shopId: String) {
        cashAlertsListenerRegistration?.remove()
        val cleanShopId = shopId.ifBlank { "shop_101" }
        cashAlertsListenerRegistration = firebaseService.observeActiveCashAlerts(cleanShopId) { alerts ->
            val cashReqTables = alerts.map { it.tableNumber }.toSet()
            _uiState.value = _uiState.value.copy(
                activeCashAlerts = alerts,
                isCashRequestedForTable = cashReqTables
            )
        }
    }

    private fun attachBlockedUsersListener(shopId: String) {
        blockedUsersListenerRegistration?.remove()
        val cleanShopId = shopId.ifBlank { "shop_101" }
        blockedUsersListenerRegistration = firebaseService.observeBlockedUsers(cleanShopId) { blockedList ->
            val currentCustId = _uiState.value.currentCustomerId
            val currentPhone = _uiState.value.currentCustomerPhone
            val matchingBlocked = blockedList.find { it.customerId == currentCustId || (currentPhone.isNotBlank() && it.phone == currentPhone) }
            _uiState.value = _uiState.value.copy(
                blockedCustomers = blockedList,
                isCurrentCustomerBlocked = matchingBlocked != null,
                blockedCustomerNotice = matchingBlocked
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        activeTableListenerRegistration?.remove()
        kitchenOrdersListenerRegistration?.remove()
        cashAlertsListenerRegistration?.remove()
        blockedUsersListenerRegistration?.remove()
        shopInfoListenerRegistration?.remove()
    }

    val filteredItems: StateFlow<List<MenuItem>> = combine(_uiState) { states ->
        filterAndSortItems(states[0])
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = filterAndSortItems(_uiState.value)
    )

    fun getFilteredList(): List<MenuItem> = filterAndSortItems(_uiState.value)

    fun filterAndSortItems(state: MenuUiState): List<MenuItem> {
        var list = state.allItems

        // Category filter
        if (state.selectedCategory != MenuCategory.ALL) {
            list = list.filter { it.category == state.selectedCategory }
        }

        // Veg only filter
        if (state.vegOnlyFilter) {
            list = list.filter { it.isVeg }
        }

        // Search query (names, descriptions, ingredients, tags, categories)
        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.trim().lowercase()
            list = list.filter { item ->
                item.nameHi.lowercase().contains(query) ||
                item.nameEn.lowercase().contains(query) ||
                item.descHi.lowercase().contains(query) ||
                item.descEn.lowercase().contains(query) ||
                item.ingredients.any { it.lowercase().contains(query) } ||
                item.tags.any { it.lowercase().contains(query) } ||
                item.category.titleHi.lowercase().contains(query) ||
                item.category.titleEn.lowercase().contains(query)
            }
        }

        // Sort
        return when (state.sortOption) {
            SortOption.POPULARITY -> list.sortedWith(compareByDescending<MenuItem> { it.isBestseller }.thenByDescending { it.rating })
            SortOption.PRICE_LOW_HIGH -> list.sortedBy { it.price }
            SortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.price }
            SortOption.RATING -> list.sortedByDescending { it.rating }
        }
    }

    fun setAppMode(mode: AppMode) {
        _uiState.value = _uiState.value.copy(appMode = mode)
    }

    fun setAuthDialogOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isAuthDialogOpen = isOpen)
    }

    fun setShopId(shopId: String) {
        val cleanId = shopId.trim().ifBlank { "shop_101" }
        val currentShop = _uiState.value.shopInfo
        val updatedShop = currentShop.copy(
            shopId = cleanId,
            menuBaseUrl = "https://cheter.app?shopId=$cleanId",
            qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?data=https://cheter.app?shopId=$cleanId"
        )
        _uiState.value = _uiState.value.copy(shopInfo = updatedShop)

        // Load existing cloud merchant config if available
        viewModelScope.launch {
            val remote = firebaseService.getShopInfo(cleanId)
            if (remote != null) {
                _uiState.value = _uiState.value.copy(shopInfo = remote)
            } else {
                firebaseService.saveShopInfo(updatedShop)
            }
        }
        startRealtimeListeners()
    }

    fun updateMerchantSettings(
        shopName: String,
        upiId: String,
        customUpiQrUrl: String = "",
        phone: String = "",
        address: String = "",
        totalTables: Int = 100
    ) {
        val currentShop = _uiState.value.shopInfo
        val cleanShopId = currentShop.shopId.ifBlank { "shop_101" }
        val updatedShopInfo = currentShop.copy(
            shopName = shopName.trim().ifBlank { "CHETER Restaurant & Lounge" },
            upiId = upiId.trim().ifBlank { "cheter.dine@okhdfcbank" },
            customUpiQrUrl = customUpiQrUrl.trim(),
            phone = phone.trim().ifBlank { "+91 98765 43210" },
            address = address.trim().ifBlank { "CHETER Premium Lounge & Dine, NH-44" },
            totalTables = totalTables.coerceIn(1, 100),
            menuBaseUrl = "https://cheter.app?shopId=$cleanShopId",
            qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?data=https://cheter.app?shopId=$cleanShopId"
        )
        _uiState.value = _uiState.value.copy(shopInfo = updatedShopInfo)
        viewModelScope.launch {
            firebaseService.saveShopInfo(updatedShopInfo)
            supabaseService.saveRestaurantSettings(
                restaurantId = updatedShopInfo.hotelId.ifBlank { "hotel1" },
                name = updatedShopInfo.shopName,
                upiId = updatedShopInfo.upiId,
                customUpiQrUrl = updatedShopInfo.customUpiQrUrl,
                phone = updatedShopInfo.phone,
                address = updatedShopInfo.address,
                totalTables = updatedShopInfo.totalTables
            )
        }
    }

    fun setHotelAndTableFromUrl(hotelParam: String?, tableParam: Int?) {
        val cleanHotel = hotelParam?.trim()?.lowercase()?.takeIf { it.isNotBlank() } ?: "hotel1"
        if (cleanHotel != _uiState.value.shopInfo.hotelId) {
            switchHotel(cleanHotel)
        }
        if (tableParam != null && tableParam in 1..100) {
            setTableNumber(tableParam)
        }
    }

    fun switchHotel(newHotelId: String) {
        val cleanHotelId = newHotelId.trim().lowercase().filter { it.isLetterOrDigit() || it == '_' || it == '-' }.ifBlank { "hotel1" }
        val hotelDisplayName = when (cleanHotelId) {
            "hotel1" -> "CHETER Grand Hotel & Lounge (Hotel 1)"
            "hotel2" -> "CHETER Royal Dhaba (Hotel 2)"
            "hotel3" -> "CHETER Elite Highway Resort (Hotel 3)"
            else -> "CHETER - ${cleanHotelId.replace('_', ' ').replace('-', ' ').replaceFirstChar { it.uppercase() }}"
        }
        val masterUrl = "https://cheter.app/?hotel=$cleanHotelId"

        _uiState.value = _uiState.value.copy(
            shopInfo = _uiState.value.shopInfo.copy(
                hotelId = cleanHotelId,
                shopId = cleanHotelId,
                shopName = hotelDisplayName,
                menuBaseUrl = masterUrl,
                qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?data=${java.net.URLEncoder.encode(masterUrl, "UTF-8")}&size=350x350"
            ),
            activeOrder = null,
            cartItems = emptyMap(),
            tableOrdersMap = emptyMap(),
            newRealtimeOrderNotification = null
        )

        // Re-init Realtime listeners and load Supabase data strictly for this hotel
        startRealtimeListeners()
        startSupabaseRealtime(cleanHotelId)
        loadSupabaseData(cleanHotelId)
    }

    fun initSession(context: Context) {
        val sessionUser = com.example.security.AuthSecurityManager.loadSession(context)
        if (sessionUser != null && sessionUser.isAuthenticated) {
            _uiState.value = _uiState.value.copy(
                shopOwner = sessionUser,
                shopInfo = _uiState.value.shopInfo.copy(
                    shopId = sessionUser.shopId,
                    shopName = sessionUser.shopName,
                    ownerEmail = sessionUser.email,
                    phone = if (sessionUser.phone.isNotBlank()) "+91 ${sessionUser.phone}" else _uiState.value.shopInfo.phone,
                    menuBaseUrl = "https://cheter.app?shopId=${sessionUser.shopId}",
                    qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?data=https://cheter.app?shopId=${sessionUser.shopId}"
                )
            )
        }
    }

    fun clearAuthError() {
        _uiState.value = _uiState.value.copy(
            authErrorMessage = null
        )
    }

    fun checkLockoutStatus(context: Context, phone: String): com.example.security.LockoutStatus {
        val status = com.example.security.AuthSecurityManager.checkLockout(context, phone)
        _uiState.value = _uiState.value.copy(
            lockoutRemainingSeconds = status.remainingSeconds,
            failedAttemptsCount = status.failedAttempts
        )
        return status
    }

    fun signInPhone(context: Context, phone: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAuthLoading = true,
                authErrorMessage = null
            )
            val result = supabaseService.signInWithPhone(context, phone, pass)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        shopOwner = user,
                        isAuthLoading = false,
                        isAuthDialogOpen = false,
                        authErrorMessage = null,
                        lockoutRemainingSeconds = 0L,
                        failedAttemptsCount = 0,
                        shopInfo = _uiState.value.shopInfo.copy(
                            shopId = user.shopId,
                            shopName = user.shopName,
                            ownerEmail = user.email,
                            phone = "+91 ${user.phone}",
                            menuBaseUrl = "https://cheter.app?shopId=${user.shopId}",
                            qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?data=https://cheter.app?shopId=${user.shopId}"
                        )
                    )
                    loadSupabaseData(user.shopId)
                    startSupabaseRealtime(user.shopId)
                },
                onFailure = { error ->
                    val lockout = com.example.security.AuthSecurityManager.checkLockout(context, phone)
                    _uiState.value = _uiState.value.copy(
                        isAuthLoading = false,
                        authErrorMessage = error.message ?: "Incorrect Credentials",
                        lockoutRemainingSeconds = lockout.remainingSeconds,
                        failedAttemptsCount = lockout.failedAttempts
                    )
                }
            )
        }
    }

    fun signUpPhone(context: Context, phone: String, pass: String, shopName: String, ownerName: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAuthLoading = true,
                authErrorMessage = null
            )
            val result = supabaseService.signUpWithPhone(context, phone, pass, shopName, ownerName)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        shopOwner = user,
                        isAuthLoading = false,
                        isAuthDialogOpen = false,
                        authErrorMessage = null,
                        lockoutRemainingSeconds = 0L,
                        failedAttemptsCount = 0,
                        shopInfo = _uiState.value.shopInfo.copy(
                            shopId = user.shopId,
                            shopName = user.shopName,
                            ownerEmail = user.email,
                            phone = "+91 ${user.phone}",
                            menuBaseUrl = "https://cheter.app?shopId=${user.shopId}",
                            qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?data=https://cheter.app?shopId=${user.shopId}"
                        )
                    )
                    loadSupabaseData(user.shopId)
                    startSupabaseRealtime(user.shopId)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isAuthLoading = false,
                        authErrorMessage = error.message ?: "Registration Failed"
                    )
                }
            )
        }
    }

    fun signInEmail(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAuthLoading = true, authErrorMessage = null)
            val result = firebaseService.signInWithEmail(email, pass)
            result.onSuccess { user ->
                _uiState.value = _uiState.value.copy(
                    shopOwner = user,
                    isAuthLoading = false,
                    isAuthDialogOpen = false,
                    authErrorMessage = null,
                    shopInfo = _uiState.value.shopInfo.copy(
                        shopId = user.shopId,
                        shopName = user.shopName,
                        ownerEmail = user.email,
                        menuBaseUrl = "https://cheter.app?shopId=${user.shopId}",
                        qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?data=https://cheter.app?shopId=${user.shopId}"
                    )
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isAuthLoading = false,
                    authErrorMessage = error.message ?: "Sign in failed"
                )
            }
        }
    }

    fun signUpEmail(email: String, pass: String, shopName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAuthLoading = true, authErrorMessage = null)
            val result = firebaseService.signUpWithEmail(email, pass, shopName)
            result.onSuccess { user ->
                _uiState.value = _uiState.value.copy(
                    shopOwner = user,
                    isAuthLoading = false,
                    isAuthDialogOpen = false,
                    authErrorMessage = null,
                    shopInfo = _uiState.value.shopInfo.copy(
                        shopId = user.shopId,
                        shopName = user.shopName,
                        ownerEmail = user.email,
                        menuBaseUrl = "https://cheter.app?shopId=${user.shopId}",
                        qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?data=https://cheter.app?shopId=${user.shopId}"
                    )
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isAuthLoading = false,
                    authErrorMessage = error.message ?: "Sign up failed"
                )
            }
        }
    }

    fun signInGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAuthLoading = true, authErrorMessage = null)
            val result = firebaseService.signInWithGoogle(context)
            result.onSuccess { user ->
                com.example.security.AuthSecurityManager.saveSession(context, user)
                _uiState.value = _uiState.value.copy(
                    shopOwner = user,
                    isAuthLoading = false,
                    isAuthDialogOpen = false,
                    authErrorMessage = null,
                    shopInfo = _uiState.value.shopInfo.copy(
                        shopId = user.shopId,
                        shopName = user.shopName,
                        ownerEmail = user.email,
                        menuBaseUrl = "https://cheter.app?shopId=${user.shopId}",
                        qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?data=https://cheter.app?shopId=${user.shopId}"
                    )
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isAuthLoading = false,
                    authErrorMessage = error.message ?: "Google sign in failed"
                )
            }
        }
    }

    fun quickDemoLogin(context: Context? = null) {
        val demoUser = ShopOwnerUser(
            uid = "usr_9876543210",
            phone = "9876543210",
            email = "owner@cheter.app",
            displayName = "CHETER Owner",
            shopId = "cheter_101",
            shopName = "CHETER Restaurant & Lounge",
            isAuthenticated = true,
            authProvider = "Phone & Password",
            sessionToken = java.util.UUID.randomUUID().toString(),
            loginTime = System.currentTimeMillis()
        )
        if (context != null) {
            com.example.security.AuthSecurityManager.saveSession(context, demoUser)
        }
        _uiState.value = _uiState.value.copy(
            shopOwner = demoUser,
            isAuthDialogOpen = false,
            authErrorMessage = null,
            lockoutRemainingSeconds = 0L,
            failedAttemptsCount = 0,
            shopInfo = _uiState.value.shopInfo.copy(
                shopId = demoUser.shopId,
                shopName = demoUser.shopName,
                phone = "+91 9876543210",
                ownerEmail = demoUser.email,
                menuBaseUrl = "https://cheter.app?shopId=${demoUser.shopId}",
                qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?data=https://cheter.app?shopId=${demoUser.shopId}"
            )
        )
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            firebaseService.signOut(context)
            _uiState.value = _uiState.value.copy(
                shopOwner = ShopOwnerUser(),
                isAuthDialogOpen = false,
                authErrorMessage = null,
                lockoutRemainingSeconds = 0L,
                failedAttemptsCount = 0
            )
        }
    }

    fun addNewDishFromSeller(
        shopId: String,
        nameHi: String,
        nameEn: String,
        category: MenuCategory,
        price: Int,
        mediaUrl: String,
        mediaType: String,
        isVeg: Boolean,
        spicyLevel: SpicyLevel,
        emoji: String,
        prepTimeMin: Int = 15
    ) {
        val cleanHotelId = shopId.ifBlank { _uiState.value.shopInfo.hotelId.ifBlank { _uiState.value.shopInfo.shopId.ifBlank { "hotel1" } } }
        val newId = (_uiState.value.allItems.maxOfOrNull { it.id } ?: 0) + 1
        val newItem = MenuItem(
            id = newId,
            nameHi = nameHi,
            nameEn = nameEn,
            category = category,
            price = price,
            prepTimeMin = prepTimeMin,
            descHi = if (isVeg) "ताज़ा व स्वादिष्ट $nameHi" else "लाजवाब $nameHi",
            descEn = "Delicious freshly prepared $nameEn",
            isVeg = isVeg,
            spicyLevel = spicyLevel,
            emoji = emoji.ifBlank { "🍛" },
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            isBestseller = false,
            tags = listOf("New Special")
        )

        _uiState.value = _uiState.value.copy(
            allItems = listOf(newItem) + _uiState.value.allItems
        )

        // Save prep time directly to Supabase and Firebase
        viewModelScope.launch {
            supabaseService.saveMenuItem(newItem, cleanHotelId)
            firebaseService.syncMenuItemToCloud(cleanHotelId, newItem)
        }
    }

    fun editDishFromSeller(
        shopId: String,
        updatedItem: MenuItem
    ) {
        val cleanHotelId = shopId.ifBlank { _uiState.value.shopInfo.hotelId.ifBlank { _uiState.value.shopInfo.shopId.ifBlank { "hotel1" } } }
        _uiState.value = _uiState.value.copy(
            allItems = _uiState.value.allItems.map { if (it.id == updatedItem.id) updatedItem else it }
        )

        // Sync edited item (with custom prep_time) to Supabase & Firebase
        viewModelScope.launch {
            supabaseService.saveMenuItem(updatedItem, cleanHotelId)
            firebaseService.syncMenuItemToCloud(cleanHotelId, updatedItem)
        }
    }

    fun deleteDish(itemId: Int) {
        val cleanHotelId = _uiState.value.shopInfo.hotelId.ifBlank { _uiState.value.shopInfo.shopId.ifBlank { "hotel1" } }
        _uiState.value = _uiState.value.copy(
            allItems = _uiState.value.allItems.filter { it.id != itemId }
        )
        viewModelScope.launch {
            supabaseService.deleteMenuItem(itemId, cleanHotelId)
            firebaseService.deleteMenuItemFromCloud(cleanHotelId, itemId)
        }
    }

    fun toggleItemAvailability(itemId: Int) {
        _uiState.value = _uiState.value.copy(
            allItems = _uiState.value.allItems.map { item ->
                if (item.id == itemId) item.copy(isAvailable = !item.isAvailable) else item
            }
        )
    }

    fun selectCategory(category: MenuCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleVegOnlyFilter() {
        _uiState.value = _uiState.value.copy(vegOnlyFilter = !_uiState.value.vegOnlyFilter)
    }

    fun setSortOption(sortOption: SortOption) {
        _uiState.value = _uiState.value.copy(sortOption = sortOption)
    }

    fun setLanguageMode(mode: LanguageMode) {
        _uiState.value = _uiState.value.copy(languageMode = mode)
    }

    fun setTableNumber(tableNum: Int) {
        val validated = tableNum.coerceIn(1, 100)
        _uiState.value = _uiState.value.copy(
            selectedTableNumber = validated,
            customerName = "Guest Table $validated"
        )
        // High traffic optimization: Re-attach online listener strictly for the selected active table session
        attachActiveTableListener(validated)
    }

    fun toggleFavorite(itemId: Int) {
        val currentFavs = _uiState.value.favoriteItemIds.toMutableSet()
        if (currentFavs.contains(itemId)) {
            currentFavs.remove(itemId)
        } else {
            currentFavs.add(itemId)
        }
        _uiState.value = _uiState.value.copy(favoriteItemIds = currentFavs)
    }

    fun addToCart(item: MenuItem, specialNote: String = "") {
        val currentCart = _uiState.value.cartItems.toMutableMap()
        val existing = currentCart[item.id]
        if (existing != null) {
            currentCart[item.id] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentCart[item.id] = CartItem(item = item, quantity = 1, specialNote = specialNote)
        }
        _uiState.value = _uiState.value.copy(cartItems = currentCart)
    }

    fun removeFromCart(itemId: Int) {
        val currentCart = _uiState.value.cartItems.toMutableMap()
        val existing = currentCart[itemId] ?: return
        if (existing.quantity > 1) {
            currentCart[itemId] = existing.copy(quantity = existing.quantity - 1)
        } else {
            currentCart.remove(itemId)
        }
        _uiState.value = _uiState.value.copy(cartItems = currentCart)
    }

    fun clearCart() {
        _uiState.value = _uiState.value.copy(cartItems = emptyMap())
    }

    fun updateOrderNotes(notes: String) {
        _uiState.value = _uiState.value.copy(orderNotes = notes)
    }

    fun setCustomerName(name: String) {
        _uiState.value = _uiState.value.copy(customerName = name)
    }

    fun showItemDetail(item: MenuItem?) {
        _uiState.value = _uiState.value.copy(selectedItemForDetail = item)
    }

    fun setCartOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isCartOpen = isOpen)
    }

    fun setTableQrOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isTableQrOpen = isOpen)
    }

    fun setAddDishOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isAddDishOpen = isOpen)
    }

    fun setOrderStatusOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isOrderStatusOpen = isOpen)
    }

    fun setQrScannerOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isQrScannerOpen = isOpen)
    }

    fun setPayBillDialogOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isPayBillDialogOpen = isOpen)
    }

    fun dismissPaymentSuccess() {
        _uiState.value = _uiState.value.copy(paymentSuccessOrder = null)
    }

    fun handleScannedCode(rawCode: String): QrScanResult {
        val trimmed = rawCode.trim()
        val result = parseScannedQr(trimmed)
        
        _uiState.value = _uiState.value.copy(
            lastScannedResult = result,
            isQrScannerOpen = false
        )

        // Automatically switch hotel if scanned QR belongs to another hotel
        result.extractedHotelId?.let { hotelId ->
            if (hotelId.isNotBlank() && hotelId != _uiState.value.shopInfo.hotelId) {
                switchHotel(hotelId)
            }
        }

        // Automatically configure table if found
        result.extractedTableNumber?.let { tableNum ->
            setTableNumber(tableNum)
        }

        // If it's a payment QR code or table with bill, can trigger billing view
        if (result.type == ScannedCodeType.UPI_PAYMENT) {
            _uiState.value = _uiState.value.copy(isPayBillDialogOpen = true)
        }

        return result
    }

    private fun parseScannedQr(raw: String): QrScanResult {
        // Check for UPI URL: upi://pay?pa=...&pn=...&am=...
        if (raw.startsWith("upi://", ignoreCase = true)) {
            val vpa = Regex("pa=([^&]+)").find(raw)?.groupValues?.getOrNull(1) ?: "royaldhaba@upi"
            val amStr = Regex("am=([^&]+)").find(raw)?.groupValues?.getOrNull(1)
            val amount = amStr?.toDoubleOrNull()?.toInt()
            return QrScanResult(
                rawText = raw,
                type = ScannedCodeType.UPI_PAYMENT,
                extractedUpiVpa = vpa,
                extractedAmount = amount
            )
        }

        val hotelParamMatch = Regex("""[?&](?:hotel|hotel_id|hotelId|shopId|shop_id)=([^&]+)""", RegexOption.IGNORE_CASE).find(raw)
        val extractedHotel = hotelParamMatch?.groupValues?.getOrNull(1)?.trim()

        // Check for table link / query: e.g. "https://cheter.app?hotel=hotel1&table=5" or "table=5" or "Table #5"
        val tableParamMatch = Regex("""[?&](?:table|table_number|tableNumber|table_id|tableId|t)=(\d+)""", RegexOption.IGNORE_CASE).find(raw)
        if (tableParamMatch != null) {
            val tableNum = tableParamMatch.groupValues[1].toIntOrNull()
            return QrScanResult(
                rawText = raw,
                type = ScannedCodeType.TABLE_SELECTION,
                extractedTableNumber = tableNum,
                extractedHotelId = extractedHotel,
                extractedShopId = extractedHotel
            )
        }

        // If raw contains hotel parameter only
        if (extractedHotel != null) {
            return QrScanResult(
                rawText = raw,
                type = ScannedCodeType.MENU_LINK,
                extractedHotelId = extractedHotel,
                extractedShopId = extractedHotel
            )
        }

        // Check for plain "Table 5" or "Table #5" or "T5"
        val plainTableMatch = Regex("""(?:table|टेबल|tbl)\s*#?\s*(\d+)""", RegexOption.IGNORE_CASE).find(raw)
        if (plainTableMatch != null) {
            val tableNum = plainTableMatch.groupValues[1].toIntOrNull()
            return QrScanResult(
                rawText = raw,
                type = ScannedCodeType.TABLE_SELECTION,
                extractedTableNumber = tableNum
            )
        }

        // Single digit/number check (1 to 100)
        val pureNumber = raw.toIntOrNull()
        if (pureNumber != null && pureNumber in 1..100) {
            return QrScanResult(
                rawText = raw,
                type = ScannedCodeType.TABLE_SELECTION,
                extractedTableNumber = pureNumber
            )
        }

        return QrScanResult(
            rawText = raw,
            type = if (raw.startsWith("http://") || raw.startsWith("https://")) ScannedCodeType.MENU_LINK else ScannedCodeType.RAW_TEXT
        )
    }

    fun getTableBillSummary(tableNum: Int): TableBillSummary {
        val ordersForTable = _uiState.value.tableOrdersMap[tableNum].orEmpty()
        val allItems = ordersForTable.flatMap { it.items }
        val subtotal = allItems.sumOf { it.totalPrice }
        val gst = (subtotal * 0.05).toInt()
        val tips = ordersForTable.sumOf { it.tip }
        val grandTotal = subtotal + gst + tips
        val isSettled = ordersForTable.isNotEmpty() && ordersForTable.all { it.paymentStatus == PaymentStatus.PAID }
        val isCashRequested = _uiState.value.isCashRequestedForTable.contains(tableNum) || ordersForTable.any { it.paymentStatus == PaymentStatus.CASH_REQUESTED }
        val isFraudUnpaid = ordersForTable.any { it.paymentStatus == PaymentStatus.FRAUD_UNPAID }

        val firstOrder = ordersForTable.firstOrNull()

        return TableBillSummary(
            tableNumber = tableNum,
            orders = ordersForTable,
            customerName = firstOrder?.customerName ?: "Guest Table $tableNum",
            customerPhone = firstOrder?.customerPhone ?: _uiState.value.currentCustomerPhone,
            customerId = firstOrder?.customerId ?: _uiState.value.currentCustomerId,
            totalItemsCount = allItems.sumOf { it.quantity },
            totalSubtotal = subtotal,
            totalGst = gst,
            totalTips = tips,
            grandTotal = grandTotal,
            isSettled = isSettled,
            isCashRequested = isCashRequested,
            isFraudUnpaid = isFraudUnpaid
        )
    }

    fun payBillOnlineUpi(tableNum: Int) {
        payBill(tableNum, PaymentMethod.UPI)
    }

    fun requestCashPayment(tableNum: Int) {
        val summary = getTableBillSummary(tableNum)
        val alertId = "CASH-${(1000..9999).random()}"
        val alert = CashPaymentAlert(
            alertId = alertId,
            tableNumber = tableNum,
            totalAmount = if (summary.grandTotal > 0) summary.grandTotal else 350,
            customerName = summary.customerName,
            customerPhone = summary.customerPhone,
            customerId = summary.customerId,
            timestamp = System.currentTimeMillis()
        )

        val currentMap = _uiState.value.tableOrdersMap.toMutableMap()
        val orders = currentMap[tableNum]?.toMutableList() ?: mutableListOf()
        val updatedOrders = orders.map { order ->
            order.copy(paymentStatus = PaymentStatus.CASH_REQUESTED)
        }
        currentMap[tableNum] = updatedOrders

        _uiState.value = _uiState.value.copy(
            activeCashAlerts = listOf(alert) + _uiState.value.activeCashAlerts.filter { it.tableNumber != tableNum },
            isCashRequestedForTable = _uiState.value.isCashRequestedForTable + tableNum,
            tableOrdersMap = currentMap,
            activeOrder = if (_uiState.value.activeOrder?.tableNumber == tableNum) {
                _uiState.value.activeOrder?.copy(paymentStatus = PaymentStatus.CASH_REQUESTED)
            } else _uiState.value.activeOrder
        )

        // Sync cash alert directly to Firebase Firestore
        viewModelScope.launch {
            val shopId = _uiState.value.shopInfo.shopId.ifBlank { "shop_101" }
            firebaseService.sendCashAlertOnline(shopId, alert)
        }
    }

    fun markCashReceived(alertId: String) {
        val alert = _uiState.value.activeCashAlerts.find { it.alertId == alertId }
        val targetTable = alert?.tableNumber ?: _uiState.value.selectedTableNumber

        val updatedAlerts = _uiState.value.activeCashAlerts.filterNot { it.alertId == alertId }
        val updatedCashRequested = _uiState.value.isCashRequestedForTable - targetTable

        _uiState.value = _uiState.value.copy(
            activeCashAlerts = updatedAlerts,
            isCashRequestedForTable = updatedCashRequested
        )

        viewModelScope.launch {
            val shopId = _uiState.value.shopInfo.shopId.ifBlank { "shop_101" }
            firebaseService.resolveCashAlertOnline(shopId, targetTable)
        }

        payBill(targetTable, PaymentMethod.CASH)
    }

    fun markOrderUnpaidAndBan(
        tableNum: Int,
        orderId: String? = null,
        reason: String = "Left without paying bill (बिल दिए बिना चले गए)"
    ) {
        val currentMap = _uiState.value.tableOrdersMap.toMutableMap()
        val orders = currentMap[tableNum]?.toMutableList() ?: mutableListOf()
        val targetOrder = orders.find { it.orderId == orderId } ?: orders.lastOrNull()

        val custId = targetOrder?.customerId ?: "CUST-${tableNum}0${(10..99).random()}"
        val custPhone = targetOrder?.customerPhone ?: "9876543210"
        val custName = targetOrder?.customerName ?: "Customer Table $tableNum"
        val unpaidAmt = targetOrder?.grandTotal ?: 450
        val targetOrderId = targetOrder?.orderId ?: "RD-${(1000..9999).random()}"

        val blockedRecord = BlockedCustomer(
            customerId = custId,
            customerName = custName,
            phone = custPhone,
            tableNumber = tableNum,
            unpaidAmount = unpaidAmt,
            orderId = targetOrderId,
            timestamp = System.currentTimeMillis(),
            reason = reason
        )

        val updatedOrders = orders.map { order ->
            if (orderId == null || order.orderId == orderId) {
                order.copy(paymentStatus = PaymentStatus.FRAUD_UNPAID)
            } else order
        }
        currentMap[tableNum] = updatedOrders

        val updatedAlerts = _uiState.value.activeCashAlerts.filterNot { it.tableNumber == tableNum }
        val updatedCashRequested = _uiState.value.isCashRequestedForTable - tableNum

        val isMatchesCurrentCustomer = (_uiState.value.currentCustomerId == custId || _uiState.value.currentCustomerPhone == custPhone)

        _uiState.value = _uiState.value.copy(
            tableOrdersMap = currentMap,
            activeCashAlerts = updatedAlerts,
            isCashRequestedForTable = updatedCashRequested,
            blockedCustomers = listOf(blockedRecord) + _uiState.value.blockedCustomers.filterNot { it.customerId == custId },
            isCurrentCustomerBlocked = if (isMatchesCurrentCustomer) true else _uiState.value.isCurrentCustomerBlocked,
            blockedCustomerNotice = if (isMatchesCurrentCustomer) blockedRecord else _uiState.value.blockedCustomerNotice,
            isPayBillDialogOpen = false
        )

        // Sync fraud blocklist and order status directly to Firebase Firestore
        viewModelScope.launch {
            val shopId = _uiState.value.shopInfo.shopId.ifBlank { "shop_101" }
            firebaseService.syncBlockedUserOnline(shopId, blockedRecord)
            if (orderId != null) {
                firebaseService.updatePaymentStatusOnline(shopId, orderId, PaymentStatus.FRAUD_UNPAID)
            }
            firebaseService.resolveCashAlertOnline(shopId, tableNum)
        }
    }

    fun unblockCustomer(customerId: String) {
        val updatedList = _uiState.value.blockedCustomers.filterNot { it.customerId == customerId }
        val wasCurrentCustomer = _uiState.value.currentCustomerId == customerId || _uiState.value.blockedCustomerNotice?.customerId == customerId

        _uiState.value = _uiState.value.copy(
            blockedCustomers = updatedList,
            isCurrentCustomerBlocked = if (wasCurrentCustomer) false else _uiState.value.isCurrentCustomerBlocked,
            blockedCustomerNotice = if (wasCurrentCustomer) null else _uiState.value.blockedCustomerNotice
        )

        // Remove from cloud blocklist in Firestore
        viewModelScope.launch {
            val shopId = _uiState.value.shopInfo.shopId.ifBlank { "shop_101" }
            firebaseService.unblockUserOnline(shopId, customerId)
        }
    }

    fun dismissBlockedNotice() {
        _uiState.value = _uiState.value.copy(blockedCustomerNotice = null)
    }

    fun payBill(
        tableNum: Int,
        paymentMethod: PaymentMethod,
        transactionNote: String = ""
    ) {
        val currentMap = _uiState.value.tableOrdersMap.toMutableMap()
        val orders = currentMap[tableNum]?.toMutableList() ?: mutableListOf()
        val now = System.currentTimeMillis()
        val txnRef = if (paymentMethod == PaymentMethod.UPI) "UPI-TXN-${(100000..999999).random()}" else "CASH-REC-${(100000..999999).random()}"

        val updatedOrders = orders.map { order ->
            order.copy(
                paymentStatus = PaymentStatus.PAID,
                paymentMethod = paymentMethod,
                transactionRef = txnRef,
                paidTimestamp = now
            )
        }
        currentMap[tableNum] = updatedOrders

        val paidSummaryOrder = updatedOrders.lastOrNull() ?: TableOrder(
            orderId = "RD-${(1000..9999).random()}",
            tableNumber = tableNum,
            customerName = _uiState.value.customerName,
            customerPhone = _uiState.value.currentCustomerPhone,
            customerId = _uiState.value.currentCustomerId,
            items = emptyList(),
            specialInstructions = "",
            subtotal = 0,
            gst = 0,
            tip = 0,
            grandTotal = 0,
            paymentStatus = PaymentStatus.PAID,
            paymentMethod = paymentMethod,
            transactionRef = txnRef,
            paidTimestamp = now
        )

        val updatedHistory = _uiState.value.orderHistory + updatedOrders
        val updatedAlerts = _uiState.value.activeCashAlerts.filterNot { it.tableNumber == tableNum }
        val updatedCashRequested = _uiState.value.isCashRequestedForTable - tableNum

        _uiState.value = _uiState.value.copy(
            tableOrdersMap = currentMap,
            orderHistory = updatedHistory,
            activeCashAlerts = updatedAlerts,
            isCashRequestedForTable = updatedCashRequested,
            activeOrder = if (_uiState.value.activeOrder?.tableNumber == tableNum) {
                _uiState.value.activeOrder?.copy(
                    paymentStatus = PaymentStatus.PAID,
                    paymentMethod = paymentMethod,
                    transactionRef = txnRef,
                    paidTimestamp = now
                )
            } else _uiState.value.activeOrder,
            isPayBillDialogOpen = false,
            paymentSuccessOrder = paidSummaryOrder
        )

        // Sync payment online to Firestore
        viewModelScope.launch {
            val shopId = _uiState.value.shopInfo.shopId.ifBlank { "shop_101" }
            updatedOrders.forEach { ord ->
                firebaseService.updatePaymentStatusOnline(shopId, ord.orderId, PaymentStatus.PAID, paymentMethod, ord.tip)
            }
            firebaseService.resolveCashAlertOnline(shopId, tableNum)
        }
    }

    fun placeOrder(tipAmount: Int = 0) {
        val cartList = _uiState.value.cartItems.values.toList()
        if (cartList.isEmpty()) return

        val tableNum = _uiState.value.selectedTableNumber
        val subtotal = cartList.sumOf { it.totalPrice }
        val gst = (subtotal * 0.05).toInt()
        val grandTotal = subtotal + gst + tipAmount
        val orderId = "RD-${(1000..9999).random()}"
        val now = System.currentTimeMillis()
        val estimatedPrepMinutes = (cartList.maxOfOrNull { it.item.prepTimeMin } ?: 15).coerceIn(1, 120)
        val readyTimestamp = now + (estimatedPrepMinutes * 60 * 1000L)

        val order = TableOrder(
            orderId = orderId,
            tableNumber = tableNum,
            customerName = _uiState.value.customerName.ifBlank { "Guest Table $tableNum" },
            customerPhone = _uiState.value.currentCustomerPhone,
            customerId = _uiState.value.currentCustomerId,
            items = cartList,
            specialInstructions = _uiState.value.orderNotes,
            subtotal = subtotal,
            gst = gst,
            tip = tipAmount,
            grandTotal = grandTotal,
            orderTimestamp = now,
            estimatedPrepTimeMinutes = estimatedPrepMinutes,
            estimatedReadyTimestamp = readyTimestamp,
            status = OrderStatus.RECEIVED,
            paymentStatus = PaymentStatus.UNPAID
        )

        val currentMap = _uiState.value.tableOrdersMap.toMutableMap()
        val list = (currentMap[tableNum] ?: emptyList()) + order
        currentMap[tableNum] = list

        _uiState.value = _uiState.value.copy(
            activeOrder = order,
            tableOrdersMap = currentMap,
            cartItems = emptyMap(),
            orderNotes = "",
            isCartOpen = false,
            isOrderStatusOpen = true
        )

        // Real-time online sync order to Supabase and Firestore
        viewModelScope.launch {
            val hotelId = _uiState.value.shopInfo.hotelId.ifBlank { _uiState.value.shopInfo.shopId.ifBlank { "hotel1" } }
            supabaseService.submitOrder(order, hotelId)
            firebaseService.syncOrderOnline(hotelId, order)
        }

        // Step-by-step progress simulation in kitchen
        simulateKitchenOrderProgress(order)
    }

    fun markOrderAsServed(orderId: String, tableNum: Int) {
        val now = System.currentTimeMillis()
        val currentMap = _uiState.value.tableOrdersMap.toMutableMap()
        val orders = currentMap[tableNum]?.toMutableList() ?: mutableListOf()

        val updatedOrders = orders.map { order ->
            if (order.orderId == orderId) {
                order.copy(
                    status = OrderStatus.SERVED,
                    servedTimestamp = now
                )
            } else order
        }
        currentMap[tableNum] = updatedOrders

        _uiState.value = _uiState.value.copy(
            tableOrdersMap = currentMap,
            activeOrder = if (_uiState.value.activeOrder?.orderId == orderId) {
                _uiState.value.activeOrder?.copy(
                    status = OrderStatus.SERVED,
                    servedTimestamp = now
                )
            } else _uiState.value.activeOrder
        )

        // Real-time online sync status update to Supabase & Firestore
        viewModelScope.launch {
            val hotelId = _uiState.value.shopInfo.hotelId.ifBlank { _uiState.value.shopInfo.shopId.ifBlank { "hotel1" } }
            supabaseService.updateOrderStatus(orderId, OrderStatus.SERVED, tableNum, hotelId)
            firebaseService.updateOrderStatusOnline(hotelId, orderId, OrderStatus.SERVED, now)
        }
    }

    fun updateOrderStatus(orderId: String, tableNum: Int, newStatus: OrderStatus) {
        val now = System.currentTimeMillis()
        val currentMap = _uiState.value.tableOrdersMap.toMutableMap()
        val orders = currentMap[tableNum]?.toMutableList() ?: mutableListOf()

        val updatedOrders = orders.map { order ->
            if (order.orderId == orderId) {
                order.copy(
                    status = newStatus,
                    servedTimestamp = if (newStatus == OrderStatus.SERVED) now else order.servedTimestamp
                )
            } else order
        }
        currentMap[tableNum] = updatedOrders

        _uiState.value = _uiState.value.copy(
            tableOrdersMap = currentMap,
            activeOrder = if (_uiState.value.activeOrder?.orderId == orderId) {
                _uiState.value.activeOrder?.copy(
                    status = newStatus,
                    servedTimestamp = if (newStatus == OrderStatus.SERVED) now else _uiState.value.activeOrder?.servedTimestamp
                )
            } else _uiState.value.activeOrder
        )

        // Real-time online sync status update to Supabase & Firestore
        viewModelScope.launch {
            val hotelId = _uiState.value.shopInfo.hotelId.ifBlank { _uiState.value.shopInfo.shopId.ifBlank { "hotel1" } }
            supabaseService.updateOrderStatus(orderId, newStatus, tableNum, hotelId)
            firebaseService.updateOrderStatusOnline(hotelId, orderId, newStatus, if (newStatus == OrderStatus.SERVED) now else null)
        }
    }

    fun dismissNewOrderNotification() {
        _uiState.value = _uiState.value.copy(newRealtimeOrderNotification = null)
    }

    fun occupyTable(tableNumber: Int, customerName: String = "", orderTotal: Int = 0) {
        viewModelScope.launch {
            val hotelId = _uiState.value.shopInfo.hotelId.ifBlank { _uiState.value.shopInfo.shopId.ifBlank { "hotel1" } }
            supabaseService.setTableOccupied(
                tableNumber = tableNumber,
                isOccupied = true,
                customerName = customerName.ifBlank { "Guest T-$tableNumber" },
                totalAmount = orderTotal,
                hotelId = hotelId
            )
            val updatedTables = _uiState.value.restaurantTables.map {
                if (it.tableNumber == tableNumber) {
                    it.copy(
                        isOccupied = true,
                        activeCustomerName = customerName.ifBlank { "Guest T-$tableNumber" },
                        activeOrderTotal = orderTotal,
                        lastOccupiedTime = System.currentTimeMillis()
                    )
                } else it
            }
            _uiState.value = _uiState.value.copy(restaurantTables = updatedTables)
        }
    }

    fun freeTable(tableNumber: Int) {
        viewModelScope.launch {
            val hotelId = _uiState.value.shopInfo.hotelId.ifBlank { _uiState.value.shopInfo.shopId.ifBlank { "hotel1" } }
            supabaseService.setTableOccupied(
                tableNumber = tableNumber,
                isOccupied = false,
                orderId = null,
                customerName = null,
                totalAmount = 0,
                hotelId = hotelId
            )
            val updatedTables = _uiState.value.restaurantTables.map {
                if (it.tableNumber == tableNumber) {
                    it.copy(
                        isOccupied = false,
                        activeOrderId = null,
                        activeCustomerName = null,
                        activeOrderTotal = 0,
                        isCashPaymentRequested = false
                    )
                } else it
            }
            _uiState.value = _uiState.value.copy(restaurantTables = updatedTables)
        }
    }

    fun toggleTableOccupied(tableNumber: Int) {
        val currentTable = _uiState.value.restaurantTables.find { it.tableNumber == tableNumber }
        if (currentTable?.isOccupied == true) {
            freeTable(tableNumber)
        } else {
            occupyTable(tableNumber)
        }
    }

    fun updateSupabaseConfig(context: Context, projectUrl: String, anonKey: String) {
        SupabaseConfig.saveConfig(context, projectUrl, anonKey)
        _uiState.value = _uiState.value.copy(
            supabaseProjectUrl = projectUrl
        )
        val hotelId = _uiState.value.shopInfo.hotelId.ifBlank { _uiState.value.shopInfo.shopId.ifBlank { "hotel1" } }
        loadSupabaseData(hotelId)
        startSupabaseRealtime(hotelId)
    }

    fun switchDatabaseEngine(engine: DatabaseEngine) {
        _uiState.value = _uiState.value.copy(databaseEngine = engine)
    }

    fun dismissDishReadyAlert() {
        _uiState.value = _uiState.value.copy(dishReadyAlert = null)
    }

    fun triggerDishReadyAlert(alert: com.example.model.DishReadyAlert) {
        _uiState.value = _uiState.value.copy(dishReadyAlert = alert)
    }

    private fun simulateKitchenOrderProgress(initialOrder: TableOrder) {
        viewModelScope.launch {
            // Stage 1: Move to PREPARING after 4 seconds
            delay(4000)
            updateOrderStatus(initialOrder.orderId, initialOrder.tableNumber, OrderStatus.PREPARING)

            // Stage 2: When the first dish completes prep (e.g. Samosa or Cold Coffee), trigger Dish Ready Alert!
            val fastestDish = initialOrder.items.minByOrNull { it.item.prepTimeMin }
            val quickDishDelay = if ((fastestDish?.item?.prepTimeMin ?: 10) <= 5) 6000L else 9000L
            delay(quickDishDelay)

            if (fastestDish != null) {
                _uiState.value = _uiState.value.copy(
                    dishReadyAlert = com.example.model.DishReadyAlert(
                        orderId = initialOrder.orderId,
                        tableNumber = initialOrder.tableNumber,
                        dishName = fastestDish.item.nameHi,
                        dishNameEn = fastestDish.item.nameEn,
                        emoji = fastestDish.item.emoji
                    )
                )
            }

            // Stage 3: Move entire order to READY_TO_SERVE after full prep
            delay(7000)
            updateOrderStatus(initialOrder.orderId, initialOrder.tableNumber, OrderStatus.READY_TO_SERVE)
        }
    }

    fun addNewDish(
        nameHi: String,
        nameEn: String,
        category: MenuCategory,
        price: Int,
        descHi: String,
        descEn: String,
        isVeg: Boolean,
        spicyLevel: SpicyLevel,
        emoji: String,
        prepTimeMin: Int = 15
    ) {
        val cleanHotelId = _uiState.value.shopInfo.hotelId.ifBlank { _uiState.value.shopInfo.shopId.ifBlank { "hotel1" } }
        val newId = (_uiState.value.allItems.maxOfOrNull { it.id } ?: 0) + 1
        val newItem = MenuItem(
            id = newId,
            nameHi = nameHi,
            nameEn = nameEn,
            category = category,
            price = price,
            prepTimeMin = prepTimeMin,
            descHi = descHi,
            descEn = descEn,
            isVeg = isVeg,
            spicyLevel = spicyLevel,
            emoji = emoji.ifBlank { "🍛" },
            isBestseller = false,
            tags = listOf("New Special")
        )
        _uiState.value = _uiState.value.copy(
            allItems = listOf(newItem) + _uiState.value.allItems,
            isAddDishOpen = false
        )

        // Save prep time directly to Supabase and Firebase
        viewModelScope.launch {
            supabaseService.saveMenuItem(newItem, cleanHotelId)
            firebaseService.syncMenuItemToCloud(cleanHotelId, newItem)
        }
    }
}
