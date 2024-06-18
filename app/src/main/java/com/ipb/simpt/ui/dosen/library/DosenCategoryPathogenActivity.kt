package com.ipb.simpt.ui.dosen.library

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityDosenCategoryPathogenBinding

class DosenCategoryPathogenActivity : AppCompatActivity() {

    //  view binding
    private lateinit var binding: ActivityDosenCategoryPathogenBinding
    private lateinit var categoryName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDosenCategoryPathogenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: ""

        setupToolbar()
        setupAction()
    }

    private fun setupAction() {
        // handle button
        binding.cvVirus.setOnClickListener {
            val intent = Intent(this, DosenCategoryListActivity::class.java)
            intent.putExtra("CATEGORY_NAME", categoryName)
            intent.putExtra("CATEGORY_VALUE", "Virus")
            this.startActivity(intent)
        }
        binding.cvBakteri.setOnClickListener {
            val intent = Intent(this, DosenCategoryListActivity::class.java)
            intent.putExtra("CATEGORY_NAME", categoryName)
            intent.putExtra("CATEGORY_VALUE", "Bakteri")
            this.startActivity(intent)
        }
        binding.cvCendawan.setOnClickListener {
            val intent = Intent(this, DosenCategoryListActivity::class.java)
            intent.putExtra("CATEGORY_NAME", categoryName)
            intent.putExtra("CATEGORY_VALUE", "Cendawan")
            this.startActivity(intent)
        }
        binding.cvNematoda.setOnClickListener {
            val intent = Intent(this, DosenCategoryListActivity::class.java)
            intent.putExtra("CATEGORY_NAME", categoryName)
            intent.putExtra("CATEGORY_VALUE", "Nematoda")
            this.startActivity(intent)
        }
        binding.cvFitoplasma.setOnClickListener {
            val intent = Intent(this, DosenCategoryListActivity::class.java)
            intent.putExtra("CATEGORY_NAME", categoryName)
            intent.putExtra("CATEGORY_VALUE", "Fitoplasma")
            this.startActivity(intent)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Category"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


}