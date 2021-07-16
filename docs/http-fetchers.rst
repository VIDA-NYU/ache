HTTP Fetchers
#############

ACHE currently has multiple HTTP fetcher implementations based on different
libraries (crawler-commons, which uses Apache HttpComponents, and okhttp3).

The fetchers support downloading URLs using both HTTP(S) and TOR protocol.


Switching the HTTP fetcher implementation
-----------------------------------------

The default implementation uses Apache HttpComponents backend. To use the
okhttp3 fetcher, you should enable it using:

.. code-block:: yaml

  crawler_manager.downloader.use_okhttp3_fetcher: true


Setting up a proxy
------------------

Crawling via a proxy is currently supported only by the okhttp3 fetcher.
To configure this you can use the following lines:

.. code-block:: yaml

  # enable okhttp3 fetcher
  crawler_manager.downloader.use_okhttp3_fetcher: true
  # okhttp3 proxy configuration
  crawler_manager.downloader.okhttp3.proxy_host: null
  crawler_manager.downloader.okhttp3.proxy_username: null
  crawler_manager.downloader.okhttp3.proxy_password: null
  crawler_manager.downloader.okhttp3.proxy_port: 8080 


Setting up a TOR proxy
----------------------

In order to crawl links from the TOR network, an external TOR proxy is necessary.
To set up such a proxy, you can use the following lines (note that only links from
domains that end with `.onion` TLD use this proxy):

.. code-block:: yaml

  # Configure to download .onion URLs through the TOR proxy running at torproxy:8118
  crawler_manager.downloader.torproxy: http://torproxy:8118

An example of crawling TOR network services is available at this
:ref:`tutorial<tutorial-crawling-tor>`.