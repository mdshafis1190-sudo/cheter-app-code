package com.example.model

enum class MenuCategory(
    val titleHi: String,
    val titleEn: String,
    val emoji: String
) {
    ALL("सब (All)", "All Dishes", "🍽️"),
    STARTERS("स्टार्टर्स", "Starters", "🥟"),
    MAIN("मेन कोर्स", "Main Course", "🍛"),
    BREADS("तंदूर & रोटी", "Breads & Roti", "🫓"),
    DRINKS("ड्रिंक्स / पेय", "Beverages", "🥤"),
    DESSERTS("डेसर्ट / मीठा", "Desserts", "🍨"),
    CHEF_SPECIAL("शेफ स्पेशल", "Chef Specials", "⭐");

    companion object {
        fun fromString(value: String): MenuCategory {
            return entries.firstOrNull { 
                it.name.equals(value, ignoreCase = true) || 
                it.titleEn.equals(value, ignoreCase = true) ||
                it.titleHi.equals(value, ignoreCase = true)
            } ?: MAIN
        }
    }
}

enum class SpicyLevel(val labelHi: String, val labelEn: String, val chilies: String) {
    MILD("हल्का तीखा", "Mild", "🌶️"),
    MEDIUM("मध्यम", "Medium", "🌶️🌶️"),
    HOT("तीखा", "Spicy", "🌶️🌶️🌶️"),
    EXTRA_HOT("बहुत तीखा", "Extra Spicy", "🌶️🌶️🌶️🌶️")
}

enum class LanguageMode {
    BOTH,
    HINDI,
    ENGLISH
}

enum class AppMode {
    CUSTOMER_MENU,
    SELLER_DASHBOARD
}

data class ShopInfo(
    val hotelId: String = "hotel1",
    val shopId: String = "hotel1",
    val shopName: String = "CHETER Restaurant & Lounge",
    val tagline: String = "QR कोड स्कैन करें और ताज़ा खाना ऑर्डर करें",
    val ownerEmail: String = "owner@cheter.com",
    val upiId: String = "cheter.dine@okhdfcbank",
    val customUpiQrUrl: String = "",
    val totalTables: Int = 100,
    val phone: String = "+91 98765 43210",
    val address: String = "CHETER Premium Lounge & Dine, NH-44",
    val qrCodeUrl: String = "https://api.qrserver.com/v1/create-qr-code/?data=https://cheter.app?hotel=hotel1",
    val menuBaseUrl: String = "https://cheter.app?hotel=hotel1",
    val isOnline: Boolean = true
)

data class ShopOwnerUser(
    val uid: String = "",
    val phone: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val hotelId: String = "hotel1",
    val shopId: String = "hotel1",
    val shopName: String = "CHETER",
    val isAuthenticated: Boolean = false,
    val authProvider: String = "Phone & Password", // "Phone & Password", "Google", "Email", "Demo"
    val sessionToken: String = "",
    val loginTime: Long = 0L
)

data class MenuItem(
    val id: Int,
    val nameHi: String,
    val nameEn: String = "",
    val category: MenuCategory = MenuCategory.MAIN,
    val price: Int = 0,
    val descHi: String = "",
    val descEn: String = "",
    val isVeg: Boolean = true,
    val spicyLevel: SpicyLevel = SpicyLevel.MEDIUM,
    val isBestseller: Boolean = false,
    val isAvailable: Boolean = true,
    val emoji: String = "🍲",
    val mediaUrl: String = "",
    val mediaType: String = "image", // "image" or "video"
    val calories: Int = 320,
    val prepTimeMin: Int = 15,
    val rating: Float = 4.8f,
    val reviewsCount: Int = 128,
    val portionSize: String = "1 Plate (Serves 2)",
    val tags: List<String> = listOf("Fresh", "Authentic"),
    val ingredients: List<String> = emptyList(),
    val hotelId: String = "hotel1"
)

data class CartItem(
    val item: MenuItem,
    val quantity: Int,
    val specialNote: String = ""
) {
    val totalPrice: Int get() = item.price * quantity
}

enum class OrderStatus(val titleHi: String, val titleEn: String, val stepIndex: Int) {
    RECEIVED("ऑर्डर प्राप्त हुआ", "Order Received", 0),
    PREPARING("रसोई में तैयार हो रहा है", "Cooking in Kitchen", 1),
    READY_TO_SERVE("परोसने के लिए तैयार", "Ready to Serve", 2),
    SERVED("टेबल पर परोसा गया", "Served to Table", 3)
}

enum class PaymentMethod(val titleHi: String, val titleEn: String, val iconEmoji: String) {
    UPI("ऑनलाइन पेमेंट (UPI)", "Online Payment (UPI)", "📱"),
    CASH("कैश भुगतान", "Pay via Cash", "💵")
}

enum class PaymentStatus {
    UNPAID,
    CASH_REQUESTED,
    PAID,
    FRAUD_UNPAID
}

