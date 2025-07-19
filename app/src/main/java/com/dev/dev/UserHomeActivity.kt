package com.dev.dev

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.dev.MainActivity2
import com.dev.dev.R
import com.dev.dev.adapters.UserWorkAdapter
import com.dev.dev.models.Work
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserHomeActivity : AppCompatActivity(), UserWorkAdapter.UserWorkListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddWork: FloatingActionButton
    private lateinit var btnLogoutUser: Button
    private lateinit var adapter: UserWorkAdapter

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""
    private val workList = mutableListOf<Work>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)

        recyclerView = findViewById(R.id.recyclerUserWorks)
        fabAddWork = findViewById(R.id.fabAddWork)
        btnLogoutUser = findViewById(R.id.btnLogoutUser)

        adapter = UserWorkAdapter(workList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fabAddWork.setOnClickListener {
            showWorkDialog()
        }

        btnLogoutUser.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity2::class.java))
            finish()
        }

        loadWorks()
    }

    private fun showWorkDialog(existingWork: Work? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_work, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etWorkTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etWorkDescription)
        val etExperience = dialogView.findViewById<EditText>(R.id.etExperience)

        if (existingWork != null) {
            etTitle.setText(existingWork.title)
            etDescription.setText(existingWork.description)
            etExperience.setText(existingWork.experience)
        }

        AlertDialog.Builder(this)
            .setTitle(if (existingWork == null) "Add Work" else "Edit Work")
            .setView(dialogView)
            .setPositiveButton(if (existingWork == null) "Add" else "Update") { dialog, _ ->
                val title = etTitle.text.toString().trim()
                val desc = etDescription.text.toString().trim()
                val exp = etExperience.text.toString().trim()

                if (title.isEmpty() || desc.isEmpty() || exp.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (existingWork == null) {
                    addWork(title, desc, exp)
                } else {
                    updateWork(existingWork.id, title, desc, exp)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addWork(title: String, description: String, experience: String) {
        val work = Work(title = title, description = description, experience = experience,
            status = "Pending", createdBy = currentUserId)

        firestore.collection("works")
            .add(work)
            .addOnSuccessListener {
                Toast.makeText(this, "Work added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add work", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateWork(workId: String, title: String, description: String, experience: String) {
        val updates = mapOf(
            "title" to title,
            "description" to description,
            "experience" to experience
        )
        firestore.collection("works").document(workId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Work updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update work", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteWork(workId: String) {
        firestore.collection("works").document(workId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Work deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete work", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadWorks() {
        firestore.collection("works")
            .whereEqualTo("createdBy", currentUserId)
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

    override fun onEdit(work: Work) {
        showWorkDialog(work)
    }

    override fun onDelete(work: Work) {
        AlertDialog.Builder(this)
            .setTitle("Delete Work")
            .setMessage("Are you sure to delete this work?")
            .setPositiveButton("Yes") { d, _ ->
                deleteWork(work.id)
                d.dismiss()
            }
            .setNegativeButton("No", null)
            .show()
    }
}