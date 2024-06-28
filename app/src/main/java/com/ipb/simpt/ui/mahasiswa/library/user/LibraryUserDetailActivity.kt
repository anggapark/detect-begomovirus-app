package com.ipb.simpt.ui.mahasiswa.library.user

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.ipb.simpt.R
import com.ipb.simpt.databinding.ActivityLibraryUserDetailBinding

class LibraryUserDetailActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityLibraryUserDetailBinding

    // toolbar
    private lateinit var toolbar: Toolbar

    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLibraryUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        // Get data from intent
        userId = intent.getStringExtra("USER_ID") ?: ""

        // Load the LibraryUserFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.categoryFragmentContainer,
                LibraryUserFragment.newInstance(userId)
            )
            .commit()

        // Load the LibraryUserRecyclerViewFragment
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.recyclerFragmentContainer,
                LibraryUserRecyclerViewFragment.newInstance(userId)
            )
            .commit()
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}