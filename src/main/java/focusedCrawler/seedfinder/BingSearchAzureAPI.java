package focusedCrawler.seedfinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import focusedCrawler.target.model.Page;
import focusedCrawler.util.parser.BackLinkNeighborhood;

public class BingSearchAzureAPI implements SearchEngineApi {
    
    private static final String BING_ADRESS = "https://api.datamarket.azure.com/Data.ashx/Bing/SearchWeb/v1/Web";

    private String accountKey = "d9zIG4ICwyPiUzBz0pDB9fvGr/UKDqk82fYBlJlXmhc";
    private String accountKeyEnc = buildKey(accountKey);
    private int top = 5;
    
    public BingSearchAzureAPI() {
    }
    
    public BingSearchAzureAPI(String accountKey) {
        this.accountKey = buildKey(accountKey);
    }

    private String buildKey(String accountKey) {
        byte[] accountKeyBytes = Base64.encodeBase64((accountKey + ":" + accountKey).getBytes());
        return new String(accountKeyBytes);
    }
    

    @Override
    public BackLinkNeighborhood[] submitQuery(String query, int page) throws IOException {
        List<String> urls = downloadResults(query, page);
        BackLinkNeighborhood[] links = new BackLinkNeighborhood[urls.size()];
        int i = 0;
        for (String link : urls) {
            links[i] = new BackLinkNeighborhood(link, null);
            i++;
        }
        return links;
    }
            
    public List<String> downloadResults(String keyword, int page) throws IOException {
        keyword = URLEncoder.encode(keyword, "UTF-8");
        URL url = null;
        try {
            int skip = page * top;
            url = new URL(BING_ADRESS+"?Query=%27" + keyword + "%27"+"&$skip="+skip+"&$top="+top);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL", e);
        }
            
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Basic " + accountKeyEnc);

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuffer output = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null) {
            output = output.append(line);
        }
        conn.disconnect();
        
        List<String> urls = parseXMLPage(new Page(url, output.toString()));
        
        return urls;
    }
    
    private List<String> parseXMLPage(Page page) {
        DOMParser parser = new DOMParser();
        try {
            parser.parse(new InputSource(new BufferedReader(new StringReader(page.getContent()))));
        } catch (SAXException | IOException e) {
            throw new RuntimeException("Failed to parse search results.", e);
        }
        Document doc = parser.getDocument();
        NodeList list = doc.getElementsByTagName("d:Url");
        List<String> urls = new ArrayList<String>();
        for (int j = 0; j < list.getLength(); j++) {
            Node node = list.item(j);
            NodeList children = node.getChildNodes();
            Node child = children.item(0);
            urls.add(child.getTextContent());
        }
        return urls;
    }

    public static void main(String[] args) throws IOException {
        new BingSearchAzureAPI().submitQuery("onion", 0);
    }
}
