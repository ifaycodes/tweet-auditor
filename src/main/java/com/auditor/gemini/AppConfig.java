package com.auditor.gemini;

import java.io.IOException;
import java.util.List;

import tools.jackson.databind.ObjectMapper;

public class AppConfig {
    private String apiKey;
    private List<String> criteria;
    private String username;

    public AppConfig() {};

    public static AppConfig loadConfig(java.nio.file.Path configPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(configPath.toFile(), AppConfig.class);
    }

    public String getApiKey() {return apiKey;}
    public List<String> getCriteria() {return criteria;}
    public String getUsername() {return username;}
}
