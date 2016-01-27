package focusedCrawler.util.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.lang.String;
import java.io.*;

import focusedCrawler.config.TargetStorageConfig;
import focusedCrawler.util.Page;

public class TargetMonitor {
    
    private PrintWriter fCrawledPages;
    private PrintWriter fRelevantPages;
    private PrintWriter fNonRelevantPages;
    private PrintWriter fHarvestInfo;
    
    private List<String> crawledUrls = new ArrayList<String>(); 
    private List<String> relevantUrls = new ArrayList<String>();
    private List<String> nonRelevantUrls = new ArrayList<String>();
    private List<String> harvestRates = new ArrayList<String>();

    private TargetStorageConfig config;

    int totalOnTopicPages = 0;
    private int totalOfPages = 0;
    
    public TargetMonitor(String dataPath, TargetStorageConfig config) {
        
        File file = new File(dataPath+"/data_monitor/");
        if(!file.exists()) file.mkdirs();
        
        this.config = config;
        String fileCrawledPages = dataPath + "/data_monitor/crawledpages.csv";
        String fileRelevantPages = dataPath + "/data_monitor/relevantpages.csv";
        String fileHarvestInfo = dataPath + "/data_monitor/harvestinfo.csv";
        String fileNonRelevantPages = dataPath + "/data_monitor/nonrelevantpages.csv";
        
        try {
            fCrawledPages = new PrintWriter(fileCrawledPages, "UTF-8");
            fRelevantPages = new PrintWriter(fileRelevantPages, "UTF-8");
            fHarvestInfo = new PrintWriter(fileHarvestInfo, "UTF-8");
            fNonRelevantPages = new PrintWriter(fileNonRelevantPages, "UTF-8");
        } catch (Exception e) {
            throw new IllegalStateException("Problem while opening files to export target metrics", e);
        }
    }
    
    public void countPage(Page page, boolean isRelevant, double prob) {
        
        totalOfPages++;
        
        crawledUrls.add(page.getIdentifier() + "\t" +
                        String.valueOf(System.currentTimeMillis() / 1000L));
        
        harvestRates.add(Integer.toString(totalOnTopicPages) + "\t" + 
                         String.valueOf(totalOfPages) + "\t" +
                         String.valueOf(System.currentTimeMillis() / 1000L));
        
        if(isRelevant) {
            totalOnTopicPages++;
            relevantUrls.add(page.getIdentifier() + "\t" + String.valueOf(System.currentTimeMillis() / 1000L));
        } else {
            nonRelevantUrls.add(page.getIdentifier() + "\t" + String.valueOf(prob) + "\t" + String.valueOf(System.currentTimeMillis() / 1000L));
        }
        
        if (config.isRefreshSync()){
          if(totalOnTopicPages % config.getRefreshFreq() == 0) {
               exportHarvestInfo(harvestRates);
               harvestRates.clear();
               exportCrawledPages(crawledUrls);
               crawledUrls.clear();    
               exportRelevantPages(relevantUrls);
               relevantUrls.clear();
               exportNonRelevantPages(nonRelevantUrls);
               nonRelevantUrls.clear();
          }
        } else{
            if(totalOfPages % config.getHarvestInfoRefreshFrequency() == 0) {
                exportHarvestInfo(harvestRates);
                harvestRates.clear();
            }
            if(totalOfPages % config.getCrawledRefreshFrequency() == 0) {
                exportCrawledPages(crawledUrls);
                crawledUrls.clear();    
            }
            if(totalOnTopicPages % config.getRelevantRefreshFrequency() == 0) {
                exportRelevantPages(relevantUrls);
                relevantUrls.clear();

                exportNonRelevantPages(nonRelevantUrls);
                nonRelevantUrls.clear();
            }
        }
        
    }

    private void export(List<String> list, PrintWriter file) {
        for (String item : list) {
            file.println(item);
        }
        file.flush();
    }

    private void exportHarvestInfo(List<String> list) {
        export(list, this.fHarvestInfo);
    }

    private void exportCrawledPages(List<String> list) {
        export(list, fCrawledPages);
    }

    private void exportRelevantPages(List<String> list) {
        export(list, this.fRelevantPages);
    }

    private void exportNonRelevantPages(List<String> list) {
        export(list, this.fNonRelevantPages);
    }

    public int getTotalOfPages() {
        return totalOfPages;
    }

}
