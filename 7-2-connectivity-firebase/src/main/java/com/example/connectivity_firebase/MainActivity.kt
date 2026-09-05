package com.example.connectivity_firebase

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.connectivity_firebase.databinding.ActivityMainBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val messagesRef = FirebaseFirestore.getInstance().collection("messages")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                binding.messageDisplay.text = snapshot?.documents
                    ?.mapNotNull { it.getString("text") }
                    ?.joinToString("\n")
            }

        binding.messageButton.setOnClickListener {
            val outputText = binding.messageContent.text.toString()
            messagesRef.add(mapOf("text" to outputText, "timestamp" to FieldValue.serverTimestamp()))
            binding.messageContent.text.clear()
        }
    }
}
