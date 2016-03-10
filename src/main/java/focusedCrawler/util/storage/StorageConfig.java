package focusedCrawler.util.storage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import focusedCrawler.util.ParameterFile;

public class StorageConfig {
    
    @JsonProperty("host")
    private String host = "localhost";
    
    @JsonProperty("port")
    private int port = 1987;
    
    @JsonProperty("try_number")
    private int tryNumber = 1;
    
    @JsonProperty("delay_after_exception")
    private long delayAfterException = 5;
    
    @JsonProperty("binder_class_name")
    private String binderClassName = "focusedCrawler.util.storage.socket.StorageBinder";
    
    @JsonProperty("factory_class_name")
    private String factoryClassname = "focusedCrawler.util.storage.socket.StorageRemoteAdapterFactory";
    
    public StorageConfig() {
    }
    
    public StorageConfig(ParameterFile parameters) {
        this.binderClassName = parameters.getParamOrDefault("STORAGE_BINDER_CLASSNAME", "focusedCrawler.util.storage.socket.StorageBinder");
        this.factoryClassname = parameters.getParamOrDefault("STORAGE_FACTORY_CLASSNAME", "focusedCrawler.util.storage.socket.StorageRemoteAdapterFactory");
        this.port = parameters.getParamIntOrDefault("RMI_STORAGE_SERVER_PORT", 1987);
        this.tryNumber = parameters.getParamIntOrDefault("STORAGE_TRY_NUMBER", 1);
        this.delayAfterException = parameters.getParamLongOrDefault("STORAGE_DELAY_AFTER_EXCEPTION", 5);
    }

    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public int getTryNumber() {
        return tryNumber;
    }
    
    public void setTryNumber(int tryNumber) {
        this.tryNumber = tryNumber;
    }
    
    public long getDelayAfterException() {
        return delayAfterException;
    }
    
    public void setDelayAfterException(long delayAfterException) {
        this.delayAfterException = delayAfterException;
    }
    
    public String getBinderClassName() {
        return binderClassName;
    }
    
    public void setBinderClassName(String binderClassName) {
        this.binderClassName = binderClassName;
    }
    
    public String getFactoryClassname() {
        return factoryClassname;
    }
    
    public void setFactoryClassname(String factoryClassname) {
        this.factoryClassname = factoryClassname;
    }
    
    public static StorageConfig create(JsonNode config, String prefix) {
        StorageConfig serverConfig = new StorageConfig();
        if(config.get(prefix + "host") != null)
            serverConfig.setHost(config.get(prefix + "host").asText());
        if(config.get(prefix + "port") != null)
            serverConfig.setPort(config.get(prefix + "port").asInt());
        if(config.get(prefix + "try_number") != null)
            serverConfig.setTryNumber(config.get(prefix + "try_number").asInt());
        if(config.get(prefix + "delay_after_exception") != null)
            serverConfig.setDelayAfterException(config.get(prefix + "delay_after_exception").asInt());
        if(config.get(prefix + "binder_class_name") != null)
            serverConfig.setBinderClassName(config.get(prefix + "binder_class_name").asText());
        if(config.get(prefix + "factory_class_name") != null)
            serverConfig.setFactoryClassname(config.get(prefix + "factory_class_name").asText());
        return serverConfig;
    }
    
}
