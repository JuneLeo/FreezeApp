package com.john.freezeapp.daemon.autoglm.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for the AI model.
 */
public class ModelConfig {
    private String baseUrl = "http://localhost:8000/v1";
    private String apiKey = "EMPTY";
    private String modelName = "autoglm-phone-9b";
    private int maxTokens = 3000;
    private double temperature = 0.0;
    private double topP = 0.85;
    private double frequencyPenalty = 0.2;
    private Map<String, Object> extraBody = new HashMap<>();

    public ModelConfig() {
    }

    public ModelConfig(String baseUrl, String modelName) {
        this.baseUrl = baseUrl;
        this.modelName = modelName;
    }

    public ModelConfig(String baseUrl, String modelName, String apiKey) {
        this.baseUrl = baseUrl;
        this.modelName = modelName;
        this.apiKey = apiKey;
    }

    // Getters and Setters
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getTopP() {
        return topP;
    }

    public void setTopP(double topP) {
        this.topP = topP;
    }

    public double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public void setFrequencyPenalty(double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }

    public Map<String, Object> getExtraBody() {
        return extraBody;
    }

    public void setExtraBody(Map<String, Object> extraBody) {
        this.extraBody = extraBody;
    }
}

