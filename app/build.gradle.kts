plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.main.accessible.tz"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.main.accessible.tz"
        minSdk = 24
        targetSdk = 33
        versionCode = 15
        versionName = "1.4.9"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        //这2个为非必选，想用哪个就保留那个 用的话一定要加上项目中的 ViewBinding & DataBinding 混淆规则
        dataBinding = true
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation( "com.squareup.retrofit2:retrofit:2.9.0")
    implementation( "com.squareup.retrofit2:converter-gson:2.9.0")
    implementation( "com.squareup.okhttp3:okhttp:4.11.0")
    implementation( "com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation( "androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation( "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation( "com.tencent.bugly:crashreport:latest.release")

    //dialog
    implementation( "com.afollestad.material-dialogs:lifecycle:3.3.0")
            implementation( "com.afollestad.material-dialogs:core:3.3.0")
            implementation( "com.afollestad.material-dialogs:color:3.3.0")
            implementation( "com.afollestad.material-dialogs:datetime:3.3.0")
            implementation( "com.afollestad.material-dialogs:bottomsheets:3.3.0")
    /*debugImplementation( "com.squareup.leakcanary:leakcanary-android:2.7")*/
}