plugins {
    jacoco // Apply the JaCoCo plugin for code coverage analysis
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
}
android {
    namespace = "com.mustalk.minisimulator"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mustalk.minisimulator"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "com.mustalk.minisimulator.HiltTestRunner"
    }

    buildTypes {
        debug {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug" // applicationIdSuffix so we can have both debug and release builds installed if we need to
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    // Allow references to generated code
    kapt {
        correctErrorTypes = true
    }
}

dependencies {
    // ------------------------------------------------------------
    // UI and Core AndroidX Libraries
    // ------------------------------------------------------------
    implementation(libs.androidx.core.ktx) // Kotlin extensions for Android framework
    implementation(libs.androidx.appcompat) // AppCompat compatibility library
    implementation(libs.material) // Material Design components
    implementation(libs.androidxKtxFagment) // Kotlin extensions for Fragments
    implementation(libs.androidx.activity) // Activity library for modern Android development
    implementation(libs.androidx.constraintlayout) // Constraint Layout for flexible UI design
    implementation(libs.androidx.recyclerview) // RecyclerView for efficient list display
    implementation(libs.androidx.drawerlayout) // DrawerLayout for navigation drawers
    implementation(libs.androidx.viewpager2) // ViewPager2 for swipeable views
    implementation(libs.androidx.legacy.support.v4) // Support library for older Android versions
    implementation(libs.androidx.navigation.fragment.ktx) // Navigation fragment library for Android
    implementation(libs.androidx.navigation.ui.ktx) // Navigation ui library for Android

    // ------------------------------------------------------------
    // Firebase Libraries
    // ------------------------------------------------------------
    implementation(platform(libs.firebase.bom)) // Firebase BOM for managing Firebase versions
    implementation(libs.firebase.crashlytics) // Firebase crashlytics
    implementation(libs.firebase.analytics) // Firebase analytics

    // ------------------------------------------------------------
    // Networking and Data Serialization Libraries
    // ------------------------------------------------------------
    implementation(libs.gson) // Gson library for JSON parsing

    // Retrofit
    implementation(libs.retrofit) // Retrofit for network requests
    implementation(libs.retrofit.converter.gson) // Gson converter for Retrofit

    // OkHttp
    implementation(libs.okhttp) // OkHttp client for network requests
    implementation(libs.okhttp.logging) // Logging interceptor for OkHttp

    // Asynchronous Programming
    implementation(libs.coroutinesCore) // Kotlin Coroutines core library
    implementation(libs.coroutinesAndroid) // Coroutines support for Android

    // Dependency Injection
    implementation(libs.hilt.android) // Hilt for dependency injection

    // ------------------------------------------------------------
    // Testing Libraries
    // ------------------------------------------------------------
    implementation(libs.androidx.core)

    // Unit Testing
    testImplementation(libs.junit) // JUnit for unit testing
    testImplementation(libs.androidx.junit.ktx) // Kotlin extensions for JUnit
    testImplementation(libs.kotlinx.coroutines.test) // Coroutines support for testing
    testImplementation(libs.mockito.core) // Mockito for mocking dependencies
    testImplementation(libs.core.ktx) // Kotlin extensions for AndroidX Test
    testImplementation(libs.mockito.kotlin) // Kotlin-friendly Mockito usage
    testImplementation(libs.androidx.core.testing) // AndroidX Test Core for testing
    testImplementation(libs.hamcrest.library) // Hamcrest assertions
    kaptTest(libs.hilt.android.compiler) // Hilt annotation processor for tests

    // Instrumentation Testing (UI and Integration Tests)
    androidTestImplementation(libs.androidx.junit) // AndroidJUnit4 for instrumentation tests
    androidTestImplementation(libs.androidx.espresso.core) // Espresso for UI testing
    androidTestImplementation(libs.mockito.kotlin) // Kotlin-friendly Mockito usage
    androidTestImplementation(libs.mockito.android) // Mockito Android
    androidTestImplementation(libs.androidx.runner) // AndroidX Test Runner
    androidTestImplementation(libs.androidx.rules) // AndroidX Test Rules
    androidTestImplementation(libs.androidx.truth) // AndroidX Truth for assertions
    androidTestImplementation(libs.truth) // Google Truth for assertions
    androidTestImplementation(libs.core.ktx) // Kotlin extensions for AndroidX Test
    androidTestImplementation(libs.hilt.android.testing) // Hilt for instrumentation tests
    androidTestImplementation(libs.androidx.espresso.contrib) // Espresso for UI testing
    // Once https://issuetracker.google.com/127986458 is fixed this can be androidTestImplementation
    debugImplementation(libs.androidx.fragment.testing) // For fragment testing
    androidTestImplementation(libs.kotlinx.coroutines.test) // Coroutines support for testing
    androidTestImplementation(libs.androidx.navigation.testing) // AndroidX navigation for testing
    kaptAndroidTest(libs.hilt.android.compiler) // Hilt annotation processor for instrumentation tests

    // Annotation Processors (kapt)
    kapt(libs.hilt.android.compiler) // Hilt annotation processor
}
