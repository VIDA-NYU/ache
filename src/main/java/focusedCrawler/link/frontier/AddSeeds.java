package focusedCrawler.link.frontier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.persistence.PersistentHashtable;

public class AddSeeds {
	
	private static final Logger logger = LoggerFactory.getLogger(AddSeeds.class);

	public static void main(String[] args) {
		try {
            String linkConfigFile = args[0] + "/link_storage/link_storage.cfg";
            String seedFile = args[1];
            String dataPath = args[2];
            
			focusedCrawler.util.ParameterFile config = new focusedCrawler.util.ParameterFile(linkConfigFile);
			
			String dir = dataPath + "/" + config.getParam("LINK_DIRECTORY");
			
			PersistentHashtable urls = new PersistentHashtable(dir,1000);
			Frontier frontier = new Frontier(urls, new BaselineLinkSelector(urls));
			int count = 0;
            /*
			if(args.length > 1){
				BufferedReader input = new BufferedReader(new FileReader(args[1]));
				for (String line1 = input.readLine(); line1 != null; line1 = input.readLine()) {
					LinkRelevance linkRel = new LinkRelevance(new URL(line1), 299);
					frontier.insert(linkRel);
					count++;
				}
			}else{
				String[] seeds = config.getParam("SEEDS"," ");
				for (int i = 0; i < seeds.length; i++) {
					urls.put(seeds[i], "299");
//					LinkRelevance linkRel = new LinkRelevance(new URL(seeds[i]), 299);
//					boolean added = frontier.insert(linkRel);
//					System.out.println(added);
					count++;
				}
			}*/
            //String[] seeds = seedConfig.getParam("SEEDS"," ");
            String[] seeds = focusedCrawler.util.ParameterFile.getSeeds(seedFile);
            for (int i = 0; i < seeds.length; i++) {
              if(urls.get(seeds[i]) == null) {
                String relevance = String.valueOf(LinkRelevance.DEFAULT_RELEVANCE);
                urls.put(seeds[i], relevance);
                count++;
              }
            }
			logger.info("Number of seeds added:" + count);
			frontier.close();
			
		} catch (Exception e) {
			logger.error("Problem while adding seeds. ", e);
		}
	}
	
}
