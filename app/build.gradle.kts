plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.appphotointern"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.azmobile.phonemirror"
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
            manifestPlaceholders["analyticsCollectionEnabled"] = false
        }

        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["analyticsCollectionEnabled"] = false
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
    implementation(libs.afs.native)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    val hilt_navigation_compose_version = "1.2.0"
    val compose_material_icons_version = "1.6.0"
    val coroutine_play_services_version = "1.6.4"
    val viewpager2_version = "1.1.0"
    val core_ktx_version = "1.13.1"
    val material_version = "1.11.0"
    val camerax_version = "1.4.2"
    val billing_version = "6.2.0"
    val work_version = "2.10.4"
    val hilt_version = "2.50"

    implementation("androidx.work:work-runtime-ktx:2.9.0")

    implementation("androidx.camera:camera-extensions:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.1.0"))
    implementation("com.google.firebase:firebase-appcheck-interop")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    implementation("com.google.firebase:firebase-appcheck-debug")
    implementation("com.google.firebase:firebase-appcheck")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.firebaseui:firebase-ui-storage:8.0.2")
    implementation("com.google.firebase:firebase-database")

    // --- Dependency Injection (Hilt) ---
    implementation("com.google.dagger:hilt-android:$hilt_version")
    ksp("com.google.dagger:hilt-compiler:$hilt_version")
    implementation("androidx.hilt:hilt-navigation-compose:$hilt_navigation_compose_version")

    // --- ViewModel ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // --- UI Components ---
    implementation("androidx.compose.material:material-icons-extended:$compose_material_icons_version")
    implementation("com.google.android.material:material:$material_version")
    implementation("androidx.viewpager2:viewpager2:$viewpager2_version")

    // --- Kotlin Extensions (KTX) ---
    implementation("androidx.core:core-ktx:$core_ktx_version")

    // --- Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutine_play_services_version")

    // --- Lottie ---
    implementation("com.airbnb.android:lottie:6.4.0")

    // --- Color Picker ---
    implementation("com.github.Dhaval2404:ColorPicker:2.3")

    // --- Gson ---
    implementation("com.google.code.gson:gson:2.13.1")

    // --- EventBus ---
    implementation("org.greenrobot:eventbus:3.3.1")

    // --- ExifInterface ---
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // --- Ads ---
    implementation("com.google.android.gms:play-services-ads:23.2.0")

    // --- Billing ---
    implementation("com.android.billingclient:billing:$billing_version")

    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
}
