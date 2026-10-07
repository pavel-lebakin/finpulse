plugins {
    id("org.jetbrains.kotlin.jvm")
    id("finpulse.detekt")
    id("finpulse.licensee")
}

kotlin {
    jvmToolchain(21)
}
