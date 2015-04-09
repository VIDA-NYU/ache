package focusedCrawler.crawler;

import focusedCrawler.util.ParameterFile;

public class CrawlerManagerConfig {
    
    private final String robotThreadGroup;
    private final int robotQuantity;
    private final long robotManagerRestingTime;
    private final long robotManagerCheckTime;
    private final long robotManagerMaxTime;
    private final long robotManagerRobotErrorSleepTime;
    private final int robotManagerRobotThreadFactor;
    private final long downloaderMaxBlockedThreads;
    
    public CrawlerManagerConfig(String filename) {
        ParameterFile params = new ParameterFile(filename);
        this.robotThreadGroup = params.getParam("ROBOT_THREAD_GROUP");
        this.robotQuantity = params.getParamInt("ROBOT_QUANTITY", 5);
        this.robotManagerRestingTime = params.getParamLong("ROBOT_MANAGER_RESTINGTIME", 10);
        this.robotManagerCheckTime = params.getParamLong("ROBOT_MANAGER_CHECKTIME", 30000);
        this.robotManagerMaxTime = params.getParamLong("ROBOT_MANAGER_MAXTIME", 30000);
        this.robotManagerRobotErrorSleepTime = params.getParamLong("ROBOT_MANAGER_ROBOT_ERROR_SLEEP_TIME", 5000);
        this.robotManagerRobotThreadFactor = params.getParamInt("ROBOT_MANAGER_ROBOT_THREAD_FACTOR", 10);
        this.downloaderMaxBlockedThreads = params.getParamLong("DOWNLOADER_MAX_BLOCKED_THREADS", 20000000);
    }
    
    public String getRobotThreadGroup() {
        return robotThreadGroup;
    }
    
    public int getRobotQuantity() {
        return robotQuantity;
    }

    public long getRobotManagerRestingTime() {
        return robotManagerRestingTime;
    }

    public long getRobotManagerSleepCheckTime() {
        return robotManagerCheckTime;
    }
    
    public long getRobotManagerMaxTime() {
        return robotManagerMaxTime;
    }
    
    public long getRobotManagerRobotErrorTime() {
        return robotManagerRobotErrorSleepTime;
    }
    
    public int getRobotManagerRobotThreadFactor() {
        return robotManagerRobotThreadFactor;
    }
    
    public long getDownloaderMaxBlockedThreads() {
        return downloaderMaxBlockedThreads;
    }
    
}