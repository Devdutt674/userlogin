package com.dev.dev

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class UserHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)

        val navController = findNavController(R.id.userNavHostFragment)
        val bottomNav = findViewById<BottomNavigationView>(R.id.userBottomNav)

        // Connect BottomNavigationView with NavController
        bottomNav.setupWithNavController(navController)
    }
}