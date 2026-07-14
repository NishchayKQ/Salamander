import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.compose.compiler)

    // Kotlin serialization plugin for type safe routes and navigation arguments
    // https://kotlinlang.org/docs/serialization.html#add-plugins-and-dependencies
    kotlin("plugin.serialization") version "2.2.20"

    // support for Parcelable
    id("kotlin-parcelize")
}

// https://developer.android.com/jetpack/androidx/releases/room#gradle-plugin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// https://stackoverflow.com/questions/77363060/how-to-replace-the-deprecated-kotlinoptions-in-a-java-library-kotlin-module
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

android {
    namespace = "nish.wry.salamander"
    compileSdk = 37

    defaultConfig {
        applicationId = "nish.wry.salamander"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
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
        // Flag to enable support for the new language APIs
        // For AGP 4.1+
        isCoreLibraryDesugaringEnabled = true

        // Sets Java compatibility to Java 17 (needed for modern hilt & compose - https://developer.android.com/training/dependency-injection/hilt-android#setup
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // https://stackoverflow.com/a/79682146/23121081
    // https://kotlinlang.org/docs/opt-in-requirements.html#opt-in-a-module
    // removing this results in dao implementation not having opt in and thus build failing
    // issue tracker - https://issuetracker.google.com/issues/410607888
    kotlin.compilerOptions.optIn.add("kotlin.time.ExperimentalTime")

    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    //testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // NavHost and NavHostController's
    implementation(libs.androidx.navigation.compose)

    // viewmodel
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    //Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // JSON serialization library, works with the Kotlin serialization plugin
    implementation(libs.kotlinx.serialization.json)

    // for window size class
    implementation(libs.androidx.material3.window.size)

    // see https://developer.android.com/studio/write/java8-support#library-desugaring
    // from https://stackoverflow.com/questions/56695997/how-to-fix-call-requires-api-level-26-current-min-is-25-error-in-android
    // For AGP 7.4+
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // should have added this before, I NEED updated sqlite please.
    // https://developer.android.com/jetpack/androidx/releases/sqlite#kts
    implementation(libs.androidx.sqlite.ktx)

    // enters the demon - paging 3 - https://developer.android.com/jetpack/androidx/releases/paging#kts
    implementation(libs.androidx.paging.runtime)
//    Jetpack Compose integration
    implementation(libs.androidx.paging.compose)

    // https://stackoverflow.com/questions/75151424/android-paging-3-codelabs-failed-on-room-paging
    // https://maven.google.com/web/index.html?q=paging#androidx.room:room-paging
    implementation(libs.androidx.room.paging)

    implementation(libs.hilt.android) //for hilt https://developer.android.com/training/dependency-injection/hilt-android
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)


//    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
//
//
//    implementation("org.jetbrains.skiko:skiko:0.8.4")
//    // The host OS and architecture should be specified explicitly.
//    implementation("org.jetbrains.skiko:skiko-awt-runtime-macos-x64:0.8.4")
//
//    // Lets-Plot Kotlin API
//    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-kernel:4.9.3")
//
//    // Lets-Plot Multiplatform
//    implementation("org.jetbrains.lets-plot:lets-plot-common:4.5.2")
//    implementation("org.jetbrains.lets-plot:platf-awt:4.5.2")
//
//    // Lets-Plot Skia Frontend
//    implementation("org.jetbrains.lets-plot:lets-plot-swing-skia:2.1.1")
}