package com.ipb.simpt.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object FileHelper {

    fun uriToFile(selectedImg: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = createTempFile(context)
        val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()
        return myFile
    }

    private fun createTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        // Use System.currentTimeMillis() to generate a unique file name
        val timeStamp: String = System.currentTimeMillis().toString()
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    fun reduceFileImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var compressQuality = 100
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > 1000000)
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
        return file
    }

    fun formatTimeStamp (timestamp: Long) : String{
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = timestamp
        // dd/MM/yyyy
        return android.text.format.DateFormat.format("dd/MM/yyyy", cal).toString()
    }

    fun getStartOfDayInMillis(dateString: String?): Long {
        if (dateString.isNullOrEmpty()) {
            // Return a default timestamp if dateString is null or empty
            // This timestamp could be the epoch start or a very old date
            return 0L
        }
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val date = sdf.parse(dateString)
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }


    fun getEndOfDayInMillis(dateString: String): Long {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val date = sdf.parse(dateString)
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}