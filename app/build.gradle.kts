plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.devtools.ksp") version "1.9.22-1.0.16"
}

android {
    namespace = "com.example.sprayconnectapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sprayconnectapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {


    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.compiler)
    implementation(libs.androidx.foundation)
    implementation(libs.material)
    implementation(libs.androidx.animation.core)
    implementation(libs.androidx.foundation.layout)
    ksp("androidx.room:room-compiler:2.6.1")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended:<compose-version>")

    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.3")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.compose.ui:ui:1.6.1")
    implementation("androidx.compose.ui:ui-text:1.6.1")
    implementation("androidx.exifinterface:exifinterface:1.3.7")


    implementation("androidx.compose.material:material-icons-extended")


    implementation("androidx.compose.animation:animation:<compose-version>")
    implementation("androidx.compose.material:material-icons-extended:<compose-version>")




    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
configurations.all {
    exclude(group = "com.google.guava", module = "listenablefuture")
}
