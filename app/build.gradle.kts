plugins {
    alias(libs.plugins.hm.android.application)
    alias(libs.plugins.hm.android.hilt)
    alias(libs.plugins.hm.android.application.compose)
    id("com.google.android.gms.oss-licenses-plugin")
    alias(libs.plugins.org.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.coldblue.haru_mandalart"

    defaultConfig {
        applicationId = "com.coldblue.haru_mandalart"
            versionCode = 16
            versionName = "1.4"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        signingConfig = signingConfigs.getByName("debug")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}


dependencies {
    implementation(project(":feature:todo"))
    implementation(project(":feature:manda"))
    implementation(project(":feature:tutorial"))
    implementation(project(":feature:setting"))
    implementation(project(":feature:login"))
    implementation(project(":feature:notice"))
    implementation(project(":feature:survey"))
    implementation(project(":feature:explain"))
    implementation(project(":feature:history"))
    implementation(project(":feature:alarm"))

    implementation(project(":core:designsystem"))
    implementation(project(":core:data"))

    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.ext.work)
    implementation(libs.androidx.work.ktx)
    implementation (libs.play.services.oss.licenses)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.lifecycle.process)
}