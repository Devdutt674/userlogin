package com.dev.dev

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity2 : AppCompatActivity() {

    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpText: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btnLogin)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        emailEt = findViewById(R.id.etEmail)
        passwordEt = findViewById(R.id.etPass)
        loginButton = findViewById(R.id.btnLogin)
        signUpText = findViewById(R.id.signUpText)

        loginButton.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val password = passwordEt.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
                            firestore.collection("users").document(uid).get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        val userType = document.getString("type")
                                        when (userType) {
                                            "user" -> {
                                                // Go to User Home Activity
                                                val intent = Intent(this, UserHomeActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }
                                            "admin" -> {
                                                // Go to Admin Home Activity
                                                val intent = Intent(this, AdminHomeActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }
                                            else -> {
                                                Toast.makeText(this, "Unknown user type", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Failed to get user data", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        signUpText.setOnClickListener {
            // Navigate back to registration activity
            val intent = Intent(this, MainActivity3::class.java)
            startActivity(intent)
            finish()
        }
    }
}