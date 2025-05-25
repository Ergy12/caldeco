plugins {
    id("com.android.application")
    id("kotlin-android")
    // Le plugin Flutter Gradle doit être appliqué après les plugins Android et Kotlin Gradle.
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    namespace = "com.votreentreprise.caldec"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    defaultConfig {
        applicationId = "com.votreentreprise.caldec"
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCodeAsInt()
        versionName = flutter.versionNameAsString()
    }

    // --- DÉBUT DES MODIFICATIONS POUR LA SIGNATURE (CORRIGÉES) ---
    signingConfigs {
        // Définition de la configuration de signature 'release'
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH"))
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    // --- FIN DES MODIFICATIONS POUR LA SIGNATURE (CORRIGÉES) ---
}

flutter {
    source = "../.."
}
