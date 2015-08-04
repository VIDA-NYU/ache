package focusedCrawler.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Downloader {
    
    public static final Logger logger = LoggerFactory.getLogger(Downloader.class);

    private URL originalURL;
    private URL redirectionURL;
    private boolean isURLRedirecting = false;
    private Map<String, List<String>> responseHeaders;
    private String mimeType;
    private String content;

    public Downloader(String url) throws MalformedURLException, CrawlerException {
        this(new URL(url));
    }

    public Downloader(URL url) throws CrawlerException {
        try {
            URLConnection connection = url.openConnection();
            connection.connect();
            
            this.originalURL = url;
            this.responseHeaders = connection.getHeaderFields();
            this.mimeType = connection.getContentType();
            
            if(connection instanceof HttpURLConnection){
                processRedirection((HttpURLConnection) connection, responseHeaders);
            }

            this.content = readContent(connection);
            
        } catch (IOException e) {
            throw new CrawlerException("Failed to donwload URL: "+url, e);
        }

    }

    private String readContent(URLConnection connection) throws IOException {
        InputStream in = connection.getInputStream();
        BufferedReader bin = new BufferedReader(new InputStreamReader(in));
        StringBuffer buffer = new StringBuffer();
        try {
            String inputLine;
            while ((inputLine = bin.readLine()) != null) {
                buffer.append(inputLine).append("\n");
            }
            return buffer.toString();
        } finally {
            bin.close();
        }
    }

    private void processRedirection(HttpURLConnection httpConnection,
                                     Map<String, List<String>> responseHeaders)
                                     throws IOException {
        
        int responseCode = httpConnection.getResponseCode();
        boolean isRedirectionCode = responseCode >= 301 && responseCode <= 307;
        
        if (isRedirectionCode && responseHeaders.keySet() != null) {
            for (String headerKey : responseHeaders.keySet()) {
                
                if(headerKey == null) {
                    continue;
                }

                if (headerKey.toLowerCase().equals("location")) {
                    // we have a redirecting URL
                    String location = httpConnection.getHeaderField(headerKey);
                    this.redirectionURL = new URL(originalURL, location);
                    this.isURLRedirecting = true;
                }
            }
        }
    }

    protected Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }
    
    public boolean isRedirection() {
        return isURLRedirecting;
    }

    public URL getOriginalUrl() {
        return originalURL;
    }

    public URL getRedirectionUrl() {
        return redirectionURL;
    }

    public String getContent() {
        return content;
    }

    public String getMimeType() {
        return mimeType;
    }

}