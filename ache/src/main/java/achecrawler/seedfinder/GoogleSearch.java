package achecrawler.seedfinder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import achecrawler.crawler.crawlercommons.fetcher.BaseFetchException;
import achecrawler.crawler.crawlercommons.fetcher.FetchedResult;
import achecrawler.crawler.crawlercommons.fetcher.http.SimpleHttpFetcher;
import achecrawler.util.TimeDelay;
import achecrawler.util.parser.BackLinkNeighborhood;

public class GoogleSearch implements SearchEngineApi {
    
    private final SimpleHttpFetcher fetcher;
    
    private int docsPerPage = 10;
    private TimeDelay timer = new TimeDelay(5000);
    
    public GoogleSearch(SimpleHttpFetcher fetcher) {
        this.fetcher = fetcher;
    }
    
    public List<BackLinkNeighborhood> submitQuery(String query, int page) throws IOException {
        
        timer.waitMinimumDelayIfNecesary();
        
        // 21 -> max number allowed by google... decreases after
        String queryUrl = "https://www.google.com/search?q=" + query + "&num="+docsPerPage + "&start="+page*docsPerPage;
        System.out.println("URL:"+queryUrl);
        try {
            FetchedResult result = fetcher.get(queryUrl);
            
            InputStream is = new ByteArrayInputStream(result.getContent());
            Document doc = Jsoup.parse(is, "UTF-8", query);
            is.close();
            
            Elements searchItems = doc.select("div#search");
            Elements linkHeaders = searchItems.select(".r");
            Elements linksUrl = linkHeaders.select("a[href]");
            
            List<BackLinkNeighborhood> links = new ArrayList<>();
            for (Element link : linksUrl) {
                String title = link.text();
                String url = link.attr("href");
                links.add(new BackLinkNeighborhood(url, title));
            }
            
            System.out.println(getClass().getSimpleName()+" hits: "+links.size());
            return links;
        } catch (IOException | BaseFetchException e) {
            throw new IOException("Failed to download backlinks from Google.", e);
        }
    
    }

}