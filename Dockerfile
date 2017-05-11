#
# ACHE Crawler Dockerfile
#
# https://github.com/ViDA-NYU/ache
#
FROM openjdk:8-jdk

ADD . /ache
WORKDIR /ache
RUN /ache/gradlew installDist

WORKDIR /data
ENTRYPOINT ["/ache/build/install/ache/bin/ache"]
VOLUME ["/data", "/config"]
