package com.example.storage_contentprovider

import android.annotation.SuppressLint
import android.content.ContentProviderClient
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.storage_contentprovider.databinding.ActivityMainBinding

/**
 * MAINACTIVITY: ACCESSING AN EXTERNAL CONTENT PROVIDER
 *
 * This Activity represents a separate, client application.
 * It does NOT have direct access to "BookStore.db" database!
 * Instead, it communicates with the `com.example.storage_database` application
 * across processes using a **ContentResolver**.
 *
 * What is a ContentResolver?
 * Think of it like a web browser. The ContentProvider is a website server.
 * The ContentResolver is your browser client. You send it a Content URI address,
 * and it fetches the data from the provider on your behalf.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    
    // Store the ID of the newly added book so we can update or delete it later
    private var newId: String? = null
    private var provider: ContentProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. INSERT DATA
        binding.addData.setOnClickListener {
            val uri = Uri.parse("content://com.example.storage_database.provider/book")
            val values = ContentValues().apply {
                put("name", "A Clash of Kings")
                put("author", "George Martin")
                put("pages", 1040)
                put("price", 55.55)
            }

            // RESOURCE MANAGEMENT (Safe Connection Closing):
            // We use a `finally` block to guarantee that the `ContentProviderClient` connection is closed.
            // The code inside `finally` is guaranteed to run whether the insert completes successfully or
            // throws an exception, preventing active connection leaks!
            //
            // *Note*: For standard tasks, you can call `contentResolver.insert(uri, values)` directly.
            // It automatically manages opening and closing the provider connection under the hood,
            // as shown in the update and delete click listeners below!
            var client: ContentProviderClient? = null
            try {
                client = contentResolver.acquireContentProviderClient(uri)
                provider = client
                
                val newUri: Uri? = client?.insert(uri, values)
                newId = newUri?.pathSegments?.get(1) // Extract the ID of the new book
                Log.d("Content Provider", "Successfully inserted row. New ID is: $newId")
            } catch (e: RemoteException) {
                Log.e("Content Provider Insert Data", "URI is not reachable: ${e.message}")
            } finally {
                // Guaranteed to run, preventing connection leaks!
                client?.close()
            }
        }

        // 2. QUERY DATA
        binding.queryData.setOnClickListener {
            val uri = Uri.parse("content://com.example.storage_database.provider/book")
            var client: ContentProviderClient? = null
            try {
                client = contentResolver.acquireContentProviderClient(uri)
                provider = client
                
                val cursor = client?.query(uri, null, null, null, null)

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        @SuppressLint("Range") val name = cursor.getString(cursor.getColumnIndex("name"))
                        @SuppressLint("Range") val author = cursor.getString(cursor.getColumnIndex("author"))
                        @SuppressLint("Range") val pages = cursor.getInt(cursor.getColumnIndex("pages"))
                        @SuppressLint("Range") val price = cursor.getDouble(cursor.getColumnIndex("price"))
                        
                        Log.d("MainActivity", "book name is $name")
                        Log.d("MainActivity", "book author is $author")
                        Log.d("MainActivity", "book pages is $pages")
                        Log.d("MainActivity", "book price is $price")
                    }
                    cursor.close() // Close the cursor result set
                }
            } catch (e: Exception) {
                Log.e("Content Provider Query", "URI is not reachable: ${e.message}")
            } finally {
                client?.close() // Safely close the client
            }
        }

        // 3. UPDATE DATA (Simplified, standard way)
        binding.updateData.setOnClickListener {
            // Update the specific book we recently inserted
            val uri = Uri.parse("content://com.example.storage_database.provider/book/$newId")
            val values = ContentValues().apply {
                put("name", "A Storm of Swords")
                put("pages", 1216)
                put("price", 24.05)
            }
            
            // Standard approach: Call contentResolver directly without acquiring clients manually!
            // Cleaner, shorter, and automatically safe from connection leaks.
            contentResolver.update(uri, values, null, null)
        }

        // 4. DELETE DATA (Simplified, standard way)
        binding.deleteData.setOnClickListener {
            // Delete the specific book we recently inserted
            val uri = Uri.parse("content://com.example.storage_database.provider/book/$newId")
            
            // Delete row directly using ContentResolver
            contentResolver.delete(uri, null, null)
        }
    }
}
