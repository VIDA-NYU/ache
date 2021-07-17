.. _tutorial-crawling-tor:

Crawling Dark Web Sites on the TOR network
##########################################

`TOR <https://en.wikipedia.org/wiki/Tor_(anonymity_network)>`_  is a well known
software that enables anonymous communications, and is becoming more popular due
to the increasingly media on *dark web* sites.
"Dark Web" sites are usually not crawled by generic crawlers because the
web servers are hidden in the TOR network and require use of specific protocols for
being accessed.
Sites hidden on the TOR network are accessed via domain addresses under the top-level domain ``.onion``.
In order to crawl such sites, ACHE relies on external HTTP proxies, such as `Privoxy <https://www.privoxy.org/>`_,
configured to route traffic trough the TOR network.
Besides configuring the proxy, we just need to configure ACHE to route requests to ``.onion`` addresses via the TOR proxy.

Fully configuring a web proxy to route traffic through TOR is out-of-scope of this tutorial, so we will just
use Docker to run the pre-configured docker image for Privoxy/TOR available at https://hub.docker.com/r/dperson/torproxy/.
For convenience, we will also run ACHE and Elasticsearch using docker containers.

To start and stop the containers, we will use `docker-compose`, so make sure that the Docker version that you installed includes it.
You can verify whether it is installed by running the following command on the Terminal (it should print the version of docker-compose to the output)::

  docker-compose -v

The following steps explain in details how to crawl ``.onion`` sites using ACHE.


**1. Create the configuration files**

  All the configuration files needed are available in ACHE's repository at `config/config_docker_tor <https://github.com/ViDA-NYU/ache/tree/master/config/config_docker_tor>`_
  (if you already cloned the git repository, you won't need to download them).
  Download the following files and put them in single directory named ``config_docker_tor``:

  #. `tor.seeds <https://raw.githubusercontent.com/ViDA-NYU/ache/master/config/config_docker_tor/tor.seeds>`_: a plain text containing the URLs of the sites you want to crawl. In this example, the file contains a few URLs taken from https://thehiddenwiki.org/. If you want to crawl specific websites, you should list them on this file (one URL per line).
  #. `ache.yml <https://raw.githubusercontent.com/ViDA-NYU/ache/master/config/config_docker_tor/ache.yml>`_: the configuration file for ACHE. It basically configures ACHE to run a in-depth website crawl of the seed URLs, to index crawled pages in the Elasticsearch container, and to download .onion addresses using the TOR proxy container.
  #. `docker-compose.yml <https://raw.githubusercontent.com/ViDA-NYU/ache/master/config/config_docker_tor/docker-compose.yml>`_: a configuration file for Docker, which specifies which containers should be used. It starts an Elasticsearch node, the TOR proxy, and ACHE crawler.

  If you are using Mac or Linux, you can run the following commands on the Terminal to create a folder and download the files automatically:

  .. code:: bash

    mkdir config_docker_tor/
    cd config_docker_tor/
    curl -O https://raw.githubusercontent.com/ViDA-NYU/ache/master/config/config_docker_tor/ache.yml
    curl -O https://raw.githubusercontent.com/ViDA-NYU/ache/master/config/config_docker_tor/docker-compose.yml
    curl -O https://raw.githubusercontent.com/ViDA-NYU/ache/master/config/config_docker_tor/tor.seeds

**2. Start the Docker containers**

  Enter the directory ``config_docker_tor`` you just created and start the containers with docker-compose::

      docker-compose up -d

  This command will automatically download all docker images and start all necessary containers in background mode.
  The downloads may take a while to finish depending on your Internet connection speed.


**3. Monitor the crawl progress**

  Once all docker images have been downloaded and the all services have been started, you will be able to open ACHE's web interface at http://localhost:8080 to see some crawl metrics.
  If you want to visualize the crawler logs, you can run::

      docker-compose logs -f

**4. Stop the Docker containers**

  You can stop the containers by hitting ``CTRL+C`` on Linux (or equivalent in your OS). You can also remove the containers by running the following command:

  .. code:: bash

      docker-compose down

**Understanding the docker-compose.yml file**

Basically, in ``docker-compose.yml`` we configure a container for the TOR proxy
named ``torproxy`` that listens on the port 8118:

.. code:: yaml

  torproxy:
    image: dperson/torproxy
    ports:
      - "8118:8118"

An Elasticsearch node named ``elasticsearch`` that listens on the port 9200 (we also add some common Elasticsearch settings):

 .. code:: yaml

   elasticsearch:
     image: elasticsearch:2.4.5
     environment:
       - xpack.security.enabled=false
       - cluster.name=docker-cluster
       - bootstrap.memory_lock=true
     ulimits:
       memlock:
         soft: -1
         hard: -1
     volumes:
       - ./data-es/:/usr/share/elasticsearch/data # elasticsearch data will be stored at ./data-es/
     ports:
       - 9200:9200



And finally, we configure a container named ``ache``.
Note that in order to make the config (``ache.yml``) and the seeds (``tor.seeds``) files available inside the container, we need to mount the volume ``/config`` to point to the current working directory.
We also mount the volume ``/data`` in the directory ``./data-ache`` so that the crawled data is stored outside the container.
In order to make ACHE communicate to the other containers, we need to link the ACHE's container to the other two containers ``elasticsearch`` and ``torproxy``.

.. code:: yaml

  ache:
    image: vidanyu/ache
    entrypoint: sh -c 'sleep 10 && /ache/bin/ache startCrawl -c /config/ -s /config/tor.seeds -o /data -e tor'
    ports:
      - "8080:8080"
    volumes:
      # mounts /config and /data directories to paths relative to path where this file is located
      - ./data-ache/:/data
      - ./:/config
    links:
      - torproxy
      - elasticsearch
    depends_on:
      - torproxy
      - elasticsearch

**Understanding the ache.yml file**

The ``ache.yml`` file basically configures ACHE to index crawled data in the ``elasticsearch`` container:

  .. code:: yaml

    # Configure both ELASTICSEARCH and FILES data formats, so data will be
    # stored locally using FILES data format and will be sent to ELASTICSEARCH
    target_storage.data_formats:
      - FILES
      - ELASTICSEARCH
    # Configure Elasticsearch REST API address
    target_storage.data_format.elasticsearch.rest.hosts:
      - http://elasticsearch:9200

and to download .onion addresses using the ``torproxy`` container:

  .. code:: yaml

    crawler_manager.downloader.torproxy: http://torproxy:8118

All remaining configuration lines are regular ACHE configurations for
running a in-depth website crawl of the seeds. Refer to the :ref:`in-depth website crawling turorial <tutorial-in-depth-crawl>` for more details.
