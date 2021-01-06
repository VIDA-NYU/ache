#
# ACHE Crawler Dockerfile
#
# https://github.com/ViDA-NYU/ache
#
FROM openjdk:11-jdk-slim as ache-build

ADD . /ache-src
WORKDIR /ache-src/ache
RUN /ache-src/gradlew installDist

FROM openjdk:11-jre-slim-buster

WORKDIR /ache
COPY --from=ache-build /ache-src/ache/build/install/ache /ache

# Makes JVM aware of memory limit available to the container (cgroups)
ENV JAVA_OPTS='-XX:+UseContainerSupport -XX:MaxRAMPercentage=80'

ENTRYPOINT ["/ache/bin/ache"]

VOLUME ["/data", "/config"]
