package com.ipb.simpt

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipb.simpt.databinding.ActivityMainBinding
import com.ipb.simpt.ui.splash.WelcomeActivity
import com.ipb.simpt.utils.Extensions.toast

class MainActivity : AppCompatActivity() {

    // view binding & nav controller
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //TAG
    private val TAG = "USER_SHOW_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //Navbar
        navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_search,
                R.id.navigation_scan,
                R.id.navigation_library,
                R.id.navigation_profile
            )
        )
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        // Navbar Visibility
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home, R.id.navigation_scan -> {
                    toolbar.visibility = View.GONE
                }

                else -> {
                    toolbar.visibility = View.VISIBLE
                }
            }
            invalidateOptionsMenu()
        }
    }

    // TODO : Lanjutkan bagian show user info ke home fragment
    //Authentication
    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            // Not signed in, launch the Welcome activity
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        } else {
            // logged in, get user info from Firestore
            val db = FirebaseFirestore.getInstance()
            db.collection("Users")
                .document(firebaseUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // get user info
                        val name = document.getString("userName")
                        val nim = document.getString("userNim")

                        // Create a bundle to pass the data
                        val bundle = Bundle().apply {
                            putString("userName", name)
                            putString("userNim", nim)
                        }

                        // Pass the bundle to the HomeFragment
                        navController.setGraph(navController.graph, bundle)
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        // Inflate the menu based on the currently active fragment
        when (navController.currentDestination?.id) {
            R.id.navigation_library -> {
                menuInflater.inflate(R.menu.menu_library, menu)
                val layoutMenuItem = menu.findItem(R.id.action_layout)
                // Set the icon based on the current layout
                layoutMenuItem.icon = getLayoutIcon()
            }

            R.id.navigation_search -> {
                menuInflater.inflate(R.menu.menu_search, menu)
            }

            R.id.navigation_profile -> {
                menuInflater.inflate(R.menu.menu_profile, menu)
            }

            else -> {
                menuInflater.inflate(R.menu.default_menu, menu)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_layout -> {
                // Handle layout change action in the Library fragment
                // Toggle between grid and list layout icons
                currentLayout = if (currentLayout == Layout.GRID) Layout.LIST else Layout.GRID
                // Set the icon based on the current layout
                item.icon = getLayoutIcon()
                true
            }

            R.id.action_sort -> {
                // Handle sorting action in the Library fragment
                // Show sort options (ascending/descending)
                showSortOptions()
                true
            }

            R.id.action_filter -> {
                // Handle filtering action in the Search fragment
                true
            }

            R.id.action_back -> {
                // Navigate back when back button is clicked
                navController.navigateUp()
                true
            }

            R.id.action_logout -> {
                signOut()
                true
            }

            // Add more cases for other menu items if needed
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun signOut() {
        firebaseAuth.signOut()
        startActivity(Intent(this, WelcomeActivity::class.java))
        toast("signed out")
        finish()
    }

    private fun getLayoutIcon(): Drawable? {
        return ResourcesCompat.getDrawable(
            resources,
            if (currentLayout == Layout.GRID) R.drawable.ic_grid_view else R.drawable.ic_list_view,
            null
        )
    }

    private fun showSortOptions() {
        // Show sort options (ascending/descending)
        // You can display a dialog or any other UI to let the user choose sort options
    }

    enum class Layout {
        GRID, LIST
    }

    private var currentLayout: Layout = Layout.GRID // Initial layout is grid

}

//TODO 1 = Layout Pengajuan
//TODO 2 = Welcome/Splash Page