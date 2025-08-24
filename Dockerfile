#
# ACHE Crawler Dockerfile
#
# https://github.com/ViDA-NYU/ache
#
FROM gradle:8.14-jdk17 AS ache-build

ADD . /ache-src
WORKDIR /ache-src/ache
RUN gradle installDist

# Create a custom Java runtime
RUN $JAVA_HOME/bin/jlink \
         --add-modules java.base,java.logging,java.management,java.naming,java.net.http,java.xml,jdk.crypto.ec \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime

# Base the runtime image on Debian 13 and the custom JRE
FROM debian:13-slim
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=ache-build /javaruntime $JAVA_HOME

WORKDIR /ache
COPY --from=ache-build /ache-src/ache/build/install/ache /ache

# Makes JVM aware of memory limit available to the container (cgroups)
ENV JAVA_OPTS='-XX:+UseContainerSupport -XX:MaxRAMPercentage=80 --add-opens java.base/java.util=ALL-UNNAMED'

ENTRYPOINT ["/ache/bin/ache"]

VOLUME ["/data", "/config"]
