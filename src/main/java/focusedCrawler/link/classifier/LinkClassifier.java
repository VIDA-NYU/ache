package focusedCrawler.link.classifier;

import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.parser.LinkNeighborhood;

/**
 *
 *
 * <p>Description: This interface represents the behavior of a link classifier
 * which classifies links from pages given them priorities.</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 *
 * @author Luciano Barbosa
 * @version 1.0
 */
public interface LinkClassifier {

  public LinkRelevance[] classify(Page page) throws LinkClassifierException;

  public LinkRelevance classify(LinkNeighborhood ln) throws LinkClassifierException;

}

