// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // Declare Kotlin plugins here so subprojects share one classloader.
    // Without this, applying the same plugin in two modules can trip the
    // "Kotlin Gradle plugin was loaded multiple times" warning.
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false

    // Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.2" apply false

}