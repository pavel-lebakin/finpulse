plugins {
    id("finpulse.jvm.library")
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testImplementation(libs.konsist)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
