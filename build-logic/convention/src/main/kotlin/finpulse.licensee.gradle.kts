import app.cash.licensee.LicenseeExtension
import app.cash.licensee.UnusedAction

plugins {
    id("app.cash.licensee")
}

extensions.configure<LicenseeExtension>("licensee") {
    allow("Apache-2.0")
    allow("MIT")
    allow("BSD-2-Clause")
    allow("BSD-3-Clause")
    // The allowlist is the policy. An identifier that no current dependency uses is not a violation.
    unusedAction(UnusedAction.IGNORE)
    // Reviewed exception: on-device receipt OCR. These coordinates are not SPDX.
    allowDependency("com.google.mlkit", "text-recognition", "16.0.1") {
        because("On-device Latin receipt text recognition. ML Kit Terms of Service.")
    }
    allowDependency("com.google.mlkit", "text-recognition-bundled-common", "17.0.0") {
        because("Bundled Latin model for ML Kit text recognition. ML Kit Terms of Service.")
    }
    allowDependency("com.google.mlkit", "common", "18.11.0") {
        because("Required by ML Kit text recognition. ML Kit Terms of Service.")
    }
    allowDependency("com.google.mlkit", "vision-common", "17.3.0") {
        because("Required by ML Kit text recognition. ML Kit Terms of Service.")
    }
    allowDependency("com.google.mlkit", "vision-interfaces", "16.3.0") {
        because("Required by ML Kit text recognition. ML Kit Terms of Service.")
    }
    allowDependency("com.google.android.gms", "play-services-mlkit-text-recognition", "19.0.1") {
        because("Required by ML Kit text recognition. ML Kit Terms of Service.")
    }
    allowDependency("com.google.android.gms", "play-services-mlkit-text-recognition-common", "19.1.0") {
        because("Required by ML Kit text recognition. ML Kit Terms of Service.")
    }
    allowDependency("com.google.android.gms", "play-services-base", "18.5.0") {
        because("Required by ML Kit text recognition. Android SDK license.")
    }
    allowDependency("com.google.android.gms", "play-services-basement", "18.4.0") {
        because("Required by ML Kit text recognition. Android SDK license.")
    }
    allowDependency("com.google.android.gms", "play-services-tasks", "18.2.0") {
        because("Required by ML Kit text recognition. Android SDK license.")
    }
    allowDependency("com.google.android.odml", "image", "1.0.0-beta1") {
        because("Required by ML Kit text recognition. Android SDK license.")
    }
    // Reviewed exception: on-device Gemini Nano. These coordinates are not SPDX.
    allowDependency("com.google.mlkit", "genai-prompt", "1.0.0-beta5") {
        because("On-device Gemini Nano prompt API. ML Kit Terms of Service.")
    }
    allowDependency("com.google.mlkit", "genai-common", "1.0.0-beta5") {
        because("Required by the ML Kit prompt API. ML Kit Terms of Service.")
    }
    allowDependency("com.google.mlkit", "genai-schema", "1.0.0-alpha2") {
        because("Required by the ML Kit prompt API. Android Software Development Kit License.")
    }
    allowDependency("com.google.android.gms", "play-services-basement", "18.9.0") {
        because("Required by the ML Kit prompt API. Android Software Development Kit License.")
    }
    allowDependency("org.reactivestreams", "reactive-streams", "1.0.3") {
        because("CC0-1.0 streams API pulled by the ML Kit prompt API through kotlinx-coroutines-reactive.")
    }
    // Reviewed exception: OkHttp pulls this API. The license is MIT.
    // Licensee 1.14.1 does not map https://opensource.org/license/mit to SPDX MIT.
    allowDependency("org.slf4j", "slf4j-api", "2.0.19") {
        because("MIT logging API pulled by the Ktor OkHttp engine. The published URL is not mapped to SPDX MIT.")
    }
}
