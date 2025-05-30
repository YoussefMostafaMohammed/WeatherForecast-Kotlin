plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}
android {
    namespace = "com.example.weatherforecast"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.weatherforecast"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        buildConfigField( "String", "WEATHER_API_KEY", "\"897f05d7107c1a4583eb10de82e05435\"")
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
        dataBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.work:work-runtime-ktx:2.10.1")
    implementation ("androidx.room:room-runtime:2.7.1")
    kapt("androidx.room:room-compiler:2.7.1")
    implementation ("androidx.room:room-ktx:2.7.1")
    implementation ("androidx.room:room-paging:2.7.1")
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.9.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation ("androidx.core:core-ktx:1.13.1")
    implementation ("androidx.activity:activity-ktx:1.9.3")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    testImplementation ("junit:junit:4.13.2")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation ("androidx.arch.core:core-testing:2.2.0")
    testImplementation ("org.mockito:mockito-core:4.11.0")
    testImplementation ("org.mockito.kotlin:mockito-kotlin:5.2.1")

    // Unit test runtime
    testImplementation ("junit:junit:4.13.2")

// Kotlin coroutines testing
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

// MockK for mocking suspend functions
    testImplementation ("io.mockk:mockk:1.13.4")

// AndroidX Arch Core (if you ever test LiveData directly)
    testImplementation ("androidx.arch.core:core-testing:2.2.0")

// (Optional) If you ever need Truth or Hamcrest for richer assertions
    testImplementation ("com.google.truth:truth:1.1.3")
    testImplementation ("org.hamcrest:hamcrest:2.2")
    testImplementation ("org.slf4j:slf4j-simple:2.0.7")
}