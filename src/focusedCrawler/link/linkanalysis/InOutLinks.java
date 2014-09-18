package focusedCrawler.link.linkanalysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
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
import focusedCrawler.util.vsm.VSMElement;
import focusedCrawler.util.vsm.VSMElementComparator;

public class InOutLinks {

	private BipartiteGraphRep graphRep;
	
	public InOutLinks(BipartiteGraphRep graphRep){
		this.graphRep = graphRep;
	}
	
	public void execute(HashSet<String> relSites) throws Exception{
		HashMap<String,VSMElement> hubCounts = new HashMap<String,VSMElement>();
		HashMap<String,VSMElement> authCounts = new HashMap<String,VSMElement>();
		Iterator<String> values = relSites.iterator();
		while(values.hasNext()){
			String site = values.next();
			BackLinkNeighborhood[] backlinks = graphRep.getBacklinks(new URL(site));
			if(backlinks == null){
				continue;
			}
			for (int j = 0; j < backlinks.length; j++) {
				VSMElement count = hubCounts.get(backlinks[j].getLink());
				if(count == null){
					count = new VSMElement(backlinks[j].getLink(), 0);
				}
				count.setWeight(count.getWeight()+1);
				hubCounts.put(backlinks[j].getLink(), count);
				LinkNeighborhood[] outlinks = graphRep.getOutlinks(new URL(backlinks[j].getLink()));
				for (int i = 0; i < outlinks.length; i++) {
					if(outlinks[i] == null){
						continue;
					}
					if(!relSites.contains(outlinks[i].getLink().toString())){
						VSMElement count1 = authCounts.get(outlinks[i].getLink().toString());
						if(count1 == null){
							count1 = new VSMElement(outlinks[i].getLink().toString(), 0);
						}
						count1.setWeight(count1.getWeight()+1);
						authCounts.put(outlinks[i].getLink().toString(), count1);
					}
				}
			}
		}
		System.out.println("------");
		System.out.println("HUB:" + hubCounts.size());
		Vector<VSMElement> finalHub = new Vector<VSMElement>(hubCounts.values());
		Collections.sort(finalHub, new VSMElementComparator());
		for (int i = 0; i < 100 && i < finalHub.size(); i++) {
			VSMElement elem = finalHub.elementAt(i);
			System.out.println(elem.getWord() + ":" + elem.getWeight());
		}
		System.out.println("------");
		System.out.println("AUTH:" + authCounts.size());
		Vector<VSMElement> finalAuth = new Vector<VSMElement>(authCounts.values());
		Collections.sort(finalAuth, new VSMElementComparator());
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
			InOutLinks iolinks = new InOutLinks(rep);
			HashSet<String> relSites = new HashSet<String>();
			BufferedReader input = new BufferedReader(new FileReader(args[1]));
			for (String line = input.readLine(); line != null; line = input.readLine()) {
				relSites.add(line);
			}
			iolinks.execute(relSites);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
	
}
