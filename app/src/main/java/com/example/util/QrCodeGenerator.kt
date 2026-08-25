package com.example.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.util.Locale

object QrCodeGenerator {

    /**
     * Constructs a UPI Payment URI with standard parameters.
     * Format: upi://pay?pa=$ownerUpiId&pn=CheterApp&am=$totalAmount&cu=INR
     */
    fun createUpiUri(
        ownerUpiId: String,
        totalAmount: Number,
        payeeName: String = "CheterApp"
    ): String {
        val cleanUpi = ownerUpiId.ifBlank { "cheter.dine@okhdfcbank" }
        val formattedAmount = String.format(Locale.US, "%.2f", totalAmount.toDouble())
        return "upi://pay?pa=$cleanUpi&pn=$payeeName&am=$formattedAmount&cu=INR"
    }

    /**
     * Generates a QR Code Bitmap locally using ZXing BarcodeEncoder.
     * @param content The URI string (e.g. upi://pay?pa=$ownerUpiId&pn=CheterApp&am=$totalAmount&cu=INR)
     * @param size The width and height in pixels (default 512)
     */
    fun generateQrBitmap(
        content: String,
        size: Int = 512
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, size, size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
