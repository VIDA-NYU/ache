package achecrawler.crawler.async;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.SiteMap;
import crawlercommons.sitemaps.SiteMapIndex;
import crawlercommons.sitemaps.SiteMapParser;
import crawlercommons.sitemaps.SiteMapURL;
import crawlercommons.sitemaps.UnknownFormatException;
import achecrawler.crawler.crawlercommons.fetcher.AbortedFetchException;
import achecrawler.crawler.crawlercommons.fetcher.FetchedResult;
import achecrawler.link.LinkStorage;
import achecrawler.link.frontier.LinkRelevance;

public class SitemapXmlHandler implements HttpDownloader.Callback {
    
    @SuppressWarnings("serial")
    public static class SitemapData implements Serializable {
        public List<String> sitemaps = new ArrayList<>();
        public List<String> links = new ArrayList<>();
    }

    private static final Logger logger = LoggerFactory.getLogger(SitemapXmlHandler.class);
    
    private LinkStorage linkStorage;
    private SiteMapParser parser = new SiteMapParser(false);
    

    public SitemapXmlHandler(LinkStorage linkStorage) {
        this.linkStorage = linkStorage;
    }
    
    @Override
    public void completed(LinkRelevance link, FetchedResult response) {
        int statusCode = response.getStatusCode();
        if(statusCode >= 200 && statusCode < 300) {
            logger.info("Successfully downloaded URL=["+response.getBaseUrl()+"] HTTP-Response-Code="+statusCode);
            processData(link, response);
        } else {
            logger.info("Server returned bad code for URL=["+response.getBaseUrl()+"] HTTP-Response-Code="+statusCode);
        }
    }
    
    @Override
    public void failed(LinkRelevance link, Exception e) {
        if(e instanceof AbortedFetchException) {
            AbortedFetchException afe = (AbortedFetchException) e;
            logger.info("Download aborted: \n>URL: {}\n>Reason: {}",
                        link.getURL().toString(), afe.getAbortReason());
        } else {
            logger.info("Failed to download URL: "+link.getURL().toString(), e.getMessage());
        }
    }
    
    private void processData(LinkRelevance link, FetchedResult response) {
        
        AbstractSiteMap sm;
        try {
            sm = parser.parseSiteMap(response.getContent(), new URL(response.getFetchedUrl()));
        } catch (UnknownFormatException | IOException e) {
            logger.warn("Failed to download sitemap: "+link.getURL().toString(), e);
            return;
        }

        SitemapData sitemapData = new SitemapData();
        if (sm.isIndex()) {
            Collection<AbstractSiteMap> links = ((SiteMapIndex) sm).getSitemaps();
            for (AbstractSiteMap asm : links) {
                sitemapData.sitemaps.add(asm.getUrl().toString());
            }
        } else {
            Collection<SiteMapURL> links = ((SiteMap) sm).getSiteMapUrls();
            for (SiteMapURL smu : links) {
                sitemapData.links.add(smu.getUrl().toString());
            }
        }
        
        try {
            linkStorage.insert(sitemapData);
        } catch (Exception e) {
            logger.error("Failed to insert sitemaps data into link storage.", e);
        }
        
    }
    
}
