package focusedCrawler.link.frontier;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.cache.CacheException;
import focusedCrawler.util.persistence.PersistentHashtable;

public class AddSeeds {

	public static void main(String[] args) {
		try {
            String linkConfigFile = args[0] + "/link_storage/link_storage.cfg";
            String seedFile = args[1];
            String dataPath = args[2];
			focusedCrawler.util.ParameterFile config = new focusedCrawler.util.ParameterFile(linkConfigFile);
			focusedCrawler.util.ParameterFile seedConfig = new focusedCrawler.util.ParameterFile(seedFile);
			String dir = dataPath + "/" + config.getParam("LINK_DIRECTORY");
			PersistentHashtable urls = new PersistentHashtable(dir,1000);
			FrontierTargetRepositoryBaseline frontier = new FrontierTargetRepositoryBaseline(urls,10000);
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
              urls.put(seeds[i], "299");
              count++;
            }
			System.out.println("Number of seeds:" + count);
			frontier.close();
		}
		catch (MalformedURLException ex) {
			ex.printStackTrace();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		catch (FrontierPersistentException ex) {
			ex.printStackTrace();
		} catch (CacheException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
