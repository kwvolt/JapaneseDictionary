plugins {
    id("com.android.library")
    id("app.cash.sqldelight") version "2.1.0"
    kotlin("multiplatform")
    id("kotlin-parcelize")
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.addAll(
                        listOf(
                            "-P", "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=io.github.kwvolt.japanesedictionary.util.CommonParcelize",
                            "-P", "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=io.github.kwvolt.japanesedictionary.util.CommonRawValue"
                        )
                    )
                }
            }
        }
    }
    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                }
            }
        }
    }
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
                implementation(libs.kotlinx.coroutines.test)
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
                implementation(libs.slf4j.api)
                implementation(libs.log4j.api)
                implementation(libs.log4j.core)
                implementation(libs.log4j.slf4j2.impl)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.junit.jupiter.api)
                runtimeOnly(libs.junit.jupiter.engine)
                implementation(libs.junit.jupiter.params)
                implementation(libs.sqlite.jdbc)
                implementation(libs.sqlite.driver)
                implementation(libs.kotlinx.coroutines.test)
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
    compileSdk = 36
    defaultConfig {
        minSdk = 31
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}