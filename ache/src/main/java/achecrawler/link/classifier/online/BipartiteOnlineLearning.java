package achecrawler.link.classifier.online;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.link.classifier.LinkClassifier;
import achecrawler.link.classifier.builder.LinkClassifierBuilder;
import achecrawler.link.frontier.FrontierManager;
import achecrawler.link.frontier.LinkRelevance;
import achecrawler.util.parser.LinkNeighborhood;
import achecrawler.util.vsm.VSMElement;


public class BipartiteOnlineLearning extends OnlineLearning {
    
    public static final Logger logger = LoggerFactory.getLogger(BipartiteOnlineLearning.class);

    private FrontierManager frontierManager;
    private LinkClassifierBuilder classifierBuilder;
    private String dataPath;
    
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

    private HashSet<String> readRelevantUrlsFromFile() throws IOException, FileNotFoundException {
        HashSet<String> relSites = new HashSet<String>();
        File file = new File(dataPath + File.separator + "entry_points");
        try (BufferedReader input = new BufferedReader(new FileReader(file))) {
            for (String line = input.readLine(); line != null; line = input.readLine()) {
                if (line.startsWith("------")) {
                    String host = line.replace("-", "");
                    String url = "http://" + host + "/";
                    if (!relSites.contains(url)) {
                        relSites.add(url);
                    }
                }
            }
        }
        return relSites;
    }

    private HashMap<String, VSMElement> createClassifiers(HashSet<String> relSites) throws Exception {
	    boolean updateFrontier = true;
		HashMap<String,VSMElement> elems = new HashMap<String,VSMElement>();
		logger.info("Building outlink classifier...");
		LinkClassifier outlinkClassifier = classifierBuilder.forwardlinkTraining(relSites,0,"LinkClassifierAuthority");
		if(updateFrontier){
			frontierManager.setOutlinkClassifier(outlinkClassifier);
		}
		LinkNeighborhood[] outLNs = frontierManager.getGraphRepository().getLNs();
		HashSet<String> visitedAuths = frontierManager.getFrontier().visitedAuths();
		HashSet<String> usedLinks = new HashSet<String>();
//		Vector<VSMElement> temp = new Vector<VSMElement> ();
		for (int i = 0; i < outLNs.length; i++) {
			if(outLNs[i] != null){
				LinkRelevance lr = outlinkClassifier.classify(outLNs[i]);
				if(updateFrontier){
				    frontierManager.getFrontier().update(lr);
					usedLinks.add(lr.getURL().toString());
				}
				String id = frontierManager.getGraphRepository().getID(outLNs[i].getLink().toString());
				if(id != null){
					VSMElement elem = new VSMElement(id, (lr.getRelevance()-200)/100);
					if(visitedAuths.contains(outLNs[i].getLink().toString())){
						if(relSites.contains(outLNs[i].getLink().toString())){
							elem.setWeight(1);
						}else{
							elem.setWeight(0.0000001);
						}
					}
					elems.put(id + "_auth",elem);
				}
			}
		}
		
		logger.info("Building backlink classifier...");
		LinkClassifier backlinkClassifier = classifierBuilder.backlinkTraining(elems);
		if(updateFrontier){
			frontierManager.setBacklinkClassifier(backlinkClassifier);
		}
		LinkNeighborhood[] backLNs = frontierManager.getGraphRepository().getBacklinkLN();
		for (int i = 0; i < backLNs.length; i++) {
			if(backLNs[i] != null){
				LinkRelevance lr = backlinkClassifier.classify(backLNs[i]);
				if(updateFrontier && lr != null && !usedLinks.contains(lr.getURL().toString())){
				    frontierManager.getFrontier().update(lr);
				}
				String id = frontierManager.getGraphRepository().getID(backLNs[i].getLink().toString());
				if(id != null && lr != null){
					VSMElement elem = new VSMElement(id, (lr.getRelevance()-100)/100);
					elems.put(id + "_hub",elem);
				}
			}
		}
		return elems;
	}
	
}
