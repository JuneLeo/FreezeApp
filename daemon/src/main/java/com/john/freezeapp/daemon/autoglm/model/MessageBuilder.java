package com.john.freezeapp.daemon.autoglm.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Map;

/**
 * Helper class for building conversation messages.
 */
public class MessageBuilder {
    
    /**
     * Create a system message.
     */
    public static JsonObject createSystemMessage(String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", "system");
        message.addProperty("content", content);
        return message;
    }

    /**
     * Create a user message with optional image.
     */
    public static JsonObject createUserMessage(String text, String imageBase64) {
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        
        JsonArray content = new JsonArray();
        
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            JsonObject imageObj = new JsonObject();
            imageObj.addProperty("type", "image_url");
            JsonObject imageUrl = new JsonObject();
            // Use jpeg format if available (smaller), otherwise png
            // The format is determined by the actual image data
            String imageFormat = "data:image/jpeg;base64,";
            imageUrl.addProperty("url", imageFormat + imageBase64);
            imageObj.add("image_url", imageUrl);
            content.add(imageObj);
        }
        
        JsonObject textObj = new JsonObject();
        textObj.addProperty("type", "text");
        textObj.addProperty("text", text);
        content.add(textObj);
        
        message.add("content", content);
        return message;
    }

    /**
     * Create an assistant message.
     */
    public static JsonObject createAssistantMessage(String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", "assistant");
        message.addProperty("content", content);
        return message;
    }

    /**
     * Remove image content from a message to save context space.
     */
    public static JsonObject removeImagesFromMessage(JsonObject message) {
        JsonObject result = new JsonObject();
        
        // Copy role
        if (message.has("role")) {
            result.addProperty("role", message.get("role").getAsString());
        }
        
        // Handle content
        if (message.has("content")) {
            if (message.get("content").isJsonArray()) {
                // Array content - filter out images
                JsonArray content = message.getAsJsonArray("content");
                JsonArray filtered = new JsonArray();
                
                for (int i = 0; i < content.size(); i++) {
                    JsonObject item = content.get(i).getAsJsonObject();
                    if (item.has("type") && "text".equals(item.get("type").getAsString())) {
                        filtered.add(item);
                    }
                    // Skip image_url items
                }
                
                result.add("content", filtered);
            } else if (message.get("content").isJsonPrimitive()) {
                // String content - keep as is
                result.add("content", message.get("content"));
            }
        }
        
        return result;
    }

    /**
     * Build screen info string for the model.
     */
    public static String buildScreenInfo(String currentApp, Map<String, Object> extraInfo) {
        JsonObject info = new JsonObject();
        info.addProperty("current_app", currentApp);
        
        if (extraInfo != null) {
            for (Map.Entry<String, Object> entry : extraInfo.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    info.addProperty(entry.getKey(), (String) value);
                } else if (value instanceof Number) {
                    info.addProperty(entry.getKey(), (Number) value);
                } else if (value instanceof Boolean) {
                    info.addProperty(entry.getKey(), (Boolean) value);
                }
            }
        }
        
        return info.toString();
    }
}

