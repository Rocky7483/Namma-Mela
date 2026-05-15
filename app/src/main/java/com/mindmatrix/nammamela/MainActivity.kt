package com.mindmatrix.nammamela

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.mindmatrix.nammamela.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val vm: MelaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
        binding.bottomNav.setupWithNavController(navController)
        lifecycleScope.launch {
            vm.currentUser.collect { user ->
                binding.bottomNav.visibility = if (user == null) View.GONE else View.VISIBLE
                binding.bottomNav.menu.clear()
                if (user?.role == UserRole.ADMIN) binding.bottomNav.inflateMenu(R.menu.admin_nav_menu)
                else binding.bottomNav.inflateMenu(R.menu.user_nav_menu)
                if (user == null && navController.currentDestination?.id != R.id.loginFragment) navController.navigate(R.id.loginFragment)
                if (user?.role == UserRole.ADMIN) navController.navigate(R.id.adminDashboardFragment)
                if (user?.role == UserRole.USER) navController.navigate(R.id.homeFragment)
            }
        }
        lifecycleScope.launch {
            vm.message.collect { text ->
                text ?: return@collect
                Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
                vm.message.value = null
            }
        }
    }
}
