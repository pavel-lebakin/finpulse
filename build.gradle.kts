plugins {
    id("finpulse.module-graph")
}

subprojects {
    version = "0.1.0"
    group = when {
        path.startsWith(":feature:") -> {
            val feature = path.split(":")[2]
            "com.lebakin.finpulse.feature.$feature"
        }
        else -> "com.lebakin.finpulse"
    }
}
