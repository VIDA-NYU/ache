package focusedCrawler.crawler.async;

import java.io.Serializable;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.BaseRobotsParser;
import crawlercommons.robots.SimpleRobotRulesParser;
import focusedCrawler.crawler.crawlercommons.fetcher.AbortedFetchException;
import focusedCrawler.crawler.crawlercommons.fetcher.FetchedResult;
import focusedCrawler.link.LinkStorage;
import focusedCrawler.link.frontier.LinkRelevance;

public class RobotsTxtHandler implements HttpDownloader.Callback {

    @SuppressWarnings("serial")
    public static class RobotsData implements Serializable {

        public BaseRobotRules robotRules;
        public LinkRelevance link;

        public RobotsData(LinkRelevance link, BaseRobotRules robotRules) {
            this.link = link;
            this.robotRules = robotRules;
        }
    }
    
    private static final Logger logger = LoggerFactory.getLogger(RobotsTxtHandler.class);
    
    private BaseRobotsParser parser = new SimpleRobotRulesParser();
    private LinkStorage linkStorage;
    private String userAgentName;
    
    public RobotsTxtHandler(LinkStorage linkStorage, String userAgentName) {
        this.linkStorage = linkStorage;
        this.userAgentName = userAgentName;
    }
    
    @Override
    public void completed(LinkRelevance link, FetchedResult response) {
        int statusCode = response.getStatusCode();
        if(statusCode >= 200 && statusCode < 300) {
            logger.info("Successfully downloaded URL=["+response.getBaseUrl()+"] HTTP-Response-Code="+statusCode);
            processRobot(link, response, false);
        } else {
            logger.info("Server returned bad code for URL=["+response.getBaseUrl()+"] HTTP-Response-Code="+statusCode);
            processRobot(link, response, true);
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
        processRobot(link, null, true);
    }
    
    private void processRobot(LinkRelevance link, FetchedResult response, boolean fetchFailed) {
        
        BaseRobotRules robotRules;
        if(fetchFailed || response == null) {
            robotRules = parser.failedFetch(HttpStatus.SC_GONE);
        }
        else {
            String contentType = response.getContentType();
            boolean isPlainText = (contentType != null) && (contentType.startsWith("text/plain"));
            if ((response.getNumRedirects() > 0) && !isPlainText) {
                robotRules = parser.failedFetch(HttpStatus.SC_GONE);
            } else {
                robotRules = parser.parseContent(
                    response.getFetchedUrl(),
                    response.getContent(),
                    response.getContentType(),
                    userAgentName   
                );
            }
        }
        
        try {
            RobotsData robotsData = new RobotsData(link, robotRules);
            linkStorage.insert(robotsData);
        } catch (Exception e) {
            logger.error("Failed to insert robots.txt data into link storage.", e);
        }
        
    }
    
}
