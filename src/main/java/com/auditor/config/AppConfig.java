package com.auditor.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.ObjectMapper;

public class AppConfig {
    private String apiKey;
    private List<String> criteria;
    private String username;

    //empty constructor
    public AppConfig() {};

    //loading elements in config file
    public static AppConfig loadConfig(Path configPath) throws JacksonIOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(configPath.toFile(), AppConfig.class);
    }

    //getters
    public String getApiKey() {return apiKey;}
    public List<String> getCriteria() {return criteria;}
    public String getUsername() {return username;}

    //setters
    public void setApiKey(String apiKey) {this.apiKey = apiKey;}
    public void setCriteria(List<String> criteria) {this.criteria = criteria;}
    public void setUsername(String username) {this.username = username;}
}
