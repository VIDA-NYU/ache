package achecrawler.link.classifier.online;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.link.frontier.FrontierManager;
import achecrawler.target.model.Page;

public abstract class OnlineLearning implements Runnable {

    public static final Logger logger = LoggerFactory.getLogger(OnlineLearning.class);

    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private volatile MonitorLock monitorLock = new MonitorLock();
    private volatile AtomicBoolean wasSignalled = new AtomicBoolean(false);

    private final boolean async;
    private final int learnLimit;
    private AtomicInteger pageCounter = new AtomicInteger(0);
    private FrontierManager frontierManager;

    public OnlineLearning(int learnLimit, boolean async, FrontierManager frontierManager) {
        this.frontierManager = frontierManager;
        this.learnLimit = learnLimit;
        this.async = async;
        if (async) {
            Thread learner = new Thread(this);
            learner.setDaemon(true);
            learner.setName("Online-Learner");
            learner.start();
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            doWait();
            executeLearning();
        }
        logger.info("Online-Learner thread finalized.");
    }

    private void executeLearning() {
        try {
            if (isRunning.compareAndSet(false, true)) {
                // isRunning was false, but now is true
                logger.info("Executing online learning...");
                this.execute();
                logger.info("Reloading Frontier...");
                frontierManager.forceReload();
            }
        } catch (Exception e) {
            logger.error("Failed to execute OnlineLearning.", e);
        } finally {
            logger.info("Online learning execution finished.");
            isRunning.set(false);
        }
    }

    /**
     * Pauses online learning thread until {@link #doNotify()} is called.
     */
    public void doWait() {
        synchronized (monitorLock) {
            while (!wasSignalled.get()) {
                try {
                    // gives up the hold on the monitorLock and goes to sleep.
                    monitorLock.wait();
                } catch (InterruptedException e) {
                }
            }
            // clear signal and continue running.
            wasSignalled.set(false);
        }
    }

    /*
     * Notifies the monitorLock, which triggers the execution of learning.
     */
    private void doNotify() {
        synchronized (monitorLock) {
            wasSignalled.set(true);
            monitorLock.notify();
        }
    }

    public void notifyPageCrawled(Page page) {
        this.pageCrawledEvent(page);
        int numberOfPages = this.pageCounter.incrementAndGet();
        if (numberOfPages % learnLimit == 0) {
            if (this.async) {
                doNotify(); // triggers execution of online learning in background thread
            } else {
                executeLearning();
            }
        }
        numberOfPages++;
    }

    /**
     * Concrete implementations may decide to override this method to receive crawled pages as soon
     * as they are fetched. They can decide to store a copy locally to train models in batch when
     * {@link #execute()} is called, or they can update models in "real-time" if the learning method
     * supports online-learning.
     */
    public void pageCrawledEvent(Page page) {
    }

    /**
     * Concrete implementations should use this method train new models. This method is called
     * periodically in a independent thread once a fixed number of pages ({@link #learnLimit}) is
     * crawled .
     */
    public void execute() throws Exception {
    }

    private static class MonitorLock {

    }

}
