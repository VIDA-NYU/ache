Welcome to ACHE's Documentation!
========================================

ACHE is a focused web crawler. It collects web pages that satisfy some specific criteria, e.g., pages that belong to a given domain or that contain a user-specified pattern.
ACHE differs from generic crawlers in sense that it uses *page classifiers* to distinguish between relevant and irrelevant pages in a given domain. A page classifier can be from a simple regular expression (that matches every page that contains a specific word, for example), to a machine-learning based classification model.
ACHE can also automatically learn how to prioritize links in order to efficiently locate relevant content while avoiding the retrieval of irrelevant content.

ACHE supports many features, such as:

  * Regular crawling of a fixed list of web sites
  * Discovery and crawling of new relevant web sites through automatic link prioritization
  * Configuration of different types of pages classifiers (machine-learning, regex, etc)
  * Continuous re-crawling of sitemaps to discover new pages
  * Indexing of crawled pages using Elasticsearch
  * Web interface for searching crawled pages in real-time
  * REST API and web-based user interface for crawler monitoring
  * Crawling of hidden services using TOR proxies

..  toctree::
    :maxdepth: 2
    :caption: Contents:

    install
    getting-started
    tutorial-focused-crawl
    tutorial-in-depth-crawl
    tutorial-crawling-cookie
    tutorial-crawling-tor
    page-classifiers
    crawling-strategies
    data-formats
    link-filters
    seed-finder

Links
-----

* `GitHub repository <https://github.com/ViDA-NYU/ache>`__
