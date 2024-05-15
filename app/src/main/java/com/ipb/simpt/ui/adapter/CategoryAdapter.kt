//package com.ipb.simpt.ui.adapter
//
//import android.app.AlertDialog
//import android.content.Context
//import android.content.Intent
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Filter
//import android.widget.Filterable
//import android.widget.ImageButton
//import android.widget.TextView
//import android.widget.Toast
//import androidx.recyclerview.widget.RecyclerView
//import com.google.firebase.firestore.FirebaseFirestore
//import com.ipb.simpt.data.model.CategoryModel
//import com.ipb.simpt.databinding.CategoryListBinding
//import com.ipb.simpt.ui.library.DosenDataListActivity
//
//
//class CategoryAdapter(
//    private val context: Context,
//    var categoryArrayList: ArrayList<CategoryModel>
//) : RecyclerView.Adapter<CategoryAdapter.HolderCategory>(), Filterable {
//
//    private var filterList: ArrayList<CategoryModel> = categoryArrayList
//    private lateinit var filter: Filter
//    private lateinit var binding: CategoryListBinding
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
//        // Inflate / bind category_list.xml
//        binding = CategoryListBinding.inflate(LayoutInflater.from(context), parent, false)
//        return HolderCategory(binding.root)
//    }
//
//    override fun getItemCount(): Int = categoryArrayList.size // number of items in list
//
//    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
//        // get data
//        val model = categoryArrayList[position]
//
//        // set data
////        holder.categoryTv.text = model.category
//
//        // handle click, delete category
//        holder.deleteIb.setOnClickListener {
//            // confirm before delete
//            AlertDialog.Builder(context).apply {
//                setTitle("Delete")
//                setMessage("Are you sure want to delete this category?")
//                setPositiveButton("Confirm") { _, _ ->
//                    Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show()
//                    deleteCategory(model)
//                }
//                setNegativeButton("Cancel") { dialog, _ ->
//                    dialog.dismiss()
//                }
//            }.show()
//        }
//
//        // handle click, start data list activity, also pass data id, title
//        holder.itemView.setOnClickListener {
//            val intent = Intent(context, DosenDataListActivity::class.java)
//            intent.putExtra("categoryId", model.id)
////            intent.putExtra("category", model.category)
//            context.startActivity(intent)
//        }
//    }
//
//    private fun deleteCategory(model: CategoryModel) {
//        // get id of category to delete
//        val id = model.id
//
//        // firebase
//        val db = FirebaseFirestore.getInstance()
//        db.collection("Categories").document(id)
//            .delete()
//            .addOnSuccessListener {
//                // remove the item from the list and notify the adapter
//                categoryArrayList.remove(model)
//                notifyDataSetChanged()
//
//                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
//
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(context, "Unable to delete due to ${e.message}", Toast.LENGTH_SHORT)
//                    .show()
//            }
//    }
//
//
//    // viewHolder class to hold/init UI views for item_list.xml
//    inner class HolderCategory(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        // init ui views
//        var categoryTv: TextView = binding.tvCategory
//        var deleteIb: ImageButton = binding.ibDelete
//    }
//
//    override fun getFilter(): Filter {
//        if (!::filter.isInitialized) {
//            filter = CategoryFilter(filterList, this)
//        }
//        return filter
//    }
//
//}