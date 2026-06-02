package com.example.storage_database

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri

/**
 * DATABASEPROVIDER: CONTENT PROVIDER PERSISTS
 *
 * What is a Content Provider?
 * A Content Provider is one of the four main building blocks of Android (alongside Activities,
 * Services, and BroadcastReceivers).
 * It acts like a secure data storefront. It allows your app to expose its internal database securely
 * to OTHER apps on the device, or allows you to access other apps' databases (e.g., querying
 * the device's system Contacts database).
 *
 * How does a Content Provider transfer data?
 * It uses **Content URIs** as addresses:
 *   Format: `content://<authority>/<path>`
 *   Example: `content://com.example.storage_database.provider/book` (refers to all books)
 *   Example: `content://com.example.storage_database.provider/book/5` (refers to book with ID 5)
 */
class DatabaseProvider : ContentProvider() {

    private lateinit var dbHelper: MyDatabaseHelper

    /**
     * ONCREATE:
     * Called when the application starts. The system boots up the provider.
     * We initialize our SQLite open helper database manager.
     */
    override fun onCreate(): Boolean {
        // Safe null assertion: 'context' is technically nullable in the super class API,
        // but guaranteed to be non-null when onCreate is executed during app launch.
        dbHelper = MyDatabaseHelper(context!!, "BookStore.db", null, 2)
        return true
    }

    /**
     * QUERY:
     * Allows client applications to read rows from our database.
     * We parse the incoming URI using `uriMatcher` to determine which table or row to read.
     */
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val db = dbHelper.readableDatabase
        return when (uriMatcher.match(uri)) {
            BOOK_DIR -> // Read whole book table
                db.query("Book", projection, selection, selectionArgs, null, null, sortOrder)
                
            BOOK_ITEM -> { // Read a single book by ID
                // uri.pathSegments[1] extracts the number at the end of the URI (e.g. ".../book/5" -> "5")
                val bookId = uri.pathSegments[1]
                db.query("Book", projection, "id = ?", arrayOf(bookId), null, null, sortOrder)
            }
            
            CATEGORY_DIR -> // Read whole category table
                db.query("Category", projection, selection, selectionArgs, null, null, sortOrder)
                
            CATEGORY_ITEM -> { // Read a single category by ID
                val categoryId = uri.pathSegments[1]
                db.query("Category", projection, "id = ?", arrayOf(categoryId), null, null, sortOrder)
            }
            
            else -> null
        }
    }

    /**
     * [getType]:
     * Returns the MIME type of data associated with the given Content URI.
     *
     * MIME types are structured strings that tell the system whether the URI points to
     * a collection of multiple items (dir) or a single item (item).
     */
    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            BOOK_DIR -> "vnd.android.cursor.dir/vnd.com.example.storage_database.provider.book"
            BOOK_ITEM -> "vnd.android.cursor.item/vnd.com.example.storage_database.provider.book"
            CATEGORY_DIR -> "vnd.android.cursor.dir/vnd.com.example.storage_database.provider.category"
            CATEGORY_ITEM -> "vnd.android.cursor.item/vnd.com.example.storage_database.provider.category"
            else -> null
        }
    }

    /**
     * INSERT:
     * Allows client applications to insert a new row.
     */
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbHelper.writableDatabase
        return when (uriMatcher.match(uri)) {
            BOOK_DIR, BOOK_ITEM -> {
                val newBookId = db.insert("Book", null, values)
                // Return the new URI pointing directly to the inserted row (e.g. ".../book/23")
                Uri.parse("content://$AUTHORITY/book/$newBookId")
            }
            CATEGORY_DIR, CATEGORY_ITEM -> {
                val newCategoryId = db.insert("Category", null, values)
                Uri.parse("content://$AUTHORITY/category/$newCategoryId")
            }
            else -> null
        }
    }

    /**
     * DELETE:
     * Allows client applications to delete rows.
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val db = dbHelper.writableDatabase
        return when (uriMatcher.match(uri)) {
            BOOK_DIR -> db.delete("Book", selection, selectionArgs)
            BOOK_ITEM -> {
                val bookId = uri.pathSegments[1]
                db.delete("Book", "id = ?", arrayOf(bookId))
            }
            CATEGORY_DIR -> db.delete("Category", selection, selectionArgs)
            CATEGORY_ITEM -> {
                val categoryId = uri.pathSegments[1]
                db.delete("Category", "id = ?", arrayOf(categoryId))
            }
            else -> 0
        }
    }

    /**
     * UPDATE:
     * Allows client applications to update existing rows.
     */
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val db = dbHelper.writableDatabase
        return when (uriMatcher.match(uri)) {
            BOOK_DIR -> db.update("Book", values, selection, selectionArgs)
            BOOK_ITEM -> {
                val bookId = uri.pathSegments[1]
                db.update("Book", values, "id = ?", arrayOf(bookId))
            }
            CATEGORY_DIR -> db.update("Category", values, selection, selectionArgs)
            CATEGORY_ITEM -> {
                val categoryId = uri.pathSegments[1]
                db.update("Category", values, "id = ?", arrayOf(categoryId))
            }
            else -> 0
        }
    }

    companion object {
        // Integer identifiers for our URI matching rules
        const val BOOK_DIR = 0
        const val BOOK_ITEM = 1
        const val CATEGORY_DIR = 2
        const val CATEGORY_ITEM = 3

        // Authority is a unique namespace identifier for our provider
        const val AUTHORITY = "com.example.storage_database.provider"

        // UriMatcher: A helper utility used to parse and route incoming Content URIs
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            // Rule 1: Match "content://com.example...provider/book" -> returns BOOK_DIR (0)
            addURI(AUTHORITY, "book", BOOK_DIR)
            // Rule 2: Match "content://com.example...provider/book/<number>" -> returns BOOK_ITEM (1)
            // '#' is a wildcard matching any integer ID
            addURI(AUTHORITY, "book/#", BOOK_ITEM)
            // Rule 3 & 4: Match Category paths
            addURI(AUTHORITY, "category", CATEGORY_DIR)
            addURI(AUTHORITY, "category/#", CATEGORY_ITEM)
        }
    }
}