data class CashPaymentAlert(
    val alertId: String,
    val tableNumber: Int,
    val totalAmount: Int,
    val customerName: String,
    val customerPhone: String,
    val customerId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)

data class BlockedCustomer(
    val customerId: String,
    val customerName: String,
    val phone: String,
    val tableNumber: Int,
    val unpaidAmount: Int,
    val orderId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val reason: String = "Left without paying bill (बिल दिए बिना चले गए)"
)

data class ConsolidatedBillItem(
    val menuItem: MenuItem,
    val quantity: Int,
    val unitPrice: Int,
    val totalPrice: Int,
    val notes: List<String> = emptyList(),
    val kotOrderIds: List<String> = emptyList()
)

data class TableOrder(
    val orderId: String,
    val tableNumber: Int,
    val customerName: String,
    val customerPhone: String = "9876543210",
    val customerId: String = "CUST-4102",
    val items: List<CartItem>,
    val specialInstructions: String,
    val subtotal: Int,
    val gst: Int,
    val tip: Int,
    val grandTotal: Int,
    val orderTimestamp: Long = System.currentTimeMillis(),
    val estimatedPrepTimeMinutes: Int = 20,
    val estimatedReadyTimestamp: Long = System.currentTimeMillis() + (20 * 60 * 1000L),
    val servedTimestamp: Long? = null,
    val status: OrderStatus = OrderStatus.RECEIVED,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val paymentMethod: PaymentMethod? = null,
    val transactionRef: String? = null,
    val paidTimestamp: Long? = null,
    val hotelId: String = "hotel1"
)

data class TableBillSummary(
    val tableNumber: Int,
    val orders: List<TableOrder>,
    val customerName: String,
    val customerPhone: String = "9876543210",
    val customerId: String = "CUST-4102",
    val totalItemsCount: Int,
    val totalSubtotal: Int,
    val totalGst: Int,
    val totalTips: Int,
    val grandTotal: Int,
    val isSettled: Boolean,
    val isCashRequested: Boolean = false,
    val isFraudUnpaid: Boolean = false
) {
    val consolidatedItems: List<ConsolidatedBillItem>
        get() {
            val map = linkedMapOf<Int, ConsolidatedBillItem>()
            orders.forEach { order ->
                order.items.forEach { cartItem ->
                    val existing = map[cartItem.item.id]
                    if (existing != null) {
                        val newQty = existing.quantity + cartItem.quantity
                        val newNotes = if (cartItem.specialNote.isNotBlank()) existing.notes + cartItem.specialNote else existing.notes
                        val newKots = (existing.kotOrderIds + order.orderId).distinct()
                        map[cartItem.item.id] = existing.copy(
                            quantity = newQty,
                            totalPrice = newQty * cartItem.item.price,
                            notes = newNotes,
                            kotOrderIds = newKots
                        )
                    } else {
                        map[cartItem.item.id] = ConsolidatedBillItem(
                            menuItem = cartItem.item,
                            quantity = cartItem.quantity,
                            unitPrice = cartItem.item.price,
                            totalPrice = cartItem.totalPrice,
                            notes = if (cartItem.specialNote.isNotBlank()) listOf(cartItem.specialNote) else emptyList(),
                            kotOrderIds = listOf(order.orderId)
                        )
                    }
                }
            }
            return map.values.toList()
        }
}

data class RestaurantTable(
    val tableNumber: Int,
    val tableCode: String = "T-$tableNumber",
    val capacity: Int = if (tableNumber % 5 == 0) 8 else if (tableNumber % 2 == 0) 6 else 4,
    val isOccupied: Boolean = false,
    val activeOrderId: String? = null,
    val activeCustomerName: String? = null,
    val activeOrderTotal: Int = 0,
    val lastOccupiedTime: Long = 0L,
    val isCashPaymentRequested: Boolean = false,
    val shopId: String = "hotel1",
    val hotelId: String = "hotel1"
)

enum class DatabaseEngine(val title: String, val badge: String) {
    SUPABASE_POSTGRESQL("Supabase PostgreSQL", "⚡ Relational Realtime"),
    FIRESTORE_NOSQL("Cloud Firestore", "🔥 Document DB")
}

enum class ScannedCodeType {
    TABLE_SELECTION,
    UPI_PAYMENT,
    MENU_LINK,
    RAW_TEXT
}

data class QrScanResult(
    val rawText: String,
    val type: ScannedCodeType,
    val extractedTableNumber: Int? = null,
    val extractedShopId: String? = null,
    val extractedHotelId: String? = null,
    val extractedUpiVpa: String? = null,
    val extractedAmount: Int? = null
)

data class DishReadyAlert(
    val alertId: String = java.util.UUID.randomUUID().toString(),
    val orderId: String,
    val tableNumber: Int,
    val dishName: String,
    val dishNameEn: String = "",
    val emoji: String = "🍲",
    val timestamp: Long = System.currentTimeMillis()
)



