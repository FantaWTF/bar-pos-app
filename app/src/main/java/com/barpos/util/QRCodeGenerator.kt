package com.barpos.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.net.URLEncoder

object QRCodeGenerator {

    fun generateMobilePayQR(
        phoneNumber: String,
        amount: Double,
        comment: String,
        size: Int = 512
    ): Bitmap? {
        val encodedComment = URLEncoder.encode(comment, "UTF-8")
        val amountFormatted = "%.2f".format(amount)
        val deepLink = "mobilepay://send?phone=$phoneNumber&amount=$amountFormatted&comment=$encodedComment"
        return generateQRBitmap(deepLink, size)
    }

    private fun generateQRBitmap(content: String, size: Int): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1
            )
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
