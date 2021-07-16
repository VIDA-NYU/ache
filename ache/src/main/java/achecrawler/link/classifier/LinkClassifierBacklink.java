package achecrawler.link.classifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import achecrawler.link.classifier.builder.Instance;
import achecrawler.link.classifier.builder.LinkNeighborhoodWrapper;
import achecrawler.link.frontier.LinkRelevance;
import achecrawler.target.model.Page;
import achecrawler.util.parser.LinkNeighborhood;

public class LinkClassifierBacklink implements LinkClassifier {

    private LinkNeighborhoodWrapper wrapper;
    private String[] attributes;

    public LinkClassifierBacklink(LinkNeighborhoodWrapper wrapper, String[] attribute) {
        this.wrapper = wrapper;
        this.attributes = attribute;
    }

    public LinkRelevance[] classify(Page page) throws LinkClassifierException {
        try {
            LinkNeighborhood[] lns = page.getParsedData().getLinkNeighborhood();
            Map<String, Instance> urlWords = wrapper.extractLinks(lns, attributes);
            
            List<LinkRelevance> linkRelevance = new ArrayList<>();
            for(String urlStr : urlWords.keySet()) {
                URL url = new URL(urlStr);
                double relevance = -1;
                
                double pageRelevance = page.getTargetRelevance().getRelevance();
                if (pageRelevance > 100 && pageRelevance < 200) {
                    if (isInitialPage(urlStr) && !page.getURL().getHost().equals(url.getHost())) {
                        relevance = 201;
                        url = new URL(url.getProtocol(), url.getHost(), "/");
                    }
                }

                linkRelevance.add(new LinkRelevance(url, relevance));
            }
            return (LinkRelevance[]) linkRelevance.toArray(new LinkRelevance[linkRelevance.size()]);
            
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            throw new LinkClassifierException(ex.getMessage());
        }
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
