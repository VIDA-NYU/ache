package focusedCrawler.link.classifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import focusedCrawler.link.classifier.builder.Instance;
import focusedCrawler.link.classifier.builder.LinkNeighborhoodWrapper;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.parser.PaginaURL;

public class LinkClassifierBacklink implements LinkClassifier {

    private LinkNeighborhoodWrapper wrapper;
    private String[] attributes;

    public LinkClassifierBacklink(LinkNeighborhoodWrapper wrapper, String[] attribute) {
        this.wrapper = wrapper;
        this.attributes = attribute;
    }

    public LinkRelevance[] classify(PaginaURL page) throws LinkClassifierException {
        LinkRelevance[] linkRelevance = null;
        try {
            HashMap<String, Instance> urlWords = wrapper.extractLinks(page, attributes);
            linkRelevance = new LinkRelevance[urlWords.size()];
            Iterator<String> iter = urlWords.keySet().iterator();
            int count = 0;
            while (iter.hasNext()) {
                String urlStr = (String) iter.next();
                URL url = new URL(urlStr);
                double relevance = -1;

                if (page.getRelevance() > 100 && page.getRelevance() < 200) {
                    if (isInitialPage(urlStr) && !page.getURL().getHost().equals(url.getHost())) {
                        relevance = 201;
                        url = new URL(url.getProtocol(), url.getHost(), "/");
                    }
                }

                linkRelevance[count] = new LinkRelevance(url, relevance);
                count++;
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            throw new LinkClassifierException(ex.getMessage());
        }
        return linkRelevance;
    }

    @Override
    public LinkRelevance classify(LinkNeighborhood ln) throws LinkClassifierException {
        // TODO Auto-generated method stub
        return null;
    }

    private boolean isInitialPage(String urlStr) throws MalformedURLException {
        boolean result = false;
        URL url = new URL(urlStr);
        String file = url.getFile();
        if (file.equals("/") || file.equals("") || file.equals("index.htm") || file.equals("index.html")) {
            result = true;
        }
        return result;
    }

}
