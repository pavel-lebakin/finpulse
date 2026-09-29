plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.kotlin.compose.compiler.gradlePlugin)
    implementation(libs.kotlin.serialization.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
}
