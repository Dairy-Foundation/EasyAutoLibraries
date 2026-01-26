import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode

repositories {
    mavenCentral()
    google()
    maven("https://repo.dairy.foundation/releases")
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("java-library")
    id("dev.frozenmilk.publish")
    id("dev.frozenmilk.doc")
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-Xreturn-value-checker=full")
        jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY)
    }
    coreLibrariesVersion = "1.9.24"
}

dependencies {
    //noinspection GradleDependency
    api("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.0")
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("EasyAutoLibraries") {
            groupId = "dev.frozenmilk"
            artifactId = "EasyAutoLibraries"

            artifact(dairyDoc.dokkaHtmlJar)
            artifact(dairyDoc.dokkaJavadocJar)

            afterEvaluate {
                from(components["java"])
            }
        }
    }
}