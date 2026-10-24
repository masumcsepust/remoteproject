plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.harf.ai"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.harf.ai"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"

        buildConfigField(
            "String",
            "VERIFY_API_BASE_URL",
            "\"https://quran-python-api.whiteground-9f1dac8d.eastasia.azurecontainerapps.io\""
        )
        // MVP-only PIN that unlocks the Hujur (teacher) screen on this device.
        buildConfigField("String", "HUJUR_PIN", "\"1234\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.okhttp)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
