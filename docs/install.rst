..  _install:

Installation
************

You can either build ACHE from the source code, download the executable binary using Conda, or use Docker to build an image and run ACHE in a container.

Using Docker
--------------------

**Prerequisite:** You will need to install a recent version of Docker. See https://docs.docker.com/engine/installation/ for details on how to install Docker for your platform.

To use ACHE with Docker, you can 1) use a pre-built image or 2) build the image yourself as follows:

**1. Using the pre-build docker image**

We publish pre-built docker images on `Docker Hub <https://hub.docker.com/r/vidanyu/ache/>`_ for each released version.
You can run the latest image using::

  docker run -p 8080:8080 vidanyu/ache:latest

Docker will automatically download the image from DockerHub and run it.

**2. Build the image on your machine**

Alternatively, you can build the image yourself and run it::

  git clone https://github.com/ViDA-NYU/ache.git
  cd ache
  docker build -t ache .

where ``ache`` is the name of the image being built.

**Running the crawler using Docker**

The `Dockerfile <https://github.com/ViDA-NYU/ache/blob/master/Dockerfile>`_ used to build the image exposes two data volumes so that you can mount a directory with your configuration files (at ``/config``) and preserve the data stored by the crawler (at ``/data``) after the container stops.
In order to run ACHE using docker, you will need a command like::

  docker run -v $PWD:/config -v $PWD/data:/data -p 8080:8080 vidanyu/ache startCrawl -c /config/ -s /config/seeds.txt -o /data/

where ``$PWD`` is the path where your config file ``ache.yml`` and the ``seeds.txt`` are located and ``$PWD/data`` is the path where the crawled data will be stored. In this command ``vidanyu/ache`` refers to the pre-built image on DockerHub. If you built the image yourself, you should use the same name that you used to build the image.

Build from source with Gradle
-----------------------------
**Prerequisite:** You will need to install recent version of Java (JDK 8 or latest) and Git.

To build ACHE from source, you can run the following commands in your terminal::

  git clone https://github.com/ViDA-NYU/ache.git
  cd ache
  ./gradlew installDist

which will generate an installation package under ``ache/build/install/``.
You can then make ``ache`` command available in the terminal by adding
ACHE binaries to the ``PATH`` environment variable::

  export ACHE_HOME="{path-to-cloned-ache-repository}/ache/build/install/ache"
  export PATH="$ACHE_HOME/bin:$PATH"


This configuration will not persist after system restarts. To make it persistent,
you will need configure the system to reload these settings automatically.
Every operating system is configured in a different way.
Following, is an example of how to install ACHE at ``/opt`` for Linux
(tested only in **Ubuntu 16.04**)::

  sudo mv ache/build/install/ache /opt/
  echo 'export ACHE_HOME="/opt/ache"' | sudo tee -a /etc/profile.d/ache.sh
  echo 'export PATH="$ACHE_HOME/bin:$PATH"' | sudo tee -a /etc/profile.d/ache.sh
  source /etc/profile.d/ache.sh

After this, the command ``ache`` will be available on the terminal, so you can simply run the crawler with the appropriate parameters.

Download with Conda
-------------------

**Prerequisite:** You need to have Conda package manager installed in your system.

If you use the `Conda <https://conda.io/docs/>`_ package manager, you can install ``ache`` from `Anaconda Cloud <https://anaconda.org/>`_ by running::

  conda install -c vida-nyu ache

..  warning::

  Only released tagged versions are published to Anaconda Cloud, so the version available through Conda may not be up-to-date.
  If you want to try the most recent version, please clone the repository and build from source or use the Docker version.
