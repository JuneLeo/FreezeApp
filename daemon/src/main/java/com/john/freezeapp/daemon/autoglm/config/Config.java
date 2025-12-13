package com.john.freezeapp.daemon.autoglm.config;

/**
 * Configuration module for Phone Agent.
 */
public class Config {
    
    /**
     * Get system prompt by language.
     */
    public static String getSystemPrompt(String lang) {
        return Prompts.getSystemPrompt(lang);
    }

    /**
     * Get messages by language.
     */
    public static Messages getMessages(String lang) {
        if ("en".equals(lang)) {
            return Messages.EN;
        }
        return Messages.ZH;
    }
}

