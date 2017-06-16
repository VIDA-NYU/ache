Welcome to ACHE's Documentation!
========================================

ACHE is an implementation of a focused crawler. A focused crawler is a web crawler that collects Web pages that satisfy some specific property.
ACHE differs from other crawlers in the sense that it includes **page classifiers** that allows it to distinguish between relevant and irrelevant pages in a given domain. The page classifier can be from a simple regular expression (that matches every page that contains a specific word, for example), to a sophisticated machine-learned classification model.
ACHE also includes **link classifiers**, which allows it decide the best order in which the links should be downloaded in order to find the relevant content on the web as fast as possible, at the same time it doesn't waste resources downloading irrelevant content.

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
