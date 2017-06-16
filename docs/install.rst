..  _install:

Installation
************

You can either build ACHE from the source code, download the executable binary using Conda, or use Docker to build an image and run ACHE in a container.

Running using Docker
--------------------

**Prerequisite:** You will need to install a recent version of Docker. See https://docs.docker.com/engine/installation/ for details on how to install Docker for your platform.

We publish pre-built docker images on `Docker Hub <https://hub.docker.com/r/vidanyu/ache/>`_ for each released version.
You can run the latest image using::

  docker run -p 8080:8080 vidanyu/ache:latest

Alternatively, you can build the image yourself and run it::


  git clone https://github.com/ViDA-NYU/ache.git
  cd ache
  docker build -t ache .
  docker run -p 8080:8080 ache

The `Dockerfile <https://github.com/ViDA-NYU/ache/blob/master/Dockerfile>`_ exposes two data volumes so that you can mount a directory with your configuration files (at ``/config``) and preserve the crawler stored data (at ``/data``) after the container stops.


Build from source with Gradle
-----------------------------
**Prerequisite:** You will need to install recent version of Java (JDK 8 or latest) and Git.

To build ACHE from source, you can run the following commands in your terminal::

  git clone https://github.com/ViDA-NYU/ache.git
  cd ache
  ./gradlew installDist

which will generate an installation package under ``ache/build/install/``.
To install ACHE binaries into ``/opt`` and make it available on the command-line run::

  sudo mv build/install/ache /opt/
  echo 'export ACHE_HOME="/opt/ache"' | sudo tee -a /etc/profile.d/ache.sh
  echo 'export PATH="$ACHE_HOME/bin:$PATH"' | sudo tee -a /etc/profile.d/ache.sh
  sh /etc/profile.d/ache.sh

Download with Conda
-------------------

If you use the `Conda <https://conda.io/docs/>`_ package manager, you can install `ache` from `Anaconda Cloud <https://anaconda.org/>`_ by running::

  conda install -c memex ache

..  warning::

  Only tagged versions are published to Anaconda Cloud, so the version available through Conda may not be up-to-date.
  If you want to try the most recent version, please clone the repository and build from source.
