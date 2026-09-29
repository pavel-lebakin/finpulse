plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("finpulse.detekt")
}

kotlin {
    jvmToolchain(21)
}
