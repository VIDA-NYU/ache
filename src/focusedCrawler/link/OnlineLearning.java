package focusedCrawler.link;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import focusedCrawler.link.classifier.LinkClassifier;
import focusedCrawler.link.classifier.builder.ClassifierBuilder;
import focusedCrawler.link.frontier.FrontierTargetRepositoryBaseline;
import focusedCrawler.link.linkanalysis.HITS;
import focusedCrawler.link.linkanalysis.SALSA;
import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.vsm.VSMElement;


public class OnlineLearning {

	private FrontierTargetRepositoryBaseline frontier;
	
	private BipartiteGraphManager manager;
	
	private BipartiteGraphRep rep;
	
	private ClassifierBuilder classifierBuilder;
	
	private String method;
	
	private String targetPath;
	
	public OnlineLearning(FrontierTargetRepositoryBaseline frontier, BipartiteGraphManager manager, ClassifierBuilder classifierBuilder, String method, String path){
		this.frontier = frontier;
		this.manager = manager;
		this.classifierBuilder = classifierBuilder;
		this.method = method;
		this.targetPath = path;
		this.rep = manager.getRepository();
	}
	
	public void execute() throws Exception{
		frontier.commit();
		if(method.equals("SALSA")){
			runSALSA(null,false);
		}
		if(method.equals("SALSA_SEED")){
			runSALSA(loadRelSites(false),false);	
		}
		if(method.equals("SALSA_CLASSIFIER")){
			runSALSA(loadRelSites(false),true);	
		}
		if(method.equals("HITS")){
			runHITS(null);
		}
		if(method.equals("HITS_1ST")){
			runHITS(loadRelSites(false));
		}
		if(method.equals("LINK_CLASSIFIERS")){
			createClassifiers(loadRelSites(false),true);
		}
		if(method.equals("FORWARD_CLASSIFIER_BINARY")){
			forwardClassifier(loadRelSites(true),true,0);
		}
		if(method.equals("FORWARD_CLASSIFIER_LEVELS")){
			forwardClassifier(loadRelSites(true),true,3);
		}

		frontier.commit();		
	}
	
	private HashSet<String> loadRelSites(boolean isDir) throws IOException{
		HashSet<String> relSites = new HashSet<String>();
		if(isDir){
			File[] dirs = new File(targetPath).listFiles();
			System.out.println(">>REL SITESs");
			for (int i = 0; i < dirs.length; i++) {
				File[] files = dirs[i].listFiles();
				for (int j = 0; j < files.length; j++) {
					String url = URLDecoder.decode(files[j].getName());
					if(!relSites.contains(url)){
						relSites.add(url);
						System.out.println(">>" + url);
					}
				}
			}
		}else{
			File file = new File(targetPath + File.separator + "entry_points");
			BufferedReader input = new BufferedReader(new FileReader(file));
			for (String line = input.readLine(); line != null; line = input.readLine()) {
				if(line.startsWith("------")){
					String host = line.replace("-", "");
					String url = "http://" + host + "/";
					if(!relSites.contains(url)){
						relSites.add(url);
						System.out.println(">>" + url);
					}
				}
			}
		}
		return relSites;
	}
	
