package focusedCrawler.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Downloader {
    public static final Logger logger = LoggerFactory.getLogger(Downloader.class);

    private Map<String, List<String>> responseHeaders;
    private URL redirectionURL;
    private boolean isURLRedirecting = false;

    private String content, mimeType;

    private URL originalURL;

    private String threadName;
    private URLConnection conn ;

    protected Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    public Downloader(String url, String threadName) throws MalformedURLException, CrawlerException {
        this(new URL(url), threadName);
    }

    public Downloader(URL urlFinal, String threadName) throws CrawlerException {
        try {

            this.threadName = threadName;
            originalURL = urlFinal;

            conn = urlFinal.openConnection();
            responseHeaders = conn.getHeaderFields();
            extractMimeType();
            getRedirectedLocation(conn, responseHeaders);

            InputStream in = conn.getInputStream();
            StringBuffer buffer = new StringBuffer();
            BufferedReader bin = new BufferedReader(new InputStreamReader(in));
            String inputLine;

            try {
                while ((inputLine = bin.readLine()) != null) {
                    buffer.append(inputLine).append("\n");
                }
            } catch (IOException ioe) {
                bin.close();
                logger.error("IOException while downloading the page: ", ioe);
            }
            bin.close();
            content = buffer.toString();

        } catch (MalformedURLException exc) {
            throw new CrawlerException(threadName + ":" + exc.getMessage(), exc);
        } catch (SocketException exc) {
            throw new CrawlerException(threadName + ":" + exc.getMessage(), exc);
        } catch (IOException exc) {
            throw new CrawlerException(threadName + ":" + exc.getMessage(), exc);
        } catch (Exception exc) {
            throw new CrawlerException(threadName + ":" + exc.getMessage(), exc);
        }

    }

    private void extractMimeType() {

        
        if(conn instanceof HttpURLConnection){
            mimeType = conn.getContentType();
        }

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

    public String getRedirectedLocation(URLConnection conn,
            Map<String, List<String>> responseHeaders) {
        if (conn instanceof HttpURLConnection) {
            HttpURLConnection myHttpUrlConnection = (HttpURLConnection) conn;
            int responseCode;
            try {
                responseCode = myHttpUrlConnection.getResponseCode();
                if (responseCode == 301 || responseCode == 302 || responseCode == 303
                        || responseCode == 304 || responseCode == 305 || responseCode == 306
                        || responseCode == 307) {
                    if (responseHeaders.keySet() != null) {
                        for (String s : responseHeaders.keySet()) {

                            if (s != null && (s.equals("Content-Type") || s.equals("content-type"))) {
                                List<String> eachKeyStringSet = responseHeaders.get(s);
                                StringBuffer mime_type = new StringBuffer();
                                for (String eachField : eachKeyStringSet) {
                                    mime_type.append(" " + eachField);
                                }
                                mimeType = mime_type.toString();
                            }
                            if (s != null && (s.equals("Location") || s.equals("location"))) {
                                // we have a redirecting URL
                                isURLRedirecting = true;
                                List<String> wholeString = responseHeaders.get(s);
                                if (wholeString.size() > 0) {
                                    StringBuffer redirectingLocation = new StringBuffer();
                                    for (String strings : wholeString) {
                                        redirectingLocation.append(strings);
                                    }
                                    String redirectLocation;
                                    try { 
                                    redirectionURL = new URL(redirectingLocation.toString());
                                    redirectLocation = redirectingLocation.toString();
                                    } catch (MalformedURLException mex){
                                        redirectionURL = new URL(originalURL.getProtocol(),originalURL.getHost(),redirectingLocation.toString());
                                        redirectLocation = redirectionURL.toString();
                                    }
                                    return redirectLocation;
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("IOException while extracting mime-type and redirection URL",e);
            }
        }

        return null;
    }

}