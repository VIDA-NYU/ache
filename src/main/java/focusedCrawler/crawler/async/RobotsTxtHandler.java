package focusedCrawler.crawler.async;

import java.io.Serializable;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import focusedCrawler.crawler.crawlercommons.fetcher.FetchedResult;
import focusedCrawler.link.LinkStorage;
import focusedCrawler.link.frontier.LinkRelevance;

public class RobotsTxtHandler implements HttpDownloader.Callback {

    @SuppressWarnings("serial")
    public static class RobotsData implements Serializable {

        public SimpleRobotRules robotRules;
        public LinkRelevance link;

        public RobotsData(LinkRelevance link, SimpleRobotRules robotRules) {
            this.link = link;
            this.robotRules = robotRules;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(RobotsTxtHandler.class);

    private SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
    private LinkStorage linkStorage;
    private String userAgentName;

    public RobotsTxtHandler(LinkStorage linkStorage, String userAgentName) {
        this.linkStorage = linkStorage;
        this.userAgentName = userAgentName;
    }

    @Override
    public void completed(LinkRelevance link, FetchedResult response) {
        int statusCode = response.getStatusCode();
        if (statusCode >= 200 && statusCode < 300) {
            // HTTP 2xx means the request was successful
            processRobot(link, response, false);
        } else {
            processRobot(link, response, true);
        }
    }

    @Override
    public void failed(LinkRelevance link, Exception e) {
        processRobot(link, null, true);
    }

    private void processRobot(LinkRelevance link, FetchedResult response, boolean fetchFailed) {

        SimpleRobotRules robotRules;
        if (fetchFailed || response == null) {
            robotRules = parser.failedFetch(HttpStatus.SC_GONE);
        } else {
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
