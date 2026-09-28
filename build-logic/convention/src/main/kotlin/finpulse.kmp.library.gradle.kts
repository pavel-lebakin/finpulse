import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

val compileSdkValue = providers.gradleProperty("finpulse.compileSdk").get().toInt()
val compileSdkMinorValue = providers.gradleProperty("finpulse.compileSdkMinor").get().toInt()
val minSdkValue = providers.gradleProperty("finpulse.minSdk").get().toInt()
val namespaceValue = checkNotNull(findProperty("finpulse.namespace") as String?) {
    "Set finpulse.namespace in ${project.path}/gradle.properties"
}

kotlin {
    android {
        namespace = namespaceValue
        compileSdk {
            version = release(compileSdkValue) {
                minorApiLevel = compileSdkMinorValue
            }
        }
        minSdk = minSdkValue
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    iosArm64()
    iosSimulatorArm64()
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    sourceSets.named("commonTest") {
        dependencies {
            implementation(kotlin("test"))
        }
    }
}
