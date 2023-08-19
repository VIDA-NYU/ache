rootProject.name = "achecrawler"

include("ache")
include("ache-dashboard")
include("ache-tools")
include("crawler-commons")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Testing
            version("junit", "5.9.2")
            library("junit.api", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit")
            library("junit.params", "org.junit.jupiter", "junit-jupiter-params").versionRef("junit")
            library("junit.engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef("junit")
            library("assertj.core", "org.assertj:assertj-core:3.20.2")
            library("mockito.core", "org.mockito:mockito-core:5.1.1")
            // Logging
            version("slf4j", "2.0.6")
            library("slf4j.api", "org.slf4j", "slf4j-api").versionRef("slf4j")
            library("logback.classic", "ch.qos.logback:logback-classic:1.4.5")
            // Commons
            library("guava", "com.google.guava:guava:23.0")
            library("commons.lang3", "org.apache.commons:commons-lang3:3.12.0")
            library("commons.compress", "org.apache.commons:commons-compress:1.22")
            library("commons.codec", "commons-codec:commons-codec:1.15")
            library("commons.validator", "commons-validator:commons-validator:1.7")
            library("crawler.commons", "com.github.crawler-commons:crawler-commons:1.2")
            // CLI
            library("airline", "io.airlift:airline:0.9")
            // Data serialization
            version("jackson", "2.14.2")
            library("jackson.core.databind", "com.fasterxml.jackson.core", "jackson-databind").versionRef("jackson")
            library("jackson.dataformat.cbor", "com.fasterxml.jackson.dataformat", "jackson-dataformat-cbor").versionRef("jackson")
            library("jackson.dataformat.yaml", "com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml").versionRef("jackson")
            library("kryo.lib", "com.esotericsoftware:kryo:4.0.2")
            library("kryo.serializers", "de.javakaffee:kryo-serializers:0.43")
            // REST server dependencies
            library("javalin", "io.javalin:javalin:4.2.0")
            // Metrics and monitoring
            version("dropwizard.metrics", "4.2.17")
            library("dropwizard.metrics.core", "io.dropwizard.metrics", "metrics-core").versionRef("dropwizard.metrics")
            library("dropwizard.metrics.json", "io.dropwizard.metrics", "metrics-json").versionRef("dropwizard.metrics")
            library("dropwizard.metrics.jvm", "io.dropwizard.metrics", "metrics-jvm").versionRef("dropwizard.metrics")
            // Data management and repositories
            library("rocksdbjni", "org.rocksdb:rocksdbjni:6.25.3")
            library("elasticsearch.rest.client", "org.elasticsearch.client:elasticsearch-rest-client:5.6.7")
            library("kafka.clients", "org.apache.kafka:kafka-clients:3.4.0")
            library("webarchive.commons", "org.netpreserve.commons:webarchive-commons:1.1.9")
            library("aws-java-sdk-s3", "com.amazonaws:aws-java-sdk-s3:1.12.+")
            // Data parsing and extraction
            library("boilerpipe", "com.syncthemall:boilerpipe:1.2.2")
            library("nekohtml", "net.sourceforge.nekohtml:nekohtml:1.9.22")
            library("jsoup", "org.jsoup:jsoup:1.15.1")
            library("lucene.analyzers.common", "org.apache.lucene:lucene-analyzers-common:8.11.1")
            library("tika.parsers", "org.apache.tika:tika-parsers:1.28.4")
            // HTTP libraries
            library("okhttp", "com.squareup.okhttp3:okhttp:4.10.0")
            library("httpclient", "org.apache.httpcomponents:httpclient:4.5.14")
            // Others
            library("smile.core", "com.github.haifengl:smile-core:1.5.3")
            library("roaringbitmap", "org.roaringbitmap:RoaringBitmap:0.9.39")
        }
    }
}
