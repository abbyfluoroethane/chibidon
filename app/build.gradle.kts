plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
}

android {
	namespace = "org.chibidon"
	compileSdk = 35

	defaultConfig {
		applicationId = "org.chibidon"
		minSdk = 30
		targetSdk = 34
		versionCode = 1
		versionName = "0.1.0"
	}

	buildTypes {
		release {
			isMinifyEnabled = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	kotlinOptions {
		jvmTarget = "17"
	}

	buildFeatures {
		compose = true
		buildConfig = true
	}

	composeOptions {
		kotlinCompilerExtensionVersion = "1.5.8"
	}
}

dependencies {
	// Wear Compose (M3 Expressive)
	val wearComposeVersion = "1.5.0"
	implementation("androidx.wear.compose:compose-material3:$wearComposeVersion")
	implementation("androidx.wear.compose:compose-foundation:$wearComposeVersion")
	implementation("androidx.wear.compose:compose-navigation:$wearComposeVersion")

	// Compose
	implementation(platform("androidx.compose:compose-bom:2024.06.00"))
	implementation("androidx.compose.ui:ui")
	implementation("androidx.compose.ui:ui-tooling-preview")
	implementation("androidx.compose.foundation:foundation")
	implementation("androidx.compose.material:material")
	implementation("androidx.compose.material:material-icons-extended")
	debugImplementation("androidx.compose.ui:ui-tooling")

	// Activity & Lifecycle
	implementation("androidx.activity:activity-compose:1.9.0")
	implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
	implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")

	// Wear
	implementation("androidx.wear:wear:1.3.0")
	implementation("androidx.wear:wear-input:1.1.0")
	implementation("com.google.android.gms:play-services-wearable:18.1.0")

	// Networking
	implementation("com.squareup.okhttp3:okhttp:4.12.0")
	implementation("com.google.code.gson:gson:2.10.1")

	// Coroutines
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

	// Image loading
	implementation("io.coil-kt:coil-compose:2.6.0")
}
