import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.example.myapplication.shared.database")
            srcDirs.setFrom("src/commonMain/sqldelight")
        }
    }
}

kotlin {
    jvm()
    
    androidLibrary {
       namespace = "com.example.myapplication.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       withHostTest {
       }
       withDeviceTestBuilder {
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.sqldelight.android.driver)
            implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
            implementation(libs.bouncycastle)
            implementation(libs.androidx.security.crypto)
            implementation("androidx.documentfile:documentfile:1.0.1")
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // Core libraries
            implementation(libs.koin.core)
            implementation(libs.napier)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.sqldelight.coroutines.extensions)
            implementation(libs.kotlinx.datetime)
            
            // Ktor Networking
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.network)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
            implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
            implementation(libs.bouncycastle)
        }
    }

}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}