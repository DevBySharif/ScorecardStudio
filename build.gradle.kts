buildscript {
  repositories {
    google()
    mavenCentral()
  }
  dependencies {
    classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.7")
    classpath("com.google.gms:google-services:4.4.2")
  }
}

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.kotlin.serialization) apply false
}
