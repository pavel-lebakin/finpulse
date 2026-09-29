import dev.detekt.gradle.extensions.DetektExtension

plugins {
    id("dev.detekt")
}

extensions.configure<DetektExtension>("detekt") {
    buildUponDefaultConfig.set(true)
    parallel.set(true)
    source.setFrom(
        fileTree(layout.projectDirectory.dir("src")) {
            include("**/*.kt")
        },
    )
}
