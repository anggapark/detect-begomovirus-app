package com.ipb.simpt

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.ipb.simpt.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home, R.id.navigation_scan, R.id.navigation_profile -> supportActionBar?.hide()
                else -> supportActionBar?.show()
            }
            invalidateOptionsMenu()
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
            // Add more cases for other fragments if needed
//            else -> {
//                // Default menu
//                menuInflater.inflate(R.menu.default_menu, menu)
//            }
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
            // Add more cases for other menu items if needed
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
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