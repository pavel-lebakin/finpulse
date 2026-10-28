plugins {
    id("finpulse.jvm.library")
}

dependencies {
    implementation(libs.kafka.clients)
    testImplementation(kotlin("test-junit5"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
