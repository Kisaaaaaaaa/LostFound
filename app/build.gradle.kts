plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.lostfound"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.lostfound"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Lifecycle (ViewModel & LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")

    // WorkManager
    implementation("androidx.work:work-runtime:2.9.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Amap (高德地图) 
    // 3D地图包 (9.5.0版本通常包含定位核心类，但没有搜索类)
    implementation("com.amap.api:3dmap:9.5.0")
    // 搜索包 (必须单独引入才能使用 GeocodeSearch 等)
    implementation("com.amap.api:search:9.4.5")
    // 注意：这里不再单独引入 location 包，以避免 Duplicate class 冲突

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}