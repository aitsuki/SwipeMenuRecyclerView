plugins {
    id("com.android.library")
}

android {
    namespace = "com.aitsuki.swipe"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    compileOnly("androidx.recyclerview:recyclerview:1.4.0")
}
