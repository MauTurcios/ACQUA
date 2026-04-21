plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.sismantec.acqua"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.sismantec.acqua"
        minSdk = 27
        targetSdk = 36
        versionCode = 12
        versionName = "1.20"

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

    buildFeatures{
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)


    //LOTTIE
    implementation("com.airbnb.android:lottie:6.7.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:3.0.0")

    // Conversor JSON (usa Gson)
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    // Corutinas (opcional pero muy recomendado)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // OkHttp Logging (para ver las peticiones)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    //ROOM
    implementation ("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1")

    // LiveData con soporte a coroutines
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    // ViewModel + coroutines
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    //IMPLEMENTANDO LIBRERIA ESCPOSTPRINTER
    implementation (files("libs/escposprinter-release.aar"))


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}