	public void runSALSA(HashSet<String> relSites, boolean useClassifier) throws Exception{
		SALSA salsa = new SALSA(rep);
		if(relSites != null){
			HashMap<String,VSMElement> probs = new HashMap<String, VSMElement>();
			if(useClassifier){
				probs = createClassifiers(relSites,false);
			}else{
				Iterator<String> iter = relSites.iterator();
				while(iter.hasNext()){
					String site = iter.next();
					System.out.println(">>>>>>>>" + site);
					String id = rep.getID(site);
					if(id == null){
						continue;
					}
					probs.put(id + "_auth", new VSMElement(id,1));
					String[] backlinks = rep.getBacklinks(id);
					for (int i = 0; i < backlinks.length; i++) {
						VSMElement elem = probs.get(id + "_hub");
						if(elem == null){
							elem = new VSMElement(id, 0);
							probs.put(id + "_hub", elem);
						}
						elem.setWeight(elem.getWeight()+1);
					}
				}
			}
			normalize(probs);
			salsa.setNodeRelevance(probs);
		}
		salsa.execute();
		VSMElement[] hubRelevance = salsa.getHubValues();
		double rel = 199;
		System.out.println(">>>>>>>FRONTIER UPDATE...");
		LinkRelevance lr = new LinkRelevance(new URL(hubRelevance[0].getWord()), rel);
		frontier.update(lr);
		for (int i = 1; i < hubRelevance.length; i++) {
			if(i % (hubRelevance.length/99) == 0 ){
				rel--;
			}
			if(hubRelevance[i].getWord() != null){
//				double weight = (hubRelevance[i].getWeight()/hubRelevance[0].getWeight())*100 + 100;
				lr = new LinkRelevance(new URL(hubRelevance[i].getWord()), rel);
//				if(i < 50){
//					System.out.println("###" + lr.getURL().toString() + "=" + lr.getRelevance());	
//				}
				frontier.update(lr);
			}
		}
		VSMElement[] authRelevance = salsa.getAuthValues();
		rel = 299;
		lr = new LinkRelevance(new URL(authRelevance[0].getWord()), rel);
		frontier.update(lr);
		for (int i = 1; i < authRelevance.length; i++) {
			if(i % (authRelevance.length/99) == 0 ){
				rel--;
			}
			if(authRelevance[i].getWord() != null){
//				double weight = (authRelevance[i].getWeight()/authRelevance[0].getWeight())*100 + 200;
				lr = new LinkRelevance(new URL(authRelevance[i].getWord()), rel);
//				if(i < 500){
//					System.out.println("###" + i + ":" + lr.getURL().toString() + "=" + lr.getRelevance() + ":" + authRelevance[i].getWeight());					
//				}
				frontier.update(lr);
			}
		}
		salsa = null;
	}

	
	private void normalize(HashMap<String,VSMElement> values){
		//normalize
		double totalAuth = 0;
		double totalHub = 0;
		Iterator<String> iter = values.keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next();
			VSMElement elem = values.get(key);
			if(key.endsWith("_auth")){
				totalAuth = totalAuth + elem.getWeight();				
			}
			if(key.endsWith("_hub")){
				totalHub = totalHub + elem.getWeight();
			}
		}		
		iter = values.keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next();
			VSMElement elem = values.get(key);
			if(key.endsWith("_auth")){
				elem.setWeight(elem.getWeight()/totalAuth);				
			}
			if(key.endsWith("_hub")){
				elem.setWeight(elem.getWeight()/totalHub);
			}
		}		
	}

	
	public void runHITS(HashSet<String> relSites) throws Exception{
		HITS hits = new HITS(rep);
		if(relSites != null){
			hits.firstIteration(relSites);
		}else{
			hits.originalHITS();	
		}
		System.out.println(">>>>>>>FRONTIER UPDATE...");
		VSMElement[] hubRelevance = hits.getHubRelevance();
		double rel = 199;
		LinkRelevance lr = new LinkRelevance(new URL(hubRelevance[0].getWord()), rel);
		frontier.update(lr);
		for (int i = 1; i < hubRelevance.length; i++) {
			if(i % (hubRelevance.length/99) == 0 ){
				rel--;
			}
			if(hubRelevance[i].getWord() != null){
				lr = new LinkRelevance(new URL(hubRelevance[i].getWord()), rel);
				frontier.update(lr);
			}
		}
		VSMElement[] authRelevance = hits.getAuthRelevance();
		rel = 299;
		lr = new LinkRelevance(new URL(authRelevance[0].getWord()), rel);
		frontier.update(lr);
		for (int i = 1; i < authRelevance.length; i++) {
			if(i % (authRelevance.length/99) == 0 ){
				rel--;
			}
			if(authRelevance[i].getWord() != null){
				lr = new LinkRelevance(new URL(authRelevance[i].getWord()), rel);
//				System.out.println(">>>>>AUTH:" + lr.getURL().toString() + "=" + lr.getRelevance());
				frontier.update(lr);
			}
		}
	}

	
	private void forwardClassifier(HashSet<String> relSites, boolean updateFrontier, int levels) throws Exception{
		System.out.println(">>>BUILDING OUTLINK CLASSIFIER...:");
		LinkClassifier outlinkClassifier = classifierBuilder.forwardlinkTraining(relSites,levels, "LinkClassifierImpl");
		if(updateFrontier){
			manager.setOutlinkClassifier(outlinkClassifier);
		}
		LinkNeighborhood[] outLNs = rep.getLNs();
		for (int i = 0; i < outLNs.length; i++) {
			if(outLNs[i] != null){
				LinkRelevance lr = outlinkClassifier.classify(outLNs[i]);
				if(updateFrontier){
					frontier.update(lr);
				}
			}
		}
	}
	
	private HashMap<String,VSMElement> createClassifiers(HashSet<String> relSites, boolean updateFrontier) throws Exception{
		HashMap<String,VSMElement> elems = new HashMap<String,VSMElement>();
		System.out.println(">>>BUILDING OUTLINK CLASSIFIER...:");
		LinkClassifier outlinkClassifier = classifierBuilder.forwardlinkTraining(relSites,0,"LinkClassifierAuthority");
		if(updateFrontier){
			manager.setOutlinkClassifier(outlinkClassifier);
		}
		LinkNeighborhood[] outLNs = rep.getLNs();
		HashSet<String> visitedAuths = frontier.visitedAuths();
		HashSet<String> usedLinks = new HashSet<String>();
//		Vector<VSMElement> temp = new Vector<VSMElement> ();
		for (int i = 0; i < outLNs.length; i++) {
			if(outLNs[i] != null){
				LinkRelevance lr = outlinkClassifier.classify(outLNs[i]);
				if(updateFrontier){
					frontier.update(lr);
					usedLinks.add(lr.getURL().toString());
				}
				String id = rep.getID(outLNs[i].getLink().toString());
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
		System.out.println(">>>BUILDING BACKLINK CLASSIFIER...");
		LinkClassifier backlinkClassifier = classifierBuilder.backlinkTraining(elems);
		if(updateFrontier){
			manager.setBacklinkClassifier(backlinkClassifier);
		}
		LinkNeighborhood[] backLNs = rep.getBacklinkLN();
		for (int i = 0; i < backLNs.length; i++) {
			if(backLNs[i] != null){
				LinkRelevance lr = backlinkClassifier.classify(backLNs[i]);
				if(updateFrontier && lr != null && !usedLinks.contains(lr.getURL().toString())){
					frontier.update(lr);
				}
				String id = rep.getID(backLNs[i].getLink().toString());
				if(id != null && lr != null){
					VSMElement elem = new VSMElement(id, (lr.getRelevance()-100)/100);
					elems.put(id + "_hub",elem);
				}
			}
		}
		return elems;
	}
	
//	public static void main(String[] args) {
//		try {
//			ParameterFile config = new ParameterFile(args[0]);
//			PersistentHashtable url2id = new PersistentHashtable(config.getParam("URL_ID_DIRECTORY"),100000);
//			PersistentHashtable authID = new PersistentHashtable(config.getParam("AUTH_ID_DIRECTORY"),100000);
//			PersistentHashtable authGraph = new PersistentHashtable(config.getParam("AUTH_GRAPH_DIRECTORY"),100000);
//			PersistentHashtable hubID = new PersistentHashtable(config.getParam("HUB_ID_DIRECTORY"),100000);
//			PersistentHashtable hubGraph = new PersistentHashtable(config.getParam("HUB_GRAPH_DIRECTORY"),100000);
//			BipartiteGraphRep rep = new BipartiteGraphRep(authGraph,url2id,authID,hubID,hubGraph);
//			PersistentHashtable persistentHash = new PersistentHashtable(args[5],100000);
//			FrontierTargetRepositoryBaseline frontier = new FrontierTargetRepositoryBaseline(persistentHash,10000);
//			StopList stoplist = new StopListArquivo(args[1]);
//			WrapperNeighborhoodLinks wrapper = new WrapperNeighborhoodLinks(stoplist);
//			ClassifierBuilder cb = new ClassifierBuilder(rep,stoplist,wrapper,frontier);
//			BipartiteGraphManager manager = new BipartiteGraphManager(frontier,rep,null,null);
//			OnlineLearning onlineLearning = new OnlineLearning(frontier, manager, cb,"LINK_CLASSIFIERS",args[2]);
//			BufferedReader input1 = new BufferedReader(new FileReader(new File(args[7])));
//			HashSet<String> relSites = new HashSet<String>();
//			for (String line = input1.readLine(); line != null; line = input1.readLine()) {
//				String[] links = line.split(" ");
////				URL url = new URL(links[1]);
//				if(!relSites.contains(links[1])){
//					relSites.add(links[1]);	
//				}
//			}
//			onlineLearning.execute();
////			onlineLearning.runSALSA(relSites);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

}
