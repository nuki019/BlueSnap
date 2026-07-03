import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// 从 local.properties 读取 API 配置（Key 不入库）
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

fun prop(name: String, default: String): String {
    val envName = name.uppercase().replace(".", "_")
    val prefixedEnvName = "BLUESNAP_$envName"
    return System.getenv(prefixedEnvName)
        ?: System.getenv(envName)
        ?: localProps.getProperty(name, default)
}

fun quotedBuildConfig(value: String): String =
    "\"" + value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"") + "\""

android {
    namespace = "com.example.bluesnap"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.bluesnap"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "AI_PROVIDER", quotedBuildConfig(prop("ai.provider", "vivo")))
        buildConfigField("String", "AI_FALLBACK_PROVIDER", quotedBuildConfig(prop("ai.fallback.provider", "mock")))
        buildConfigField("boolean", "AI_DEMO_MODE", prop("ai.demo.mode", "false").toBooleanStrictOrNull()?.toString() ?: "false")
        buildConfigField("String", "AI_API_KEY", quotedBuildConfig(prop("ai.api.key", "YOUR_API_KEY_HERE")))
        buildConfigField("String", "AI_BASE_URL", quotedBuildConfig(prop("ai.base.url", "https://api-ai.vivo.com.cn/v1")))
        buildConfigField("String", "AI_MODEL", quotedBuildConfig(prop("ai.model", "Doubao-Seed-2.0-pro")))
        buildConfigField("String", "AI_FALLBACK_API_KEY", quotedBuildConfig(prop("ai.fallback.api.key", "")))
        buildConfigField("String", "AI_FALLBACK_BASE_URL", quotedBuildConfig(prop("ai.fallback.base.url", "https://api.deepseek.com")))
        buildConfigField("String", "AI_FALLBACK_MODEL", quotedBuildConfig(prop("ai.fallback.model", "deepseek-chat")))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("contestDemo") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
