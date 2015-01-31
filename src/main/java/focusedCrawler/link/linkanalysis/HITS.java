package focusedCrawler.link.linkanalysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import focusedCrawler.link.BipartiteGraphRep;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.parser.BackLinkNeighborhood;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.persistence.Tuple;
import focusedCrawler.util.vsm.VSMElement;
import focusedCrawler.util.vsm.VSMElementComparator;

/**
 * This class implements the HITS algorithm
 * @author lbarbosa
 *
 */


public class HITS {

	private BipartiteGraphRep graphRep;
	
	private HashMap<String,VSMElement> authValues;
	
	private HashMap<String,VSMElement> hubValues;
	
	private double maxAuth = 0;
	
	private double maxHub = 0;
	
	private int iterations = 1;
	
	private VSMElement[] hubRelevance;
	
	private VSMElement[] authRelevance;

	
	public HITS(){
		this.authValues = new HashMap<String, VSMElement>();
		this.hubValues = new HashMap<String, VSMElement>();
	}

	public HITS(BipartiteGraphRep graphRep){
		this.graphRep = graphRep;
		this.authValues = new HashMap<String, VSMElement>();
		this.hubValues = new HashMap<String, VSMElement>();
	}


	public VSMElement[] getHubRelevance(){
		return hubRelevance;
	}
	
	public VSMElement[] getAuthRelevance(){
		return authRelevance;
	}

	public void originalHITS() throws Exception{
		Tuple[] authTuples = graphRep.getAuthGraph();
		Tuple[] hubTuples = graphRep.getHubGraph();
//		Tuple[] authTuples = new Tuple[7];
//		Tuple t1 = new Tuple("D", "A###");
//		Tuple t2 = new Tuple("E", "A###");
//		Tuple t3 = new Tuple("F", "A###B###");
//		Tuple t4 = new Tuple("G", "B###");
//		Tuple t5 = new Tuple("H", "B###C###");
//		Tuple t6 = new Tuple("I", "B###C###");		
//		Tuple t7 = new Tuple("J", "C###");
//		authTuples[0] = t1;
//		authTuples[1] = t2;
//		authTuples[2] = t3;
//		authTuples[3] = t4;
//		authTuples[4] = t5;
//		authTuples[5] = t6;
//		authTuples[6] = t7;
//		Tuple[] hubTuples = new Tuple[3];
//		t1 = new Tuple("A", "D###E###F###");
//		t2 = new Tuple("B", "F###G###H###I###");
//		t3 = new Tuple("C", "H###I###J###");
//		hubTuples[0] = t1;
//		hubTuples[1] = t2;
//		hubTuples[2] = t3;
		inicialization(authTuples,hubTuples);
//		normalization();
		for (int l = 0; l < iterations; l++) {
			updateHub(hubTuples);
			maxNormalizationHub();
			updateAuth(authTuples);
			maxNormalizationAuth();
//			normalization();
			maxAuth = 0;
			maxHub = 0;
//			System.out.println(authValues.toString());
//			System.out.println(hubValues.toString());
			print();
		}
		setValues();
	}

	private void inicialization(Tuple[] authTuples, Tuple[] hubTuples){
		for (int i = 0; i < authTuples.length; i++) {
			authValues.put(authTuples[i].getKey(), new VSMElement(authTuples[i].getKey(), 1));
		}
		for (int i = 0; i < hubTuples.length; i++) {
			hubValues.put(hubTuples[i].getKey(), new VSMElement(hubTuples[i].getKey(), 1));
		}
	}
	
	private void print() throws IOException{
		Vector<VSMElement> topAuths = new Vector<VSMElement>(authValues.values());
		Collections.sort(topAuths,new VSMElementComparator());
		System.out.println("-----TOP AUTHS-----");
		for (int i = 0; i < topAuths.size() && i < 50; i++) {
			String url = graphRep.getAuthURL(topAuths.elementAt(i).getWord());
			System.out.println(i + ":" + url + "=" + topAuths.elementAt(i).getWeight());
		}

		Vector<VSMElement> topHubs = new Vector<VSMElement>(hubValues.values());
		Collections.sort(topHubs,new VSMElementComparator());
		System.out.println("-----TOP HUBS-----");
		for (int i = 0; i < topHubs.size() && i < 50; i++) {
			String url = graphRep.getHubURL(topHubs.elementAt(i).getWord()).toString();
			System.out.println(i + ":" + URLDecoder.decode(url) + "=" + topHubs.elementAt(i).getWeight());
		}
	}
	
	private void setValues() throws IOException{
		Vector<VSMElement> topAuths = new Vector<VSMElement>(authValues.values());
		Collections.sort(topAuths,new VSMElementComparator());
		
		authRelevance = new VSMElement[topAuths.size()];
		for (int i = 0; i < topAuths.size(); i++) {
			String url = graphRep.getAuthURL(topAuths.elementAt(i).getWord());
			topAuths.elementAt(i).setWord(url);
			authRelevance[i] = topAuths.elementAt(i);
		}
		Vector<VSMElement> topHubs = new Vector<VSMElement>(hubValues.values());
		Collections.sort(topHubs,new VSMElementComparator());
		hubRelevance = new VSMElement[topHubs.size()];
		for (int i = 0; i < topHubs.size(); i++) {
			String url = graphRep.getHubURL(topHubs.elementAt(i).getWord()).toString();
			topHubs.elementAt(i).setWord(url);
			hubRelevance[i] = topHubs.elementAt(i);
		}
	}

	
	private void updateAuth(Tuple[] authTuples){
		for (int i = 0; i < authTuples.length; i++) {
			String key = authTuples[i].getKey();
			String[] backlinks = parseRecord(authTuples[i].getValue());
			double totalAuth = 0;
			for (int j = 0; j < backlinks.length; j++) {
				VSMElement value = hubValues.get(backlinks[j]);
				if(value != null){
					totalAuth = totalAuth + value.getWeight();	
				}
			}
			if(totalAuth > maxAuth){
				maxAuth = totalAuth;
			}
			authValues.put(key, new VSMElement(key,totalAuth));
		}
		
	}
	
