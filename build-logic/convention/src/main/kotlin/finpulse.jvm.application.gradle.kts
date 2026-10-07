plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("finpulse.detekt")
    id("finpulse.licensee")
}

kotlin {
    jvmToolchain(21)
}
