package focusedCrawler.link.frontier.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import focusedCrawler.link.frontier.LinkRelevance;

public class MinRelevanceRecrawlSelector implements LinkSelector {

    private Cache<String, Boolean> accessTimeCache;
    private List<LinkRelevance> links;
    private int numberOfLinks;
    private double minRelevance;

    public MinRelevanceRecrawlSelector() {
        this(30, TimeUnit.SECONDS, 299d);
    }

    public MinRelevanceRecrawlSelector(int recrawlInterval, double minRelevance) {
        this(recrawlInterval, TimeUnit.MINUTES, minRelevance);
    }

    public MinRelevanceRecrawlSelector(long recrawlAfter, TimeUnit unit, double minRelevance) {
        this.minRelevance = minRelevance;
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
        if (links.size() >= this.numberOfLinks || (-1 * link.getRelevance()) < minRelevance) {
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
