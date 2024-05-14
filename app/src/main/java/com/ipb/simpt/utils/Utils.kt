package com.ipb.simpt.utils

import android.app.Application
import android.text.format.DateFormat
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Locale

class Utils : Application() {
    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        // create a static method to convert timestamp to proper date format
        fun formatTimeStamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp

            // format dd//MM/yyyy
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        fun loadDataFromUrl(
            dataUrl: String,
            imageView: ImageView
        ) {
            val TAG = "DATA_THUMBNAIL_TAG"

            // using url to get file and its metadata from firebase storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(dataUrl)
            ref.metadata
                .addOnSuccessListener { storageMetaData ->
                    Log.d(TAG, "loadDataFromUrl: got metadata")

                    // set to imageView
                    Glide.with(imageView.context)
                        .load(dataUrl)
                        .into(imageView)
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "loadDataFromUrl: Failed to get metadata due to ${e.message}")
                }
        }

        fun loadCategory(categoryId: String, tvCategory: TextView) {
            // load category using category id from Firestore
            val TAG = "DATA_CATEGORY_TAG"
            val db = FirebaseFirestore.getInstance()
            db.collection("Categories")
                .document(categoryId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                        // get category
                        val category = document.getString("category")

                        // set category
                        tvCategory.text = category
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }
    }
}