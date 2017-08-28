.. _restapi:

REST API
#################

When ACHE is started, it automatically starts a REST API on port 8080.
If that port is busy, it will try the following ports (8081, 8082, etc).
The default HTTP settings can be configured using the following lines in the
``ache.yml`` file:

.. sourcecode:: yaml

  http.port: 8080
  http.host: 127.0.0.1
  http.cors.enabled: true


API Endpoints
-------------

.. http:post:: /startCrawl

    Starts a crawl.

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



.. http:get:: /status

    Returns the status of the currently running crawl.



.. http:get:: /metrics

    Returns detailed runtime metrics of the current crawler execution.



.. http:get:: /stopCrawl

    Stops the crawler execution if there is a crawler running.

    :query boolean awaitStopped: One of ``true`` or ``false`` (default).
      Indicates whether the request should block until the crawler is completely stopped.



.. http:post:: /seeds

    Adds more seeds to the crawl if there is a crawler running.

    :reqjson array seeds: An array containing the URLs to be added to the crawl
      that is currently running.


    Request body example:

    .. sourcecode:: js

      {
        "seeds": ["http://en.wikipedia.org/", "http://example.com/"]
      }
