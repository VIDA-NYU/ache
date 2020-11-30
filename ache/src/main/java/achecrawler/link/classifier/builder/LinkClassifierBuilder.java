package achecrawler.link.classifier.builder;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.link.BipartiteGraphRepository;
import achecrawler.link.classifier.LNClassifier;
import achecrawler.link.classifier.LinkClassifier;
import achecrawler.link.classifier.LinkClassifierAuthority;
import achecrawler.link.classifier.LinkClassifierHub;
import achecrawler.link.classifier.LinkClassifierImpl;
import achecrawler.link.frontier.FrontierManager;
import achecrawler.link.frontier.LinkRelevance;
import achecrawler.util.Sampler;
import achecrawler.util.parser.LinkNeighborhood;
import achecrawler.util.persistence.Tuple;
import achecrawler.util.string.StopList;
import achecrawler.util.vsm.VSMElement;

public class LinkClassifierBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(LinkClassifierBuilder.class);
    
    private int maxSamples = 5000; // TODO make this configurable
    private BipartiteGraphRepository graphRep;
	private Path linkClassifierFolder;
	private LinkClassifierTrainer classifierTrainer;
    private FrontierManager frontierManager;
	
    public LinkClassifierBuilder(String dataPath, StopList stoplist, FrontierManager frontierManager) {
		this.frontierManager = frontierManager;
		this.graphRep = frontierManager.getGraphRepository();
		this.classifierTrainer = new LinkClassifierTrainer(stoplist);
		this.linkClassifierFolder = Paths.get(dataPath, "/link_classifier/");

        if (!Files.exists(linkClassifierFolder)) {
            try {
                Files.createDirectories(linkClassifierFolder);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create link classifier folder: "
                        + linkClassifierFolder.toString(), e);
            }
        }
	}

    public synchronized LinkClassifier forwardlinkTraining(Set<String> relUrls,
            int levels, String className) throws Exception {

        List<Sampler<LinkNeighborhood>> instances = loadTrainingData(relUrls, levels);

        List<String> classValues;
        if (levels == 0) {
            classValues = asList("POS", "NEG");
        } else {
            classValues = asList("0", "1", "2");
        }
        
        LNClassifier classifier = classifierTrainer.trainLNClassifier(instances, classValues);
        classifier.writeToFolder(linkClassifierFolder);
        return createLinkClassifierImpl(className, classifier);
    }

    public LinkClassifier createLinkClassifierImpl(String className, LNClassifier lnClassifier) {
        if (className.indexOf("LinkClassifierImpl") != -1) {
            return new LinkClassifierImpl(lnClassifier);
        }
        if (className.indexOf("LinkClassifierAuthority") != -1) {
            return new LinkClassifierAuthority(lnClassifier);
        }
        if (className.indexOf("LinkClassifierHub") != -1) {
            return new LinkClassifierHub(lnClassifier);
        }
        return null;
    }

    /**
     * Loads training data to train the link classifier. The training data for each class were are
     * trying to predict is returned in a separate 'ordered' list. The position of the list
     * indicates the priority of the links in that list.
     * 
     * @param relevantUrls
     * @param levels
     * @return
     * @throws Exception
     */
    private List<Sampler<LinkNeighborhood>> loadTrainingData(Set<String> relevantUrls,
            int levels) throws Exception {

        final List<Sampler<LinkNeighborhood>> instances = new ArrayList<>();
        if (levels == 0) {
            // positive and negative case
            instances.add(new Sampler<LinkNeighborhood>(maxSamples));
            instances.add(new Sampler<LinkNeighborhood>(maxSamples));
        } else {
            // levels case
            for (int i = 0; i < levels; i++) {
                instances.add(new Sampler<LinkNeighborhood>(maxSamples));
            }
        }

        frontierManager.getFrontier().visitedLinks((LinkRelevance lr) -> {
            try {
                URL url = lr.getURL();
                LinkNeighborhood ln = graphRep.getLN(url);
                if (ln == null) {
                    return;
                }

                if (levels == 0) {
                    if (relevantUrls.contains(url.toString())) {
                        instances.get(0).sample(ln);
                    } else {
                        // consider add link to irrelevant sample (instances[1]) only if the number
                        // of irrelevant samples is smaller than the number of relevant sample
                        // (instances[1]). This is done to avoid a highly unbalanced number of
                        // samples of each class in the training data.
                        // The number of irrelevant links found is usually much higher than relevant
                        // links, so we only check if the number of irrelevant is not greater than
                        // the number of relevant links.
                        boolean considerSample = instances.get(1).reservoirSize() < instances.get(0).reservoirSize();
                        if (considerSample) {
                            instances.get(1).sample(ln);
                        }
                    }
                } else {
                    if (relevantUrls.contains(ln.getLink().toString())) {
                        instances.get(0).sample(ln);
                        addBacklinks(instances, ln.getLink(), 1, levels, relevantUrls);
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to include link relevance in training data.", e);
            }
        });

        return instances;
    }
	
	
    private void addBacklinks(List<Sampler<LinkNeighborhood>> instances, URL url, int level,
            int limit, Set<String> relUrls) throws IOException {
        if (level >= limit) {
            return;
        }
        LinkNeighborhood[] backlinks = graphRep.getBacklinksLN(url);
        for (int i = 0; i < backlinks.length; i++) {
            URL tempURL = backlinks[i].getLink();
            if (!relUrls.contains(tempURL.toString())) {
                instances.get(level).sample(backlinks[i]);
            }
            addBacklinks(instances, tempURL, level + 1, limit, relUrls);
        }
    }

    public LinkClassifier backlinkTraining(HashMap<String, VSMElement> outlinkWeights)
            throws Exception {

        List<VSMElement> trainingSet = new ArrayList<VSMElement>();
        Tuple<String>[] tuples = graphRep.getHubGraph();
        for (int i = 0; i < tuples.length; i++) {
            String hubId = tuples[i].getKey();
            String[] outlinks = tuples[i].getValue().split("###");
            double totalProb = 0;
            for (int j = 0; j < outlinks.length; j++) {
                VSMElement elem = outlinkWeights.get(outlinks[j] + "_auth");
                if (elem != null) {
                    totalProb = totalProb + elem.getWeight();
                }
            }
            String url = graphRep.getHubURL(hubId);
            if (url != null && outlinks.length > 20) {
                LinkNeighborhood ln = graphRep.getBacklinkLN(new URL(url));
                if (ln != null) {
                    VSMElement elem =
                            new VSMElement(ln.getLink().toString() + ":::" + ln.getAroundString(),
                                    totalProb / outlinks.length);
                    trainingSet.add(elem);
                }
            }
        }
        System.out.println("TOTAL TRAINING:" + trainingSet.size());
		
		
        Collections.sort(trainingSet, VSMElement.DESC_ORDER_COMPARATOR);

        List<LinkNeighborhood> allLNs = new ArrayList<LinkNeighborhood>();
        for (int i = 0; i < trainingSet.size(); i++) {
            String[] parts = trainingSet.get(i).getWord().split(":::");
            LinkNeighborhood ln = new LinkNeighborhood(new URL(parts[0]));
            if (parts.length > 1) {
                StringTokenizer tokenizer = new StringTokenizer(parts[1], " ");
                Vector<String> aroundTemp = new Vector<String>();
                while (tokenizer.hasMoreTokens()) {
                    aroundTemp.add(tokenizer.nextToken());
                }
                String[] aroundArray = new String[aroundTemp.size()];
                aroundTemp.toArray(aroundArray);
                ln.setAround(aroundArray);
            }
            allLNs.add(ln);
        }

        // Sample the backlinks based on position in the list
        List<LinkNeighborhood> posSites = new ArrayList<LinkNeighborhood>();
        List<LinkNeighborhood> negSites = new ArrayList<LinkNeighborhood>();
        int sampleSize = Math.min(5000, allLNs.size() / 2);
        for (int i = 0; i < allLNs.size(); i++) {
            if (posSites.size() < sampleSize) {
                posSites.add(allLNs.get(i));
            }
        }
        for (int i = allLNs.size() - 1; i >= 0; i--) {
            if (negSites.size() < sampleSize) {
                negSites.add(allLNs.get(i));
            }
        }


        // TODO Following code is not really doing a sample, it's just converting it the lists in
        // Sampler objects to be compatible with createSmileInput method. Need to do some refactoring
        // in this whole class.
        List<Sampler<LinkNeighborhood>> instances = new ArrayList<Sampler<LinkNeighborhood>>(2);
        Sampler<LinkNeighborhood> posSamples = new Sampler<LinkNeighborhood>(posSites.size());
        Sampler<LinkNeighborhood> negSamples = new Sampler<LinkNeighborhood>(negSites.size());
        instances.add(posSamples);
        instances.add(negSamples);

        // Train actual classifier
        List<String> classValues = asList("POS", "NEG");
        LNClassifier classifier = classifierTrainer.trainLNClassifierBacklink(instances, classValues);

        return createLinkClassifierImpl("LinkClassifierHub", classifier);
    }

}
