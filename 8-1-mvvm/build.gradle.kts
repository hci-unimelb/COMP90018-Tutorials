plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.mvvm"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mvvm"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // --- The only new pieces for this week ---
    // activity-ktx gives us the `by viewModels()` delegate.
    implementation(libs.androidx.activity.ktx)
    // doAfterTextChanged, used by the login demo.
    implementation(libs.androidx.core.ktx)
    // ViewModel itself, plus viewModelScope for coroutines.
    implementation(libs.lifecycle.viewmodel.ktx)
    // LiveData - the observable holder the UI subscribes to.
    implementation(libs.lifecycle.livedata.ktx)
    // lifecycleScope, used in the BROKEN example so we can contrast the two.
    implementation(libs.lifecycle.runtime.ktx)
    // SavedStateHandle - survives process death, not just rotation.
    implementation(libs.lifecycle.viewmodel.savedstate)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
