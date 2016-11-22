package focusedCrawler.distributed;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastService {
    
    private static Logger logger = LoggerFactory.getLogger(HazelcastService.class);
    
    public static class HazelcastClusterConfig {
        
        @JsonProperty("cluster.name")
        private String clusterName = "ache";
        
        @JsonProperty("cluster.discovery.multicast.enabled")
        private boolean multicastEnabled = true;
        
        @JsonProperty("cluster.discovery.multicast.timeout-seconds")
        private int multicastTimeoutSeconds = 3;
        
        @JsonProperty("cluster.discovery.tcp.enabled")
        private boolean tcpIpEnabled = false;
        
        @JsonProperty("cluster.discovery.tcp.members")
        private List<String> tpcIpMembers = Arrays.asList("127.0.0.1");
        
        @JsonProperty("cluster.discovery.tcp.timeout-seconds")
        private int tcpIpTimeoutSeconds = 3;
        
        public HazelcastClusterConfig() {
        }
        
        public HazelcastClusterConfig(JsonNode config, ObjectMapper objectMapper) throws IOException {
            objectMapper.readerForUpdating(this).readValue(config);
        }
        
    }

    private HazelcastInstance hz;
    private HazelcastClusterConfig clusterConfig;

    public HazelcastService(HazelcastClusterConfig clusterConfig) {
        this.clusterConfig = clusterConfig;
    }

    public HazelcastInstance getInstance() {
        if (hz == null) {
            logger.info("Starting Hazelcast cluster service...");
            Config hzConfig = new Config();
            
            hzConfig.getGroupConfig().setName(clusterConfig.clusterName);
            hzConfig.setProperty("hazelcast.shutdownhook.enabled", "false");
            MulticastConfig multicastConfig = hzConfig.getNetworkConfig().getJoin().getMulticastConfig();
            multicastConfig.setEnabled(clusterConfig.multicastEnabled);
            multicastConfig.setMulticastTimeoutSeconds(clusterConfig.multicastTimeoutSeconds);

            TcpIpConfig tcpIpConfig = hzConfig.getNetworkConfig().getJoin().getTcpIpConfig();
            tcpIpConfig.setEnabled(clusterConfig.tcpIpEnabled);
            tcpIpConfig.setConnectionTimeoutSeconds(clusterConfig.tcpIpTimeoutSeconds);
            for(String member : clusterConfig.tpcIpMembers) {
                tcpIpConfig.addMember(member);
            }

            hz = Hazelcast.newHazelcastInstance(hzConfig);
            
            logger.info("Hazecalst service started.");
        }
        return hz;
    }

    public synchronized void stop() {
        if(hz != null) {
            hz.shutdown();
            hz = null;
            logger.info("Hazecalst service stopped.");
        }
    }

}
