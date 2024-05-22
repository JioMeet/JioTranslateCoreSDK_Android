package com.jio.jiotranslatecoresdk

import android.content.Context
import android.os.Environment
import java.io.File

object FileUtility {

    fun getFileDirectory(context: Context): File {
        val filesDir = context.filesDir.apply { mkdirs() }
        return getApplicationPublicDirectory(context) ?: filesDir
    }

    private fun getApplicationPublicDirectory(context: Context): File? {
        return try {
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.apply {
                mkdirs()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }
}