Focused Crawl
#############

A Focused crawl (also known as Topical Crawl) is similar to giving ACHE a topic and asking it to return pages on that topic. The topic is fed to the crawler in the form of samples of relevant and irrelevant pages before the crawl starts. ACHE analyzes these pages, learns how to classify pages as they are being crawled in the future and creates a model.

The next step is providing a list of seeds, which will be the starting point for ACHE. Ideally these are just HTTP or HTTPS links of some pages on the relevant topic and can be the same as relevant pages supplied during model generation. ACHE will start crawling the internet from these links.

Finally, a config file needs to be created which will be used by ACHE as a control panel for the different settings/options for the crawl.

The following steps explain how to run a focused crawl using ACHE.


Steps
-----

#. Build Model
        A list of relevant and irrelevant pages can be created using the Domain Discovery Tool **[link]**. Ideally, there should be as many relevant pages as irrelevant ones for good classification unless the topic isn't as popular. In such a case, appropriate ratio of irrelevant to relevant pages (say 5:1 would suffice).

        Once this set of training data has been attained, they can be used as training data by ACHE (make sure they are in a directory with relevant pages in a folder called 'positive' and the irrelevant ones in a folder called 'negative').

        A list of words which contain stopwords needs to be provided to the model-builder as well. Stopwords are usually words which contain little or no information about the context of the page. Good examples are: 'the', 'at', 'a' etc. A sample stoplist is included in the ``ache/config/sample_config`` folder and can be used easily.

        In the terminal, type::

            ache buildModel -c <Path of Stoplist> -t <Path of training data folder> -o <Path to save model at>


        * Make sure the training data has the folders called 'positive' and 'negative' containing relevant and irrelevant training pages respectively.

        .. attention::
            You do not need to do this step if you're using the Domain Discovery Tool. Just save the model in an appropriate folder and point to it in the next steps.


#. Find Seeds
    	Accumulate a list of URLs from which ACHE will start crawling. This can be the same list as the URLs for relevant pages generated in the previous step. You can use the :ref:`seedfinder<seedfinder>` for this aswell. A sample seeds file can be seen at ``ache/config/sample.seeds``.

#. Create Configuration File
    When running ACHE, a configuration file needs to be created containing the settings and rules the crawler will adhere to. A sample of the config file is provided in ``ache/config/config_focused_crawl`` containing the following important parameters:

        * ``target_storage.use_classifier`` - Whether to use the page classifier. More detail :ref:`here <pageclassifiers>`.

        * ``target_storage.data_format.type`` - Which format to save the retrieved pages in. More detail :ref:`here <dataformat-filesystem>`.

        * ``link_storage.max_pages_per_domain`` - The number of maximum pages to download per domain. Useful in case of traps/redirects.

        * ``link_storage.link_strategy.use_scope`` - Restricts the crawler to crawl the websites provided as seeds only.

        * ``link_storage.online_learning.enabled`` - Whether to enable relearning of the link classifier.

        * ``link_storage.online_learning.type`` - Type of online learning to use. More detail :ref:`here <crawlingstrategies-onlinelearning>`.

        * ``link_storage.scheduler.host_min_access_interval`` - Time delay between sending subsequent requests to the same host.


#. Run ACHE
        To run ACHE, in the terminal::

            ache startCrawl -c <Path of Config Folder> -o <Path of Output Folder> -s <Path of Seeds File> -m <Path of Model Folder>

        ACHE will continue crawling until interrupted using ``CTRL+C``.