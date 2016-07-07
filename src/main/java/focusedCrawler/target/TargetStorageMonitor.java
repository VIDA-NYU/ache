package focusedCrawler.target;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import focusedCrawler.target.model.Page;

public class TargetStorageMonitor {
    
    private PrintWriter fCrawledPages;
    private PrintWriter fRelevantPages;
    private PrintWriter fNonRelevantPages;
    private PrintWriter fHarvestInfo;
    
    int totalOnTopicPages = 0;
    private int totalOfPages = 0;
    
    public TargetStorageMonitor(String dataPath) {
        
        File file = new File(dataPath+"/data_monitor/");
        if(!file.exists()) {
            file.mkdirs();
        }
        
        String fileCrawledPages = dataPath + "/data_monitor/crawledpages.csv";
        String fileRelevantPages = dataPath + "/data_monitor/relevantpages.csv";
        String fileHarvestInfo = dataPath + "/data_monitor/harvestinfo.csv";
        String fileNonRelevantPages = dataPath + "/data_monitor/nonrelevantpages.csv";
        
        try {
            fCrawledPages = createBufferedWriter(fileCrawledPages);
            fRelevantPages = createBufferedWriter(fileRelevantPages);
            fHarvestInfo = createBufferedWriter(fileHarvestInfo);
            fNonRelevantPages = createBufferedWriter(fileNonRelevantPages);
        } catch (Exception e) {
            throw new IllegalStateException("Problem while opening files to export target metrics", e);
        }
    }

    private PrintWriter createBufferedWriter(String file) throws FileNotFoundException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        boolean autoFlush = true;
        return new PrintWriter(bos, autoFlush);
    }
    
    public void countPage(Page page, boolean isRelevant, double prob) {
        long currentTime = System.currentTimeMillis();
        totalOfPages++;
        fCrawledPages.printf("%s\t%d\n", page.getIdentifier(), (currentTime));
        fHarvestInfo.printf("%d\t%d\t%d\n", totalOnTopicPages, totalOfPages, (currentTime));
        if(isRelevant) {
            totalOnTopicPages++;
            fRelevantPages.printf("%s\t%.10f\t%d\n", page.getIdentifier(), prob, (currentTime));
        } else {
            fNonRelevantPages.printf("%s\t%.10f\t%d\n", page.getIdentifier(), prob, (currentTime));
        }
    }

    public int getTotalOfPages() {
        return totalOfPages;
    }

}
