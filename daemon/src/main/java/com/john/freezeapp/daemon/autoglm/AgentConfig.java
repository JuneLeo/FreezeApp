package com.john.freezeapp.daemon.autoglm;

import com.john.freezeapp.daemon.autoglm.config.Config;

/**
 * Configuration for the PhoneAgent.
 */
public class AgentConfig {
    private int maxSteps = 100;
    private String deviceId = null;
    private String lang = "cn";
    private String systemPrompt = null;
    private boolean verbose = true;

    public AgentConfig() {
        this.systemPrompt = Config.getSystemPrompt(this.lang);
    }

    public AgentConfig(int maxSteps, String deviceId, String lang, boolean verbose) {
        this.maxSteps = maxSteps;
        this.deviceId = deviceId;
        this.lang = lang;
        this.verbose = verbose;
        this.systemPrompt = Config.getSystemPrompt(this.lang);
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
        this.systemPrompt = Config.getSystemPrompt(lang);
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}

