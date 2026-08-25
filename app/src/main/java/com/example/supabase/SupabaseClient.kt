package com.example.supabase

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class SupabaseClient(private val context: Context?) {

    private val tag = "SupabaseClient"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    @Volatile
    var isHostReachable: Boolean = true
        private set

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private fun getBaseRestUrl(): String {
        val projectUrl = SupabaseConfig.getProjectUrl(context)
        return "$projectUrl/rest/v1"
    }

    private fun getAnonKey(): String {
        return SupabaseConfig.getAnonKey(context)
    }

    private fun createRequestBuilder(url: String): Request.Builder {
        val apiKey = getAnonKey()
        return Request.Builder()
            .url(url)
            .addHeader("apikey", apiKey)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=representation")
    }

    private inline fun <T> executeSafely(operation: () -> Result<T>): Result<T> {
        return try {
            val result = operation()
            isHostReachable = true
            result
        } catch (e: java.net.UnknownHostException) {
            if (isHostReachable) {
                Log.i(tag, "Supabase host is currently unreachable (${e.message}). Switching gracefully to local offline engine.")
                isHostReachable = false
            }
            Result.failure(e)
        } catch (e: java.net.SocketTimeoutException) {
            Log.w(tag, "Supabase network timeout: ${e.message}")
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.w(tag, "Supabase I/O connection warning: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.w(tag, "Supabase request failed: ${e.message}")
            Result.failure(e)
        }
    }

    // ================= MENU ITEMS =================

    suspend fun fetchMenuItems(hotelId: String = "hotel1"): Result<List<SupabaseMenuItemDto>> = withContext(Dispatchers.IO) {
        executeSafely {
            val cleanHotelId = hotelId.ifBlank { "hotel1" }
            val url = "${getBaseRestUrl()}/${SupabaseConfig.TABLE_MENU_ITEMS}?or=(hotel_id.eq.$cleanHotelId,shop_id.eq.$cleanHotelId)&order=id.asc"
            val request = createRequestBuilder(url).get().build()
            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body?.string() ?: "[]"
                val listType = Types.newParameterizedType(List::class.java, SupabaseMenuItemDto::class.java)
                val adapter = moshi.adapter<List<SupabaseMenuItemDto>>(listType)
                val items = adapter.fromJson(body) ?: emptyList()
                Result.success(items)
            } else {
                Log.w(tag, "Supabase fetchMenuItems status: ${response.code}")
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        }
    }

    suspend fun upsertMenuItem(dto: SupabaseMenuItemDto): Result<Boolean> = withContext(Dispatchers.IO) {
        executeSafely {
            val url = "${getBaseRestUrl()}/${SupabaseConfig.TABLE_MENU_ITEMS}?on_conflict=id"
            val adapter = moshi.adapter(SupabaseMenuItemDto::class.java)
            val json = adapter.toJson(dto)
            val body = json.toRequestBody(jsonMediaType)

            val request = createRequestBuilder(url)
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        }
    }

    suspend fun deleteMenuItem(itemId: Int, hotelId: String = "hotel1"): Result<Boolean> = withContext(Dispatchers.IO) {
        executeSafely {
            val cleanHotelId = hotelId.ifBlank { "hotel1" }
            val url = "${getBaseRestUrl()}/${SupabaseConfig.TABLE_MENU_ITEMS}?id=eq.$itemId&or=(hotel_id.eq.$cleanHotelId,shop_id.eq.$cleanHotelId)"
            val request = createRequestBuilder(url).delete().build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("HTTP ${response.code}"))
            }
        }
    }

    // ================= RESTAURANT TABLES (T-1 to T-100) =================

    suspend fun fetchTables(hotelId: String = "hotel1"): Result<List<SupabaseRestaurantTableDto>> = withContext(Dispatchers.IO) {
        executeSafely {
            val cleanHotelId = hotelId.ifBlank { "hotel1" }
            val url = "${getBaseRestUrl()}/${SupabaseConfig.TABLE_RESTAURANT_TABLES}?or=(hotel_id.eq.$cleanHotelId,shop_id.eq.$cleanHotelId)&order=table_number.asc"
            val request = createRequestBuilder(url).get().build()
            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body?.string() ?: "[]"
                val listType = Types.newParameterizedType(List::class.java, SupabaseRestaurantTableDto::class.java)
                val adapter = moshi.adapter<List<SupabaseRestaurantTableDto>>(listType)
                val tables = adapter.fromJson(body) ?: emptyList()
                Result.success(tables)
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        }
    }

    suspend fun upsertTable(dto: SupabaseRestaurantTableDto): Result<Boolean> = withContext(Dispatchers.IO) {
        executeSafely {
            val url = "${getBaseRestUrl()}/${SupabaseConfig.TABLE_RESTAURANT_TABLES}?on_conflict=table_number,hotel_id"
            val adapter = moshi.adapter(SupabaseRestaurantTableDto::class.java)
            val json = adapter.toJson(dto)
            val body = json.toRequestBody(jsonMediaType)

            val request = createRequestBuilder(url)
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        }
    }

    suspend fun updateTableOccupancy(
        tableNumber: Int,
        isOccupied: Boolean,
        activeOrderId: String? = null,
        activeCustomerName: String? = null,
        orderTotal: Int = 0,
        isCashRequested: Boolean = false,
        hotelId: String = "hotel1"
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        executeSafely {
            val cleanHotelId = hotelId.ifBlank { "hotel1" }
            val url = "${getBaseRestUrl()}/${SupabaseConfig.TABLE_RESTAURANT_TABLES}?table_number=eq.$tableNumber&or=(hotel_id.eq.$cleanHotelId,shop_id.eq.$cleanHotelId)"
            val jsonPayload = """
                {
                    "is_occupied": $isOccupied,
                    "active_order_id": ${if (activeOrderId != null) "\"$activeOrderId\"" else "null"},
                    "active_customer_name": ${if (activeCustomerName != null) "\"$activeCustomerName\"" else "null"},
                    "active_order_total": $orderTotal,
                    "is_cash_payment_requested": $isCashRequested,
                    "last_occupied_time": ${if (isOccupied) System.currentTimeMillis() else 0L}
                }
            """.trimIndent()

            val body = jsonPayload.toRequestBody(jsonMediaType)
            val request = createRequestBuilder(url).patch(body).build()
            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        }
    }

    suspend fun batchSeedTables(totalTables: Int = 100, hotelId: String = "hotel1"): Result<Boolean> = withContext(Dispatchers.IO) {
        executeSafely {
            val cleanHotelId = hotelId.ifBlank { "hotel1" }
            val list = (1..totalTables).map { num ->
                SupabaseRestaurantTableDto(
                    tableNumber = num,
                    tableCode = "T-$num",
                    capacity = if (num % 5 == 0) 8 else if (num % 2 == 0) 6 else 4,
                    isOccupied = false,
                    hotelId = cleanHotelId,
                    shopId = cleanHotelId
                )
            }
            val listType = Types.newParameterizedType(List::class.java, SupabaseRestaurantTableDto::class.java)
            val adapter = moshi.adapter<List<SupabaseRestaurantTableDto>>(listType)
            val json = adapter.toJson(list)
            val body = json.toRequestBody(jsonMediaType)

            val url = "${getBaseRestUrl()}/${SupabaseConfig.TABLE_RESTAURANT_TABLES}?on_conflict=table_number,hotel_id"
            val request = createRequestBuilder(url)
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        }
    }

    // ================= ORDERS =================

    suspend fun fetchOrders(hotelId: String = "hotel1"): Result<List<SupabaseOrderDto>> = withContext(Dispatchers.IO) {
        executeSafely {
            val cleanHotelId = hotelId.ifBlank { "hotel1" }
            val url = "${getBaseRestUrl()}/${SupabaseConfig.TABLE_ORDERS}?or=(hotel_id.eq.$cleanHotelId,shop_id.eq.$cleanHotelId)&order=order_time.desc&limit=100"
            val request = createRequestBuilder(url).get().build()
            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body?.string() ?: "[]"
                val listType = Types.newParameterizedType(List::class.java, SupabaseOrderDto::class.java)
                val adapter = moshi.adapter<List<SupabaseOrderDto>>(listType)
                val orders = adapter.fromJson(body) ?: emptyList()
                Result.success(orders)
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        }
    }

    suspend fun insertOrder(orderDto: SupabaseOrderDto): Result<Boolean> = withContext(Dispatchers.IO) {
        executeSafely {
            val url = "${getBaseRestUrl()}/${SupabaseConfig.TABLE_ORDERS}"
            val adapter = moshi.adapter(SupabaseOrderDto::class.java)
            val json = adapter.toJson(orderDto)
            val body = json.toRequestBody(jsonMediaType)

            val request = createRequestBuilder(url)
                .addHeader("Prefer", "return=minimal")
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: String): Result<Boolean> = withContext(Dispatchers.IO) {
        executeSafely {
            val url = "${getBaseRestUrl()}/${SupabaseConfig.TABLE_ORDERS}?id=eq.$orderId"
            val jsonPayload = """{"status": "$status"}"""
            val body = jsonPayload.toRequestBody(jsonMediaType)

            val request = createRequestBuilder(url).patch(body).build()
            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        }
    }

    suspend fun updateOrderPayment(
        orderId: String,
        paymentStatus: String,
        paymentMode: String? = null,
        transactionRef: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        executeSafely {
            val url = "${getBaseRestUrl()}/${SupabaseConfig.TABLE_ORDERS}?id=eq.$orderId"
            val modeJson = if (paymentMode != null) "\"payment_mode\": \"$paymentMode\"," else ""
            val refJson = if (transactionRef != null) "\"transaction_ref\": \"$transactionRef\"," else ""
            val jsonPayload = """
                {
                    "payment_status": "$paymentStatus",
                    $modeJson
                    $refJson
                    "paid_timestamp": ${System.currentTimeMillis()}
                }
            """.trimIndent()

            val body = jsonPayload.toRequestBody(jsonMediaType)
            val request = createRequestBuilder(url).patch(body).build()
            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        }
    }

    // ================= SUPABASE AUTH / USERS =================

    suspend fun fetchUserByPhone(phone: String): Result<SupabaseUserDto?> = withContext(Dispatchers.IO) {
        executeSafely {
            val url = "${getBaseRestUrl()}/${SupabaseConfig.TABLE_USERS}?phone=eq.$phone&limit=1"
            val request = createRequestBuilder(url).get().build()
            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body?.string() ?: "[]"
                val listType = Types.newParameterizedType(List::class.java, SupabaseUserDto::class.java)
                val adapter = moshi.adapter<List<SupabaseUserDto>>(listType)
                val users = adapter.fromJson(body) ?: emptyList()
                Result.success(users.firstOrNull())
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        }
    }

    suspend fun registerUser(userDto: SupabaseUserDto): Result<Boolean> = withContext(Dispatchers.IO) {
        executeSafely {
            val url = "${getBaseRestUrl()}/${SupabaseConfig.TABLE_USERS}?on_conflict=phone"
            val adapter = moshi.adapter(SupabaseUserDto::class.java)
            val json = adapter.toJson(userDto)
            val body = json.toRequestBody(jsonMediaType)

            val request = createRequestBuilder(url)
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        }
    }

    suspend fun updateUserSession(phone: String, sessionToken: String): Result<Boolean> = withContext(Dispatchers.IO) {
        executeSafely {
            val url = "${getBaseRestUrl()}/${SupabaseConfig.TABLE_USERS}?phone=eq.$phone"
            val jsonPayload = """
                {
                    "session_token": "$sessionToken",
                    "last_login": ${System.currentTimeMillis()}
                }
            """.trimIndent()
            val body = jsonPayload.toRequestBody(jsonMediaType)
            val request = createRequestBuilder(url).patch(body).build()
            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        }
    }

    // ================= RESTAURANTS (UPI & SETTINGS) =================

    suspend fun fetchRestaurant(restaurantId: String = "hotel1"): Result<SupabaseRestaurantDto?> = withContext(Dispatchers.IO) {
        executeSafely {
            val cleanId = restaurantId.ifBlank { "hotel1" }
            val url = "${getBaseRestUrl()}/${SupabaseConfig.TABLE_RESTAURANTS}?id=eq.$cleanId&limit=1"
            val request = createRequestBuilder(url).get().build()
            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body?.string() ?: "[]"
                val listType = Types.newParameterizedType(List::class.java, SupabaseRestaurantDto::class.java)
                val adapter = moshi.adapter<List<SupabaseRestaurantDto>>(listType)
                val list = adapter.fromJson(body) ?: emptyList()
                Result.success(list.firstOrNull())
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        }
    }

    suspend fun upsertRestaurant(dto: SupabaseRestaurantDto): Result<Boolean> = withContext(Dispatchers.IO) {
        executeSafely {
            val url = "${getBaseRestUrl()}/${SupabaseConfig.TABLE_RESTAURANTS}?on_conflict=id"
            val adapter = moshi.adapter(SupabaseRestaurantDto::class.java)
            val json = adapter.toJson(dto)
            val body = json.toRequestBody(jsonMediaType)

            val request = createRequestBuilder(url)
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        }
    }
}

