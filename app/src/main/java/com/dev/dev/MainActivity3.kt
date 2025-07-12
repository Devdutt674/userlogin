package com.dev.dev

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity3 : AppCompatActivity() {

    private lateinit var buttonCreate: Button
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var typeSpinner: Spinner
    private lateinit var loginText: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main3)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.createbtn)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        emailEt = findViewById(R.id.emailEditText)
        passwordEt = findViewById(R.id.passwordEditText)
        typeSpinner = findViewById(R.id.typeSpinner)
        loginText = findViewById(R.id.logInText)
        buttonCreate = findViewById(R.id.createbtn)

        val types = listOf("user", "admin")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = spinnerAdapter

        buttonCreate.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val password = passwordEt.text.toString()
            val selectedType = typeSpinner.selectedItem.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val userData = hashMapOf(
                            "email" to email,
                            "type" to selectedType
                        )
                        firestore.collection("users").document(uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, BlankFragment::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save user data: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "User ID not found after registration", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        loginText.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
            finish()
        }
    }
}  