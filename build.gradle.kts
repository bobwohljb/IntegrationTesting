import com.jetbrains.plugin.structure.base.utils.contentBuilder.buildDirectory
import com.jetbrains.plugin.structure.base.utils.contentBuilder.buildZipFile
import org.gradle.kotlin.dsl.intellijPlatform
import java.util.*
import org.jetbrains.intellij.platform.gradle.*
import org.jetbrains.intellij.platform.gradle.models.*
import org.jetbrains.intellij.platform.gradle.tasks.*

plugins {
    id("org.jetbrains.intellij.platform") version "2.2.1"
    kotlin("jvm") version "2.0.0"
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}


sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.3")
        //intellijIdeaUltimate("2025.1")
        testFramework(TestFrameworkType.Starter)
    }
    integrationTestImplementation("com.jetbrains.intellij.tools:ide-metrics-collector:LATEST-EAP-SNAPSHOT")
    integrationTestImplementation("com.jetbrains.intellij.tools:ide-metrics-collector-starter:LATEST-EAP-SNAPSHOT")
    integrationTestImplementation("com.jetbrains.intellij.tools:ide-performance-testing-commands:LATEST-EAP-SNAPSHOT")
    integrationTestImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    integrationTestImplementation("org.kodein.di:kodein-di-jvm:7.20.2")
    integrationTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.1")

}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
//            sinceBuild.set("243")
//            untilBuild.set("243.*")
            sinceBuild.set("231")
            untilBuild.set("999.*")
        }
    }
}

kotlin {
    jvmToolchain(17)
}

val integrationTest = task<Test>("integrationTest") {
    val integrationTestSourceSet = sourceSets.getByName("integrationTest")
    testClassesDirs = integrationTestSourceSet.output.classesDirs
    classpath = integrationTestSourceSet.runtimeClasspath
    systemProperty("path.to.build.plugin", tasks.prepareSandbox.get().pluginDirectory.get().asFile)
    useJUnitPlatform()
    dependsOn(tasks.prepareSandbox)
}