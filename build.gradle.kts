import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
	mavenCentral()
	google()
}

plugins {
	id("org.jetbrains.kotlin.jvm") version "2.0.21"
	//noinspection JavaPluginLanguageLevel
	id("java-library")
	id("dev.frozenmilk.publish")
}

kotlin {
	jvmToolchain(17)
	compilerOptions {
		freeCompilerArgs.add("-Xjvm-default=all")
	}
	coreLibrariesVersion = "1.9.24"
}

tasks.withType<KotlinCompile>().configureEach {
	compilerOptions.apiVersion.set(KotlinVersion.KOTLIN_1_9)
}

dependencies {
	//noinspection GradleDependency
	implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")
	implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
	compileOnly(gradleApi())
}

publishing {
	publications {
		create<MavenPublication>("EasyAutoLibraries") {
			groupId = "dev.frozenmilk"
			artifactId = "EasyAutoLibraries"

			afterEvaluate {
				from(components["java"])
			}
		}
	}
}