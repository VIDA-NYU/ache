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
TODO

.. _crawlingstrategies-onlinelearning:

Online Learning
---------------
TODO


Backlink/Bipartite Crawling
---------------------------
TODO
