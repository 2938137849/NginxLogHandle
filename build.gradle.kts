import org.codehaus.groovy.control.CompilerConfiguration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.6.20"
	java
	id("org.jetbrains.kotlin.plugin.serialization") version "1.6.20"
}

group = "me.bin"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}
dependencies {
	implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.0")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.3.2")
}

tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = CompilerConfiguration.JDK17
}

tasks.jar.configure {
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
	from(configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) })
}
