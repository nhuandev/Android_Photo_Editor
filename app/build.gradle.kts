plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.example.appphotointern"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.appphotointern"
        minSdk = 24
        targetSdk = 35
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.auth.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    val hilt_version = "2.50"
    val hilt_navigation_compose_version = "1.2.0"
    val viewpager2_version = "1.1.0"
    val core_ktx_version = "1.13.1"
    val material_version = "1.11.0"
    val compose_material_icons_version = "1.6.0"
    val coroutine_play_services_version = "1.6.4"

    // --- Dependency Injection (Hilt) ---
    implementation("com.google.dagger:hilt-android:$hilt_version")
    ksp("com.google.dagger:hilt-compiler:$hilt_version")
    implementation("androidx.hilt:hilt-navigation-compose:$hilt_navigation_compose_version")

    // --- UI Components ---
    implementation("androidx.compose.material:material-icons-extended:$compose_material_icons_version")
    implementation("com.google.android.material:material:$material_version")
    implementation("androidx.viewpager2:viewpager2:$viewpager2_version")

    // --- Kotlin Extensions (KTX) ---
    implementation("androidx.core:core-ktx:$core_ktx_version")

    // --- Firebase Authentication ---
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // --- Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutine_play_services_version")

    // On-device Face Detection
    implementation("com.google.mlkit:face-detection:16.1.7")
}