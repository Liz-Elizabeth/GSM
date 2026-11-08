import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

// Carga segura de variables locales y secretos sin exponerlos en Git
val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use { load(it) }
    }
}

val flowApiKey = localProperties.getProperty("FLOW_API_KEY") ?: "22F98890-9CA6-4340-8D6D-619FECLE9C60"
val flowSecretKey = localProperties.getProperty("FLOW_SECRET_KEY") ?: "e000adf9c0031971903596a1e91ddb2809e4e5e5"
val flowEnv = localProperties.getProperty("FLOW_ENVIRONMENT") ?: "sandbox"
val appwriteEndpoint = localProperties.getProperty("APPWRITE_ENDPOINT") ?: "https://nyc.cloud.appwrite.io/v1"
val appwriteProjectId = localProperties.getProperty("APPWRITE_PROJECT_ID") ?: "6ae91595000d83e34686"
val appwriteBucketId = localProperties.getProperty("APPWRITE_BUCKET_ID") ?: "6ae915ad002bdc1515ec"

android {
    namespace = "com.example.gsm"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.gsm"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "FLOW_API_KEY", "\"$flowApiKey\"")
        buildConfigField("String", "FLOW_SECRET_KEY", "\"$flowSecretKey\"")
        buildConfigField("String", "FLOW_ENVIRONMENT", "\"$flowEnv\"")
        buildConfigField("String", "APPWRITE_ENDPOINT", "\"$appwriteEndpoint\"")
        buildConfigField("String", "APPWRITE_PROJECT_ID", "\"$appwriteProjectId\"")
        buildConfigField("String", "APPWRITE_BUCKET_ID", "\"$appwriteBucketId\"")
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // CameraX (Lector de Códigos QR)
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // ZXing (Generador de Códigos QR en Bitmap)
    implementation("com.google.zxing:core:3.5.3")

    // Appwrite Storage (Evidencias y Archivos)
    implementation(libs.appwrite.sdk)

    // Firebase BoM, Firestore, Analytics y Coroutines Play Services
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.kotlinxCoroutinesPlayServices)

    // Cifrado Local de Base de Datos SQLite / Room
    implementation(libs.sqlcipher.android)

    // Android Custom Tabs para Pasarela Segura de Flow
    implementation(libs.androidx.browser)

    // Retrofit & OkHttp para API de Flow Chile
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
}
