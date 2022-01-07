HTTP Fetchers
#############

ACHE currently supports two HTTP fetcher implementations based on different
libraries: `Apache HttpComponents HttpClient <https://hc.apache.org/>`_
and `OkHttp3 <https://square.github.io/okhttp/>`_.

The fetchers support downloading URLs using both HTTP(S) and TOR protocol.


Switching the HTTP fetcher implementation
-----------------------------------------

The default implementation uses Apache HttpComponents backend. To use the
okhttp fetcher, you should enable it using:

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


Setting connection timeouts
---------------------------
.. _http-fetchers-connection-timeouts:

The following configurations allow to configure connection timeouts.
This might be useful when crawling from slow networks (e.g., TOR network).

To configure timeouts for the TOR fetcher, use the following properties:

.. code-block:: yaml

        crawler_manager.downloader.tor.max_retry_count: 3
        crawler_manager.downloader.tor.socket_timeout: 300000
        crawler_manager.downloader.tor.connection_timeout: 300000
        crawler_manager.downloader.tor.connection_request_timeout: 300000

If you are using the `httpclient` fetcher (default), you can use:

.. code-block:: yaml

    crawler_manager.downloader.httpclient.socket_timeout: 30000
    crawler_manager.downloader.httpclient.connection_timeout: 30000
    crawler_manager.downloader.httpclient.connection_request_timeout: 60000

If you are using the `okhttp` fetcher, you can use:

.. code-block:: yaml

    crawler_manager.downloader.okhttp.connect_timeout: 30000
    crawler_manager.downloader.okhttp.read_timeout: 30000

