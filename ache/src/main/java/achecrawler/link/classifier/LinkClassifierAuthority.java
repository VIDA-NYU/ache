package achecrawler.link.classifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import achecrawler.link.frontier.LinkRelevance;
import achecrawler.target.model.Page;
import achecrawler.util.parser.LinkNeighborhood;

public class LinkClassifierAuthority implements LinkClassifier {

    private LNClassifier classifier;

    public LinkClassifierAuthority() {}
    
    public LinkClassifierAuthority(LNClassifier classifier) {
        this.classifier = classifier;
    }

    public LinkRelevance[] classify(Page page) throws LinkClassifierException {
        try {
            LinkNeighborhood[] lns = page.getParsedData().getLinkNeighborhood();

            List<LinkRelevance> linkRelevance = new ArrayList<>();
            if (classifier != null) {
                for (int i = 0; i < lns.length; i++) {
                    LinkNeighborhood ln = lns[i];
                    URL url = ln.getLink();
                    double relevance = -1;
                    if (!page.getURL().getHost().equals(url.getHost())) {
                        double[] prob = classifier.classify(ln);
                        relevance = LinkRelevance.DEFAULT_AUTH_RELEVANCE + (prob[0] * 100);
                    }
                    linkRelevance.add(new LinkRelevance(url, relevance));
                }
            } else {
                for (int i = 0; i < lns.length; i++) {
                    double relevance = -1;
                    if (!page.getURL().getHost().equals(lns[i].getLink().getHost())) {
                        relevance = LinkRelevance.DEFAULT_AUTH_RELEVANCE + 1;
                    }
                    linkRelevance.add(new LinkRelevance(lns[i].getLink(), relevance));
                }
            }
            return (LinkRelevance[]) linkRelevance.toArray(new LinkRelevance[linkRelevance.size()]);

        } catch (Exception e) {
            throw new LinkClassifierException(e.getMessage(), e);
        }
    }

    @Override
    public LinkRelevance classify(LinkNeighborhood ln) throws LinkClassifierException {
        try {
            double relevance = -1;
            String url = ln.getLink().toString();
            if (isRootPage(url)) {
                if (classifier != null) {
                    double[] prob = classifier.classify(ln);
                    if (prob[0] == 1) {
                        prob[0] = 0.99;
                    }
                    relevance = LinkRelevance.DEFAULT_AUTH_RELEVANCE + (prob[0] * 100);
                } else {
                    relevance = LinkRelevance.DEFAULT_AUTH_RELEVANCE + 1;
                }
            }
            return new LinkRelevance(new URL(url), relevance);
        } catch (Exception e) {
            throw new LinkClassifierException("Failed to classify link", e);
        }
    }

    private boolean isRootPage(String urlStr) throws MalformedURLException {
        boolean result = false;
        URL url = new URL(urlStr);
        String file = url.getFile();
        if (file.equals("/") || file.equals("") ||
                file.equals("index.htm") || file.equals("index.html")) {
            result = true;
        }
        return result;
    }

}
