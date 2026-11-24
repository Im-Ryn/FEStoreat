package com.example.food_shelf_life_predictor.core.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import kotlin.math.max

object ImageStorage {
    private const val MAX_SIDE = 1280
    private const val MAX_BYTES = 5 * 1024 * 1024   // batas 5 MB sesuai kontrak

    /** Uri sementara tempat kamera menulis foto. */
    fun createCameraUri(context: Context): Uri {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File.createTempFile("capture_", ".jpg", dir)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /** Kompres foto lalu simpan permanen. Mengembalikan path file. Panggil dari thread IO. */
    fun saveCompressed(context: Context, source: Uri): String {
        val resolver = context.contentResolver

        // 1. Baca ukuran asli tanpa memuat bitmap
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }

        // 2. Decode dengan sampling supaya hemat memori
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > MAX_SIDE * 2) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = resolver.openInputStream(source)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: error("Gagal membaca gambar")

        // 3. Perbaiki rotasi (foto kamera sering miring) dan perkecil
        val rotation = resolver.openInputStream(source)?.use { stream ->
            when (ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
        val scale = minOf(1f, MAX_SIDE.toFloat() / max(decoded.width, decoded.height))
        val matrix = Matrix().apply {
            postRotate(rotation)
            postScale(scale, scale)
        }
        val bitmap = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)

        // 4. Kompres JPEG, turunkan kualitas sampai di bawah 5 MB
        var quality = 90
        val out = ByteArrayOutputStream()
        do {
            out.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            quality -= 10
        } while (out.size() > MAX_BYTES && quality >= 40)

        // 5. Simpan di penyimpanan internal aplikasi
        val dir = File(context.filesDir, "food_photos").apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        file.writeBytes(out.toByteArray())
        return file.absolutePath
    }

    fun delete(path: String?) {
        if (path != null) File(path).delete()
    }
}