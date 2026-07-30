plugins {
    alias(libs.plugins.android.application) apply false
    // Declare kotlin and ksp plugin aliases here as 'apply false' so subprojects
    // can apply them without attempting to re-register extensions at the root.
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false

    // Firebase plugin
    alias(libs.plugins.google.services) apply false
}