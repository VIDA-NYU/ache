package focusedCrawler.link.frontier;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.cache.CacheException;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.vsm.VSMElement;
import focusedCrawler.util.vsm.VSMElementComparator;

public class FrontierTargetTopic extends FrontierTargetRepositoryBaseline{

	public FrontierTargetTopic(PersistentHashtable urlRelevance) {
		super(urlRelevance,50);
	}

//	boolean higher = true;
	int[] classLimits = new int[]{100,100,1500};
	
	public LinkRelevance[] select(int numberOfLinks) throws FrontierPersistentException {
			LinkRelevance[] result = null;
			
			int[] classCount = new int[classLimits.length];
			try {
				Iterator keys = urlRelevance.getKeys();
				Vector<LinkRelevance> tempList = new Vector<LinkRelevance>();
				int count = 0;
//				if(higher){
					for (int i = 0; count < numberOfLinks && keys.hasNext(); i++) {
						String key = ((String)keys.next()).toString();
						String url = URLDecoder.decode(key);
//						System.out.println(url);
						if (url != null){
//							System.out.println("$$$"+(String)urlRelevance.get(url));
							Integer relevInt = new Integer((String)urlRelevance.get(url));
							if(relevInt != null){
								int relev = relevInt.intValue();
//								Integer numOccur = ((Integer)queue.get(relevInt));
								if(relev > 100){
//									if(numOccur == null || (numOccur != null && numOccur.intValue() <= limit)){
										int index = relev/100;
										if(index < 3 && classCount[index] < classLimits[index]){
											LinkRelevance linkRel = new LinkRelevance(new URL(url),relev);
											tempList.add(linkRel);
											count++;
											classCount[index]++;
//											System.out.println("###"+ relev + ":" + classCount[index]);
										}
//									}
//									if(relev > 200){
//										int rest = 100 - (relev % 200);
//										int numOccurInt = 0;
//										if(numOccur != null){
//											numOccurInt = numOccur.intValue();
//											if(numOccur.intValue() <= limit){
//												numOccurInt = numOccur.intValue() + rest;
//											}
//												
//										}else{
//											numOccurInt = rest;
//										}
////										System.out.println(relev+":"+numOccurInt);
//										queue.put(relevInt,new Integer(numOccurInt));
//									}
								}
							}
						}
					}
					if(classCount[2] < classLimits[2] && classLimits[1] < 1000){
						classLimits[1] = classLimits[1]+50;
					}
//					higher = false;
//				}else{
//					HashSet<String> hosts = new HashSet<String>();
//					Vector<VSMElement> elems = new Vector<VSMElement>(middle.values());
//					Collections.sort(elems, new VSMElementComparator());
//					for (int i = 0; i < elems.size() && hosts.size() < 300; i++) {
//						VSMElement elem = elems.elementAt(i);
//						URL url = new URL(elem.getWord());
//						if(!hosts.contains(url.getHost())){
//							LinkRelevance linkRel = new LinkRelevance(url,elem.getWeight());
//							tempList.add(linkRel);
//							middle.remove(elem.getWord());
//							hosts.add(url.getHost());
//							
//						}
//					}
//					higher = true;
//				}
				
				result = new LinkRelevance[tempList.size()];
				tempList.toArray(result);
//				System.out.println(">> FRONTIER: " + queue.toString());
				System.out.println(">> TOTAL LOADED: " + result.length);
			}catch (IOException ex) {
				ex.printStackTrace();
			}catch (CacheException ex) {
				ex.printStackTrace();
			}
			return result;

	 }
	
}
