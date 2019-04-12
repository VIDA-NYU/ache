package focusedCrawler.crawler.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.crawler.crawlercommons.fetcher.FetchedResult;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.TargetStorage;
import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.ParsedData;
import focusedCrawler.util.parser.PaginaURL;

public class ForwardLinkHandler implements HttpDownloader.Callback {

    private static final Logger logger = LoggerFactory.getLogger(ForwardLinkHandler.class);

    private String crawlerId;
    private TargetStorage targetStorage;

    public ForwardLinkHandler(String crawlerId, TargetStorage targetStorage) {
        this.crawlerId = crawlerId;
        this.targetStorage = targetStorage;
    }

    @Override
    public void completed(LinkRelevance link, FetchedResult response) {

        int statusCode = response.getStatusCode();
        if (statusCode >= 200 && statusCode < 300) {
            processPage(link, response);
        }
        //else {
        //     TODO: Update metadata about page visits in link storage
        //}
    }

    @Override
    public void failed(LinkRelevance link, Exception e) {
    }

    private void processPage(LinkRelevance link, FetchedResult response) {
        try {
            Page page = new Page(response);
            page.setLinkRelevance(link);
            page.setCrawlerId(crawlerId);
            if (page.isHtml()) {
                PaginaURL pageParser = new PaginaURL(page);
                page.setParsedData(new ParsedData(pageParser));
            }
            targetStorage.insert(page);

        } catch (Exception e) {
            logger.error("Problem while processing data.", e);
        }
    }

}
