package achecrawler.link.backlink;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import achecrawler.util.parser.BackLinkNeighborhood;

public class GoogleBacklinkApi implements BacklinkApi {
    
    final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
    
    public BackLinkNeighborhood[] downloadBacklinks(String host) throws IOException {
        
        // 21 -> max number allowed by google... decreases after
        String backlink = "https://www.google.com/search?q=link:" + host + "&num=21";

        try {
            URLConnection connection = new URL(backlink).openConnection();
            connection.setRequestProperty("User-Agent", userAgent);
            connection.connect();
    
            Document doc = Jsoup.parse(connection.getInputStream(), "UTF-8", host);
            Elements searchItems = doc.select("div#search");
            Elements linkHeaders = searchItems.select(".r");
            Elements linksUrl = linkHeaders.select("a[href]");
    
            int resultSize = linksUrl.size();
    
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
}

