package focusedCrawler.seedfinder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import focusedCrawler.util.parser.BackLinkNeighborhood;

public class BingSearch {
    
    private final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
    
    UrlValidator urlValidator = new UrlValidator();
    private long lastQueryTimestamp = 0;
    int minimumTimeInterval = 5000;
    int docsPerPage = 15;

    public BackLinkNeighborhood[] submitQuery(String query, int page) throws IOException {
        
        waitMinimumDelayIfNecesary();
        
        // 21 -> max number allowed by google... decreases after
        String queryUrl = "https://www.bing.com/search?q=" + query + "&count="+docsPerPage + "&first="+(page*docsPerPage+1)+"&FORM=PORE";
        System.out.println("QUERY:"+query);
        System.out.println("URL:"+queryUrl);
        try {
            URLConnection connection = new URL(queryUrl).openConnection();
            connection.setRequestProperty("User-Agent", userAgent);
            connection.connect();
    
            InputStream is = connection.getInputStream();
            Document doc = Jsoup.parse(is, "UTF-8", query);
            is.close();
//            #b_results > li > h2 > a
            Elements searchItems = doc.select("ol#b_results");
            Elements linkHeaders = searchItems.select("h2");
            Elements linksUrl = linkHeaders.select("a");
            
            List<BackLinkNeighborhood> links = new ArrayList<>();
            for (Element link : linksUrl) {
                String linkStr = link.attr("href");
                if(urlValidator.isValid(linkStr)) {
                    BackLinkNeighborhood bl = new BackLinkNeighborhood();
                    bl.setLink(linkStr);
                    bl.setTitle(link.text());
                    links.add(bl);
                }
            }
            System.out.println("Hits: "+links.size());
            return (BackLinkNeighborhood[]) links.toArray(new BackLinkNeighborhood[links.size()]);
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