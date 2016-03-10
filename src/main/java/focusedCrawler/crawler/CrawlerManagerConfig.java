package focusedCrawler.crawler;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import focusedCrawler.util.ParameterFile;

public class CrawlerManagerConfig {
    
    @JsonProperty("crawler_manager.robot_mananger.thread_group")
    private String robotThreadGroup = "crawler_group";
    @JsonProperty("crawler_manager.robot_mananger.resting_time")
    private long robotManagerRestingTime = 10;
    @JsonProperty("crawler_manager.robot_mananger.quantity")
    private int robotQuantity = 5;
    @JsonProperty("crawler_manager.robot_mananger.check_time")
    private long robotManagerCheckTime = 30000;
    @JsonProperty("crawler_manager.robot_mananger.max_time")
    private long robotManagerMaxTime = 30000;
    @JsonProperty("crawler_manager.robot_mananger.robot_error_sleep_time")
    private long robotManagerRobotErrorSleepTime = 5000;
    @JsonProperty("crawler_manager.robot_mananger.thread_factor")
    private int robotManagerRobotThreadFactor = 10;
    @JsonProperty("crawler_manager.downloader.max_blocked_threads")
    private long downloaderMaxBlockedThreads = 20000000;
    
    public CrawlerManagerConfig(String filename) {
        ParameterFile params = new ParameterFile(filename);
        this.robotThreadGroup = params.getParam("ROBOT_THREAD_GROUP");
        this.robotQuantity = params.getParamIntOrDefault("ROBOT_QUANTITY", 5);
        this.robotManagerRestingTime = params.getParamLongOrDefault("ROBOT_MANAGER_RESTINGTIME", 10);
        this.robotManagerCheckTime = params.getParamLongOrDefault("ROBOT_MANAGER_CHECKTIME", 30000);
        this.robotManagerMaxTime = params.getParamLongOrDefault("ROBOT_MANAGER_MAXTIME", 30000);
        this.robotManagerRobotErrorSleepTime = params.getParamLongOrDefault("ROBOT_MANAGER_ROBOT_ERROR_SLEEP_TIME", 5000);
        this.robotManagerRobotThreadFactor = params.getParamIntOrDefault("ROBOT_MANAGER_ROBOT_THREAD_FACTOR", 10);
        this.downloaderMaxBlockedThreads = params.getParamLongOrDefault("DOWNLOADER_MAX_BLOCKED_THREADS", 20000000);
    }
    
    public CrawlerManagerConfig(JsonNode config, ObjectMapper objectMapper) throws IOException {
        objectMapper.readerForUpdating(this).readValue(config);
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