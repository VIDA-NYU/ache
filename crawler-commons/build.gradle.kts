plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    api(group="org.slf4j", name = "slf4j-api", version = "1.7.36")
    implementation(group = "org.apache.commons", name = "commons-lang3", version = "3.12.0")
    implementation(group = "org.apache.httpcomponents", name = "httpclient", version = "4.5.14")
    implementation(group = "commons-io", name = "commons-io", version = "2.11.0")

    testImplementation(group = "junit", name = "junit", version = "4.13.2")
    testImplementation(group = "org.eclipse.jetty", name = "jetty-server", version = "9.4.48.v20220622")
}
