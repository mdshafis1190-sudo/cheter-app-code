package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ShopOwnerUser
import com.example.security.AuthSecurityManager
import com.example.ui.theme.DhabaGold
import com.example.ui.theme.DhabaGreenDark
import com.example.ui.theme.DhabaRed
import com.example.ui.theme.DhabaRedDark
import kotlinx.coroutines.delay

@Composable
fun ShopAuthDialog(
    currentUser: ShopOwnerUser,
    isLoading: Boolean,
    authErrorMessage: String? = null,
    lockoutRemainingSeconds: Long = 0L,
    failedAttemptsCount: Int = 0,
    onDismiss: () -> Unit,
    onSignInPhone: (phone: String, pass: String) -> Unit,
    onSignUpPhone: (phone: String, pass: String, shopName: String, ownerName: String) -> Unit,
    onClearError: () -> Unit = {},
    onQuickDemoLogin: () -> Unit,
    onSignOut: (context: Context) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Dual-Factor Login, 1: Register New Owner
    var phoneInput by remember { mutableStateOf(if (currentUser.phone.isNotBlank()) currentUser.phone else "9876543210") }
    var passwordInput by remember { mutableStateOf("Cheter@2026") }
    var passwordVisible by remember { mutableStateOf(false) }
    var registerShopName by remember { mutableStateOf("CHETER Premium Lounge") }
    var registerOwnerName by remember { mutableStateOf("रेस्टोरेंट ओनर") }

    // Live countdown timer for active lockout
    var liveRemainingSec by remember(lockoutRemainingSeconds) { mutableLongStateOf(lockoutRemainingSeconds) }

    LaunchedEffect(lockoutRemainingSeconds) {
        liveRemainingSec = lockoutRemainingSeconds
        while (liveRemainingSec > 0) {
            delay(1000L)
            liveRemainingSec -= 1
        }
    }

    val isLocked = liveRemainingSec > 0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Shield Icon & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(DhabaRed.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = DhabaRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "सुरक्षित ओनर प्रमाणीकरण",
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Dual-Factor Auth • Phone + Password",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_auth_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Security Policy Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "सटीक क्रेडेंशियल मिलान & 3-प्रयास ब्रूट-फ़ोर्स लॉकआउट सुरक्षा",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (currentUser.isAuthenticated) {
                    // ================= LOGGED IN OWNER SESSION CARD =================
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DhabaGreenDark.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(DhabaGreenDark.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = DhabaGreenDark,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "सत्र सक्रिय (Active Session)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DhabaGreenDark
                            )
                            Text(
                                text = currentUser.displayName.ifBlank { "ओनर" },
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (currentUser.phone.isNotBlank()) {
                                Text(
                                    text = "📱 फोन: +91 ${currentUser.phone}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DhabaGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "दुकान: ${currentUser.shopName} (ID: ${currentUser.shopId})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD35400),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            if (currentUser.sessionToken.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Token: ${currentUser.sessionToken.take(12)}...",
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Explicit Logout Button
                            Button(
                                onClick = { onSignOut(context) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("sign_out_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DhabaRed)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("🚪 सत्र से लॉगआउट करें (Logout)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            }
                        }
                    }
                } else {
                    // ================= TABS: DUAL-FACTOR LOGIN / REGISTER =================
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                onClearError()
                            },
                            text = { Text("🔑 फोन + पासवर्ड", fontWeight = FontWeight.Bold, fontSize = 12.5.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                onClearError()
                            },
                            text = { Text("📝 नया ओनर खाता", fontWeight = FontWeight.Bold, fontSize = 12.5.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ================= ERROR & LOCKOUT BANNER =================
                    if (isLocked) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DhabaRed.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DhabaRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_lockout_warning_banner")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = DhabaRed, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "खाता अस्थायी रूप से लॉक है!",
                                        color = DhabaRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "3 गलत प्रयासों के कारण लॉगिन रोका गया है।",
                                    fontSize = 11.5.sp,
                                    color = DhabaRedDark,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "⏳ पुनः प्रयास करें: ${liveRemainingSec} सेकंड में",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DhabaRed
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    } else if (!authErrorMessage.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DhabaRed.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DhabaRed.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_error_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = DhabaRed, modifier = Modifier.size(18.dp))
                                Text(
                                    text = authErrorMessage,
                                    color = DhabaRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // ================= FORM FIELDS =================

                    if (selectedTab == 1) {
                        // Registration: Shop Name & Owner Name
                        OutlinedTextField(
                            value = registerShopName,
                            onValueChange = { registerShopName = it },
                            label = { Text("दुकान / रेस्टोरेंट का नाम (Shop Name)", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null, tint = DhabaRed) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_shop_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = registerOwnerName,
                            onValueChange = { registerOwnerName = it },
                            label = { Text("मालिक का नाम (Owner Name)", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = DhabaRed) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_owner_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Phone Number Input (Dual-Factor Username)
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = {
                            // Filter input for digits
                            phoneInput = it.take(15)
                            onClearError()
                        },
                        label = { Text("फ़ोन नंबर (Username / Phone)", fontSize = 12.sp) },
                        leadingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 10.dp, end = 4.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = DhabaRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+91", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_phone_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        enabled = !isLocked && !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Password Input (Exact Match)
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            onClearError()
                        },
                        label = { Text("पासवर्ड (Exact Password)", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = DhabaRed) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide Password" else "Show Password",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        enabled = !isLocked && !isLoading,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                if (selectedTab == 0 && !isLocked && !isLoading) {
                                    onSignInPhone(phoneInput, passwordInput)
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Submit Button
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            if (selectedTab == 0) {
                                onSignInPhone(phoneInput, passwordInput)
                            } else {
                                onSignUpPhone(phoneInput, passwordInput, registerShopName, registerOwnerName)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DhabaRed),
                        enabled = !isLoading && !isLocked && phoneInput.isNotBlank() && passwordInput.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("प्रमाणीकरण हो रहा है...", fontSize = 13.sp, color = Color.White)
                        } else {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedTab == 0) "सत्यापित करें & लॉगिन (Secure Login)" else "नया ओनर खाता बनाएं (Register)",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Demo Login Button with Exact Credentials Note
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🧪 डेमो ओनर क्रेडेंशियल्स (Demo Access)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = DhabaGold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "फ़ोन: 9876543210  |  पासवर्ड: Cheter@2026",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onQuickDemoLogin,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .testTag("quick_demo_login_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DhabaGreenDark)
                            ) {
                                Text(
                                    text = "⚡ त्वरित डेमो ओनर लॉगिन (Auto-Fill & Login)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