	private void updateHub(Tuple[] hubTuples){
		for (int i = 0; i < hubTuples.length; i++) {
			String key = hubTuples[i].getKey();
			String[] outlinks = parseRecord(hubTuples[i].getValue());
			double totalHub = 0;
			for (int j = 0; j < outlinks.length; j++) {
				VSMElement value = authValues.get(outlinks[j]);
				if(value != null){
					totalHub = totalHub + value.getWeight();	
				}
			}
			if(totalHub > maxHub){
				maxHub = totalHub;
			}
			hubValues.put(key, new VSMElement(key,totalHub));
		}
	}
	
	private void maxNormalizationAuth(){
		Iterator<String> authKeys = authValues.keySet().iterator();
		while(authKeys.hasNext()){
			String key = authKeys.next();
			VSMElement value = authValues.get(key);
			authValues.put(key, new VSMElement(key,value.getWeight()/maxAuth));
		}
	}

	private void maxNormalizationHub(){
		Iterator<String> hubKeys = hubValues.keySet().iterator();
		while(hubKeys.hasNext()){
			String key = hubKeys.next();
			VSMElement value = hubValues.get(key);
			hubValues.put(key, new VSMElement(key,value.getWeight()/maxHub));
		}
	}
	
	
	private String[] parseRecord(String strLinks){
		if(strLinks != null){
			return strLinks.split("###");
		}else{
			return null;
		}
	}
	
	public void firstIteration(HashSet<String> relSites) throws Exception{
		authValues = new HashMap<String,VSMElement>();
		hubValues = new HashMap<String,VSMElement>();
		Iterator<String> values = relSites.iterator();
		while(values.hasNext()){
			String site = values.next();
			BackLinkNeighborhood[] backlinks = graphRep.getBacklinks(new URL(site));
			if(backlinks == null){
				continue;
			}
			for (int j = 0; j < backlinks.length; j++) {
				VSMElement count = hubValues.get(backlinks[j].getLink());
				if(count == null){
					count = new VSMElement(backlinks[j].getLink(), 0);
				}
				count.setWeight(count.getWeight()+1);
				hubValues.put(backlinks[j].getLink(), count);
				LinkNeighborhood[] outlinks = graphRep.getOutlinks(new URL(backlinks[j].getLink()));
				for (int i = 0; i < outlinks.length; i++) {
					if(outlinks[i] == null){
						continue;
					}
					if(!relSites.contains(outlinks[i].getLink().toString())){
						VSMElement count1 = authValues.get(outlinks[i].getLink().toString());
						if(count1 == null){
							count1 = new VSMElement(outlinks[i].getLink().toString(), 0);
						}
						count1.setWeight(count1.getWeight()+1);
						authValues.put(outlinks[i].getLink().toString(), count1);
					}
				}
			}
		}
		System.out.println("------");
		System.out.println("HUB:" + hubValues.size());
		Vector<VSMElement> finalHub = new Vector<VSMElement>(hubValues.values());
		Collections.sort(finalHub, new VSMElementComparator());
		hubRelevance = new VSMElement[finalHub.size()];
		finalHub.toArray(hubRelevance);
		for (int i = 0; i < 100 && i < finalHub.size(); i++) {
			VSMElement elem = finalHub.elementAt(i);
			System.out.println(elem.getWord() + ":" + elem.getWeight());
		}
		System.out.println("------");
		System.out.println("AUTH:" + authValues.size());
		Vector<VSMElement> finalAuth = new Vector<VSMElement>(authValues.values());
		Collections.sort(finalAuth, new VSMElementComparator());
		authRelevance = new VSMElement[finalAuth.size()];
		finalAuth.toArray(authRelevance);
		for (int i = 0; i < 100 && i < finalAuth.size(); i++) {
			VSMElement elem = finalAuth.elementAt(i);
			System.out.println(elem.getWord() + ":" + elem.getWeight());
		}
		
	}
	
	public static void main(String[] args) {
		ParameterFile config = new ParameterFile(args[0]);
		try {
			PersistentHashtable url2id = new PersistentHashtable(config.getParam("URL_ID_DIRECTORY"),100000);
			PersistentHashtable authID = new PersistentHashtable(config.getParam("AUTH_ID_DIRECTORY"),100000);
			PersistentHashtable authGraph = new PersistentHashtable(config.getParam("AUTH_GRAPH_DIRECTORY"),100000);
			PersistentHashtable hubID = new PersistentHashtable(config.getParam("HUB_ID_DIRECTORY"),100000);
			PersistentHashtable hubGraph = new PersistentHashtable(config.getParam("HUB_GRAPH_DIRECTORY"),100000);
			BipartiteGraphRep rep = new BipartiteGraphRep(authGraph,url2id,authID,hubID,hubGraph);
			HITS hits = new HITS(rep);
			hits.originalHITS();
		}catch(Exception ex){
			ex.printStackTrace();
		}

	}
	
}
