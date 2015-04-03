package focusedCrawler.crawler;

import focusedCrawler.util.ParameterFile;

public class CrawlerManagerConfig {
    
    private final ParameterFile config;
    
    public CrawlerManagerConfig(String filename) {
        this.config = new ParameterFile(filename);
    }
    
    private int configIntValue(String configKey, int defaultValue) {
        int  configValue;
        try {
            configValue = config.getParamInt(configKey);
        } catch (NumberFormatException e) {
            CrawlerManager.logger.warn(String.format("Valid integer value not found for config key %s."
                    + " Using default value: %l", configKey, defaultValue));
            configValue = defaultValue;
        }
        return configValue;
    }
    
    private long configLongValue(String configKey, long defaultValue) {
        long configValue;
        try {
            configValue = config.getParamLong(configKey);
        } catch (NumberFormatException e) {
            CrawlerManager.logger.warn(String.format("Valid long value not found for config key %s."
                    + " Using default value: %l", configKey, defaultValue));
            configValue = defaultValue;
        }
        return configValue;
    }
    
    public String getRobotThreadGroup() {
        return config.getParam("ROBOT_THREAD_GROUP");
    }
    
    public int getRobotQuantity() {
        return configIntValue("ROBOT_QUANTITY", 5);
    }

    public long getRobotManagerRestingTime() {
        return configLongValue("ROBOT_MANAGER_RESTINGTIME", 10);
    }

    public long getRobotManagerSleepCheckTime() {
        return configLongValue("ROBOT_MANAGER_CHECKTIME", 30000);
    }
    
    public long getRobotManagerMaxTime() {
        return  configLongValue("ROBOT_MANAGER_MAXTIME", 30000);
    }
    
    public long getRobotManagerRobotErrorTime() {
        return  configLongValue("ROBOT_MANAGER_ROBOT_ERROR_SLEEP_TIME", 5000);
    }
    
    public int getRobotManagerRobotThreadFactor() {
        return  configIntValue("ROBOT_MANAGER_ROBOT_THREAD_FACTOR", 10);
    }
    
    public long getDownloaderMaxBlockedThreads() {
        return configLongValue("DOWNLOADER_MAX_BLOCKED_THREADS", 20000000);
    }
    
}