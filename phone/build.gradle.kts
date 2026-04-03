plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
}

android {
	namespace = "org.chibidon.phone"
	compileSdk = 35

	defaultConfig {
		applicationId = "org.chibidon"
		minSdk = 26
		targetSdk = 34
		versionCode = 1
		versionName = "0.1.0"
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	kotlinOptions {
		jvmTarget = "17"
	}
}

dependencies {
	implementation("com.google.android.gms:play-services-wearable:18.1.0")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
}
