Link Filters
############

ACHE allows one to customize which domains and paths within a domain
should be crawled. This can be done by configuring link filters using
*regular expressions (regex)* or *wildcard* patterns.
Regex filters are evaluated using `Java's regular expression rules <https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html>`_,
and wildcard filters accept only the special character ``*``, which matches any character.

Link Filters are composed of two lists of patterns:

  * **whitelists** - patterns for URLs that are allowed to be followed, i.e., any URL that doesn't match the patterns is discarded.
  * **blacklists** - patterns for URLs that are *NOT* allowed to be followed, i.e., any URL that matches the patterns is discarded.

Links filters can have **global** or **per-domain** scope. Global filters are
evaluated against all links, whereas per-domain filters are evaluated only
against URLs that belong to the specified domain (only top-private domain level).
There are two ways to configure link filters:

 * :ref:`.yml file <link_filters_yml>`: Allows to configure global and per-domain link filters using YAML.
 * :ref:`.txt files <link_filters_txt>`: Allows to configure only regex-based global link filters.


.. _link_filters_yml:

Configuring using YAML
----------------------

ACHE automatically searches for a file named ``link_filters.yml``
in the same directory of the ``ache.yml`` file. This file can contain a single
``global`` entry and one entry per domain. Each entry should
specify a type (regex or wildcard) and a list of "whitelist" and
"blacklist" patterns, as shown in the example bellow:

.. code-block:: yaml

  global:
    type: wildcard
    whitelist:
      - "http://*allowed*"
    blacklist:
      - "*bad-bad-pattern*"
  www.example1.com:
    type: wildcard
    blacklist:
      - http://www.example1.com/*disallowed*.html
    whitelist:
      - http://www.example1.com/*allowed*
  www.example2.com:
    type: regex
    blacklist:
      - http:\/\/www\.example2\.com\/disallowed[0-9]+\.html
    whitelist:
      - http:\/\/www\.example2\.com\/allowed[0-9]+\.html

.. _link_filters_txt:

Configuring using .txt files
----------------------------

This is the old way to configure link filters. Only regex-based "global" filters
can be configured, i.e., filters that are applied to all URLs.
To configure a link filter, you will need to create text files containing one
regular expression per line.
All regular expressions loaded are evaluated against all links found on the
web pages crawled in other to determine whether the crawler should accept or
reject them.
For whitelist filters, ACHE will automatically search for a file named
``link_whitelist.txt``, whereas for blacklist filters the file name is
``link_blacklist.txt``. These files should be placed under the same directory
as the ``ache.yml``. These files are loaded once during crawler start-up.
The link filter files should look like this::

  https?:\/\/www\.example\.com\/some_path\/.*
  https?:\/\/www\.another-example\.com\/some_path\/.*
