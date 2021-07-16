.. _restapi:

Web Server & REST API
#####################

When an ACHE crawl is started, it automatically starts a REST API on port 8080.
If that port is busy, it will try the following ports (8081, 8082, etc).
The default HTTP settings can be changed using the following lines in the
``ache.yml`` file:

.. sourcecode:: yaml

  http.port: 8080
  http.host: 127.0.0.1
  http.cors.enabled: true

Security
--------

There is only *HTTP Basic* authentication available at this time. To configure it,
add the following lines to ``ache.yml``:

.. sourcecode:: yaml

  http.auth.basic.user: myusername
  http.auth.basic.password: mypasswd


Server Mode
-----------

Besides using the ``ache startCrawl`` command, ACHE can also be started in server
mode and controlled using the web user interface or the REST API.

To start ACHE in server mode, you can use::

    ache startServer -d /data -c /config/

Alternatively, if you are using Docker, run::

    docker run -v $CONFIG:/config -v $DATA/data:/data vidanyu/ache startServer -d /data -c /config/

where:

 * ``$CONFIG`` is the path to where ``ache.yml`` is stored and
 * ``$DATA`` is the path where ACHE is going to store its data.


If you want to configure a proxy to serve ACHE user interface from a **non-root**
path, you will need to specify the path in ``ache.yml`` file using the following
configuration::

  http.base_path: /my-new-path

API Endpoints
-------------

.. http:post:: /crawls/(string:crawler_id)/startCrawl

    Starts a crawler with the crawler id ``crawler_id``.

    :reqjson string crawlType: Type of crawl to be started. Either ``DeepCrawl`` or ``FocusedCrawl``.
    :reqjson string model: (**Required for FocusedCrawl**) A base64 encoded string of the zipped model file.
      The zip file should contain the model files (``pageclassifier.yml``) and
      the seed file (``*_seeds.txt``).
    :reqjson array seeds: (**Required for DeepCrawl**) An array of strings. Each string must be a
      fully-qualified URL that will be used starting point of the crawl.

    Request body example for DeepCrawl:

    .. sourcecode:: js

      {
        "crawlType": "DeepCrawl",
        "seeds": ["http://en.wikipedia.org/", "http://example.com/"],
        "model": null
      }

    Request body example for FocusedCrawl:

    .. sourcecode:: js

      {
        "crawlType": "FocusedCrawl",
        "seeds": null,
        "model": "<Base64 encoded zipped model file>"
      }

    Response body example:

    .. sourcecode:: js

      {
        "message": "Crawler started successfully.",
        "crawlerStarted": true
      }



.. http:get:: /crawls/(string:crawler_id)/status

    Returns the status of the crawler with crawler id ``crawler_id``.

    Response body example:

    .. sourcecode:: js

      {
        "status": 200,
        "version": "0.10.0",
        "searchEnabled": false,
        "crawlerRunning": true,
        "crawlerState": "RUNNING"
      }

.. http:get:: /crawls/(string:crawler_id)/metrics

    Returns detailed runtime metrics of the crawler with crawler id
    ``crawler_id``. The metrics returned are generated using the
    `Dropwizard Metrics` library.

    Response body example:

    .. sourcecode:: js

      {
          "version": "3.1.3",
          "gauges": {
            "downloader.dispatch_queue.size": {
              "value": 0
            },
            "downloader.download_queue.size": {
              "value": 0
            },
            "downloader.pending_downloads": {
              "value": 2
            },
            "downloader.running_handlers": {
              "value": 1
            },
            "downloader.running_requests": {
              "value": 1
            },
            "frontier_manager.last_load.available": {
              "value": 0
            },
            "frontier_manager.last_load.rejected": {
              "value": 11610
            },
            "frontier_manager.last_load.uncrawled": {
              "value": 11610
            },
            "frontier_manager.scheduler.empty_domains": {
              "value": 0
            },
            "frontier_manager.scheduler.non_expired_domains": {
              "value": 1
            },
            "frontier_manager.scheduler.number_of_links": {
              "value": 2422
            },
            "target.storage.harvest.rate": {
              "value": 0.9777777777777777
            }
          },
          "counters": {
            "downloader.fetches.aborted": {
              "count": 0
            },
            "downloader.fetches.errors": {
              "count": 1
            },
            "downloader.fetches.successes": {
              "count": 48
            },
            "downloader.http_response.status.2xx": {
              "count": 47
            },
            "downloader.http_response.status.401": {
              "count": 0
            },
            "downloader.http_response.status.403": {
              "count": 0
            },
            "downloader.http_response.status.404": {
              "count": 1
            },
            "downloader.http_response.status.5xx": {
              "count": 0
            },
            "target.storage.pages.downloaded": {
              "count": 45
            },
            "target.storage.pages.relevant": {
              "count": 44
            }
          },
          "histograms": {},
          "meters": {},
          "timers": {
            "downloader.fetch.time": {
              "count": 48,
              "max": 584.693196,
              "mean": 160.64529857175228,
              "min": 51.161457,
              "p50": 114.816344,
              "p75": 218.304927,
              "p95": 377.469511,
              "p98": 584.693196,
              "p99": 584.693196,
              "p999": 584.693196,
              "stddev": 118.74270199105285,
              "m15_rate": 0.4281665582051108,
              "m1_rate": 0.7030438799915493,
              "m5_rate": 0.4803778789487069,
              "mean_rate": 0.9178383293058442,
              "duration_units": "milliseconds",
              "rate_units": "calls/second"
            },
            [... Other metrics...]
          }
        }

.. http:get:: /crawls/(string:crawler_id)/stopCrawl

    Stops the crawler with crawler id ``crawler_id`` if it is running.

    :query boolean awaitStopped: One of ``true`` or ``false`` (default).
      Indicates whether the request should block until the crawler is completely
      stopped.

    Response body example:

    .. sourcecode:: js

      {
        "message": "Crawler shutdown initiated.",
        "shutdownInitiated": true,
        "crawlerStopped": false
      }

.. http:post:: /crawls/(string:crawler_id)/seeds

    Adds seeds to the crawler with crawler id ``crawler_id``.

    :reqjson array seeds: An array containing the URLs to be added to the crawl
      that is currently running.


    Request body example:

    .. sourcecode:: js

      {
        "seeds": ["http://en.wikipedia.org/", "http://example.com/"]
      }


    Response body example:

    .. sourcecode:: js

      {
        "message": "Seeds added successfully.",
        "addedSeeds": true
      }
