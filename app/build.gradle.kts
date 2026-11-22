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

fun localProp(name: String, default: String = ""): String =
    (localProperties.getProperty(name) ?: System.getenv(name) ?: default)
        .trim()
        .removeSurrounding("\"")

val flowApiKey = localProp("FLOW_API_KEY")
val flowSecretKey = localProp("FLOW_SECRET_KEY")
val flowEnv = localProp("FLOW_ENVIRONMENT", "sandbox")
val appwriteEndpoint = localProp("APPWRITE_ENDPOINT")
val appwriteProjectId = localProp("APPWRITE_PROJECT_ID")
val appwriteBucketId = localProp("APPWRITE_BUCKET_ID")
val keystoreFilePath = localProp("KEYSTORE_FILE", "keystore/gsm-release.jks")
val ksStorePassword = localProp("KEYSTORE_PASSWORD")
val ksAlias = localProp("KEY_ALIAS")
val ksKeyPassword = localProp("KEY_PASSWORD", ksStorePassword)

android {
    namespace = "com.example.gsm"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.gsm"
        minSdk = 26
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

    signingConfigs {
        val ksFile = rootProject.file(keystoreFilePath)
        if (ksFile.exists() && ksStorePassword.isNotBlank() && ksAlias.isNotBlank()) {
            create("release") {
                storeFile = ksFile
                storePassword = ksStorePassword
                keyAlias = ksAlias
                keyPassword = ksKeyPassword
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            optimization {
                enable = false
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
    testImplementation(libs.kotlinxCoroutinesTest)
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
    implementation(libs.firebase.messaging)
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
