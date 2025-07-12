package com.dev.dev

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        val navController = findNavController(R.id.adminNavHostFragment)
        val bottomNav = findViewById<BottomNavigationView>(R.id.adminBottomNav)

        // Connect BottomNavigationView with NavController
        bottomNav.setupWithNavController(navController)
    }
}