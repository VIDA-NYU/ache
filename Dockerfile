#
# ACHE Crawler Dockerfile
#
# https://github.com/ViDA-NYU/ache
#
FROM gradle:8.14-jdk17 as ache-build

ADD . /ache-src
WORKDIR /ache-src/ache
RUN gradle installDist

# Base the runtime image on Debian 13 and the Temurin JRE from the Gradle image
FROM debian:13-slim
ENV JAVA_HOME=/opt/java/openjdk
COPY --from=ache-build $JAVA_HOME $JAVA_HOME
ENV PATH="${JAVA_HOME}/bin:${PATH}"

WORKDIR /ache
COPY --from=ache-build /ache-src/ache/build/install/ache /ache

# Makes JVM aware of memory limit available to the container (cgroups)
ENV JAVA_OPTS='-XX:+UseContainerSupport -XX:MaxRAMPercentage=80 --add-opens java.base/java.util=ALL-UNNAMED'

ENTRYPOINT ["/ache/bin/ache"]

VOLUME ["/data", "/config"]
