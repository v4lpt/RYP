plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "v4lpt.vpt.f012.ryp"
    compileSdk = 34

    defaultConfig {
        applicationId = "v4lpt.vpt.f012.ryp"
        minSdk = 24
        targetSdk = 34
        versionCode = 104
        versionName = "1.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    implementation(libs.documentfile)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

}