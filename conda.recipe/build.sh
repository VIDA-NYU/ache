#!/bin/bash

#if [ "$(uname)" == "Darwin" ]; then
#    export JAVA_HOME=$(/usr/libexec/java_home)
#    export JRE_HOME=${JAVA_HOME}/jre
#else
#    export JAVA_HOME="/usr/lib/jvm/java"
#    export JRE_HOME="/usr/lib/jvm/jre"
#fi

#BLD_DIR=`pwd`

#SRC_DIR=$RECIPE_DIR/..
#pushd $SRC_DIR

mkdir -vp ${PREFIX}/bin;
mkdir -vp ${PREFIX}/lib/ache/bin;
mkdir -vp ${PREFIX}/lib/ache/config;
mkdir -vp ${PREFIX}/lib/ache/lib;

# build focused_crawler
export TERM=${TERM:-dumb} 
./gradlew clean installDist --stacktrace


pushd ache/build/install/ache
cp -r bin/* ${PREFIX}/lib/ache/bin/
cp -r lib/* ${PREFIX}/lib/ache/lib/
# needed by DDT
cp -r config/* ${PREFIX}/lib/ache/config/

pushd "${PREFIX}/bin"
ln -vs "../lib/ache/bin/ache" ache
