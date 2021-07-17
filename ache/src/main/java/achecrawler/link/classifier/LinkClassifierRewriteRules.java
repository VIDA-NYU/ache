package achecrawler.link.classifier;

import achecrawler.dedup.rules.RewriteRule;
import achecrawler.link.frontier.LinkRelevance;
import achecrawler.target.model.Page;
import achecrawler.target.model.ParsedData;
import achecrawler.util.parser.LinkNeighborhood;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LinkClassifierRewriteRules implements LinkClassifier {

    private List<RewriteRule> rules;

    public LinkClassifierRewriteRules(List<RewriteRule> rules) {
        this.rules = rules;
    }

    @Override
    public LinkRelevance[] classify(Page page) throws LinkClassifierException {
        ParsedData parsedData = page.getParsedData();
        if (parsedData == null) {
            return new LinkRelevance[0];
        }

        URL[] lns = parsedData.getLinks();
        LinkNeighborhood ln = null;
        try {
            List<LinkRelevance> links = new ArrayList<>();
            for (int i = 0; i < lns.length; i++) {
                for (RewriteRule rule : rules) {
                    String url = lns[i].toString();
                    double relevance = 1.0d;
//                    double relevance = Math.abs(page.getLinkRelevance().getRelevance()) - 1;
                    if (rule.matches(url)) {
//                        url = rule.rewrite(url);
                        relevance = 1e-10;
                    }
                    links.add(new LinkRelevance(url, relevance));
                }
            }
            return links.toArray(new LinkRelevance[links.size()]);
        } catch (Exception ex) {
            throw new LinkClassifierException("Failed to classify link [" + ln.getLink().toString()
                    + "] from page: " + page.getURL().toString(), ex);
        }
    }

    @Override
    public LinkRelevance classify(LinkNeighborhood ln) {
        throw new UnsupportedOperationException("Method not supported yet.");
    }

}
