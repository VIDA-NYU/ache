package focusedCrawler.link.classifier.online;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.link.classifier.LinkClassifier;
import focusedCrawler.link.classifier.builder.LinkClassifierBuilder;
import focusedCrawler.link.frontier.FrontierManager;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.TargetStorageMonitor;
import focusedCrawler.util.parser.LinkNeighborhood;

public class ForwardOnlineLearning extends OnlineLearning {
    
    public enum Type {
        BINARY, LEVELS
    }
    
    private static Logger logger = LoggerFactory.getLogger(ForwardOnlineLearning.class);
    
    private LinkClassifierBuilder classifierBuilder;
    private String dataPath;
    private int levels;

    private FrontierManager frontierManager;

    public ForwardOnlineLearning(int learnLimit, FrontierManager frontierManager,
                                 LinkClassifierBuilder classifierBuilder,
                                 Type method, String dataPath) {
        super(learnLimit, frontierManager);
        this.frontierManager = frontierManager;
        this.classifierBuilder = classifierBuilder;
        this.levels = (method == Type.BINARY ? 0 : 3);
        this.dataPath = dataPath;
    }

    @Override
    public synchronized void execute() throws Exception {
        logger.info("Building outlink classifier...");
        frontierManager.getFrontier().commit();
        HashSet<String> relevantUrls = TargetStorageMonitor.readRelevantUrls(dataPath);
        LinkClassifier outlinkClassifier = classifierBuilder.forwardlinkTraining(relevantUrls, levels, "LinkClassifierImpl");
        frontierManager.setOutlinkClassifier(outlinkClassifier);
        LinkNeighborhood[] outLNs = frontierManager.getGraphRepository().getLNs();
        for (int i = 0; i < outLNs.length; i++) {
            if (outLNs[i] != null) {
                LinkRelevance lr = outlinkClassifier.classify(outLNs[i]);
                frontierManager.getFrontier().update(lr);
            }
        }
        frontierManager.getFrontier().commit();
    }

}
