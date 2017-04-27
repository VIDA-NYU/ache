Getting Started
###############

#. Clone ACHE's repository::

    git clone https://github.com/ViDA-NYU/ache.git

#. Compile and install ACHE (binaries will be available at ``ache/build/install/ache/bin/``)::

    cd ache
    ./gradlew installDist

#. For convenience, let's create an alias for ACHE's command line interface::

    alias ache=./build/install/ache/bin/ache

#. Now let's start ACHE using the *pre-trained page classifier model* and *seed URL list available* in the repository (hit ``Ctrl+C`` at any time to stop the crawler)::

    ache startCrawl -c config/config_focused_crawl/ -m config/sample_model/ -o data -s config/sample.seeds
