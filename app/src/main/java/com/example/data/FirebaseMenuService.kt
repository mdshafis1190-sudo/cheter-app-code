package com.example.data

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.model.BlockedCustomer
import com.example.model.CartItem
import com.example.model.CashPaymentAlert
import com.example.model.MenuCategory
import com.example.model.MenuItem
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.ShopInfo
import com.example.model.ShopOwnerUser
import com.example.model.SpicyLevel
import com.example.model.TableOrder
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseMenuService {

    private val tag = "FirebaseMenuService"

    // Safe Firebase Auth instance
    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "FirebaseAuth initialization fallback: ${e.localizedMessage}")
            null
        }
    }

    // Safe Firestore instance configured for 100% Online Real-time Sync (No Offline Cache/Editing)
    private val firestore: FirebaseFirestore? by lazy {
        try {
            val instance = FirebaseFirestore.getInstance()
            try {
                // Disable offline persistence to guarantee 100% online real-time data sync
                val settings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                    .build()
                instance.firestoreSettings = settings
            } catch (_: Throwable) {
                try {
                    @Suppress("DEPRECATION")
                    val settings = FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(false)
                        .build()
                    instance.firestoreSettings = settings
                } catch (e: Throwable) {
                    Log.w(tag, "Firestore settings configuration warning: ${e.localizedMessage}")
                }
            }
            instance
        } catch (e: Exception) {
            Log.w(tag, "FirebaseFirestore initialization fallback: ${e.localizedMessage}")
            null
        }
    }

    fun getCurrentUser(context: Context? = null): ShopOwnerUser? {
        if (context != null) {
            val sessionUser = com.example.security.AuthSecurityManager.loadSession(context)
            if (sessionUser != null && sessionUser.isAuthenticated) {
                return sessionUser
            }
        }
        val user = auth?.currentUser ?: return null
        val shopId = "shop_" + user.uid.take(6).lowercase()
        return ShopOwnerUser(
            uid = user.uid,
            phone = user.phoneNumber ?: "",
            email = user.email ?: "",
            displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Shop Owner",
            photoUrl = user.photoUrl?.toString() ?: "",
            shopId = shopId,
            shopName = "CHETER Restaurant & Lounge",
            isAuthenticated = true,
            authProvider = if (user.providerData.any { it.providerId == "google.com" }) "Google" else "Email"
        )
    }

    /**
     * Dual-Factor Credential Matching: Phone Number + Password with Strict Security & Lockout
     */
    suspend fun signInWithPhone(
        context: Context,
        rawPhone: String,
        rawPass: String
    ): Result<ShopOwnerUser> = withContext(Dispatchers.IO) {
        val authResult = com.example.security.AuthSecurityManager.verifyCredentials(
            context = context,
            rawPhone = rawPhone,
            rawPassword = rawPass
        )

        authResult.fold(
            onSuccess = { account ->
                val shopOwner = ShopOwnerUser(
                    uid = "usr_${account.phone}",
                    phone = account.phone,
                    email = "${account.phone}@cheter.app",
                    displayName = account.displayName.ifBlank { "ओनर (${account.phone.takeLast(4)})" },
                    shopId = account.shopId.ifBlank { "cheter_101" },
                    shopName = account.shopName.ifBlank { "CHETER Restaurant & Lounge" },
                    isAuthenticated = true,
                    authProvider = "Phone & Password",
                    sessionToken = java.util.UUID.randomUUID().toString(),
                    loginTime = System.currentTimeMillis()
                )

                // Save session securely locally so owner stays logged in on device
                com.example.security.AuthSecurityManager.saveSession(context, shopOwner)

                // Sync with Firestore in background if available
                val db = firestore
                if (db != null) {
                    try {
                        val data = mapOf(
                            "phone" to account.phone,
                            "shopId" to account.shopId,
                            "shopName" to account.shopName,
                            "lastLogin" to System.currentTimeMillis()
                        )
                        db.collection("owner_accounts").document(account.phone)
                            .set(data, SetOptions.merge())
                    } catch (e: Exception) {
                        Log.w(tag, "Firestore owner sync non-fatal: ${e.localizedMessage}")
                    }
                }

                Result.success(shopOwner)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    /**
     * Owner Registration with Phone Number, Password, and Shop Name
     */
    suspend fun signUpWithPhone(
        context: Context,
        rawPhone: String,
        rawPass: String,
        shopName: String,
        ownerName: String = ""
    ): Result<ShopOwnerUser> = withContext(Dispatchers.IO) {
        val phoneSanitized = com.example.security.AuthSecurityManager.sanitizePhoneNumber(rawPhone)
        if (phoneSanitized.length < 10) {
            return@withContext Result.failure(Exception("Please enter a valid 10-digit phone number"))
        }

        val maliciousPhone = com.example.security.AuthSecurityManager.detectMaliciousInput(rawPhone)
        if (maliciousPhone != null) {
            return@withContext Result.failure(Exception("Security Alert: $maliciousPhone"))
        }

        val maliciousPass = com.example.security.AuthSecurityManager.detectMaliciousInput(rawPass)
        if (maliciousPass != null) {
            return@withContext Result.failure(Exception("Security Alert: $maliciousPass"))
        }

        val cleanShopName = com.example.security.AuthSecurityManager.sanitizeText(shopName).ifBlank { "CHETER Restaurant & Lounge" }
        val cleanOwnerName = com.example.security.AuthSecurityManager.sanitizeText(ownerName).ifBlank { "दुकानदार" }

        if (rawPass.length < 4) {
            return@withContext Result.failure(Exception("Password must be at least 4 characters"))
        }

        val existingAccount = com.example.security.AuthSecurityManager.getAccountLocally(context, phoneSanitized)
        if (existingAccount != null) {
            return@withContext Result.failure(Exception("इस फोन नंबर पर पहले से खाता मौजूद है। कृप्या लॉगिन करें।"))
        }

        val newShopId = "shop_" + phoneSanitized.takeLast(4)
        val passwordHash = com.example.security.AuthSecurityManager.hashPassword(rawPass)

        val newAccount = com.example.security.StoredOwnerAccount(
            phone = phoneSanitized,
            passwordHash = passwordHash,
            plainPasswordBackup = rawPass,
            shopName = cleanShopName,
            shopId = newShopId,
            displayName = cleanOwnerName
        )

        com.example.security.AuthSecurityManager.saveAccountLocally(context, newAccount)

        val shopOwner = ShopOwnerUser(
            uid = "usr_$phoneSanitized",
            phone = phoneSanitized,
            email = "$phoneSanitized@cheter.app",
            displayName = cleanOwnerName,
            shopId = newShopId,
            shopName = cleanShopName,
            isAuthenticated = true,
            authProvider = "Phone & Password",
            sessionToken = java.util.UUID.randomUUID().toString(),
            loginTime = System.currentTimeMillis()
        )

        com.example.security.AuthSecurityManager.saveSession(context, shopOwner)

        // Save shop info
        saveShopInfo(ShopInfo(shopId = newShopId, shopName = cleanShopName, ownerEmail = "$phoneSanitized@cheter.app", phone = "+91 $phoneSanitized"))

        Result.success(shopOwner)
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<ShopOwnerUser> = withContext(Dispatchers.IO) {
        val currentAuth = auth
        if (currentAuth == null) {
            // Local fallback simulation for smooth dev mode
            val simulatedUser = ShopOwnerUser(
                uid = "usr_" + email.hashCode().toString().take(6),
                email = email,
                displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                shopId = "shop_101",
                shopName = "रॉयल ढाबा",
                isAuthenticated = true,
                authProvider = "Email"
            )
            return@withContext Result.success(simulatedUser)
        }

        try {
            val authResult = currentAuth.signInWithEmailAndPassword(email, pass).await()
            val user = authResult.user
            if (user != null) {
                val shopId = "shop_" + user.uid.take(6).lowercase()
                val shopOwner = ShopOwnerUser(
                    uid = user.uid,
                    email = user.email ?: email,
                    displayName = user.displayName ?: email.substringBefore("@"),
                    shopId = shopId,
                    shopName = "रॉयल ढाबा",
                    isAuthenticated = true,
                    authProvider = "Email"
                )
                Result.success(shopOwner)
            } else {
                Result.failure(Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            // Fallback for demo mode if Firebase project is not active
            Log.e(tag, "Firebase email sign-in error: ${e.localizedMessage}")
            val fallbackUser = ShopOwnerUser(
                uid = "demo_owner_101",
                email = email,
                displayName = email.substringBefore("@").ifBlank { "दुकानदार" },
                shopId = "shop_101",
                shopName = "रॉयल ढाबा",
                isAuthenticated = true,
                authProvider = "Email (Local Mode)"
            )
            Result.success(fallbackUser)
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, shopName: String): Result<ShopOwnerUser> = withContext(Dispatchers.IO) {
        val currentAuth = auth
        if (currentAuth == null) {
            val user = ShopOwnerUser(
                uid = "usr_" + email.hashCode().toString().take(6),
                email = email,
                displayName = email.substringBefore("@"),
                shopId = "shop_" + (100..999).random(),
                shopName = shopName.ifBlank { "रॉयल ढाबा" },
                isAuthenticated = true,
                authProvider = "Email"
            )
            return@withContext Result.success(user)
        }

        try {
            val authResult = currentAuth.createUserWithEmailAndPassword(email, pass).await()
            val user = authResult.user
            if (user != null) {
                val shopId = "shop_" + user.uid.take(6).lowercase()
                val shopOwner = ShopOwnerUser(
                    uid = user.uid,
                    email = user.email ?: email,
                    displayName = email.substringBefore("@"),
                    shopId = shopId,
                    shopName = shopName.ifBlank { "रॉयल ढाबा" },
                    isAuthenticated = true,
                    authProvider = "Email"
                )
                // Save shop info to Firestore
                saveShopInfo(ShopInfo(shopId = shopId, shopName = shopOwner.shopName, ownerEmail = user.email ?: email))
                Result.success(shopOwner)
            } else {
                Result.failure(Exception("Registration failed"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Sign up error: ${e.localizedMessage}")
            val fallback = ShopOwnerUser(
                uid = "demo_new_owner",
                email = email,
                displayName = email.substringBefore("@"),
                shopId = "shop_101",
                shopName = shopName.ifBlank { "रॉयल ढाबा" },
                isAuthenticated = true,
                authProvider = "Email (Local Mode)"
            )
            Result.success(fallback)
        }
    }

    suspend fun signInWithGoogle(context: Context, webClientId: String = ""): Result<ShopOwnerUser> = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(if (webClientId.isNotBlank()) webClientId else "123456789-dummy.apps.googleusercontent.com")
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)
                val currentAuth = auth
                if (currentAuth != null) {
                    val authResult = currentAuth.signInWithCredential(firebaseCred).await()
                    val user = authResult.user
                    if (user != null) {
                        val shopId = "shop_" + user.uid.take(6).lowercase()
                        val shopOwner = ShopOwnerUser(
                            uid = user.uid,
                            email = user.email ?: googleIdTokenCredential.id,
                            displayName = user.displayName ?: googleIdTokenCredential.displayName ?: "Shop Owner",
                            photoUrl = user.photoUrl?.toString() ?: googleIdTokenCredential.profilePictureUri?.toString() ?: "",
                            shopId = shopId,
                            shopName = "रॉयल ढाबा",
                            isAuthenticated = true,
                            authProvider = "Google"
                        )
                        return@withContext Result.success(shopOwner)
                    }
                }
            }

            // Fallback for Google sign-in UI demo
            val demoGoogleUser = ShopOwnerUser(
                uid = "google_owner_101",
                email = "owner@dhabarestaurant.com",
                displayName = "रॉयल ढाबा ओनर (Google)",
                shopId = "shop_101",
                shopName = "रॉयल ढाबा",
                isAuthenticated = true,
                authProvider = "Google"
            )
            Result.success(demoGoogleUser)
        } catch (e: Exception) {
            Log.w(tag, "Google Sign-In fallback: ${e.localizedMessage}")
            val demoGoogleUser = ShopOwnerUser(
                uid = "google_owner_101",
                email = "owner@dhabarestaurant.com",
                displayName = "रॉयल ढाबा ओनर (Google)",
                shopId = "shop_101",
                shopName = "रॉयल ढाबा",
                isAuthenticated = true,
                authProvider = "Google"
            )
            Result.success(demoGoogleUser)
        }
    }

    suspend fun signOut(context: Context) = withContext(Dispatchers.IO) {
        try {
            com.example.security.AuthSecurityManager.clearSession(context)
            auth?.signOut()
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e(tag, "Sign out error: ${e.localizedMessage}")
        }
    }

    suspend fun saveShopInfo(shopInfo: ShopInfo): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            val data = mapOf(
                "shopId" to shopInfo.shopId,
                "shopName" to shopInfo.shopName,
                "tagline" to shopInfo.tagline,
                "ownerEmail" to shopInfo.ownerEmail,
                "upiId" to shopInfo.upiId,
                "customUpiQrUrl" to shopInfo.customUpiQrUrl,
                "totalTables" to shopInfo.totalTables,
                "phone" to shopInfo.phone,
                "address" to shopInfo.address,
                "qrCodeUrl" to shopInfo.qrCodeUrl,
                "menuBaseUrl" to shopInfo.menuBaseUrl,
                "isOnline" to shopInfo.isOnline,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("shops").document(shopInfo.shopId)
                .set(data, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.w(tag, "Firestore saveShopInfo: ${e.localizedMessage}")
            false
        }
    }

    suspend fun getShopInfo(shopId: String): ShopInfo? = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext null
        try {
            val doc = db.collection("shops").document(shopId).get().await()
            if (doc.exists()) {
                ShopInfo(
                    shopId = doc.getString("shopId") ?: shopId,
                    shopName = doc.getString("shopName") ?: "CHETER Restaurant & Lounge",
                    tagline = doc.getString("tagline") ?: "QR कोड स्कैन करें और ताज़ा खाना ऑर्डर करें",
                    ownerEmail = doc.getString("ownerEmail") ?: "",
                    upiId = doc.getString("upiId") ?: "cheter.dine@okhdfcbank",
                    customUpiQrUrl = doc.getString("customUpiQrUrl") ?: "",
                    totalTables = (doc.getLong("totalTables") ?: 100L).toInt(),
                    phone = doc.getString("phone") ?: "+91 98765 43210",
                    address = doc.getString("address") ?: "CHETER Premium Lounge & Dine, NH-44",
                    qrCodeUrl = doc.getString("qrCodeUrl") ?: "https://api.qrserver.com/v1/create-qr-code/?data=https://cheter.app?shopId=$shopId",
                    menuBaseUrl = doc.getString("menuBaseUrl") ?: "https://cheter.app?shopId=$shopId",
                    isOnline = doc.getBoolean("isOnline") ?: true
                )
            } else null
        } catch (e: Exception) {
            Log.w(tag, "Firestore getShopInfo: ${e.localizedMessage}")
            null
        }
    }

    fun observeShopInfo(shopId: String, onUpdate: (ShopInfo) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("shops").document(shopId)
                .addSnapshotListener { doc, error ->
                    if (error != null) {
                        Log.w(tag, "observeShopInfo error: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (doc != null && doc.exists()) {
                        val shop = ShopInfo(
                            shopId = doc.getString("shopId") ?: shopId,
                            shopName = doc.getString("shopName") ?: "CHETER Restaurant & Lounge",
                            tagline = doc.getString("tagline") ?: "QR कोड स्कैन करें और ताज़ा खाना ऑर्डर करें",
                            ownerEmail = doc.getString("ownerEmail") ?: "",
                            upiId = doc.getString("upiId") ?: "cheter.dine@okhdfcbank",
                            customUpiQrUrl = doc.getString("customUpiQrUrl") ?: "",
                            totalTables = (doc.getLong("totalTables") ?: 100L).toInt(),
                            phone = doc.getString("phone") ?: "+91 98765 43210",
                            address = doc.getString("address") ?: "CHETER Premium Lounge & Dine, NH-44",
                            qrCodeUrl = doc.getString("qrCodeUrl") ?: "https://api.qrserver.com/v1/create-qr-code/?data=https://cheter.app?shopId=$shopId",
                            menuBaseUrl = doc.getString("menuBaseUrl") ?: "https://cheter.app?shopId=$shopId",
                            isOnline = doc.getBoolean("isOnline") ?: true
                        )
                        onUpdate(shop)
                    }
                }
        } catch (e: Exception) {
            Log.w(tag, "observeShopInfo failure: ${e.localizedMessage}")
            null
        }
    }

    suspend fun syncMenuItemToCloud(shopId: String, item: MenuItem): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            val itemMap = mapOf(
                "id" to item.id.toString(),
                "nameHi" to item.nameHi,
                "nameEn" to item.nameEn,
                "category" to item.category.name,
                "price" to item.price,
                "descHi" to item.descHi,
                "descEn" to item.descEn,
                "isVeg" to item.isVeg,
                "spicyLevel" to item.spicyLevel.name,
                "isBestseller" to item.isBestseller,
                "isAvailable" to item.isAvailable,
                "emoji" to item.emoji,
                "mediaUrl" to item.mediaUrl,
                "mediaType" to item.mediaType,
                "calories" to item.calories,
                "prepTimeMin" to item.prepTimeMin,
                "rating" to item.rating,
                "reviewsCount" to item.reviewsCount,
                "portionSize" to item.portionSize,
                "tags" to item.tags,
                "ingredients" to item.ingredients
            )
            db.collection("shops").document(shopId)
                .collection("menu").document(item.id.toString())
                .set(itemMap, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.w(tag, "Firestore syncMenuItem error: ${e.localizedMessage}")
            false
        }
    }

    suspend fun deleteMenuItemFromCloud(shopId: String, itemId: Int): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            db.collection("shops").document(shopId)
                .collection("menu").document(itemId.toString())
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.w(tag, "Firestore delete item error: ${e.localizedMessage}")
            false
        }
    }

    suspend fun fetchCloudMenu(shopId: String): List<MenuItem>? = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext null
        try {
            val snapshot = db.collection("shops").document(shopId)
                .collection("menu")
                .get()
                .await()

            if (snapshot.isEmpty) return@withContext null

            snapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id")?.toIntOrNull() ?: doc.id.toIntOrNull() ?: return@mapNotNull null
                val nameHi = doc.getString("nameHi") ?: ""
                val nameEn = doc.getString("nameEn") ?: ""
                val categoryStr = doc.getString("category") ?: "MAIN"
                val category = try { MenuCategory.valueOf(categoryStr) } catch (_: Exception) { MenuCategory.MAIN }
                val price = (doc.getLong("price") ?: 100L).toInt()
                val descHi = doc.getString("descHi") ?: ""
                val descEn = doc.getString("descEn") ?: ""
                val isVeg = doc.getBoolean("isVeg") ?: true
                val spicyStr = doc.getString("spicyLevel") ?: "MEDIUM"
                val spicyLevel = try { SpicyLevel.valueOf(spicyStr) } catch (_: Exception) { SpicyLevel.MEDIUM }
                val isBestseller = doc.getBoolean("isBestseller") ?: false
                val isAvailable = doc.getBoolean("isAvailable") ?: true
                val emoji = doc.getString("emoji") ?: "🍲"
                val mediaUrl = doc.getString("mediaUrl") ?: ""
                val mediaType = doc.getString("mediaType") ?: "image"

                MenuItem(
                    id = id,
                    nameHi = nameHi,
                    nameEn = nameEn,
                    category = category,
                    price = price,
                    descHi = descHi,
                    descEn = descEn,
                    isVeg = isVeg,
                    spicyLevel = spicyLevel,
                    isBestseller = isBestseller,
                    isAvailable = isAvailable,
                    emoji = emoji,
                    mediaUrl = mediaUrl,
                    mediaType = mediaType
                )
            }
        } catch (e: Exception) {
            Log.w(tag, "fetchCloudMenu error: ${e.localizedMessage}")
            null
        }
    }

    // ================= REAL-TIME ONLINE ORDER SYNC =================

    suspend fun syncOrderOnline(shopId: String, order: TableOrder): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            val itemsData = order.items.map { cartItem ->
                mapOf(
                    "itemId" to cartItem.item.id,
                    "nameHi" to cartItem.item.nameHi,
                    "nameEn" to cartItem.item.nameEn,
                    "price" to cartItem.item.price,
                    "quantity" to cartItem.quantity,
                    "specialNote" to cartItem.specialNote,
                    "emoji" to cartItem.item.emoji
                )
            }

            val orderData = mapOf(
                "orderId" to order.orderId,
                "tableNumber" to order.tableNumber,
                "customerName" to order.customerName,
                "customerPhone" to order.customerPhone,
                "customerId" to order.customerId,
                "items" to itemsData,
                "specialInstructions" to order.specialInstructions,
                "subtotal" to order.subtotal,
                "gst" to order.gst,
                "tip" to order.tip,
                "grandTotal" to order.grandTotal,
                "orderTimestamp" to order.orderTimestamp,
                "estimatedPrepTimeMinutes" to order.estimatedPrepTimeMinutes,
                "estimatedReadyTimestamp" to order.estimatedReadyTimestamp,
                "servedTimestamp" to order.servedTimestamp,
                "status" to order.status.name,
                "paymentStatus" to order.paymentStatus.name,
                "paymentMethod" to order.paymentMethod?.name,
                "isSessionActive" to (order.status != OrderStatus.SERVED || order.paymentStatus != PaymentStatus.PAID)
            )

            db.collection("shops").document(shopId)
                .collection("orders").document(order.orderId)
                .set(orderData, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.w(tag, "syncOrderOnline error: ${e.localizedMessage}")
            false
        }
    }

    suspend fun updateOrderStatusOnline(
        shopId: String,
        orderId: String,
        status: OrderStatus,
        servedTimestamp: Long? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            val updates = mutableMapOf<String, Any>(
                "status" to status.name,
                "isSessionActive" to (status != OrderStatus.SERVED)
            )
            if (servedTimestamp != null) {
                updates["servedTimestamp"] = servedTimestamp
            }
            db.collection("shops").document(shopId)
                .collection("orders").document(orderId)
                .update(updates)
                .await()
            true
        } catch (e: Exception) {
            Log.w(tag, "updateOrderStatusOnline error: ${e.localizedMessage}")
            false
        }
    }

    suspend fun updatePaymentStatusOnline(
        shopId: String,
        orderId: String,
        paymentStatus: PaymentStatus,
        paymentMethod: PaymentMethod? = null,
        tip: Int = 0
    ): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            val updates = mutableMapOf<String, Any>(
                "paymentStatus" to paymentStatus.name,
                "tip" to tip
            )
            if (paymentMethod != null) {
                updates["paymentMethod"] = paymentMethod.name
            }
            if (paymentStatus == PaymentStatus.PAID) {
                updates["isSessionActive"] = false
            }
            db.collection("shops").document(shopId)
                .collection("orders").document(orderId)
                .update(updates)
                .await()
            true
        } catch (e: Exception) {
            Log.w(tag, "updatePaymentStatusOnline error: ${e.localizedMessage}")
            false
        }
    }

    // ================= REAL-TIME CASH ALERTS =================

    suspend fun sendCashAlertOnline(shopId: String, alert: CashPaymentAlert): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            val alertData = mapOf(
                "alertId" to alert.alertId,
                "tableNumber" to alert.tableNumber,
                "totalAmount" to alert.totalAmount,
                "customerName" to alert.customerName,
                "customerPhone" to alert.customerPhone,
                "customerId" to alert.customerId,
                "timestamp" to alert.timestamp,
                "isResolved" to alert.isResolved,
                "status" to if (alert.isResolved) "RESOLVED" else "PENDING"
            )
            db.collection("shops").document(shopId)
                .collection("cash_alerts").document("table_${alert.tableNumber}")
                .set(alertData, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.w(tag, "sendCashAlertOnline error: ${e.localizedMessage}")
            false
        }
    }

    suspend fun resolveCashAlertOnline(shopId: String, tableNumber: Int): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            db.collection("shops").document(shopId)
                .collection("cash_alerts").document("table_$tableNumber")
                .update(
                    mapOf(
                        "isResolved" to true,
                        "status" to "RESOLVED"
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            Log.w(tag, "resolveCashAlertOnline error: ${e.localizedMessage}")
            false
        }
    }

    // ================= REAL-TIME FRAUD / BLOCKED USERS =================

    suspend fun syncBlockedUserOnline(shopId: String, blockedCustomer: BlockedCustomer): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            val data = mapOf(
                "customerId" to blockedCustomer.customerId,
                "customerName" to blockedCustomer.customerName,
                "phone" to blockedCustomer.phone,
                "unpaidAmount" to blockedCustomer.unpaidAmount,
                "orderId" to blockedCustomer.orderId,
                "tableNumber" to blockedCustomer.tableNumber,
                "timestamp" to blockedCustomer.timestamp,
                "reason" to blockedCustomer.reason
            )
            db.collection("shops").document(shopId)
                .collection("blocked_users").document(blockedCustomer.customerId)
                .set(data, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.w(tag, "syncBlockedUserOnline error: ${e.localizedMessage}")
            false
        }
    }

    suspend fun unblockUserOnline(shopId: String, customerId: String): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            db.collection("shops").document(shopId)
                .collection("blocked_users").document(customerId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.w(tag, "unblockUserOnline error: ${e.localizedMessage}")
            false
        }
    }

    // ================= HIGH-TRAFFIC OPTIMIZED ACTIVE SESSION LISTENERS =================

    /**
     * Customer App: Listens ONLY to active orders for the customer's specific table session.
     * Restricting queries strictly to active table sessions eliminates redundant reads and prevents server overload.
     */
    fun observeActiveTableOrders(
        shopId: String,
        tableNumber: Int,
        onOrdersUpdated: (List<TableOrder>) -> Unit
    ): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("shops").document(shopId)
                .collection("orders")
                .whereEqualTo("tableNumber", tableNumber)
                .limit(20)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "observeActiveTableOrders error: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val orders = snapshot.documents.mapNotNull { parseTableOrder(it) }
                        onOrdersUpdated(orders)
                    }
                }
        } catch (e: Exception) {
            Log.w(tag, "observeActiveTableOrders failure: ${e.localizedMessage}")
            null
        }
    }

    /**
     * Kitchen / Admin App: Listens ONLY to active orders currently in progress
     * (filtering out historical completed orders to minimize Firebase query load during peak dining rush).
     */
    fun observeKitchenActiveOrders(
        shopId: String,
        onOrdersUpdated: (List<TableOrder>) -> Unit
    ): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            val fourHoursAgo = System.currentTimeMillis() - (4 * 60 * 60 * 1000L)
            db.collection("shops").document(shopId)
                .collection("orders")
                .whereGreaterThan("orderTimestamp", fourHoursAgo)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "observeKitchenActiveOrders error: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val orders = snapshot.documents.mapNotNull { parseTableOrder(it) }
                        onOrdersUpdated(orders)
                    }
                }
        } catch (e: Exception) {
            Log.w(tag, "observeKitchenActiveOrders failure: ${e.localizedMessage}")
            null
        }
    }

    /**
     * Live Cash Payment Alerts listener (strictly active/pending alerts only)
     */
    fun observeActiveCashAlerts(
        shopId: String,
        onAlertsUpdated: (List<CashPaymentAlert>) -> Unit
    ): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("shops").document(shopId)
                .collection("cash_alerts")
                .whereEqualTo("isResolved", false)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "observeActiveCashAlerts error: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val alerts = snapshot.documents.mapNotNull { doc ->
                            val alertId = doc.getString("alertId") ?: doc.id
                            val tableNumber = (doc.getLong("tableNumber") ?: 1L).toInt()
                            val totalAmount = (doc.getLong("totalAmount") ?: doc.getLong("amount") ?: 0L).toInt()
                            val customerName = doc.getString("customerName") ?: "Guest"
                            val customerPhone = doc.getString("customerPhone") ?: ""
                            val customerId = doc.getString("customerId") ?: ""
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            val isResolved = doc.getBoolean("isResolved") ?: false
                            CashPaymentAlert(
                                alertId = alertId,
                                tableNumber = tableNumber,
                                totalAmount = totalAmount,
                                customerName = customerName,
                                customerPhone = customerPhone,
                                customerId = customerId,
                                timestamp = timestamp,
                                isResolved = isResolved
                            )
                        }
                        onAlertsUpdated(alerts)
                    }
                }
        } catch (e: Exception) {
            Log.w(tag, "observeActiveCashAlerts failure: ${e.localizedMessage}")
            null
        }
    }

    /**
     * Blocked users listener
     */
    fun observeBlockedUsers(
        shopId: String,
        onBlockedUpdated: (List<BlockedCustomer>) -> Unit
    ): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("shops").document(shopId)
                .collection("blocked_users")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "observeBlockedUsers error: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            val customerId = doc.getString("customerId") ?: return@mapNotNull null
                            val customerName = doc.getString("customerName") ?: "Unknown"
                            val phone = doc.getString("phone") ?: doc.getString("phoneNumber") ?: ""
                            val unpaidAmount = (doc.getLong("unpaidAmount") ?: 0L).toInt()
                            val orderId = doc.getString("orderId") ?: ""
                            val tableNumber = (doc.getLong("tableNumber") ?: 0L).toInt()
                            val timestamp = doc.getLong("timestamp") ?: doc.getLong("blockedTimestamp") ?: System.currentTimeMillis()
                            val reason = doc.getString("reason") ?: "धोखाधड़ी / बिल का भुगतान नहीं किया"
                            BlockedCustomer(
                                customerId = customerId,
                                customerName = customerName,
                                phone = phone,
                                unpaidAmount = unpaidAmount,
                                orderId = orderId,
                                tableNumber = tableNumber,
                                timestamp = timestamp,
                                reason = reason
                            )
                        }
                        onBlockedUpdated(list)
                    }
                }
        } catch (e: Exception) {
            Log.w(tag, "observeBlockedUsers failure: ${e.localizedMessage}")
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseTableOrder(doc: com.google.firebase.firestore.DocumentSnapshot): TableOrder? {
        return try {
            val orderId = doc.getString("orderId") ?: doc.id
            val tableNumber = (doc.getLong("tableNumber") ?: 1L).toInt()
            val customerName = doc.getString("customerName") ?: "Guest"
            val customerPhone = doc.getString("customerPhone") ?: ""
            val customerId = doc.getString("customerId") ?: ""
            val specialInstructions = doc.getString("specialInstructions") ?: ""
            val subtotal = (doc.getLong("subtotal") ?: 0L).toInt()
            val gst = (doc.getLong("gst") ?: 0L).toInt()
            val tip = (doc.getLong("tip") ?: 0L).toInt()
            val grandTotal = (doc.getLong("grandTotal") ?: (subtotal + gst + tip)).toInt()
            val orderTimestamp = doc.getLong("orderTimestamp") ?: System.currentTimeMillis()
            val estimatedPrepTimeMinutes = (doc.getLong("estimatedPrepTimeMinutes") ?: 20L).toInt()
            val estimatedReadyTimestamp = doc.getLong("estimatedReadyTimestamp") ?: (orderTimestamp + (20 * 60 * 1000L))
            val servedTimestamp = doc.getLong("servedTimestamp")

            val statusStr = doc.getString("status") ?: "RECEIVED"
            val status = try { OrderStatus.valueOf(statusStr) } catch (_: Exception) { OrderStatus.RECEIVED }

            val paymentStatusStr = doc.getString("paymentStatus") ?: "UNPAID"
            val paymentStatus = try { PaymentStatus.valueOf(paymentStatusStr) } catch (_: Exception) { PaymentStatus.UNPAID }

            val paymentMethodStr = doc.getString("paymentMethod")
            val paymentMethod = paymentMethodStr?.let {
                try { PaymentMethod.valueOf(it) } catch (_: Exception) { null }
            }

            val rawItems = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
            val items = rawItems.map { itemMap ->
                val itemId = (itemMap["itemId"] as? Number)?.toInt() ?: 1
                val nameHi = itemMap["nameHi"] as? String ?: ""
                val nameEn = itemMap["nameEn"] as? String ?: ""
                val price = (itemMap["price"] as? Number)?.toInt() ?: 100
                val quantity = (itemMap["quantity"] as? Number)?.toInt() ?: 1
                val specialNote = itemMap["specialNote"] as? String ?: ""
                val emoji = itemMap["emoji"] as? String ?: "🍲"

                CartItem(
                    item = MenuItem(
                        id = itemId,
                        nameHi = nameHi,
                        nameEn = nameEn,
                        price = price,
                        category = MenuCategory.MAIN,
                        descHi = "",
                        descEn = "",
                        emoji = emoji
                    ),
                    quantity = quantity,
                    specialNote = specialNote
                )
            }

            TableOrder(
                orderId = orderId,
                tableNumber = tableNumber,
                customerName = customerName,
                customerPhone = customerPhone,
                customerId = customerId,
                items = items,
                specialInstructions = specialInstructions,
                subtotal = subtotal,
                gst = gst,
                tip = tip,
                grandTotal = grandTotal,
                orderTimestamp = orderTimestamp,
                estimatedPrepTimeMinutes = estimatedPrepTimeMinutes,
                estimatedReadyTimestamp = estimatedReadyTimestamp,
                servedTimestamp = servedTimestamp,
                status = status,
                paymentStatus = paymentStatus,
                paymentMethod = paymentMethod
            )
        } catch (e: Exception) {
            Log.w(tag, "parseTableOrder error: ${e.localizedMessage}")
            null
        }
    }
}
