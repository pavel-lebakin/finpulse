plugins {
    id("finpulse.jvm.library")
    application
}

application {
    mainClass.set("com.lebakin.finpulse.server.gateway.GatewayKt")
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(kotlin("test-junit5"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
