package focusedCrawler.link.frontier.selector;

import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.link.frontier.Frontier;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.persistence.Tuple;

public class MultiLevelLinkSelector implements LinkSelector {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiLevelLinkSelector.class);

	@Override
	public LinkRelevance[] select(Frontier frontier, int numberOfLinks) {
	    
	    PersistentHashtable<LinkRelevance> urlRelevance = frontier.getUrlRelevanceHashtable();

		LinkRelevance[] result = null;
		int[] classLimits = new int[]{10000,20000,30000};
		int[] countTopClass = new int[classLimits.length];
		int[] classCount = new int[classLimits.length];
		try {
            List<Tuple<LinkRelevance>> tuples = urlRelevance.getTable();

			Vector<LinkRelevance> tempList = new Vector<LinkRelevance>();
			int count = 0;
			for (int i = 0; count < numberOfLinks && i < tuples.size(); i++) {
			    
				Tuple<LinkRelevance> tuple = tuples.get(i);
				
                String key = tuple.getKey();
				String url = URLDecoder.decode(key, "UTF-8");

				if (url != null){

					Integer relevInt = (int) tuple.getValue().getRelevance();
					if(relevInt != null){
						int relev = relevInt.intValue();
						if(relev > 0){
							int index = relev/100;
							if(classCount[index] < classLimits[index]){

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
				logger.info("LEVEL:" + i + ":" + classCount[i]);
			}
			
			result = new LinkRelevance[tempList.size()];
			tempList.toArray(result);
			
			logger.info("Links loaded: " + result.length);

		} catch (Exception e) {
            logger.error("Failed to select links from frontier.", e);
        }
		return result;
	}
	
}
