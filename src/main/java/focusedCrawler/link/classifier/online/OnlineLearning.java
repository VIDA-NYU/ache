package focusedCrawler.link.classifier.online;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.link.frontier.FrontierManager;
import focusedCrawler.target.model.Page;

public abstract class OnlineLearning {
    
    public static final Logger logger = LoggerFactory.getLogger(OnlineLearning.class);

    private final int learnLimit;
    private AtomicBoolean onlineLearningIsRunning = new AtomicBoolean(false);
    private AtomicInteger numberOfPages = new AtomicInteger(0);

    private FrontierManager frontierManager;
    
    public OnlineLearning(int learnLimit, FrontierManager frontierManager) {
        this.learnLimit = learnLimit;
        this.frontierManager = frontierManager;
    }
    
    public void pushFeedback(Page page) throws Exception {
        
        int numberOfPages = this.numberOfPages.incrementAndGet();
        
        if (numberOfPages % learnLimit == 0) {
            if(onlineLearningIsRunning.compareAndSet(false, true)) {
                // onlineLearningIsRunning is true
                logger.info("Running Online Learning...");
                this.execute();
                frontierManager.clearFrontier();
                logger.info("Online Learning finished.");
                onlineLearningIsRunning.set(false);
            }
        }
    }

    public abstract void execute() throws Exception;
    
}
