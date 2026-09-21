package com.lastasylum.automation.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import com.lastasylum.automation.ScreenAnalyzer
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * Saves screenshots with OCR results for debugging.
 * Useful for verifying OCR accuracy and game state detection.
 *
 * Screenshots are saved to: /sdcard/LastAsylumAutomation/
 */
object ScreenshotLogger {
    private const val TAG = "ScreenshotLogger"
    private const val MAX_SCREENSHOTS = 100

    private val screenshotCounter = AtomicInteger(0)
    private var logDir: File? = null

    fun init(context: Context) {
        val dir = File(context.getExternalFilesDir(null), "screenshots")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        logDir = dir
        pruneOldFiles()
    }

    /**
     * Save a screenshot with OCR text for debugging.
     * @param screenshot The captured bitmap
     * @param ocrText The OCR-extracted text from this frame
     * @param moduleId Current active module
     */
    fun logScreenshot(screenshot: Bitmap, ocrText: String, moduleId: String) {
        val dir = logDir ?: return
        val count = screenshotCounter.incrementAndGet()
        if (count > MAX_SCREENSHOTS) {
            screenshotCounter.set(MAX_SCREENSHOTS)
            return
        }

        val timeFormat = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US)
        val filename = "LAS_${moduleId}_${timeFormat.format(Date())}_$count.png"
        val file = File(dir, filename)

        try {
            FileOutputStream(file).use { out ->
                screenshot.compress(Bitmap.CompressFormat.PNG, 85, out)
            }
            // Also save metadata
            val metaFile = File(dir, filename.replace(".png", ".txt"))
            FileOutputStream(metaFile).use { out ->
                out.write("Screenshot #$count\n".toByteArray())
                out.write("Module: $moduleId\n".toByteArray())
                out.write("Timestamp: ${System.currentTimeMillis()}\n".toByteArray())
                out.write("OCR Text:\n$ocrText".toByteArray())
            }
            Log.d(TAG, "Saved screenshot: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save screenshot: ${e.message}")
        }
    }

    private fun pruneOldFiles() {
        val dir = logDir ?: return
        val files = dir.listFiles() ?: return
        if (files.size > MAX_SCREENSHOTS * 2) {
            files.sortBy { it.lastModified() }
            val toDelete = files.take(files.size - MAX_SCREENSHOTS)
            for (f in toDelete) {
                f.delete()
            }
        }
    }

    fun getScreenshotDir(): File? = logDir
}
