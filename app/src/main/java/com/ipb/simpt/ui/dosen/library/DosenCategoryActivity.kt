package com.ipb.simpt.ui.dosen.library

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityDosenCategoryBinding

class DosenCategoryActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityDosenCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDosenCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        setupToolbar()
        setupAction()
    }

    private fun setupAction() {
        // handle button
        binding.cvKomoditas.setOnClickListener {
            val intent = Intent(this, DosenCategoryListActivity::class.java)
            intent.putExtra("CATEGORY_NAME", "Komoditas")
            this.startActivity(intent)
        }
        binding.cvPenyakit.setOnClickListener {
            val intent = Intent(this, DosenCategoryListActivity::class.java)
            intent.putExtra("CATEGORY_NAME", "Penyakit")
            this.startActivity(intent)
        }
        binding.cvGejala.setOnClickListener {
            val intent = Intent(this, DosenCategoryListActivity::class.java)
            intent.putExtra("CATEGORY_NAME", "Gejala Penyakit")
            this.startActivity(intent)
        }
        binding.cvPathogen.setOnClickListener {
            val intent = Intent(this, DosenCategoryPathogenActivity::class.java)
            intent.putExtra("CATEGORY_NAME", "Kategori Pathogen")
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