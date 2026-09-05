package com.example.storage_database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.storage_database.databinding.ActivityMainBinding

/**
 * MAINACTIVITY: SQLITE DATABASE PERSISTENCE
 *
 * What is a Database?
 * For structured, relational, complex data (like tables with columns and rows, e.g. a bookstore catalog),
 * we use databases. Android includes built-in support for **SQLite** databases.
 *
 * How does Android database work?
 *  - We write a subclass of `SQLiteOpenHelper` (like our `MyDatabaseHelper`) to manage creating
 *    and upgrading database tables.
 *  - We call `.writableDatabase` to get a connection instance to perform CRUD operations:
 *     - **C**reate (Insert)
 *     - **R**ead (Query)
 *     - **U**pdate
 *     - **D**elete
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Helper object that acts as our database connection manager
    private lateinit var dbHelper: MyDatabaseHelper

    private var deleteLessThan = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize helper: Name of db is "BookStore.db", version is 2
        dbHelper = MyDatabaseHelper(this, "BookStore.db", null, 2)

        updateDeleteConditionLabel()

        // 1. CREATE DATABASE
        binding.createDatabase.setOnClickListener {
            // Calling writableDatabase checks if the file exists.
            // If it doesn't exist, it triggers onCreate() in MyDatabaseHelper to build tables.
            dbHelper.writableDatabase
            refreshPreview()
        }

        // 2. ADD (INSERT) DATA
        binding.addData.setOnClickListener {
            val db = dbHelper.writableDatabase
            
            // ContentValues: A key-value container used to hold data for a single database row.
            // Keys represent column names, and values represent the cell contents.
            val values = ContentValues()

            // Prepare record 1: The Da Vinci Code
            values.put("name", "The Da Vinci Code")
            values.put("author", "Dan Brown")
            values.put("pages", 454)
            values.put("price", 16.96)
            db.insert("Book", null, values) // Insert row into "Book" table

            values.clear() // Clear container to prepare record 2

            // Prepare record 2: The Lost Symbol
            values.put("name", "The Lost Symbol")
            values.put("author", "Dan Brown")
            values.put("pages", 510)
            values.put("price", 19.95)
            db.insert("Book", null, values)

            // Refresh the on-screen preview so the new rows are visible immediately
            refreshPreview()
        }

        // 3. UPDATE DATA
        binding.updateData.setOnClickListener {
            val db = dbHelper.writableDatabase
            val values = ContentValues()
            values.put("price", 10.99) // Change price to 10.99
            
            // SQL Injection Safety:
            // We use '?' placeholders in our selection string ("name = ?") and pass actual arguments
            // in an array: `arrayOf("The Da Vinci Code")`. The SQL compiler sanitizes this to prevent hackers!
            db.update("Book", values, "name = ?", arrayOf("The Da Vinci Code"))

            refreshPreview()
        }

        // 4. DELETE DATA
        binding.deleteData.setOnClickListener {
            val db = dbHelper.writableDatabase

            // Delete books matching whichever condition the button above currently shows.
            val operator = if (deleteLessThan) "<" else ">"
            db.delete("Book", "pages $operator?", arrayOf("500"))

            refreshPreview()
        }

        // Toggle delete condition (pages < 500 or pages > 500) — the button's own label
        // is the source of truth for what Delete Data is about to do, so there's no
        // hidden rule the audience can't see.
        binding.modifyDeleteData.setOnClickListener {
            deleteLessThan = !deleteLessThan
            updateDeleteConditionLabel()
        }

        // 5. QUERY (READ) DATA
        binding.queryData.setOnClickListener {
            val db = dbHelper.writableDatabase

            // A 'Cursor' represents the table result set returned by a database query.
            // Think of a Cursor like a pointer pointing to a spreadsheet row.
            // It starts just *before* the first row of data.
            val cursor = db.query("Book", null, null, null, null, null, null)

            // Move the pointer to the first row. Returns true if there is at least 1 record.
            if (cursor.moveToFirst()) {
                do {
                    // Extract data columns from the current row.
                    // We must find the index of the column name first, then read its type.
                    @SuppressLint("Range") val name = cursor.getString(cursor.getColumnIndex("name"))
                    @SuppressLint("Range") val author = cursor.getString(cursor.getColumnIndex("author"))
                    @SuppressLint("Range") val pages = cursor.getInt(cursor.getColumnIndex("pages"))
                    @SuppressLint("Range") val price = cursor.getDouble(cursor.getColumnIndex("price"))
                    
                    Log.d("MainActivity", "------------------------")
                    Log.d("MainActivity", "book name is $name")
                    Log.d("MainActivity", "book author is $author")
                    Log.d("MainActivity", "book pages is $pages")
                    Log.d("MainActivity", "book price is $price")
                    Log.d("MainActivity", "------------------------")
                } while (cursor.moveToNext()) // Move to the next row until we hit the end!
            }
            
            // ALWAYS CLOSE YOUR CURSORS! If you don't, it leaks native memory and resources!
            cursor.close()

            refreshPreview()
        }
    }

    /**
     * DEMO UI: makes the Delete condition button say what it will actually do,
     * instead of showing a bare "<" or ">" with no context.
     */
    private fun updateDeleteConditionLabel() {
        binding.modifyDeleteData.text = if (deleteLessThan) {
            "Delete condition: pages < 500  (tap to flip)"
        } else {
            "Delete condition: pages > 500  (tap to flip)"
        }
    }

    /**
     * DEMO VISUAL: paints every row currently in the Book table onto the screen.
     *
     * This is what makes the CRUD buttons above actually visible during a live demo —
     * without it, insert/update/delete only prove themselves through Logcat, which the
     * class can't see. We re-query and re-render on every button tap, so the on-screen
     * list is always the ground truth of what's in BookStore.db right now.
     */
    private fun refreshPreview() {
        val cursor = dbHelper.writableDatabase.query("Book", null, null, null, null, null, "id asc")
        val rows = cursor.use {
            buildList {
                while (it.moveToNext()) {
                    @SuppressLint("Range") val id = it.getInt(it.getColumnIndex("id"))
                    @SuppressLint("Range") val name = it.getString(it.getColumnIndex("name"))
                    @SuppressLint("Range") val author = it.getString(it.getColumnIndex("author"))
                    @SuppressLint("Range") val pages = it.getInt(it.getColumnIndex("pages"))
                    @SuppressLint("Range") val price = it.getDouble(it.getColumnIndex("price"))
                    add("#$id  $name — $author — ${pages}pg — \$$price")
                }
            }
        }

        binding.dataPreview.text = if (rows.isEmpty()) {
            "(no rows yet — tap Create Database, then Add Data)"
        } else {
            rows.joinToString("\n")
        }
        binding.queryCount.text = "N = ${rows.size}"
    }
}
