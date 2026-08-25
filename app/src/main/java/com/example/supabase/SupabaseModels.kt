package com.example.supabase

import com.example.model.CartItem
import com.example.model.MenuCategory
import com.example.model.MenuItem
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.RestaurantTable
import com.example.model.ShopOwnerUser
import com.example.model.SpicyLevel
import com.example.model.TableOrder
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
data class SupabaseMenuItemDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name_hi") val nameHi: String,
    @Json(name = "name_en") val nameEn: String,
    @Json(name = "category") val category: String,
    @Json(name = "price") val price: Int,
    @Json(name = "desc_hi") val descHi: String = "",
    @Json(name = "desc_en") val descEn: String = "",
    @Json(name = "is_veg") val isVeg: Boolean = true,
    @Json(name = "spicy_level") val spicyLevel: String = "MEDIUM",
    @Json(name = "is_bestseller") val isBestseller: Boolean = false,
    @Json(name = "is_available") val isAvailable: Boolean = true,
    @Json(name = "emoji") val emoji: String = "🍲",
    @Json(name = "media_url") val mediaUrl: String = "",
    @Json(name = "media_type") val mediaType: String = "image",
    @Json(name = "calories") val calories: Int = 300,
    @Json(name = "prep_time") val prepTime: Int = 15,
    @Json(name = "rating") val rating: Float = 4.8f,
    @Json(name = "reviews_count") val reviewsCount: Int = 100,
    @Json(name = "portion_size") val portionSize: String = "1 Plate",
    @Json(name = "tags") val tags: List<String> = emptyList(),
    @Json(name = "hotel_id") val hotelId: String = "hotel1",
    @Json(name = "shop_id") val shopId: String = "hotel1",
    @Json(name = "created_at") val createdAt: Long = System.currentTimeMillis()
) {
    fun toMenuItem(): MenuItem {
        return MenuItem(
            id = id,
            nameHi = nameHi,
            nameEn = nameEn,
            category = MenuCategory.fromString(category),
            price = price,
            descHi = descHi,
            descEn = descEn,
            isVeg = isVeg,
            spicyLevel = try { SpicyLevel.valueOf(spicyLevel) } catch (_: Exception) { SpicyLevel.MEDIUM },
            isBestseller = isBestseller,
            isAvailable = isAvailable,
            emoji = emoji,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            calories = calories,
            prepTimeMin = prepTime,
            rating = rating,
            reviewsCount = reviewsCount,
            portionSize = portionSize,
            tags = tags,
            hotelId = if (hotelId.isNotBlank() && hotelId != "hotel1") hotelId else shopId.ifBlank { "hotel1" }
        )
    }

    companion object {
        fun fromMenuItem(item: MenuItem, hotelId: String = "hotel1"): SupabaseMenuItemDto {
            val targetHotelId = hotelId.ifBlank { item.hotelId.ifBlank { "hotel1" } }
            return SupabaseMenuItemDto(
                id = item.id,
                nameHi = item.nameHi,
                nameEn = item.nameEn,
                category = item.category.name,
                price = item.price,
                descHi = item.descHi,
                descEn = item.descEn,
                isVeg = item.isVeg,
                spicyLevel = item.spicyLevel.name,
                isBestseller = item.isBestseller,
                isAvailable = item.isAvailable,
                emoji = item.emoji,
                mediaUrl = item.mediaUrl,
                mediaType = item.mediaType,
                calories = item.calories,
                prepTime = item.prepTimeMin,
                rating = item.rating,
                reviewsCount = item.reviewsCount,
                portionSize = item.portionSize,
                tags = item.tags,
                hotelId = targetHotelId,
                shopId = targetHotelId
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class SupabaseRestaurantTableDto(
    @Json(name = "table_number") val tableNumber: Int,
    @Json(name = "table_code") val tableCode: String = "T-$tableNumber",
    @Json(name = "capacity") val capacity: Int = 4,
    @Json(name = "is_occupied") val isOccupied: Boolean = false,
    @Json(name = "active_order_id") val activeOrderId: String? = null,
    @Json(name = "active_customer_name") val activeCustomerName: String? = null,
    @Json(name = "active_order_total") val activeOrderTotal: Int = 0,
    @Json(name = "last_occupied_time") val lastOccupiedTime: Long = 0L,
    @Json(name = "is_cash_payment_requested") val isCashPaymentRequested: Boolean = false,
    @Json(name = "hotel_id") val hotelId: String = "hotel1",
    @Json(name = "shop_id") val shopId: String = "hotel1"
) {
    fun toRestaurantTable(): RestaurantTable {
        val effectiveHotel = if (hotelId.isNotBlank() && hotelId != "hotel1") hotelId else shopId.ifBlank { "hotel1" }
        return RestaurantTable(
            tableNumber = tableNumber,
            tableCode = tableCode.ifBlank { "T-$tableNumber" },
            capacity = capacity,
            isOccupied = isOccupied,
            activeOrderId = activeOrderId,
            activeCustomerName = activeCustomerName,
            activeOrderTotal = activeOrderTotal,
            lastOccupiedTime = lastOccupiedTime,
            isCashPaymentRequested = isCashPaymentRequested,
            shopId = effectiveHotel,
            hotelId = effectiveHotel
        )
    }

    companion object {
        fun fromRestaurantTable(table: RestaurantTable, hotelId: String = "hotel1"): SupabaseRestaurantTableDto {
            val targetHotelId = hotelId.ifBlank { table.hotelId.ifBlank { "hotel1" } }
            return SupabaseRestaurantTableDto(
                tableNumber = table.tableNumber,
                tableCode = table.tableCode,
                capacity = table.capacity,
                isOccupied = table.isOccupied,
                activeOrderId = table.activeOrderId,
                activeCustomerName = table.activeCustomerName,
                activeOrderTotal = table.activeOrderTotal,
                lastOccupiedTime = table.lastOccupiedTime,
                isCashPaymentRequested = table.isCashPaymentRequested,
                hotelId = targetHotelId,
                shopId = targetHotelId
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class SupabaseCartItemDto(
    @Json(name = "item_id") val itemId: Int,
    @Json(name = "item_name") val itemName: String,
    @Json(name = "item_price") val itemPrice: Int,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "special_note") val specialNote: String = "",
    @Json(name = "is_veg") val isVeg: Boolean = true
)

@JsonClass(generateAdapter = true)
data class SupabaseOrderDto(
    @Json(name = "id") val id: String,
    @Json(name = "hotel_id") val hotelId: String = "hotel1",
    @Json(name = "shop_id") val shopId: String = "hotel1",
    @Json(name = "table_number") val tableNumber: Int,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "customer_phone") val customerPhone: String = "9876543210",
    @Json(name = "customer_id") val customerId: String = "CUST-4102",
    @Json(name = "items_json") val itemsJson: String = "[]",
    @Json(name = "special_instructions") val specialInstructions: String = "",
    @Json(name = "subtotal") val subtotal: Int = 0,
    @Json(name = "gst") val gst: Int = 0,
    @Json(name = "tip") val tip: Int = 0,
    @Json(name = "grand_total") val grandTotal: Int = 0,
    @Json(name = "status") val status: String = "RECEIVED",
    @Json(name = "payment_status") val paymentStatus: String = "UNPAID",
    @Json(name = "payment_mode") val paymentMode: String? = null,
    @Json(name = "order_time") val orderTime: Long = System.currentTimeMillis(),
    @Json(name = "estimated_prep_min") val estimatedPrepMin: Int = 20,
    @Json(name = "transaction_ref") val transactionRef: String? = null
) {
    fun toTableOrder(availableMenuItems: List<MenuItem>): TableOrder {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val listType = Types.newParameterizedType(List::class.java, SupabaseCartItemDto::class.java)
        val adapter = moshi.adapter<List<SupabaseCartItemDto>>(listType)
        
        val cartItemsList = try {
            val parsed = adapter.fromJson(itemsJson) ?: emptyList()
            parsed.map { dto ->
                val matchingMenu = availableMenuItems.firstOrNull { it.id == dto.itemId }
                    ?: MenuItem(
                        id = dto.itemId,
                        nameHi = dto.itemName,
                        nameEn = dto.itemName,
                        category = MenuCategory.MAIN,
                        price = dto.itemPrice,
                        descHi = "",
                        descEn = "",
                        isVeg = dto.isVeg
                    )
                CartItem(
                    item = matchingMenu,
                    quantity = dto.quantity,
                    specialNote = dto.specialNote
                )
            }
        } catch (_: Exception) {
            emptyList()
        }

        val effectiveHotel = if (hotelId.isNotBlank() && hotelId != "hotel1") hotelId else shopId.ifBlank { "hotel1" }
        return TableOrder(
            orderId = id,
            tableNumber = tableNumber,
            customerName = customerName,
            customerPhone = customerPhone,
            customerId = customerId,
            items = cartItemsList,
            specialInstructions = specialInstructions,
            subtotal = subtotal,
            gst = gst,
            tip = tip,
            grandTotal = grandTotal,
            orderTimestamp = orderTime,
            estimatedPrepTimeMinutes = estimatedPrepMin,
            estimatedReadyTimestamp = orderTime + (estimatedPrepMin * 60 * 1000L),
            status = try { OrderStatus.valueOf(status) } catch (_: Exception) { OrderStatus.RECEIVED },
            paymentStatus = try { PaymentStatus.valueOf(paymentStatus) } catch (_: Exception) { PaymentStatus.UNPAID },
            paymentMethod = paymentMode?.let {
                try { PaymentMethod.valueOf(it) } catch (_: Exception) { null }
            },
            transactionRef = transactionRef,
            hotelId = effectiveHotel
        )
    }

    companion object {
        fun fromTableOrder(order: TableOrder, hotelId: String = "hotel1"): SupabaseOrderDto {
            val targetHotelId = hotelId.ifBlank { order.hotelId.ifBlank { "hotel1" } }
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val listType = Types.newParameterizedType(List::class.java, SupabaseCartItemDto::class.java)
            val adapter = moshi.adapter<List<SupabaseCartItemDto>>(listType)

            val dtoList = order.items.map {
                SupabaseCartItemDto(
                    itemId = it.item.id,
                    itemName = it.item.nameHi.ifBlank { it.item.nameEn },
                    itemPrice = it.item.price,
                    quantity = it.quantity,
                    specialNote = it.specialNote,
                    isVeg = it.item.isVeg
                )
            }
            val jsonString = adapter.toJson(dtoList)

            return SupabaseOrderDto(
                id = order.orderId,
                hotelId = targetHotelId,
                shopId = targetHotelId,
                tableNumber = order.tableNumber,
                customerName = order.customerName,
                customerPhone = order.customerPhone,
                customerId = order.customerId,
                itemsJson = jsonString,
                specialInstructions = order.specialInstructions,
                subtotal = order.subtotal,
                gst = order.gst,
                tip = order.tip,
                grandTotal = order.grandTotal,
                status = order.status.name,
                paymentStatus = order.paymentStatus.name,
                paymentMode = order.paymentMethod?.name,
                orderTime = order.orderTimestamp,
                estimatedPrepMin = order.estimatedPrepTimeMinutes,
                transactionRef = order.transactionRef
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class SupabaseUserDto(
    @Json(name = "id") val id: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "password_hash") val passwordHash: String,
    @Json(name = "salt") val salt: String = "",
    @Json(name = "hotel_id") val hotelId: String = "hotel1",
    @Json(name = "shop_id") val shopId: String = "hotel1",
    @Json(name = "shop_name") val shopName: String = "CHETER Restaurant & Lounge",
    @Json(name = "display_name") val displayName: String = "Shop Owner",
    @Json(name = "email") val email: String = "",
    @Json(name = "role") val role: String = "OWNER",
    @Json(name = "session_token") val sessionToken: String = "",
    @Json(name = "last_login") val lastLogin: Long = System.currentTimeMillis()
) {
    fun toShopOwnerUser(): ShopOwnerUser {
        val effectiveHotel = if (hotelId.isNotBlank() && hotelId != "hotel1") hotelId else shopId.ifBlank { "hotel1" }
        return ShopOwnerUser(
            uid = id,
            phone = phone,
            email = email,
            displayName = displayName,
            hotelId = effectiveHotel,
            shopId = effectiveHotel,
            shopName = shopName,
            isAuthenticated = true,
            authProvider = "Supabase Auth (Phone + Password)",
            sessionToken = sessionToken,
            loginTime = lastLogin
        )
    }
}

@JsonClass(generateAdapter = true)
data class SupabaseRestaurantDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String = "",
    @Json(name = "upi_id") val upiId: String = "",
    @Json(name = "custom_upi_qr_url") val customUpiQrUrl: String = "",
    @Json(name = "phone") val phone: String = "",
    @Json(name = "address") val address: String = "",
    @Json(name = "total_tables") val totalTables: Int = 100,
    @Json(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)

