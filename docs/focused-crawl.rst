Running a Focused Crawl
#######################

A Focused Crawl (also known as Topical Crawl) is similar to giving ACHE a topic and asking it to return webpages on that topic. The topic is fed to the crawler in the form of samples of relevant and irrelevant pages before the crawl starts. ACHE analyzes these pages, and learns classification model that is able to classify pages as they are being crawled in the future.
Besides the classification model, ACHE needs a list of seed URLs, which will be the starting point for the crawl. The seeds are just HTTP or HTTPS links of some pages on the relevant topic and can be the same as relevant pages supplied during model generation. ACHE will start crawling the Web from these links.
Finally, a config file needs to be created and provided to ACHE. This config file will be used to read the different settings/options for the crawl.

The following steps explain in details how to run a focused crawl using ACHE.

#. Build a Classification Model
    Ideally, there should be as many relevant pages as irrelevant ones for training a good classification model. You should try to get as close as possible to a ratio of irrelevant to relevant pages of 1:1. If it's hard to collect relevant pages, a ratio of 5:1 would suffice. Another good practice is to collect samples of relevant and irrelevant pages for every web site. Collecting only relevant pages from a web site may lead to a model that classifies *any* page from that web site as relevant, which may not be appropriate.

    In order to collect the relevant and irrelevant pages, you can use a web-based system like the `Domain Discovery Tool (DDT) <https://github.com/ViDA-NYU/domain_discovery_tool_react>`_. DDT provides tools for streamline the process of gathering the training pages and building a model for ACHE. Alternatively, you can build the training set manually and build the model using ACHE's command line tool ``ache buildModel``, as we are going to describe next.

    .. attention::
        You do NOT need to do the following instructions if you're using the Domain Discovery Tool. Just save the model in an appropriate folder and point to it in the following steps.

    As you collect the training data, make sure the HTML files are organized in a directory with relevant pages in a folder named ``positive`` and the irrelevant ones in a folder named ``negative``). See the directory `config/sample_training_data <https://github.com/ViDA-NYU/ache/tree/master/config/sample_training_data>`_  in ACHE's repository for an example.

    Optionaly, you can provide a file containing *stopwords* to the model-builder as well. Stopwords are usually words which contain little or no information about the context of the page. Good examples are: "the", "at", and "a". A sample file is included in the repository as well at `config/sample_config/stoplist.txt <https://github.com/ViDA-NYU/ache/blob/master/config/sample_config/stoplist.txt>`_. If you don't provide a stopwords file, a default list of common english stopwords bundled into ACHE will be used.

    Finally, type in the terminal::

        ache buildModel -c <Path of Stoplist> -t <Path of training data folder> -o <Path to save model at>


    This command should take a while to execute, depending on how many training samples you collected. In the end, it will print some accuracy statistics about the model.

#. Find Seeds
  	Accumulate a list of URLs from which ACHE will start crawling. This can be the same list as the URLs for relevant pages generated in the previous step. A sample seeds file can be seen at `config/sample.seeds <https://github.com/ViDA-NYU/ache/blob/master/config/sample.seeds>`_. You can use the :ref:`SeedFinder<seedfinder>` to help with this step as well. It will automatically try to discover a large set of seed URLs using commercial search engines.

#. Create Configuration File
    When running ACHE, a configuration file needs to be created containing the settings and rules the crawler will adhere to. A config file sample is provided in `config/config_focused_crawl/ache.yml <https://github.com/ViDA-NYU/ache/blob/master/config/config_focused_crawl/ache.yml>`_ containing the following important parameters:

        * ``link_storage.max_pages_per_domain`` - The number of maximum pages to download per domain. Useful in case of traps/redirects.

        * ``link_storage.link_strategy.use_scope`` - Whether the crawler should crawl the websites provided as seeds only. This needs to be set to ``false`` in a focused crawl.

        * ``link_storage.online_learning.enabled`` - Whether to enable relearning of the link classifier while the crawler is running.

        * ``link_storage.online_learning.type`` - Type of online learning to use. More details :ref:`here <crawlingstrategies-onlinelearning>`.

        * ``link_storage.scheduler.host_min_access_interval`` - Time delay between sending subsequent requests to the same host. Should not be too small to avoid overloading web servers.


#. Run ACHE
    Finally, when you have created the *model*, the config file *ache.yml*, and *seeds file*, you can run ACHE, in the terminal::

      ache startCrawl -c <Path of Config Folder> -o <Path of Output Folder> -s <Path of Seeds File> -m <Path of Model Folder>

    ACHE will continue crawling until interrupted using ``CTRL+C``.
