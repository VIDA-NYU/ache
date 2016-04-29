package focusedCrawler.seedfinder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import focusedCrawler.util.parser.BackLinkNeighborhood;

public class GoogleSearch {
    
    private final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
    
    private long lastQueryTimestamp = 0;
    private int minimumTimeInterval = 5000;
    private int docsPerPage = 21;
    
    public BackLinkNeighborhood[] submitQuery(String query, int page) throws IOException {
        
        waitMinimumDelayIfNecesary();
        
        // 21 -> max number allowed by google... decreases after
        String queryUrl = "https://www.google.com/search?q=" + query + "&num="+docsPerPage + "&start="+page*docsPerPage;
        System.out.println("QUERY:"+query);
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
            
            int resultSize = linksUrl.size();
            System.out.println("Hits: "+resultSize);
    
            BackLinkNeighborhood[] backlinks = new BackLinkNeighborhood[resultSize];
            int i = 0;
            for (Element link : linksUrl) {
                backlinks[i] = new BackLinkNeighborhood();
                backlinks[i].setLink(link.attr("href"));
                backlinks[i].setTitle(link.text());
                i++;
            }
            return backlinks;
        } catch (IOException e) {
            throw new IOException("Failed to download backlinks from Google.", e);
        }
    
    }

    private void waitMinimumDelayIfNecesary() {
        if (lastQueryTimestamp == 0) {
            lastQueryTimestamp = System.currentTimeMillis();
            return;
        }
        
        long elapsedTime = System.currentTimeMillis() - lastQueryTimestamp;
        if (elapsedTime < minimumTimeInterval) {
            System.out.println("Waiting minimum delay: "+elapsedTime);
            long waitTime = minimumTimeInterval - elapsedTime;
            if(waitTime < 0) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Tread interrupted while waiting.");
                }
            }
        }
        
        lastQueryTimestamp = System.currentTimeMillis();
    }
}