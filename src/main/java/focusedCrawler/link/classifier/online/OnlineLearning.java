package focusedCrawler.link.classifier.online;

import focusedCrawler.link.LinkStorage.MonitorLock;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.link.frontier.FrontierManager;

public abstract class OnlineLearning implements Runnable{
    
    public static final Logger logger = LoggerFactory.getLogger(OnlineLearning.class);

    private AtomicBoolean onlineLearningIsRunning = new AtomicBoolean(false);
    private static volatile MonitorLock monitorLock;
    private static volatile AtomicBoolean wasSignalled;

    private FrontierManager frontierManager;
    
    public OnlineLearning(FrontierManager frontierManager, MonitorLock monitorLock, AtomicBoolean wasSignalled) {
        this.frontierManager = frontierManager;
        this.monitorLock = monitorLock;
        this.wasSignalled = wasSignalled;
    }

    public void doWait(){
        synchronized(monitorLock){
            while(!wasSignalled.get()){
                try{
                    monitorLock.wait();
                } catch(InterruptedException e){

                }
            }
            //clear signal and continue running.
            wasSignalled.set(false);
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            doWait();
            try {
                if (onlineLearningIsRunning.compareAndSet(false, true)) {
                    // onlineLearningIsRunning is true
                    logger.info("Running Online Learning...");
                    this.execute();
                    frontierManager.forceReload();
                    logger.info("Online Learning finished.");
                    onlineLearningIsRunning.set(false);
                }
            } catch (Exception e) {
                logger.error("LEARNING EXCEPTION - "+e.toString());
            }
        }
    }
    public abstract void execute() throws Exception;
    
}
