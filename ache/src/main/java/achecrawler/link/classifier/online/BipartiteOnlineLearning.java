package achecrawler.link.classifier.online;

import achecrawler.link.classifier.LinkClassifier;
import achecrawler.link.classifier.builder.LinkClassifierBuilder;
import achecrawler.link.frontier.FrontierManager;
import achecrawler.link.frontier.LinkRelevance;
import achecrawler.util.parser.LinkNeighborhood;
import achecrawler.util.vsm.VSMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;


public class BipartiteOnlineLearning extends OnlineLearning {
    
    public static final Logger logger = LoggerFactory.getLogger(BipartiteOnlineLearning.class);

    private final FrontierManager frontierManager;
    private final LinkClassifierBuilder classifierBuilder;
    private final String dataPath;
    
    public BipartiteOnlineLearning(int learnLimit, boolean async, FrontierManager frontierManager,
            LinkClassifierBuilder classifierBuilder, String dataPath) {
        super(learnLimit, async, frontierManager);
        this.frontierManager = frontierManager;
        this.classifierBuilder = classifierBuilder;
        this.dataPath = dataPath;
    }

    public synchronized void execute() throws Exception {
        frontierManager.getFrontier().commit();
        createClassifiers(readRelevantUrlsFromFile());
        frontierManager.getFrontier().commit();
    }

    private HashSet<String> readRelevantUrlsFromFile() throws IOException {
        HashSet<String> relSites = new HashSet<>();
        File file = new File(dataPath + File.separator + "entry_points");
        try (BufferedReader input = new BufferedReader(new FileReader(file))) {
            for (String line = input.readLine(); line != null; line = input.readLine()) {
                if (line.startsWith("------")) {
                    String host = line.replace("-", "");
                    String url = "http://" + host + "/";
					relSites.add(url);
                }
            }
        }
        return relSites;
    }

    private HashMap<String, VSMElement> createClassifiers(HashSet<String> relSites) throws Exception {
	    final boolean updateFrontier = true;
		HashMap<String,VSMElement> elems = new HashMap<>();
		logger.info("Building outlink classifier...");
		LinkClassifier outlinkClassifier = classifierBuilder.forwardlinkTraining(relSites,0,"LinkClassifierAuthority");
		if(updateFrontier){
			frontierManager.setOutlinkClassifier(outlinkClassifier);
		}
		LinkNeighborhood[] outLNs = frontierManager.getGraphRepository().getLNs();
		HashSet<String> visitedAuths = frontierManager.getFrontier().visitedAuths();
		HashSet<String> usedLinks = new HashSet<>();
//		Vector<VSMElement> temp = new Vector<VSMElement> ();
		for (LinkNeighborhood outLN : outLNs) {
			if (outLN != null) {
				LinkRelevance lr = outlinkClassifier.classify(outLN);
				if (updateFrontier) {
					frontierManager.getFrontier().update(lr);
					usedLinks.add(lr.getURL().toString());
				}
				String id = frontierManager.getGraphRepository().getID(outLN.getLink().toString());
				if (id != null) {
					VSMElement elem = new VSMElement(id, (lr.getRelevance() - 200) / 100);
					if (visitedAuths.contains(outLN.getLink().toString())) {
						if (relSites.contains(outLN.getLink().toString())) {
							elem.setWeight(1);
						} else {
							elem.setWeight(0.0000001);
						}
					}
					elems.put(id + "_auth", elem);
				}
			}
		}
		
		logger.info("Building backlink classifier...");
		LinkClassifier backlinkClassifier = classifierBuilder.backlinkTraining(elems);
		if(updateFrontier){
			frontierManager.setBacklinkClassifier(backlinkClassifier);
		}
		LinkNeighborhood[] backLNs = frontierManager.getGraphRepository().getBacklinkLN();
		for (LinkNeighborhood backLN : backLNs) {
			if (backLN != null) {
				LinkRelevance lr = backlinkClassifier.classify(backLN);
				if (updateFrontier && lr != null && !usedLinks.contains(lr.getURL().toString())) {
					frontierManager.getFrontier().update(lr);
				}
				String id = frontierManager.getGraphRepository().getID(backLN.getLink().toString());
				if (id != null && lr != null) {
					VSMElement elem = new VSMElement(id, (lr.getRelevance() - 100) / 100);
					elems.put(id + "_hub", elem);
				}
			}
		}
		return elems;
	}
	
}
