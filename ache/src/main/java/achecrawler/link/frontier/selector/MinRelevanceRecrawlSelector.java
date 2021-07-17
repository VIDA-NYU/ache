package achecrawler.link.frontier.selector;

import achecrawler.link.frontier.LinkRelevance.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import achecrawler.link.frontier.LinkRelevance;

public class MinRelevanceRecrawlSelector implements LinkSelector {

    private boolean recrawlRobots = true;
    private boolean recrawlSitemaps = true;

    private Cache<String, Boolean> accessTimeCache;
    private List<LinkRelevance> links;
    private int numberOfLinks;
    private double minRelevance;

    public MinRelevanceRecrawlSelector(int recrawlInterval, double minRelevance,
            boolean recrawlRobots, boolean recrawlSitemaps) {
        this(recrawlInterval, TimeUnit.MINUTES, minRelevance, recrawlRobots, recrawlSitemaps);
    }

    public MinRelevanceRecrawlSelector(long recrawlAfter, TimeUnit unit, double minRelevance,
            boolean recrawlRobots, boolean recrawlSitemaps) {
        this.minRelevance = minRelevance;
        this.recrawlRobots = recrawlRobots;
        this.recrawlSitemaps = recrawlSitemaps;
        this.accessTimeCache = CacheBuilder.newBuilder()
                .expireAfterWrite(recrawlAfter, unit)
                .build();
    }

    @Override
    public void startSelection(int numberOfLinks) {
        this.numberOfLinks = numberOfLinks;
        this.links = new ArrayList<>();
    }

    @Override
    public void evaluateLink(LinkRelevance link) {
        // already reached max capacity
        if (links.size() >= this.numberOfLinks) {
            return;
        }
        // recrawl only links within min relevance
        if (Math.abs(link.getRelevance()) < minRelevance) {
            return;
        }
        // do not recrawl robots if configured not to
        if (!recrawlRobots && link.getType() == Type.ROBOTS) {
            return;
        }
        // do not recrawl sitemaps if configured not to
        if (!recrawlSitemaps && link.getType() == Type.SITEMAP) {
            return;
        }

        String url = link.getURL().toString();
        Boolean isPresent = accessTimeCache.getIfPresent(url);
        if (isPresent == null) {
            links.add(link);
            accessTimeCache.put(url, Boolean.TRUE);
        }
    }

    @Override
    public List<LinkRelevance> getSelectedLinks() {
        List<LinkRelevance> selectedLinks = this.links;
        this.links = null; // clean-up reference
        return selectedLinks;
    }

}
