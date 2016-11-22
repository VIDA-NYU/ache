package focusedCrawler.crawler.async;

import focusedCrawler.link.frontier.LinkRelevance;


public interface Downloader {

    public void dispatchDownload(LinkRelevance link);

    public boolean hasPendingDownloads();

    public void close();

}
