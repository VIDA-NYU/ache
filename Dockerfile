#
# ACHE Crawler Dockerfile
#
# https://github.com/ViDA-NYU/ache
#
FROM openjdk:8-jdk

ADD . /ache-src
WORKDIR /ache-src
RUN /ache-src/gradlew installDist && mv /ache-src/build/install/ache /ache && rm -rf /ache-src/ /root/.gradle

ENTRYPOINT ["/ache/bin/ache"]
VOLUME ["/data", "/config"]
