package com.lastasylum.automation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.text.TextRecognizer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

/**
 * Captures the game screen and runs OCR (ML Kit) to produce a [UiState]
 * describing every visible UI element, its position and confidence.
 */
class ScreenAnalyzer(private val context: Context) {

    companion object {
        private const val TAG = "ScreenAnalyzer"
    }

    private val recognizer: TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var mediaProjection: MediaProjection? = null

    suspend fun captureScreen(): Bitmap? {
        if (mediaProjection == null) {
            Log.d(TAG, "No active MediaProjection; trigger picker")
            return null
        }
        return try {
            createImageBitmap(mediaProjection!!)
        } catch (e: Exception) {
            Log.e(TAG, "Screen capture failed: ${e.message}")
            null
        }
    }

    suspend fun analyzeUI(bitmap: Bitmap): UiState? {
        return withContext(Dispatchers.Default) {
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                val result = recognizer.process(image).await()

                val elements = result.textBlocks.map { block ->
                    val rect: Rect = block.boundingBox ?: Rect(0, 0, 0, 0)
                    UiState.Element(
                        text = block.text.trim(),
                        x = rect.left.toFloat(),
                        y = rect.top.toFloat(),
                        width = rect.width().toFloat(),
                        height = rect.height().toFloat(),
                        confidence = 1.0f,
                        elementType = mapElementType(block.text)
                    )
                }

                UiState(
                    screenshotWidth = bitmap.width,
                    screenshotHeight = bitmap.height,
                    elements = elements,
                    rawText = result.text
                )
            } catch (e: Exception) {
                Log.e(TAG, "OCR failed: ${e.message}")
                null
            }
        }
    }

    fun attachMediaProjection(projection: MediaProjection) {
        mediaProjection = projection
    }

    fun detachMediaProjection() {
        mediaProjection?.stop()
        mediaProjection = null
    }

    private suspend fun createImageBitmap(projection: MediaProjection): Bitmap? {
        val density = context.resources.displayMetrics.densityDpi
        val width = 1080
        val height = 1920

        val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        val handler = Handler(Looper.getMainLooper())
        projection.createVirtualDisplay(
            "LostAsylumCapture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface, null, handler
        )

        val result = CompletableDeferred<Bitmap?>()
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage() ?: run {
                result.complete(null)
                return@setOnImageAvailableListener
            }
            try {
                val buffer = image.planes[0].buffer
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.copyPixelsFromBuffer(buffer)
                result.complete(bitmap)
            } catch (e: Exception) {
                result.complete(null)
            } finally {
                image.close()
            }
        }, handler)

        return result.await()
    }

    private fun mapElementType(text: String): UiState.Element.ElementType {
        val upper = text.uppercase()
        return when {
            upper.contains("BUTTON") || upper.contains("CONFIRM") || upper.contains("OK") ->
                UiState.Element.ElementType.BUTTON
            upper.contains("CITY") || upper.contains("BIOME") || upper.contains("TAB") ->
                UiState.Element.ElementType.TAB
            upper.contains("ITEM") || upper.contains("RESOURCE") || upper.contains("SCROLL") ->
                UiState.Element.ElementType.LIST_ITEM
            else -> UiState.Element.ElementType.UNKNOWN
        }
    }
}
