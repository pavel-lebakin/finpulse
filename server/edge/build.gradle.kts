plugins {
    id("finpulse.jvm.library")
}

dependencies {
    testImplementation(kotlin("test-junit5"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
