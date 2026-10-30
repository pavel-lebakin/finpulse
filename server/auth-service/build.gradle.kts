plugins {
    id("finpulse.jvm.library")
    application
}

application {
    mainClass.set("com.lebakin.finpulse.server.auth.AuthKt")
}

dependencies {
    implementation(project(":cache"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(kotlin("test-junit5"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
