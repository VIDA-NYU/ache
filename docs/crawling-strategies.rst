Crawling Strategies
###################

ACHE has several configuration options to control the crawling strategy, i.e.,
which links the crawler should follow and priority of each link.

Scope
-----

Scope refers to the ability of the crawler only follow links that point to the
same "host". If the crawler is configured to use the "seed scope", it will only
follow links that belong to the same host of the URLs included in the seeds
file. You can enable scope adding the following line to ``ache.yml``:

.. code:: yaml

    link_storage.link_strategy.use_scope: true

For example, if the scope is enabled and the seed file contains the following
URLs::

  http://pt.wikipedia.org/
  http://en.wikipedia.org/

then the crawler will only follow links within the domains ``pt.wikipedia.org``
and ``en.wikipedia.org``. Links to any other domains will be ignored.

Hard-focus vs. Soft-focus
-------------------------

The focus mode (hard vs. soft) is another way to prune the search space, i.e,
discard links that will not lead to relevant pages. Relevant pages tend to
cluster in `connected components
<https://en.wikipedia.org/wiki/Connected_component_(graph_theory)>`_,
therefore the crawler can ignore all links from irrelevant pages to reduce
the amount of links that should be considered for crawling.
In "hard-focus mode", the crawler will ignore all links from irrelevant pages.
In "soft-focus mode", the crawler will not ignore links from irrelevant pages,
and will rely solely on the link classifier to define which links should be
followed and their priority. The hard-focus mode can be enabled (or disabled)
using the following setting in ``ache.yml``:

.. code:: yaml

    target_storage.hard_focus: true

When the hard focus mode is disabled, the number of discovered links will grow
quickly, so the use of a link classifier (described bellow) is highly recommended
to define the priority that links should be crawled.

Link Classifiers
----------------

The order in which pages are crawled depends on the ``Link Classifier`` used.
A link classifier assigns a score (a double value) to each link discovered,
and the crawler will crawl every link with a positive score with priority
proportional to its score.

To configure link classifiers, you should add the key
``link_storage.link_classifier.type`` to ``ache.yml`` configuration file.

ACHE ships with several link classifier implementations, which are
detailed next.


======================
MaxDepthLinkClassifier
======================

The max depth link classifier assigns scores to discovered links proportional to
their depth the web tree (assuming the URLs provided as seeds are the roots) and
will ignore any links whose depth is higher than the configured threshold.

For example, if you would like to crawl only the URLs provided as seeds,
you could use the following configuration:

.. code:: yaml

  link_storage.link_classifier.type: MaxDepthLinkClassifier
  link_storage.link_classifier.max_depth: 0

This configuration instructs ACHE to use the MaxDepthLinkClassifier and only crawl
links within distance from the seeds equal to 0 (i.e., only the seeds).

If, instead, you use:

.. code:: yaml

  link_storage.link_classifier.type: MaxDepthLinkClassifier
  link_storage.link_classifier.max_depth: 1

ACHE will crawl the seeds and every page linked from the seed pages
(depth equals to 1).

Keep in mind that you can also combine this configuration with other
configurations such as the scope. For example if you add the scope
configuration as follows:

.. code:: yaml

  link_storage.link_strategy.use_scope: true
  link_storage.link_classifier.type: MaxDepthLinkClassifier
  link_storage.link_classifier.max_depth: 1


then ACHE will crawl only pages with a maximum depth of 1 AND belong to the
sites provided as seeds. Without the scope configuration, ACHE would crawl pages
from any web site with depth equal to 1.

==================
LinkClassifierImpl
==================
TODO

.. _crawlingstrategies-onlinelearning:

Online Learning
---------------
TODO


Backlink/Bipartite Crawling
---------------------------
TODO
