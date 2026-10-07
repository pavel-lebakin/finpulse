import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("finpulse.detekt")
    id("finpulse.licensee")
}

extensions.configure<ApplicationExtension>("android") {
    compileSdk {
        version = release(providers.gradleProperty("finpulse.compileSdk").get().toInt()) {
            minorApiLevel = providers.gradleProperty("finpulse.compileSdkMinor").get().toInt()
        }
    }
    defaultConfig {
        minSdk = providers.gradleProperty("finpulse.minSdk").get().toInt()
        targetSdk = providers.gradleProperty("finpulse.targetSdk").get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
}
