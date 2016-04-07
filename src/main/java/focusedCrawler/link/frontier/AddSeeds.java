package focusedCrawler.link.frontier;

import java.net.URL;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.config.ConfigService;
import focusedCrawler.util.ParameterFile;

public class AddSeeds {

    private static final Logger logger = LoggerFactory.getLogger(AddSeeds.class);

    
    public static void main(ConfigService config, String seedFile, String dataOutputPath) {
        try {
            String linkDirectory = config.getLinkStorageConfig().getLinkDirectory();
            String dir = Paths.get(dataOutputPath, linkDirectory).toString();
            Frontier frontier = new Frontier(dir, 1000);
            int count = 0;

            logger.info("Adding seeds from file: " + seedFile);

            String[] seeds = ParameterFile.getSeeds(seedFile);
            if (seeds != null && seeds.length > 0) {
                for (String seed : seeds) {
                    LinkRelevance linkRel = new LinkRelevance(new URL(seed), LinkRelevance.DEFAULT_RELEVANCE);
                    Integer exist = frontier.exist(linkRel);
                    if (exist == null || exist == -1) {
                        System.out.println("Adding seed URL: "+seed);
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
