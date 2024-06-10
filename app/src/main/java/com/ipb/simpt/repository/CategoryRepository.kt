package com.ipb.simpt.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.ipb.simpt.model.CategoryModel

class CategoryRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun getCategoryPath(categoryName: String, categoryValue: String?): String? {
        return when (categoryName) {
            "Komoditas", "Penyakit", "Gejala Penyakit" -> "Categories/$categoryName/Items"
            "Kategori Pathogen" -> "Categories/Kategori Pathogen/$categoryValue"
            else -> null
        }
    }

    fun fetchDataByCategory(categoryName: String, categoryValue: String?, callback: (List<CategoryModel>) -> Unit) {
        val categoryPath = getCategoryPath(categoryName, categoryValue)
        if (categoryPath == null) {
            callback(emptyList())
            return
        }

        db.collection(categoryPath).get()
            .addOnSuccessListener { result ->
                val dataList = result.map { document ->
                    document.toObject(CategoryModel::class.java)
                }
                callback(dataList)
            }
            .addOnFailureListener { exception ->
                Log.w("DataRepository", "Error getting documents: ", exception)
                callback(emptyList())
            }
    }

    fun updateCategoryName(categoryName: String, categoryValue: String, itemId: String, updatedName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val categoryPath = getCategoryPath(categoryName, categoryValue)
        if (categoryPath == null) {
            onFailure(Exception("Invalid category path"))
            return
        }

        db.collection(categoryPath)
            .document(itemId)
            .update("name", updatedName)
            .addOnSuccessListener {
                Log.d("CategoryRepository", "Category name updated successfully")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("CategoryRepository", "Error updating category name", exception)
                onFailure(exception)
            }
    }
    fun deleteCategory(categoryName: String, categoryValue: String, itemId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val categoryPath = getCategoryPath(categoryName, categoryValue)
        if (categoryPath == null) {
            onFailure(Exception("Invalid category path"))
            return
        }

        db.collection(categoryPath)
            .document(itemId)
            .delete()
            .addOnSuccessListener {
                Log.d("CategoryRepository", "Category deleted successfully")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("CategoryRepository", "Error deleting category", exception)
                onFailure(exception)
            }
    }
}