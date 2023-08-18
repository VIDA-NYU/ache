plugins {
    id("java")
    id("idea")
    id("application")
    id("jacoco")
    id("com.github.kt3k.coveralls") version "2.12.2"
}

version = "0.16.0-SNAPSHOT"

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

application {
    mainClass = "achecrawler.Main"
    applicationDefaultJvmArgs = listOf("-Dname=ache -XX:+HeapDumpOnOutOfMemoryError")
}

dependencies {
    // Sub-projects
    implementation(project(":crawler-commons"))
    // Logging
    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)
    // Commons
    implementation(libs.guava)
    implementation(libs.commons.lang3)
    implementation(libs.commons.compress)
    implementation(libs.commons.codec)
    implementation(libs.commons.validator)
    implementation(libs.crawler.commons)
    // CLI
    implementation(libs.airline)
    // Data serialization
    implementation(libs.jackson.core.databind)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.dataformat.cbor)
    implementation(libs.kryo.lib)
    implementation(libs.kryo.serializers)
    // REST server dependencies
    implementation(libs.javalin)
    // Metrics and monitoring
    implementation(libs.dropwizard.metrics.core)
    implementation(libs.dropwizard.metrics.json)
    implementation(libs.dropwizard.metrics.jvm)
    // Data management and repositories
    implementation(libs.rocksdbjni)
    implementation(libs.elasticsearch.rest.client)
    implementation(libs.kafka.clients)
    implementation(libs.webarchive.commons) {
        exclude(group = "org.apache.hadoop", module = "hadoop-core")
        exclude(group = "junit", module = "junit")
    }
    // Data parsing and extraction
    implementation(libs.boilerpipe)
    implementation(libs.nekohtml)
    implementation(libs.jsoup)
    implementation(libs.lucene.analyzers.common)
    // HTTP libraries
    implementation(libs.okhttp)
    implementation(libs.httpclient)
    // Others
    implementation(libs.smile.core)
    implementation(libs.roaringbitmap)
    // TODO: fill these in from Maven Central instead of that local libs dir
    implementation(files("libs/langdetect-03-03-2014.jar")) // TODO: upgrade to newer version from maven
    implementation(files("libs/jsonic-1.2.0.jar")) // required by by langdetect-03-03-2014.jar

    // Test framework dependencies
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.mockito.core)
    testImplementation(libs.assertj.core)
}

//
// Make sure that ache-dashboard is compiled and copied into resources folder
// before the resources are processed and bundled into the JAR file
//
tasks.processResources {
    dependsOn(":ache-dashboard:install")
}



//
// Adds version to final JAR artifact
//
tasks.named<Jar>("jar") {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to archiveVersion
        )
    }
}


//
// Copies config folder into final distribution file
//
val copyConfigTask = tasks.register<Copy>("copyConfig") {
    // copies config directory to the project build directory
    val outputDir = project.layout.buildDirectory.dir("config").get()
    outputs.dir(outputDir)
    doLast {
        copy {
            from(rootProject.file("config/").absolutePath)
            into(outputDir)
            exclude("sample_model", "sample_training_data", "sample.seeds")
        }
    }
}

application {
    // instructs the application plugin to include config directory into the final build
    applicationDistribution.from(copyConfigTask) {
        into("config")
    }
}


//
// Integration for test coverage service: coveralls
//
tasks.jacocoTestReport {
    reports {
        xml.required.set(true) // coveralls plugin depends on xml format report
        html.required.set(true)
    }
}


//
// Task to compute total size of dependencies:
//    gradle depsize
// Taken from:
// - https://gist.github.com/medvedev/968119d7786966d9ed4442ae17aca279
// - https://stackoverflow.com/questions/22175847/maven-gradle-way-to-calculate-the-total-size-of-a-dependency-with-all-its-transi
//
tasks.register("depsize") {
    description = "Prints dependencies for \"default\" configuration"
    doLast {
        listConfigurationDependencies(configurations.runtimeClasspath.get())
    }
}

tasks.register("depsize-all-configurations") {
    description = "Prints dependencies for all available configurations"
    doLast {
        configurations
            .filter { it.isCanBeResolved }
            .forEach { listConfigurationDependencies(it) }
    }
}

fun listConfigurationDependencies(configuration: Configuration) {
    val formatStr = "%,10.2f"
    val size = configuration.files.sumOf { it.length() / (1024.0 * 1024.0)}
    val out = StringBuilder()
    out.appendLine("\nConfiguration name: \"${configuration.name}\"")
    if (size > 0) {
        out.append("Total dependencies size:".padEnd(65))
        out.appendLine(String.format(formatStr, size.toDouble()) + " Mb\n")
        configuration.sortedByDescending { it.length() }.forEach {
            out.append(it.name.padEnd(65))
            out.appendLine(String.format(formatStr, (it.length() / 1024).toDouble()) + " kb")
        }
    } else {
        out.appendLine("No dependencies found")
    }
    println(out)
}
