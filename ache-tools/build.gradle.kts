plugins {
    id("java")
    id("application")
}

application {
    mainClass = "achecrawler.RunCliTool"
    applicationDefaultJvmArgs = listOf("-Dname=ache-tools -XX:+HeapDumpOnOutOfMemoryError")
}

dependencies {
    // Sub-projects
    implementation(project(":ache"))
    implementation(project(":crawler-commons"))

    // Dependencies
    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)
    implementation(libs.commons.compress)
    implementation(libs.commons.validator)
    implementation(libs.airline)
    implementation(libs.jackson.core.databind)
    implementation(libs.jackson.dataformat.cbor)
    implementation(libs.elasticsearch.rest.client)
    implementation(libs.kafka.clients)
    implementation(libs.tika.parsers)
    implementation(libs.jsoup)
    implementation(libs.aws.java.sdk.s3)

    // Test framework dependencies
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.assertj.core)
}