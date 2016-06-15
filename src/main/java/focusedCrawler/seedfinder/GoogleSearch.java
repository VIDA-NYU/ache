package focusedCrawler.seedfinder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import focusedCrawler.util.TimeDelay;
import focusedCrawler.util.parser.BackLinkNeighborhood;

public class GoogleSearch implements SearchEngineApi {
    
    private final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
    private int docsPerPage = 10;
    
    private TimeDelay timer = new TimeDelay(5000);
    
    public List<BackLinkNeighborhood> submitQuery(String query, int page) throws IOException {
        
        timer.waitMinimumDelayIfNecesary();
        
        // 21 -> max number allowed by google... decreases after
        String queryUrl = "https://www.google.com/search?q=" + query + "&num="+docsPerPage + "&start="+page*docsPerPage;
        System.out.println("URL:"+queryUrl);
        try {
            URLConnection connection = new URL(queryUrl).openConnection();
            connection.setRequestProperty("User-Agent", userAgent);
            connection.connect();
    
            InputStream is = connection.getInputStream();
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
        } catch (IOException e) {
            throw new IOException("Failed to download backlinks from Google.", e);
        }
    
    }

}