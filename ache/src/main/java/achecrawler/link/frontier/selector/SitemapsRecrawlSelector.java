package achecrawler.link.frontier.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import achecrawler.link.frontier.LinkRelevance;

public class SitemapsRecrawlSelector implements LinkSelector {

    private Cache<String, Boolean> accessTimeCache;
    private List<LinkRelevance> links;
    private int numberOfLinks;

    public SitemapsRecrawlSelector() {
        this(30, TimeUnit.SECONDS);
    }
    
    public SitemapsRecrawlSelector(int sitemapsRecrawlInterval) {
        this(sitemapsRecrawlInterval, TimeUnit.MINUTES);
    }
    
    public SitemapsRecrawlSelector(long recrawlAfter, TimeUnit unit) {
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
        if (link.getType() != LinkRelevance.Type.SITEMAP && links.size() < this.numberOfLinks) {
            return;
        }
        String url = link.getURL().toString();
        Boolean isPresent = accessTimeCache.getIfPresent(url);
        if(isPresent == null) {
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
