import com.android.build.gradle.internal.packaging.defaultExcludes
plugins {
    id("com.android.library")
    id("app.cash.sqldelight") version "2.0.2"
    kotlin("multiplatform")
}

kotlin {
    androidTarget()
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.sqldelight.runtime)
                implementation(libs.coroutines.extensions)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.sqlite.jdbc)
                implementation(libs.sqlite.driver)
                implementation(libs.kotlinx.coroutines.test.v173)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.runtime) // Android-compatible Compose
                implementation(libs.sqldelight.android.driver)
                implementation(libs.sqlite.driver)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.sqlite.driver)
                implementation(libs.slf4j.api.v209)
                implementation(libs.log4j.api.v2200)
                implementation(libs.log4j.core.v2200)
                implementation(libs.log4j.slf4j2.impl)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.junit.jupiter.api)
                runtimeOnly(libs.junit.jupiter.engine)
                implementation(libs.sqlite.jdbc)
                implementation(libs.sqlite.driver)
                implementation(libs.kotlinx.coroutines.test.v173)
            }
        }
    }
}

sqldelight {
    databases {
        create("DictionaryDB") {
            packageName.set("io.github.kwvolt.japanesedictionary.domain.data.database")
            generateAsync.set(true)
        }
    }
}

android {
    namespace = "io.github.kwvolt.japanesedictionary.Shared"
    compileSdk = 31
    defaultConfig {
        minSdk = 31
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}