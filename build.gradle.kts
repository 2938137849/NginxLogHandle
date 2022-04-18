import org.codehaus.groovy.control.CompilerConfiguration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.6.20"
	java
}

group = "me.bin"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = CompilerConfiguration.JDK17
}

tasks.jar.configure {
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
	manifest.attributes["Main-Class"] = "my.test.MainKt"
	from(configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) })
}
