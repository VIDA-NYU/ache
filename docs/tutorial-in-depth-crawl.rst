.. _tutorial-in-depth-crawl:

Running a In-Depth Website Crawl
################################

When you already know sites you want to crawl, you can run a in-depth website crawl using ACHE. Given a list of URLs (sites), ACHE will crawl all pages in each site.  The crawler stops when no more links are found in the sites.

The process for running an in-depth crawl is simpler than for a focused crawl. An in-depth crawl doesn't require a model, it just needs a list of websites to crawl along with a configuration file with the appropriate settings for ACHE.

The following steps explain how to run such a crawl using ACHE.

#. Prepare the seeds file
    A seeds file should contain URLs of all websites that need to be crawled. A sample seeds file can be seen at `config/sample.seeds <https://github.com/ViDA-NYU/ache/blob/master/config/sample.seeds>`_.

#. Create an ``ache.yml`` configuration file
    The configuration file will contain the settings and rules the crawler will adhere to. A sample of the config file for running a in-depth website crawl is provided in `config/config_website_crawl/ache.yml <https://github.com/ViDA-NYU/ache/blob/master/config/config_website_crawl/ache.yml>`_ It contains the following important parameters:

        * ``link_storage.link_strategy.use_scope`` - Whether the crawler should crawl the websites provided as seeds only. This needs to be set to ``true`` in a in-depth website crawl.

        * ``link_storage.download_sitemap_xml`` - Whether to use the Sitemap protocol for discovery of links (if available). It helps to discover URLs more quickly.

        * ``link_storage.scheduler.host_min_access_interval`` - Configures the minimum time interval (in milliseconds) to wait between subsequent requests to the same host to avoid overloading servers. If you are crawling your own web site, you can descrease this value to speed-up the crawl.

#. Run ACHE
    Finally, when you have created the config file *ache.yml*, and the *seeds file*, you can run ACHE in the terminal::

      ache startCrawl -c <Path of Config Folder> -o <Path of Output Folder> -s <Path of Seeds File>

    ACHE will continue crawling until interrupted using ``CTRL+C`` or until the queue of all links found during the crawl has been exhausted.

    For large crawls, you should consider using ``nohup`` for running the process in background::

      nohup ache startCrawl -c <Path of Config Folder> -o <Path of Output Folder> -s <Path of Seeds File> > crawler-log.txt &
