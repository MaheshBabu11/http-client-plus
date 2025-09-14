plugins {
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.intellij") version "1.17.3"
    id("org.sonarqube") version "6.3.1.5724"
}
sonar {
    properties {
        property("sonar.projectKey", "MaheshBabu11_http-client-plus")
        property("sonar.organization", "maheshbabu11")
    }
}

group = "dev.maheshbabu11"
version = "1.0.7"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

// Map a simple IDE line selector (build baseline) to product version string
val ideLine: String = providers.gradleProperty("ideLine").orNull ?: "252"
val ideVersion: String = providers.gradleProperty("overrideIdeVersion").orNull ?:when (ideLine) {
    "252" -> "2025.2"
    "251" -> "2025.1"
    "243" -> "2024.3"
    "242" -> "2024.2"
    "241" -> "2024.1"
    // Allow passing a full product version like 2024.3.2
    else -> ideLine
}

intellij {
    version.set(ideVersion)
    type.set("IU")
    plugins.set(
        listOf(
            "com.intellij.java"
        )
    )
    instrumentCode.set(true)
    updateSinceUntilBuild.set(false)
}

kotlin {
    jvmToolchain(17)
}

// Ensure Kotlin compiles for JVM 17
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
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

    buildSearchableOptions {
        enabled = false
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

    // Verify plugin against the selected IDE version
    runPluginVerifier {
        ideVersions.set(listOf(ideVersion))
    }
    verifyPlugin {
      }

    // Copy the built ZIP to dist/<version>/ after build
    val buildPluginTask = named<org.gradle.api.tasks.bundling.Zip>("buildPlugin")
    val copyPluginToDist by registering(Copy::class) {
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
