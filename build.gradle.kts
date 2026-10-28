plugins {
    kotlin("jvm") version "2.4.0"
    id("org.jetbrains.intellij.platform") version "2.19.0"
    id("org.sonarqube") version "6.3.1.5724"
}
sonar {
    properties {
        property("sonar.projectKey", "MaheshBabu11_http-client-plus")
        property("sonar.organization", "maheshbabu11")
    }
}

group = "dev.maheshbabu11"
version = "1.1.0"

// Map a simple IDE line selector (build baseline) to product version string
val ideLine: String = providers.gradleProperty("ideLine").orNull ?: "262"
val ideVersion: String = providers.gradleProperty("overrideIdeVersion").orNull ?: when (ideLine) {
    "262" -> "2026.2"
    "261" -> "2026.1"
    "253" -> "2025.3"
    "252" -> "2025.2"
    "251" -> "2025.1"
    "243" -> "2024.3"
    "242" -> "2024.2"
    "241" -> "2024.1"
    // Allow passing a full product version like 2024.3.2
    else -> ideLine
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

val gsonVersion = "2.11.0"
dependencies {
    implementation("com.google.code.gson:gson:$gsonVersion")
    intellijPlatform {
        create("IU", ideVersion)
        bundledPlugin("com.intellij.java")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

intellijPlatform {
    instrumentCode.set(true)
    buildSearchableOptions.set(false)
    pluginVerification {
        ides {
            current()
        }
    }
}

kotlin {
    jvmToolchain(25)
}

// Ensure Kotlin compiles for JVM 25
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
}

// Produce a plugin ZIP via the build task by default
tasks.named("build") {
    dependsOn("buildPlugin")
}


tasks {
    // Baseline build number (sinceBuild) tracks ideLine;
    patchPluginXml {
        sinceBuild.set("242")
    }

    // Name the ZIP distinctly per IDE line to avoid collisions
    buildPlugin {
        archiveFileName.set("${project.name}-${project.version}-${ideLine}.zip")
    }

    runIde {
        jvmArgs = listOf(
            "-Xmx2g",
            "-Didea.is.internal=true",
            "-Didea.debug.mode=true",
            "-Didea.ProcessCanceledException=disabled"
        )
    }

    // Copy the built ZIP to dist/<version>/ after build
    val buildPluginTask = named<Zip>("buildPlugin")
    val copyPluginToDist = register<Copy>("copyPluginToDist") {
        dependsOn(buildPluginTask)
        from(buildPluginTask.flatMap { it.archiveFile })
        into(layout.projectDirectory.dir("dist/${project.version}"))
    }

    // Ensure build triggers buildPlugin and then copies to dist
    named("build") {
        dependsOn(buildPluginTask)
        finalizedBy(copyPluginToDist)
    }
}
