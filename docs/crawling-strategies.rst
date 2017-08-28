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
TODO

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
