plugins {
    id("finpulse.jvm.library")
}

dependencies {
    implementation(libs.postgresql)
    testImplementation(kotlin("test-junit5"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
