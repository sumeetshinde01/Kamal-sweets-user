package com.example.kamalsweets

import android.content.Intent
import android.content.SharedPreferences

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.kamalsweets.Activity.EditProfileActivity
import com.example.kamalsweets.Activity.LoginActivity
import com.example.kamalsweets.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private  var userName:String?=null
    private var phoneNumber:String?=null
    private lateinit var navController: NavController

    private lateinit var preference: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preference= this.getSharedPreferences("otpActivityUser", MODE_PRIVATE)
        val check :Boolean = preference.getBoolean("flag",false)
        if (!check){
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
        userName=preference.getString("name","Default2")
        phoneNumber=preference.getString("number",null)


        val navHostFragment =supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        navController = navHostFragment!!.findNavController()

        val popupMenu= PopupMenu(this,null)
        popupMenu.inflate(R.menu.bottom_nav)

        binding.bottomBar.setupWithNavController(popupMenu.menu,navController)

        // Check for cart redirection
        val infoPreference = getSharedPreferences("info", MODE_PRIVATE)
        if (infoPreference.getBoolean("isCart", false)) {
            infoPreference.edit().putBoolean("isCart", false).apply()
            lifecycleScope.launch {
                delay(200)
                val navOptions = NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setRestoreState(true)
                    .setPopUpTo(navController.graph.startDestinationId, false, true)
                    .build()
                navController.navigate(R.id.cartFragment, null, navOptions)
                binding.bottomBar.itemActiveIndex = 1
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val index = when(destination.id) {
                R.id.homeFragment -> 0
                R.id.cartFragment -> 1
                R.id.moreFragment -> 2
                else -> -1
            }
            
            if (index != -1 && binding.bottomBar.itemActiveIndex != index) {
                binding.bottomBar.itemActiveIndex = index
            }

            title = when(destination.id) {
                R.id.homeFragment -> {
                    "Kamal Sweets"
                }
                R.id.cartFragment -> {
                    "My Cart"
                }
                R.id.moreFragment -> {
                    "My Orders"
                }
                else -> "Kamal Sweets"
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val editor=preference.edit()

        when(item.itemId){
            R.id.edit_profile -> {
                startActivity(Intent(this, EditProfileActivity::class.java))
            }
            R.id.logout->{
                editor.putBoolean("flag",false)
                editor.apply()
                startActivity(Intent(this,LoginActivity::class.java))
                finish()

            }
            R.id.about->{
                Toast.makeText(this, "Kamal Sweets v1.0", Toast.LENGTH_SHORT).show()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.homeFragment) {
            super.onBackPressed()
            finish()
        } else {
            navController.popBackStack(R.id.homeFragment, false)
            if (navController.currentDestination?.id != R.id.homeFragment) {
                navController.navigate(R.id.homeFragment)
            }
        }
    }
}
