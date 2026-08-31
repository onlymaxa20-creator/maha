package com.example.data.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

data class ImageValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val sizeBytes: Long = 0,
    val mimeType: String? = null
)

object ImageStorageHelper {

    private const val MAX_IMAGE_SIZE_BYTES = 15 * 1024 * 1024L // 15 MB
    private const val MIN_IMAGE_SIZE_BYTES = 100L // 100 Bytes

    /**
     * Validates that the selected URI points to a real, decodable image within allowable size constraints.
     */
    fun validateImageFile(context: Context, uri: Uri): ImageValidationResult {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: ""

            // Check if MIME type is an image
            val isImageMime = mimeType.startsWith("image/", ignoreCase = true) ||
                    mimeType.isEmpty() // Some file pickers return empty MIME, decode check below will verify

            // Check file size
            var fileSize: Long = 0
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        fileSize = it.getLong(sizeIndex)
                    }
                }
            }

            if (fileSize > MAX_IMAGE_SIZE_BYTES) {
                val sizeMb = String.format("%.1f", fileSize / (1024.0 * 1024.0))
                return ImageValidationResult(
                    isValid = false,
                    errorMessage = "Rasm hajmi juda katta ($sizeMb MB). Maksimal ruxsat etilgan hajm: 15 MB",
                    sizeBytes = fileSize,
                    mimeType = mimeType
                )
            }

            // Verify image decodability and get dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            val checkStream: InputStream? = contentResolver.openInputStream(uri)
            if (checkStream == null) {
                return ImageValidationResult(
                    isValid = false,
                    errorMessage = "Faylni ochib bo‘lmadi. Qaytadan tanlang."
                )
            }

            checkStream.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return ImageValidationResult(
                    isValid = false,
                    errorMessage = "Tanlangan fayl haqiqiy rasm emas yoki buzilgan."
                )
            }

            ImageValidationResult(
                isValid = true,
                errorMessage = null,
                width = options.outWidth,
                height = options.outHeight,
                sizeBytes = fileSize,
                mimeType = if (mimeType.isNotBlank()) mimeType else options.outMimeType ?: "image/jpeg"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ImageValidationResult(
                isValid = false,
                errorMessage = "Rasmni tekshirishda xatolik: ${e.localizedMessage ?: "Noma’lum xato"}"
            )
        }
    }

    /**
     * Saves a classified ad image picked from the gallery into persistent internal storage.
     * Returns a persistable file URI string (e.g. file:///data/user/0/.../ad_123.jpg)
     */
    fun saveAdImage(context: Context, sourceUri: Uri): Result<String> {
        return try {
            val validation = validateImageFile(context, sourceUri)
            if (!validation.isValid) {
                return Result.failure(IllegalArgumentException(validation.errorMessage ?: "Yaroqsiz rasm fayli"))
            }

            val adsDir = File(context.filesDir, "ad_images")
            if (!adsDir.exists()) {
                adsDir.mkdirs()
            }

            val extension = when {
                validation.mimeType?.contains("png", true) == true -> "png"
                validation.mimeType?.contains("webp", true) == true -> "webp"
                else -> "jpg"
            }

            val fileName = "ad_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.$extension"
            val destFile = File(adsDir, fileName)

            val inputStream: InputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return Result.failure(IllegalStateException("Fayl oqimi ochilmadi"))

            val outputStream = FileOutputStream(destFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            val savedUri = Uri.fromFile(destFile).toString()
            Result.success(savedUri)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Saves a banner image picked from the gallery into persistent internal storage.
     * Returns a persistable file URI string (e.g. file:///data/user/0/.../banner_123.jpg)
     */
    fun saveBannerImage(context: Context, sourceUri: Uri): Result<String> {
        return try {
            val validation = validateImageFile(context, sourceUri)
            if (!validation.isValid) {
                return Result.failure(IllegalArgumentException(validation.errorMessage ?: "Yaroqsiz rasm fayli"))
            }

            val bannersDir = File(context.filesDir, "banner_images")
            if (!bannersDir.exists()) {
                bannersDir.mkdirs()
            }

            val extension = when {
                validation.mimeType?.contains("png", true) == true -> "png"
                validation.mimeType?.contains("webp", true) == true -> "webp"
                else -> "jpg"
            }

            val fileName = "banner_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.$extension"
            val destFile = File(bannersDir, fileName)

            val inputStream: InputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return Result.failure(IllegalStateException("Fayl oqimi ochilmadi"))

            val outputStream = FileOutputStream(destFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            val savedUri = Uri.fromFile(destFile).toString()
            Result.success(savedUri)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Compresses an image picked by the user and encodes it as a web-safe Base64 data URI string.
     * This allows images to be safely synchronized across all devices via Firebase Firestore,
     * so other users and Admins on different phones can see the product image clearly without broken local file paths.
     */
    fun compressAndEncodeImageToBase64(context: Context, sourceUri: Uri, maxDimension: Int = 600, quality: Int = 75): String? {
        return try {
            val contentResolver = context.contentResolver
            
            // First decode bounds
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(sourceUri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return null
            }
            
            // Calculate inSampleSize for optimal memory usage
            var sampleSize = 1
            val maxOut = maxOf(options.outWidth, options.outHeight)
            while (maxOut / (sampleSize * 2) >= maxDimension) {
                sampleSize *= 2
            }
            
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
            }
            
            val originalBitmap = contentResolver.openInputStream(sourceUri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: return null
            
            // Scale to exact target dimensions
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scaleFactor = if (maxOf(width, height) > maxDimension) {
                maxDimension.toFloat() / maxOf(width, height).toFloat()
            } else {
                1.0f
            }
            
            val scaledBitmap = if (scaleFactor < 1.0f) {
                val targetW = (width * scaleFactor).toInt().coerceAtLeast(1)
                val targetH = (height * scaleFactor).toInt().coerceAtLeast(1)
                android.graphics.Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
            } else {
                originalBitmap
            }
            
            val outputStream = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, outputStream)
            val bytes = outputStream.toByteArray()
            val base64String = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            
            // Also save locally as cache fallback
            try {
                saveImageFromUri(context, sourceUri)
            } catch (_: Exception) {}
            
            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to local URI if base64 conversion fails
            saveImageFromUri(context, sourceUri)
        }
    }

    /**
     * Copies an image picked from the local gallery / document picker to internal app storage for products.
     */
    fun saveImageFromUri(context: Context, sourceUri: Uri): String? {
        return try {
            val imagesDir = File(context.filesDir, "product_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val fileName = "product_${System.currentTimeMillis()}_${(100..999).random()}.jpg"
            val destFile = File(imagesDir, fileName)

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            val outputStream = FileOutputStream(destFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Normalizes image URLs including complex Google Images search results, Google Drive, and web links.
     * Extracts direct image sources so Coil and image components can display them instantly.
     */
    fun normalizeImageUrl(inputUrl: String?): String {
        if (inputUrl.isNullOrBlank()) return ""
        var url = inputUrl.trim()

        // Strip surrounding quotes if copied with quotation marks
        if ((url.startsWith("\"") && url.endsWith("\"")) || (url.startsWith("'") && url.endsWith("'"))) {
            url = url.substring(1, url.length - 1).trim()
        }

        // 1. Google Images Search Result URL (e.g. https://www.google.com/imgres?imgurl=... or https://google.uz/imgres?...)
        if (url.contains("google.") && (url.contains("/imgres") || url.contains("imgurl="))) {
            try {
                val uri = Uri.parse(url)
                val extractedImgUrl = uri.getQueryParameter("imgurl")
                if (!extractedImgUrl.isNullOrBlank()) {
                    val decoded = java.net.URLDecoder.decode(extractedImgUrl, "UTF-8")
                    return normalizeImageUrl(decoded)
                }
            } catch (_: Exception) {}
        }

        // 2. Google Redirect / Click URL (e.g. https://www.google.com/url?q=... or url=...)
        if (url.contains("google.") && url.contains("/url?")) {
            try {
                val uri = Uri.parse(url)
                val qParam = uri.getQueryParameter("q") ?: uri.getQueryParameter("url")
                if (!qParam.isNullOrBlank()) {
                    val decoded = java.net.URLDecoder.decode(qParam, "UTF-8")
                    return normalizeImageUrl(decoded)
                }
            } catch (_: Exception) {}
        }

        // 3. Google Drive file URL (e.g. https://drive.google.com/file/d/FILE_ID/view or /open?id=FILE_ID or /uc?id=FILE_ID)
        if (url.contains("drive.google.com")) {
            try {
                val fileId = when {
                    url.contains("/file/d/") -> {
                        url.substringAfter("/file/d/").substringBefore("/").substringBefore("?")
                    }
                    url.contains("id=") -> {
                        Uri.parse(url).getQueryParameter("id") ?: ""
                    }
                    else -> ""
                }
                if (fileId.isNotBlank()) {
                    return "https://lh3.googleusercontent.com/d/$fileId"
                }
            } catch (_: Exception) {}
        }

        // 4. Google Photos / Google User Content
        if (url.contains("googleusercontent.com")) {
            if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
                url = "https://$url"
            }
            return url
        }

        // 5. General Web cleanup (decode &amp; into &)
        if (url.contains("&amp;")) {
            url = url.replace("&amp;", "&")
        }

        if (url.startsWith("//")) {
            url = "https:$url"
        } else if (url.startsWith("www.", ignoreCase = true)) {
            url = "https://$url"
        }

        return url
    }

    private val bitmapCache = android.util.LruCache<String, android.graphics.Bitmap>(30)

    /**
     * Resolves an image URI / Base64 string into a Coil or Compose compatible image model (Bitmap, File, Uri, or URL).
     * Handles:
     * - "data:image/...;base64,..."
     * - Raw Base64 string
     * - "http://..." / "https://..." (including Google search image results, Google Drive, Google Photos)
     * - "file://..." or local absolute file paths
     * - "content://..."
     * - "android.resource://..."
     */
    fun resolveImageModel(imageUri: String?, context: Context): Any? {
        if (imageUri.isNullOrBlank()) return null

        val trimmed = imageUri.trim()

        // 1. Data URI with Base64
        if (trimmed.startsWith("data:image/", ignoreCase = true) && trimmed.contains("base64,", ignoreCase = true)) {
            val cached = bitmapCache.get(trimmed)
            if (cached != null && !cached.isRecycled) return cached

            return try {
                val base64Data = trimmed.substringAfter("base64,").trim()
                val decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bitmap != null) {
                    bitmapCache.put(trimmed, bitmap)
                    bitmap
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        // 2. Web URL (Google Image links, Drive, Web images normalized)
        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true) || trimmed.contains("google.") || trimmed.contains("googleusercontent.com")) {
            return normalizeImageUrl(trimmed)
        }

        // 3. Android Content or Resource URI
        if (trimmed.startsWith("content://", ignoreCase = true) || trimmed.startsWith("android.resource://", ignoreCase = true)) {
            return Uri.parse(trimmed)
        }

        // 3.5 Embedded drawable resource reference (e.g., "drawable:img_headphones_1787475184856" or "img_...")
        if (trimmed.startsWith("drawable:", ignoreCase = true) || trimmed.startsWith("img_", ignoreCase = true) || trimmed.startsWith("ic_", ignoreCase = true)) {
            val resName = trimmed.removePrefix("drawable:").trim()
            val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
            if (resId != 0) {
                return resId
            }
        }

        // 4. File URI or local absolute path
        if (trimmed.startsWith("file://", ignoreCase = true)) {
            val path = trimmed.removePrefix("file://")
            val file = File(path)
            if (file.exists() && file.canRead()) {
                return file
            }
        } else if (trimmed.startsWith("/")) {
            val file = File(trimmed)
            if (file.exists() && file.canRead()) {
                return file
            }
        }

        // 5. Plain Base64 string without data: header (if lengthy and base64 valid)
        if (trimmed.length > 50 && !trimmed.contains(" ") && !trimmed.contains("\n")) {
            val cached = bitmapCache.get(trimmed)
            if (cached != null && !cached.isRecycled) return cached

            return try {
                val decodedBytes = android.util.Base64.decode(trimmed, android.util.Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bitmap != null) {
                    bitmapCache.put(trimmed, bitmap)
                    bitmap
                } else null
            } catch (e: Exception) {
                // Not valid base64
                null
            }
        }

        return trimmed
    }

    /**
     * Safely deletes an image file from app storage if it is stored locally.
     */
    fun deleteImageFile(fileUriString: String): Boolean {
        return try {
            if (fileUriString.startsWith("file://")) {
                val path = fileUriString.removePrefix("file://")
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                } else false
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

