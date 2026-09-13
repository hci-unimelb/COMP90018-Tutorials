package com.example.mvvm.model

/**
 * THE "MODEL" - part 1: the data itself
 *
 * A Model class is just a plain description of a thing in your app. No Android
 * imports, no UI, no logic about how it gets on screen. In Kotlin a `data class`
 * gives you equals(), hashCode(), toString() and copy() for free.
 *
 * The GeeksforGeeks version of this example writes it in Java with a private
 * field, a getter and a setter for each property. The Kotlin equivalent is
 * this one line, and it is immutable by default, which is what you want for
 * something that is going to be passed between layers.
 */
data class User(
    val email: String,
    val password: String
)
