package com.example

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.CheterCyan
import com.example.ui.theme.CheterPurple
import com.example.ui.theme.DhabaGold
import com.example.ui.theme.DhabaGreen
import com.example.ui.theme.DhabaRed
import com.example.ui.theme.MyApplicationTheme
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class CheckoutActivity : ComponentActivity() {

    companion object {
        const val EXTRA_OWNER_UPI_ID = "extra_owner_upi_id"
        const val EXTRA_TOTAL_AMOUNT = "extra_total_amount"
        const val EXTRA_TABLE_NUMBER = "extra_table_number"
        const val EXTRA_HOTEL_ID = "extra_hotel_id"

        fun createIntent(
            context: Context,
            ownerUpiId: String,
            totalAmount: Double,
            tableNumber: Int = 1,
            hotelId: String = "hotel1"
        ): Intent {
            return Intent(context, CheckoutActivity::class.java).apply {
                putExtra(EXTRA_OWNER_UPI_ID, ownerUpiId)
                putExtra(EXTRA_TOTAL_AMOUNT, totalAmount)
                putExtra(EXTRA_TABLE_NUMBER, tableNumber)
                putExtra(EXTRA_HOTEL_ID, hotelId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ownerUpiId = intent.getStringExtra(EXTRA_OWNER_UPI_ID)?.ifBlank { "cheter.dine@okhdfcbank" } ?: "cheter.dine@okhdfcbank"
        val totalAmount = intent.getDoubleExtra(EXTRA_TOTAL_AMOUNT, 0.0)
        val tableNumber = intent.getIntExtra(EXTRA_TABLE_NUMBER, 1)

        // Core logic: Generate UPI QR bitmap using BarcodeEncoder
        val upiUri = "upi://pay?pa=$ownerUpiId&pn=CheterApp&am=$totalAmount&cu=INR"
        val qrBitmap: Bitmap? = try {
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.encodeBitmap(upiUri, BarcodeFormat.QR_CODE, 512, 512)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        setContent {
            MyApplicationTheme {
                CheckoutScreenContent(
                    ownerUpiId = ownerUpiId,
                    totalAmount = totalAmount,
                    tableNumber = tableNumber,
                    upiUri = upiUri,
                    qrBitmap = qrBitmap,
                    onBackClick = { finish() },
                    onPaymentComplete = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreenContent(
    ownerUpiId: String,
    totalAmount: Double,
    tableNumber: Int,
    upiUri: String,
    qrBitmap: Bitmap?,
    onBackClick: () -> Unit,
    onPaymentComplete: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Table $tableNumber Checkout",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("checkout_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E293B)
                )
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Scan & Pay via any UPI App",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DhabaGold
                        )
                        Text(
                            text = "Google Pay • PhonePe • Paytm • BHIM",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // QR Code container rendering inside ImageView via AndroidView / Image
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .border(3.dp, CheterCyan, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (qrBitmap != null) {
                                // Native Android ImageView rendering using BarcodeEncoder output
                                AndroidView(
                                    factory = { ctx ->
                                        ImageView(ctx).apply {
                                            scaleType = ImageView.ScaleType.FIT_CENTER
                                            setImageBitmap(qrBitmap)
                                        }
                                    },
                                    update = { imageView ->
                                        imageView.setImageBitmap(qrBitmap)
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("checkout_qr_imageview")
                                )
                            } else {
                                Text(
                                    text = "Unable to generate QR code",
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "₹%.2f".format(totalAmount),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "UPI ID: $ownerUpiId",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = CheterCyan
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onPaymentComplete,
                    colors = ButtonDefaults.buttonColors(containerColor = DhabaGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("checkout_paid_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "I Have Completed Payment",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = onBackClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("checkout_cancel_button")
                ) {
                    Text("Cancel / Back to Menu")
                }
            }
        }
    }
}
