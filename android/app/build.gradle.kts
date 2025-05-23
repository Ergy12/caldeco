plugins {
    id("com.android.application")
    id("kotlin-android")
    // Le plugin Flutter Gradle doit être appliqué après les plugins Android et Kotlin Gradle.
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

    // --- DÉBUT DES MODIFICATIONS POUR LA SIGNATURE (CORRIGÉES) ---
    signingConfigs {
        // Définition de la configuration de signature 'release'
        create("release") {
            // Utiliser '=' pour l'assignation des propriétés en Kotlin DSL
            // La fonction 'file()' est utilisée pour convertir le chemin en objet File
            storeFile = file(System.getenv("KEYSTORE_PATH"))
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            // Appliquer la configuration de signature 'release' à ce buildType
            // Utiliser 'getByName("release")' ou 'release' si le nom est valide comme propriété
            signingConfig = signingConfigs.getByName("release")

            // Activer la minification et l'obfuscation pour un build de release
            // Utiliser 'isMinifyEnabled' pour les propriétés booléennes
            isMinifyEnabled = true
            // Utiliser 'proguardFiles()' comme une fonction qui prend des arguments
            // Assurez-vous que les chaînes de caractères sont entre guillemets doubles ("")
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
