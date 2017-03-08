..  _install:

Installation
************


You can either build ACHE from the source code or download the executable binary using Conda.

Build from source with Gradle
-----------------------------

To build `ache` from source, you can run the following commands in your terminal::


  git clone https://github.com/ViDA-NYU/ache.git
  cd ache
  ./gradlew installDist

which will generate an installation package under ``ache/build/install/``. You can then make ACHE command line available in the terminal by adding ACHE to the PATH::

  export ACHE_HOME="{path-to-cloned-ache-repository}/build/install/ache"
  export PATH="$ACHE_HOME/bin:$PATH"


Download with Conda
-------------------

If you use the `Conda <https://conda.io/docs/>`_ package manager, you can install `ache` from `Anaconda Cloud <https://anaconda.org/>`_ by running::

  conda install -c memex ache

..  warning::

  Only tagged versions are published to Anaconda Cloud, so the version available through Conda may not be up-to-date.
  If you want to try the most recent version, please clone the repository and build from source.
