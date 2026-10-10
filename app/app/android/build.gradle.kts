import com.android.build.api.dsl.ProductFlavor
import com.momosi.trucktrack.utils.stringProperty

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.trucktrack.spotless)
    alias(libs.plugins.trucktrack.android.signing)
    alias(libs.plugins.trucktrack.detekt)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.momosi.trucktrack"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.momosi.trucktrack"
        targetSdk = 37
        minSdk = 28
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

        versionCode = stringProperty("appVersionCode")?.toIntOrNull() ?: 1
        versionName = stringProperty("appVersionName") ?: "dev"
    }

    flavorDimensions += "environment"
    productFlavors {
        create("staging") {
            dimension = "environment"
            isDefault = true
            applicationIdSuffix = ".staging"
            appConfig(
                apiBaseUrl = "https://tt.momosi.org/",
                realmUrl = "https://sso.momosi.org/realms/trucktrack/",
                oauthClientId = "trucktrack-app",
                appScheme = "com.momosi.trucktrack.staging",
            )
        }
        create("prod") {
            dimension = "environment"
            appConfig(
                apiBaseUrl = "https://transroute.org/",
                realmUrl = "https://sso.transroute.org/realms/transroute/",
                oauthClientId = "trucktrack-mobile",
                appScheme = "com.momosi.trucktrack",
            )
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        sarifReport = true
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        )
    }
}

dependencies {
    implementation(projects.app.shared)
    implementation(projects.core.common)
    implementation(projects.core.network)
    implementation(projects.core.user)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.coil.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
}

private fun ProductFlavor.appConfig(apiBaseUrl: String, realmUrl: String, oauthClientId: String, appScheme: String) {
    buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    buildConfigField("String", "REALM_URL", "\"$realmUrl\"")
    buildConfigField("String", "OAUTH_CLIENT_ID", "\"$oauthClientId\"")
    buildConfigField("String", "APP_SCHEME", "\"$appScheme\"")
    manifestPlaceholders["oidcRedirectScheme"] = appScheme
}
