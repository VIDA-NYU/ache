Running a In-Depth Website Crawl with Cookies
#############################################

Some websites require users to login in order to access its content.
ACHE allows crawling these type of websites by simulating the user login through sending the cookies along with HTTP requests.

The following steps show how to crawl sites that require login using ACHE.

#. Get the cookies for each website using a web browser and also the user-agent string of that browser
       The following instructions assume that you are using Chrome browser, although it should be similar with other browsers (i.e., Firefox and IE). For each website, repeat the following steps:

        * Sign up and login to the website.

        * Right click anywhere in the page and select ``Inspect``. It will show the Developer Tools of the browser.

        * In the Developer Tools Bar on top, select the ``Network`` Tab.

        * Reload the page, to trigger the browser sending a request to the website. Then we will inspect this request to retrieve the cookie and user-agent string.

        * Select the first request in the ``Name`` panel on the left and a new panel with a tab named "Headers" will show up. Look for the section "Request Headers". Under this section, you need to locate and copy the values from the keys ``cookie`` and ``user-agent`` into ACHE's configuration file as shown in the next section.

#. Create an ``ache.yml`` configuration file
    The configuration file will contain the settings and rules the crawler will adhere to. A sample of the config file for running an in-depth website crawl with cookies is provided in `config/config_login/ache.yml <https://github.com/ViDA-NYU/ache/blob/master/config/config_login/ache.yml>`_ containing the following important parameters. Note that all the parameters are the same as ones used in in-depth website crawl except ``crawler_manager.downloader.user_agent.string`` and ``crawler_manager.downloader.cookies``

        * ``link_storage.link_strategy.use_scope`` - Whether the crawler should crawl the websites provided as seeds only. This needs to be set to ``true`` in a in-depth website crawl.

        * ``link_storage.download_sitemap_xml`` - Whether to use the Sitemap protocol for discovery of links (if available). It helps to discover URLs quicker.

        * ``link_storage.scheduler.host_min_access_interval`` - Configures the minimum time interval (in milliseconds) to wait between subsequent requests to the same host to avoid overloading servers. If you are crawling your own website, you can decrease this value to speed-up the crawl.

        * ``crawler_manager.downloader.user_agent.string`` - The user-agent string acquired in the previous step.

        * ``crawler_manager.downloader.cookies`` - A list of the website and its cookies acquired in the previous step. For example::

                    - domain: website1.com
                      cookie: cookie1
                    - domain: website2.com
                      cookie: cookie2


#. Prepare the seeds file
    A seeds file should contain URLs of all websites that need to be crawled. A sample seeds file can be seen at `config/sample.seeds <https://github.com/ViDA-NYU/ache/blob/master/config/sample.seeds>`_.

#. Run ACHE
    Finally, when you have created the config file *ache.yml*, and the *seeds file*, you can run ACHE in the terminal::

      ache startCrawl -c <Path of Config Folder> -o <Path of Output Folder> -s <Path of Seeds File>

    ACHE will continue crawling until interrupted using ``CTRL+C`` or until the queue of all links found during the crawl has been exhausted.

    For large crawls, you should consider using ``nohup`` for running the process in background::

      nohup ache startCrawl -c <Path of Config Folder> -o <Path of Output Folder> -s <Path of Seeds File> > crawler-log.txt &
