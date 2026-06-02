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

        binding.modifyDeleteData.text = "<"

        // 1. CREATE DATABASE
        binding.createDatabase.setOnClickListener {
            // Calling writableDatabase checks if the file exists.
            // If it doesn't exist, it triggers onCreate() in MyDatabaseHelper to build tables.
            dbHelper.writableDatabase
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

            // Update on-screen count label
            updateQueryCountLabel(queryAllDataCount().toString())
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

            updateQueryCountLabel(queryAllDataCount().toString())
        }

        // 4. DELETE DATA
        binding.deleteData.setOnClickListener {
            val db = dbHelper.writableDatabase
            
            // Delete books matching selection (e.g. pages < 500 or pages > 500)
            db.delete("Book", "pages ${binding.modifyDeleteData.text}?", arrayOf("500"))

            updateQueryCountLabel(queryAllDataCount().toString())
        }

        // Toggle delete button operator (< or >)
        binding.modifyDeleteData.setOnClickListener {
            deleteLessThan = !deleteLessThan
            binding.modifyDeleteData.text = if (deleteLessThan) "<" else ">"
        }

        // 5. QUERY (READ) DATA
        binding.queryData.setOnClickListener {
            val db = dbHelper.writableDatabase

            // A 'Cursor' represents the table result set returned by a database query.
            // Think of a Cursor like a pointer pointing to a spreadsheet row.
            // It starts just *before* the first row of data.
            val cursor = db.query("Book", null, null, null, null, null, null)

            updateQueryCountLabel(cursor.count.toString())

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
        }
    }

    /**
     * Helper to query the count of all records in the Book table.
     *
     * RESOURCE MANAGEMENT (Auto-Closing Cursors):
     * We use Kotlin's built-in `.use { ... }` extension function on the query result Cursor.
     * A Cursor is a Closeable resource. By using `.use`, Kotlin guarantees that the Cursor is
     * automatically closed at the end of the block, preventing resource leaks and saving native system memory!
     */
    private fun queryAllDataCount(): Int {
        val cursor = dbHelper.writableDatabase.query("Book", null, null, null, null, null, null)
        return cursor.use {
            it.count
        }
    }

    private fun updateQueryCountLabel(value: String) {
        binding.queryCount.text = "N = $value"
    }
}
