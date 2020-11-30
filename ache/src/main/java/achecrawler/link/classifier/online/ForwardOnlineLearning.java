package achecrawler.link.classifier.online;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.link.classifier.LinkClassifier;
import achecrawler.link.classifier.builder.LinkClassifierBuilder;
import achecrawler.link.frontier.FrontierManager;
import achecrawler.target.TargetStorageMonitor;

public class ForwardOnlineLearning extends OnlineLearning {
    
    public enum Type {
        BINARY, LEVELS
    }
    
    private static Logger logger = LoggerFactory.getLogger(ForwardOnlineLearning.class);
    
    private LinkClassifierBuilder classifierBuilder;
    private String dataPath;
    private int levels;

    private FrontierManager frontierManager;

    public ForwardOnlineLearning(int learnLimit, boolean async, FrontierManager frontierManager,
                                 LinkClassifierBuilder classifierBuilder,
                                 Type method, String dataPath) {
        super(learnLimit, async, frontierManager);
        this.frontierManager = frontierManager;
        this.classifierBuilder = classifierBuilder;
        this.levels = (method == Type.BINARY ? 0 : 3);
        this.dataPath = dataPath;
    }

    @Override
    public synchronized void execute() throws Exception {
        logger.info("Building outlink classifier...");
        frontierManager.getFrontier().commit();
        
        logger.info("Reading relevant URLs...");
        Set<String> relevantUrls = TargetStorageMonitor.readRelevantUrls(dataPath);
        logger.info("Read {} relevant URLs.", relevantUrls.size());
        
        logger.info("Training new forward link classifier...");
        LinkClassifier outlinkClassifier = classifierBuilder.forwardlinkTraining(relevantUrls, levels, "LinkClassifierImpl");
        
        logger.info("Updating new forward link classifier...");
        frontierManager.updateOutlinkClassifier(outlinkClassifier);
        
        frontierManager.getFrontier().commit();
    }

}
