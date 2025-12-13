package com.john.freezeapp.daemon.autoglm.actions;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Parser for actions from model response.
 */
public class ActionParser {
    
    /**
     * Parse action from model response.
     */
    public static JsonObject parseAction(String response) {
        if (response == null) {
            System.err.println("WARNING: Action string is null, creating finish action");
            JsonObject action = new JsonObject();
            action.addProperty("_metadata", "finish");
            action.addProperty("message", "Action string is null");
            return action;
        }
        
        response = response.trim();
        
        if (response.isEmpty() || response.equals("[]")) {
            System.err.println("WARNING: Action string is empty or '[]', creating finish action");
            JsonObject action = new JsonObject();
            action.addProperty("_metadata", "finish");
            action.addProperty("message", "Action string is empty");
            return action;
        }
        
        try {
            // Try to parse as do(action=...) format
            if (response.startsWith("do")) {
                return parseDoAction(response);
            } 
            // Try to parse as finish(message=...) format
            else if (response.startsWith("finish")) {
                return parseFinishAction(response);
            }
            // Try to parse XML format <answer>...</answer>
            else if (response.contains("<answer>")) {
                return parseXmlFormat(response);
            }
            // Try to parse as JSON
            else if (response.startsWith("{") || response.startsWith("[")) {
                try {
                    JsonObject jsonObj = JsonParser.parseString(response).getAsJsonObject();
                    if (!jsonObj.has("_metadata")) {
                        jsonObj.addProperty("_metadata", "do");
                    }
                    return jsonObj;
                } catch (Exception e) {
                    // Not valid JSON, continue to error
                }
            }
            // If none of the above, treat as finish with the response as message
            else {
                System.err.println("Warning: Unknown action format, treating as finish. Action: " + response);
                JsonObject action = new JsonObject();
                action.addProperty("_metadata", "finish");
                action.addProperty("message", response);
                return action;
            }
        } catch (Exception e) {
            // Provide detailed error message with the actual action string
            String errorMsg = "Failed to parse action. Action string: [" + response + "]. Error: " + e.getMessage();
            System.err.println(errorMsg);
            throw new IllegalArgumentException(errorMsg, e);
        }
        
        // Should not reach here, but just in case
        throw new IllegalArgumentException("Failed to parse action: " + response);
    }
    
    /**
     * Parse XML format like <answer>do(action="...")</answer>
     */
    private static JsonObject parseXmlFormat(String response) {
        int answerStart = response.indexOf("<answer>");
        int answerEnd = response.indexOf("</answer>");
        
        if (answerStart != -1 && answerEnd != -1) {
            String answerContent = response.substring(answerStart + "<answer>".length(), answerEnd).trim();
            // Recursively parse the content inside <answer>
            return parseAction(answerContent);
        }
        
        throw new IllegalArgumentException("Invalid XML format: " + response);
    }

    private static JsonObject parseDoAction(String response) {
        JsonObject action = new JsonObject();
        action.addProperty("_metadata", "do");
        
        // Simple parsing - extract key-value pairs
        // This is a simplified parser. For production, consider using a proper parser
        
        // Extract action type (required)
        if (response.contains("action=")) {
            String actionValue = extractQuotedValue(response, "action");
            if (actionValue != null && !actionValue.isEmpty()) {
                action.addProperty("action", actionValue);
            } else {
                // If action is empty, try to infer from context
                System.err.println("Warning: action parameter is empty in: " + response);
            }
        } else {
            // If no action parameter, this might be malformed
            System.err.println("Warning: No action parameter found in: " + response);
        }
        
        if (response.contains("app=")) {
            String appValue = extractQuotedValue(response, "app");
            if (appValue != null) {
                action.addProperty("app", appValue);
            }
        }
        
        if (response.contains("element=")) {
            // Parse array like [x, y]
            String elementStr = extractArrayValue(response, "element");
            if (elementStr != null) {
                try {
                    action.add("element", JsonParser.parseString(elementStr));
                } catch (Exception e) {
                    // Fallback: try to parse manually
                }
            }
        }
        
        if (response.contains("text=")) {
            String textValue = extractQuotedValue(response, "text");
            if (textValue != null) {
                action.addProperty("text", textValue);
            }
        }
        
        if (response.contains("start=")) {
            String startStr = extractArrayValue(response, "start");
            if (startStr != null) {
                try {
                    action.add("start", JsonParser.parseString(startStr));
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        
        if (response.contains("end=")) {
            String endStr = extractArrayValue(response, "end");
            if (endStr != null) {
                try {
                    action.add("end", JsonParser.parseString(endStr));
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        
        if (response.contains("message=")) {
            String messageValue = extractQuotedValue(response, "message");
            if (messageValue != null) {
                action.addProperty("message", messageValue);
            }
        }
        
        return action;
    }

    private static JsonObject parseFinishAction(String response) {
        JsonObject action = new JsonObject();
        action.addProperty("_metadata", "finish");
        
        if (response.contains("message=")) {
            String message = extractQuotedValue(response, "message");
            if (message != null) {
                action.addProperty("message", message);
            }
        }
        
        return action;
    }

    private static String extractQuotedValue(String text, String key) {
        String searchKey = key + "=\"";
        int start = text.indexOf(searchKey);
        if (start == -1) {
            return null;
        }
        start += searchKey.length();
        int end = text.indexOf("\"", start);
        if (end == -1) {
            return null;
        }
        return text.substring(start, end);
    }

    private static String extractArrayValue(String text, String key) {
        String searchKey = key + "=[";
        int start = text.indexOf(searchKey);
        if (start == -1) {
            return null;
        }
        start += searchKey.length() - 1; // Include '['
        int bracketCount = 1;
        int end = start + 1;
        while (end < text.length() && bracketCount > 0) {
            if (text.charAt(end) == '[') {
                bracketCount++;
            } else if (text.charAt(end) == ']') {
                bracketCount--;
            }
            end++;
        }
        if (bracketCount == 0) {
            return text.substring(start, end);
        }
        return null;
    }
}

