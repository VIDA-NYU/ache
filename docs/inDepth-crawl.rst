In-Depth Crawl
##############

An in-depth crawl in ACHE will allow the user to crawl a list of websites and the links present in them completely. The crawler will stop once these links have been crawled.

The process for running an in-depth crawl is simpler than for a focused crawl. An in-depth crawl doesn't require a model, it just needs a list of websites to crawl along with a configuration file to act as the appropriate settings for ACHE.

The following steps explain how to run such a crawl on ACHE.


Steps
-----
#. Prepare Seeds
    A seeds file should contain URLs of all websites that need to be crawled. A sample seeds file can be seen at ``ache/config/sample.seeds``.

#. Create Configuration File
    The configuration file will contain the settings and rules the crawler will adhere to. A sample of the config file is provided in ``ache/config/config_focused_crawl`` containing the following important parameters:

        * ``target_storage.data_format.type`` - Which format to save the retrieved pages in. More detail :ref:`here <dataformat-filesystem>`.

        * ``link_storage.max_pages_per_domain`` - The number of maximum pages to download per domain. Useful in case of traps/redirects.

        * ``link_storage.download_sitemap_xml`` - Whether to use the sitemap for the website (if available).

        * ``link_storage.scheduler.host_min_access_interval`` - Time delay between sending subsequent requests to the same host.

#. Run ACHE
        To run ACHE, in the terminal::

            ache startCrawl -c <Path of Config Folder> -o <Path of Output Folder> -s <Path of Seeds File>

        ACHE will continue crawling until interrupted using ``CTRL+C`` or until the seeds list has been exhausted.