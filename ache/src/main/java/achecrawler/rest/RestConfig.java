package achecrawler.rest;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestConfig {

    @JsonProperty("http.port")
    private int port = 8080;

    @JsonProperty("http.host")
    private String host = "0.0.0.0";

    @JsonProperty("http.cors.enabled")
    private boolean enableCors = true;

    @JsonProperty("http.auth.basic.user")
    private String basicUser = null;

    @JsonProperty("http.auth.basic.password")
    private String basicPassword = null;

    @JsonProperty("http.base_path")
    private String basePath = null;

    public RestConfig() {
        // required for de-serialization
    }

    public RestConfig(JsonNode config, ObjectMapper objectMapper) throws IOException {
        objectMapper.readerForUpdating(this).readValue(config);
    }

    public RestConfig(String host, int port, boolean enableCors) throws IOException {
        this.host = host;
        this.port = port;
        this.enableCors = enableCors;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public boolean isEnableCors() {
        return enableCors;
    }

    public String getBasicUser() {
        return basicUser;
    }

    public String getBasicPassword() {
        return basicPassword;
    }

    public String getBasePath() {
        return basePath;
    }

}
