package com.signagepro.app.core.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import javax.inject.Inject

interface QrCodeGenerator {
    fun generateQrCode(content: String, width: Int = 512, height: Int = 512): Bitmap?
}

class QrCodeGeneratorImpl @Inject constructor() : QrCodeGenerator {
    override fun generateQrCode(content: String, width: Int, height: Int): Bitmap? {
        if (content.isEmpty()) return null
        val writer = QRCodeWriter()
        return try {
            val bitMatrix = writer.encode(
                content,
                BarcodeFormat.QR_CODE,
                width,
                height,
                mapOf(EncodeHintType.MARGIN to 1) // Small margin
            )
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}