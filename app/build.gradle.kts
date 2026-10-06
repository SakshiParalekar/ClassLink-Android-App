
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.classlink"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.classlink"
        minSdk = 24
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)

    // Realtime Database //chatgptcode
    implementation ("com.google.firebase:firebase-database:20.3.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.0")

    // Firebase Firestore
    implementation ("com.google.firebase:firebase-firestore:25.0.0") // Check for the latest version

    // Firebase Storage
    implementation ("com.google.firebase:firebase-storage:21.0.0") // Check for the latest version

    // Material Design (for attractive UI components)
    implementation ("com.google.android.material:material:1.12.0") // Check for the latest version

    //chatgpt
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("com.google.firebase:firebase-bom:33.2.0")

    // For image loading (optional, but highly recommended for image preview from URI)
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.cardview)
    implementation(libs.vision.common)
    implementation(libs.play.services.mlkit.text.recognition.common)
    implementation(libs.play.services.mlkit.text.recognition)
    implementation(libs.firebase.messaging)
    implementation(libs.room.common.jvm)
    implementation(libs.room.runtime.android)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")


    implementation ("com.github.bumptech.glide:glide:4.14.2")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.14.2")
    implementation ("com.makeramen:roundedimageview:2.3.0")


    implementation ("com.android.volley:volley:1.2.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("androidx.cardview:cardview:1.0.0")


  // Gemini API dependencies


    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation ("com.google.firebase:firebase-database:20.3.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    implementation ("androidx.annotation:annotation:1.3.0")

    implementation ("com.airbnb.android:lottie:6.0.0")
    // ML Kit Text Recognition






    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

