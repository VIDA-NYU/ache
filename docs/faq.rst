Frequently Asked Questions
==========================

What is inside the output directory?
------------------------------------
By default ACHE creates the following output folders:

    * **data_pages** contains relevant pages and all non-html content.
    * **data_hosts**
    * **data_monitor** contains current status of the crawler including list of relevant pages, irrelevant pages and download requests.
    * **data_url** and **data_backlinks** are where the persistent storages keep data of the frontier and the crawled graph.

When will the crawler stop?
---------------------------
Unless you stop it, the crawler exits when the number of visited pages exceeds the limit in the ``ache.yml`` file.

You can look at **output/data_monitor/harvestinfo.csv** to check how many pages have been downloaded and decide whether you want to stop the crawler manually. The columns are **number of relevant pages**, **number of visited pages** and **timestamp** respectively.

How to limit the number of visited pages?
-----------------------------------------
The crawler will exit when the number of visited pages reaches the default setting, which is 9 Million. You can modify it by changing ``target_storage.visited_page_limit`` key in the configuration file.

What format is crawled data saved in?
-------------------------------------
In default setting, the crawler stores crawled in html format without metadata information. You can store data in other data formats as well. For more information, see :ref:`Data Formats <dataformat-filesystem>`

Indexing web pages directly into :ref:`ELATICSEARCH <dataformat-elasticsearch>` is available too.

How can I save irrelevant pages?
--------------------------------
By default, this is off so you will need to set the value of ``target_storage.store_negative_pages`` to true in the configuration file.

Does ACHE crawl webpages in languages other than English?
---------------------------------------------------------
ACHE does language detection and tries to crawl only pages with content in English. You can enable or disable language detection in the configuration file by changing ``target_storage.english_language_detection_enabled``.

Is there any limit on number of crawled webpages per website?
-------------------------------------------------------------
Yes, we limit this number so that the crawler doesn't get trapped by particular domains. The default is 100, however you can set it in the configuration file using ``link_storage.max_pages_per_domain``.

Where to report bugs?
---------------------
We welcome feedback. Please submit any suggestions or bug reports using the Github issue tracker (https://github.com/ViDA-NYU/ache/issues)