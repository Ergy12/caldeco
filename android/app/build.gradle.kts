plugins {
    id("com.android.application")
    id("kotlin-android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    namespace = "com.example.helloworld"
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
        applicationId = "com.example.helloworld"
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
    }

    // --- DÉBUT DES MODIFICATIONS POUR LA SIGNATURE ---
    signingConfigs {
        release {
            // Pour un test simple, nous lisons directement depuis les variables d'environnement.
            // Rappel : ce n'est PAS sécurisé pour la production.
            storeFile file(System.getenv("KEYSTORE_PATH"))
            storePassword System.getenv("KEYSTORE_PASSWORD")
            keyAlias System.getenv("KEY_ALIAS")
            keyPassword System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            // Activez la minification et l'obfuscation pour un build de release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'

            // Appliquez la configuration de signature 'release' que nous venons de définir
            signingConfig signingConfigs.release
        }
    }
    // --- FIN DES MODIFICATIONS POUR LA SIGNATURE ---
}

flutter {
    source = "../.."
}
