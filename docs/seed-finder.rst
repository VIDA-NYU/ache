.. _seedfinder:

SeedFinder Tool
#################

ACHE includes a tool called SeedFinder, which helps to discover more pages and web sites that contain relevant content. After you have your target page classifier ready, you can use SeedFinder to automatically discover a large set of seed URLs to start a crawl. You can feed SeedFinder with your page classifier and a initial search engine query, and SeedFinder will go ahead and automatically generate more queries that will potentially retrieve more relevant pages and issues them to a search engine until the max number of queries parameter is reached.

SeedFinder is available as an ACHE sub-command. For more instructions, you can run ``ache help seedFinder``::

  NAME
          ache seedFinder - Runs the SeedFinder tool

  SYNOPSIS
          ache seedFinder [--csvPath <csvPath>] [(-h | --help)]
                  --initialQuery <initialQuery> [--maxPages <maxPagesPerQuery>]
                  [--maxQueries <maxNumberOfQueries>] [--minPrecision <minPrecision>]
                  --modelPath <modelPath> [--searchEngine <searchEngine>]
                  [--seedsPath <seedsPath>]

  OPTIONS
          --csvPath <csvPath>
              The path where to write a CSV file with stats

          -h, --help
              Display help information

          --initialQuery <initialQuery>
              The inital query to issue to the search engine

          --maxPages <maxPagesPerQuery>
              Maximum number of pages per query

          --maxQueries <maxNumberOfQueries>
              Max number of generated queries

          --minPrecision <minPrecision>
              Stops query pagination after precision drops bellow this minimum
              precision threshold

          --modelPath <modelPath>
              The path to the page classifier model

          --searchEngine <searchEngine>
              The search engine to be used

          --seedsPath <seedsPath>
              The path where the seeds generated should be saved


For more details on how SeedFinder works, you can refer to:

Karane Vieira, Luciano Barbosa, Altigran Soares da Silva, Juliana Freire, Edleno Moura. **Finding seeds to bootstrap focused crawlers.** World Wide Web. 2016.
https://link.springer.com/article/10.1007/s11280-015-0331-7
