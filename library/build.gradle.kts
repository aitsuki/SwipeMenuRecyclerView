plugins {
    id("com.android.library")
    id("maven-publish")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.aitsuki"
            artifactId = "SwipeMenuRecyclerView"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

android {
    namespace = "com.aitsuki.swipe"

    publishing {
        singleVariant("release")
    }

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
