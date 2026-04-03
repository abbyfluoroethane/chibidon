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

	signingConfigs {
		create("release") {
			val keystorePath = System.getenv("KEYSTORE_PATH")
			if (keystorePath != null) {
				storeFile = file(keystorePath)
				storePassword = System.getenv("KEYSTORE_PASSWORD")
				keyAlias = System.getenv("KEY_ALIAS")
				keyPassword = System.getenv("KEY_PASSWORD")
			}
		}
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			val keystorePath = System.getenv("KEYSTORE_PATH")
			if (keystorePath != null) {
				signingConfig = signingConfigs.getByName("release")
			}
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
	}

	composeOptions {
		kotlinCompilerExtensionVersion = "1.5.8"
	}
}

dependencies {
	// Compose
	implementation(platform("androidx.compose:compose-bom:2024.06.00"))
	implementation("androidx.compose.material3:material3")
	implementation("androidx.compose.ui:ui")
	implementation("androidx.compose.ui:ui-tooling-preview")
	debugImplementation("androidx.compose.ui:ui-tooling")

	// Activity & Lifecycle
	implementation("androidx.activity:activity-compose:1.9.0")
	implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
	implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")

	// Custom Tabs
	implementation("androidx.browser:browser:1.8.0")

	// Networking (for OAuth + server search)
	implementation("com.squareup.okhttp3:okhttp:4.12.0")
	implementation("com.google.code.gson:gson:2.10.1")

	// Wearable Data Layer
	implementation("com.google.android.gms:play-services-wearable:18.1.0")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
}
