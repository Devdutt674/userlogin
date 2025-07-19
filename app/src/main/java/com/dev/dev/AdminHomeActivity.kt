package com.dev.dev

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.dev.MainActivity2
import com.dev.dev.R
import com.dev.dev.adapters.AdminWorkAdapter
import com.dev.dev.models.Work
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminHomeActivity : AppCompatActivity(), AdminWorkAdapter.AdminWorkListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnLogoutAdmin: Button
    private lateinit var adapter: AdminWorkAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val workList = mutableListOf<Work>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        recyclerView = findViewById(R.id.recyclerAdminWorks)
        btnLogoutAdmin = findViewById(R.id.btnLogoutAdmin)

        adapter = AdminWorkAdapter(workList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnLogoutAdmin.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity2::class.java))
            finish()
        }

        loadWorks()
    }

    private fun loadWorks() {
        firestore.collection("works")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                workList.clear()
                snapshot?.documents?.forEach {
                    val work = it.toObject(Work::class.java)
                    work?.id = it.id
                    work?.let { w -> workList.add(w) }
                }
                adapter.notifyDataSetChanged()
            }
    }

    override fun onAccept(work: Work) {
        if (work.status == "Accepted") {
            Toast.makeText(this, "Work already accepted", Toast.LENGTH_SHORT).show()
            return
        }
        firestore.collection("works").document(work.id)
            .update("status", "Accepted")
            .addOnSuccessListener {
                Toast.makeText(this, "Work accepted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to accept work", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDelete(work: Work) {
        AlertDialog.Builder(this)
            .setTitle("Delete Work")
            .setMessage("Are you sure to delete this work?")
            .setPositiveButton("Yes") { d, _ ->
                firestore.collection("works").document(work.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Work deleted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to delete work", Toast.LENGTH_SHORT).show()
                    }
                d.dismiss()
            }
            .setNegativeButton("No", null)
            .show()
    }
}