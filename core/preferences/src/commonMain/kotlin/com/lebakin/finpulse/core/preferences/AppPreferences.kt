package com.lebakin.finpulse.core.preferences

data class AppPreferences(
    val showcase: ShowcasePreferences,
    val onboarding: OnboardingPreferences,
    val consent: ConsentPreferences,
    val theme: ThemePreferences,
)
