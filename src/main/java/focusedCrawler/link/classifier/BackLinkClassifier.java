package focusedCrawler.link.classifier;

import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.parser.BackLinkNeighborhood;


public interface BackLinkClassifier {

	public LinkRelevance classify(BackLinkNeighborhood ln) throws LinkClassifierException;

}

