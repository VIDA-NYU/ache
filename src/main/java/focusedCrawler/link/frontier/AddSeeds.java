package focusedCrawler.link.frontier;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.persistence.PersistentHashtable;

public class AddSeeds {

    private static final Logger logger = LoggerFactory.getLogger(AddSeeds.class);

    public static void main(String[] args) {
        try {
            String linkConfigFile = args[0] + "/link_storage/link_storage.cfg";
            String seedFile = args[1];
            String dataPath = args[2];

            ParameterFile config = new ParameterFile(linkConfigFile);
            String dir = dataPath + "/" + config.getParam("LINK_DIRECTORY");

            PersistentHashtable urls = new PersistentHashtable(dir, 1000);
            Frontier frontier = new Frontier(urls, new BaselineLinkSelector(urls));

            int count = 0;

            logger.info("Adding seeds from file: " + seedFile);

            String[] seeds = ParameterFile.getSeeds(seedFile);
            if (seeds != null && seeds.length > 0) {
                for (String seed : seeds) {
                    if (urls.get(seed) == null) {
                        LinkRelevance linkRel = new LinkRelevance(new URL(seed), LinkRelevance.DEFAULT_RELEVANCE);
                        frontier.insert(linkRel);
                        count++;
                    }
                }
            }
            logger.info("Number of seeds added:" + count);
            frontier.close();

        } catch (Exception e) {
            logger.error("Problem while adding seeds. ", e);
        }
    }

}
