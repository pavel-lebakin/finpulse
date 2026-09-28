plugins {
    id("finpulse.jvm.application")
}

dependencies {
    implementation(projects.catalog.catalogShared)
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "com.lebakin.finpulse.catalog.MainKt"
    }
}
