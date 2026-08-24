package com.example.supabase

import android.content.Context
import android.util.Log
import com.example.data.SampleMenuData
import com.example.model.CartItem
import com.example.model.MenuItem
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.RestaurantTable
import com.example.model.ShopOwnerUser
import com.example.model.TableOrder
import com.example.security.AuthSecurityManager
import com.example.security.AuthSecurityManager.toShopOwnerUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class SupabaseMenuService(private val context: Context?) {

    private val tag = "SupabaseMenuService"
    val client = SupabaseClient(context)
    private var realtimeManager: SupabaseRealtimeManager? = null

    // In-memory cache for ultra-fast instant UI responsiveness
    private val localTablesCache = (1..100).associateWith { num ->
        RestaurantTable(
            tableNumber = num,
            tableCode = "T-$num",
            capacity = if (num % 5 == 0) 8 else if (num % 2 == 0) 6 else 4,
            isOccupied = (num == 4 || num == 12),
            activeOrderId = if (num == 4) "ORD-8941" else if (num == 12) "ORD-8943" else null,
            activeCustomerName = if (num == 4) "Rahul Sharma" else if (num == 12) "Amit Patel" else null,
            activeOrderTotal = if (num == 4) 860 else if (num == 12) 540 else 0,
            lastOccupiedTime = if (num == 4 || num == 12) System.currentTimeMillis() - 15 * 60 * 1000L else 0L
        )
    }.toMutableMap()

    fun initRealtime(scope: CoroutineScope, hotelId: String, menuItemsProvider: () -> List<MenuItem>): SharedFlow<SupabaseRealtimeEvent>? {
        if (realtimeManager == null) {
            realtimeManager = SupabaseRealtimeManager(client, scope)
        }
        val cleanHotelId = hotelId.ifBlank { "hotel1" }
        realtimeManager?.startListening(cleanHotelId, menuItemsProvider)
        return realtimeManager?.realtimeEvents
    }

    fun stopRealtime() {
        realtimeManager?.stopListening()
    }

    // ================= MENU ITEMS =================

    suspend fun getMenuItems(hotelId: String = "hotel1"): Result<List<MenuItem>> = withContext(Dispatchers.IO) {
        val cleanHotelId = hotelId.ifBlank { "hotel1" }
        val res = client.fetchMenuItems(cleanHotelId)
        if (res.isSuccess && res.getOrNull()?.isNotEmpty() == true) {
            val list = res.getOrNull()!!.map { it.toMenuItem() }
            Result.success(list)
        } else {
            // Seed sample menu if database table was empty or not yet seeded
            Log.d(tag, "Seeding initial menu items to Supabase for hotel: $cleanHotelId...")
            val sampleItems = SampleMenuData.initialMenuItems
            sampleItems.forEach { item ->
                client.upsertMenuItem(SupabaseMenuItemDto.fromMenuItem(item, cleanHotelId))
            }
            Result.success(sampleItems)
        }
    }

    suspend fun saveMenuItem(item: MenuItem, hotelId: String = "hotel1"): Result<Boolean> = withContext(Dispatchers.IO) {
        val cleanHotelId = hotelId.ifBlank { item.hotelId.ifBlank { "hotel1" } }
        client.upsertMenuItem(SupabaseMenuItemDto.fromMenuItem(item, cleanHotelId))
    }

    suspend fun deleteMenuItem(itemId: Int, hotelId: String = "hotel1"): Result<Boolean> = withContext(Dispatchers.IO) {
        val cleanHotelId = hotelId.ifBlank { "hotel1" }
        client.deleteMenuItem(itemId, cleanHotelId)
    }

    // ================= RESTAURANT TABLES (T-1 to T-100) =================

    suspend fun getTables(hotelId: String = "hotel1"): List<RestaurantTable> = withContext(Dispatchers.IO) {
        val cleanHotelId = hotelId.ifBlank { "hotel1" }
        val res = client.fetchTables(cleanHotelId)
        if (res.isSuccess && res.getOrNull()?.isNotEmpty() == true) {
            val fetched = res.getOrNull()!!.map { it.toRestaurantTable() }
            fetched.forEach { localTablesCache[it.tableNumber] = it }
            fetched
        } else {
            // Return cached 100 tables and batch seed to Supabase
            client.batchSeedTables(100, cleanHotelId)
            localTablesCache.values.toList().sortedBy { it.tableNumber }
        }
    }

    suspend fun setTableOccupied(
        tableNumber: Int,
        isOccupied: Boolean,
        orderId: String? = null,
        customerName: String? = null,
        totalAmount: Int = 0,
        isCashRequested: Boolean = false,
        hotelId: String = "hotel1"
    ): Boolean = withContext(Dispatchers.IO) {
        val cleanHotelId = hotelId.ifBlank { "hotel1" }
        val updated = localTablesCache[tableNumber]?.copy(
            isOccupied = isOccupied,
            activeOrderId = orderId,
            activeCustomerName = customerName,
            activeOrderTotal = totalAmount,
            isCashPaymentRequested = isCashRequested,
            lastOccupiedTime = if (isOccupied) System.currentTimeMillis() else 0L,
            hotelId = cleanHotelId,
            shopId = cleanHotelId
        ) ?: RestaurantTable(
            tableNumber = tableNumber,
            tableCode = "T-$tableNumber",
            isOccupied = isOccupied,
            activeOrderId = orderId,
            activeCustomerName = customerName,
            activeOrderTotal = totalAmount,
            isCashPaymentRequested = isCashRequested,
            lastOccupiedTime = if (isOccupied) System.currentTimeMillis() else 0L,
            hotelId = cleanHotelId,
            shopId = cleanHotelId
        )
        localTablesCache[tableNumber] = updated

        val res = client.updateTableOccupancy(
            tableNumber = tableNumber,
            isOccupied = isOccupied,
            activeOrderId = orderId,
            activeCustomerName = customerName,
            orderTotal = totalAmount,
            isCashRequested = isCashRequested,
            hotelId = cleanHotelId
        )
        res.isSuccess
    }

    // ================= ORDERS =================

    suspend fun submitOrder(
        order: TableOrder,
        hotelId: String = "hotel1"
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val cleanHotelId = hotelId.ifBlank { order.hotelId.ifBlank { "hotel1" } }
        val dto = SupabaseOrderDto.fromTableOrder(order, cleanHotelId)
        val res = client.insertOrder(dto)

        // Mark table as occupied in Supabase
        setTableOccupied(
            tableNumber = order.tableNumber,
            isOccupied = true,
            orderId = order.orderId,
            customerName = order.customerName,
            totalAmount = order.grandTotal,
            isCashRequested = (order.paymentStatus == PaymentStatus.CASH_REQUESTED),
            hotelId = cleanHotelId
        )

        realtimeManager?.notifyLocalNewOrder(order)
        res
    }

    suspend fun updateOrderStatus(
        orderId: String,
        newStatus: OrderStatus,
        tableNumber: Int,
        hotelId: String = "hotel1"
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val res = client.updateOrderStatus(orderId, newStatus.name)
        if (newStatus == OrderStatus.SERVED) {
            // keep table occupied until paid & cleared
        }
        res
    }

    suspend fun updateOrderPayment(
        orderId: String,
        paymentStatus: PaymentStatus,
        paymentMethod: PaymentMethod? = null,
        transactionRef: String? = null,
        tableNumber: Int,
        hotelId: String = "hotel1"
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val cleanHotelId = hotelId.ifBlank { "hotel1" }
        val res = client.updateOrderPayment(
            orderId = orderId,
            paymentStatus = paymentStatus.name,
            paymentMode = paymentMethod?.name,
            transactionRef = transactionRef
        )
        if (paymentStatus == PaymentStatus.PAID) {
            // Free up the table upon bill settlement
            setTableOccupied(
                tableNumber = tableNumber,
                isOccupied = false,
                orderId = null,
                customerName = null,
                totalAmount = 0,
                isCashRequested = false,
                hotelId = cleanHotelId
            )
        }
        res
    }

    // ================= SUPABASE AUTHENTICATION =================

    suspend fun signInWithPhone(
        context: Context,
        rawPhone: String,
        rawPassword: String
    ): Result<ShopOwnerUser> = withContext(Dispatchers.IO) {
        val verification = AuthSecurityManager.verifyCredentials(context, rawPhone, rawPassword)
        if (verification.isFailure) {
            return@withContext Result.failure(verification.exceptionOrNull() ?: Exception("Incorrect Credentials"))
        }

        val account = verification.getOrThrow()
        val sessionToken = UUID.randomUUID().toString()

        // Sync with Supabase users table
        val userDto = SupabaseUserDto(
            id = "owner_${account.phone}",
            phone = account.phone,
            passwordHash = account.passwordHash,
            salt = "CHETER_SALT",
            shopId = account.shopId,
            shopName = account.shopName,
            displayName = account.displayName,
            email = "${account.phone}@cheter.app",
            role = "OWNER",
            sessionToken = sessionToken,
            lastLogin = System.currentTimeMillis()
        )
        client.registerUser(userDto)
        client.updateUserSession(account.phone, sessionToken)

        val authenticatedUser = account.toShopOwnerUser(sessionToken)
        AuthSecurityManager.saveSession(context, authenticatedUser)

        Result.success(authenticatedUser)
    }

    suspend fun signUpWithPhone(
        context: Context,
        rawPhone: String,
        rawPassword: String,
        shopName: String,
        ownerName: String = ""
    ): Result<ShopOwnerUser> = withContext(Dispatchers.IO) {
        val regResult = AuthSecurityManager.registerAccount(
            context = context,
            rawPhone = rawPhone,
            rawPassword = rawPassword,
            shopName = shopName,
            displayName = ownerName
        )
        if (regResult.isFailure) {
            return@withContext Result.failure(regResult.exceptionOrNull() ?: Exception("Registration Failed"))
        }

        val account = regResult.getOrThrow()
        val sessionToken = UUID.randomUUID().toString()

        val userDto = SupabaseUserDto(
            id = "owner_${account.phone}",
            phone = account.phone,
            passwordHash = account.passwordHash,
            salt = "CHETER_SALT",
            shopId = account.shopId,
            shopName = account.shopName,
            displayName = account.displayName,
            email = "${account.phone}@cheter.app",
            role = "OWNER",
            sessionToken = sessionToken,
            lastLogin = System.currentTimeMillis()
        )
        client.registerUser(userDto)

        val authenticatedUser = account.toShopOwnerUser(sessionToken)
        AuthSecurityManager.saveSession(context, authenticatedUser)

        Result.success(authenticatedUser)
    }
}
