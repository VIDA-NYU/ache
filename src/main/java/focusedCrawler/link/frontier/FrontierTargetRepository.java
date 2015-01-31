package focusedCrawler.link.frontier;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.cache.CacheException;
import focusedCrawler.util.persistence.PersistentHashtable;

public class FrontierTargetRepository extends FrontierTargetRepositoryBaseline{

	public FrontierTargetRepository(PersistentHashtable urlRelevance,
			HashMap<String,Integer> scope) {
		super(urlRelevance, scope);

	}

	public FrontierTargetRepository(PersistentHashtable urlRelevance, int pagesPerSite) {
		super(urlRelevance,pagesPerSite);
	}

	public LinkRelevance[] select(int numberOfLinks) throws	FrontierPersistentException {
//		HashMap<Integer, Integer> queue = new HashMap<Integer, Integer>();
		LinkRelevance[] result = null;
		int[] classLimits = new int[]{10000,20000,30000};
		int[] countTopClass = new int[classLimits.length];
		int[] classCount = new int[classLimits.length];
		try {
			Iterator keys = urlRelevance.getKeys();
			Vector<LinkRelevance> tempList = new Vector<LinkRelevance>();
			int count = 0;
			for (int i = 0; count < numberOfLinks && keys.hasNext(); i++) {
				String key = ((String)keys.next()).toString();
				String url = URLDecoder.decode(key);
//				System.out.println(url);
				if (url != null){
//					System.out.println("$$$"+(String)urlRelevance.get(url));
					Integer relevInt = new Integer((String)urlRelevance.get(url));
					if(relevInt != null){
						int relev = relevInt.intValue();
						if(relev > 0){
							int index = relev/100;
							if(classCount[index] < classLimits[index]){
//								Integer numOccur = ((Integer)queue.get(relevInt));
//								int numOccurInt = 0;
//								if(numOccur != null){
//									numOccurInt++;
//								}else{
//									numOccurInt = 1;
//								}
//								queue.put(relevInt,new Integer(numOccurInt));
								boolean insert = false;
								if(index == 2){//top class
									if(relev >= 280 && countTopClass[2] < 15000){
										insert = true;
										countTopClass[2]++;
									}
									if(relev >= 250 && relev < 280 && countTopClass[1] < 10000){
										insert = true;
										countTopClass[1]++;
									}
									if(relev > 200 && relev < 250 && countTopClass[0] < 5000){
										insert = true;
										countTopClass[0]++;
									}
									if(insert){
										LinkRelevance linkRel = new LinkRelevance(new URL(url),relev);
										tempList.add(linkRel);
										count++;
										classCount[index]++;
									}
								}else{
									LinkRelevance linkRel = new LinkRelevance(new URL(url),relev);
									tempList.add(linkRel);
									count++;
									classCount[index]++;
								}
							}
						}
					}
				}
			}
			for (int i = 0; i < classCount.length; i++) {
				System.out.println(">>>>LEVEL:" + i + ":" + classCount[i]);
			}
			result = new LinkRelevance[tempList.size()];
			tempList.toArray(result);
			System.out.println(">> TOTAL LOADED: " + result.length);
//			queue.clear();
		}catch (IOException ex) {
			ex.printStackTrace();
		}catch (CacheException ex) {
			ex.printStackTrace();
		}
		return result;
	}
}
