package com.john.freezeapp.daemon.autoglm.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.Interceptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Client for interacting with OpenAI-compatible vision-language models.
 *
 * This implementation directly sends JSON requests, similar to the Python version,
 * to support multimodal messages with array content.
 */
public class ModelClient {
    private final ModelConfig config;
    private final OkHttpClient client;
    private final Gson gson;
    private final String baseUrl;
    private final String apiKey;

    public ModelClient(ModelConfig config) {
        this.config = config != null ? config : new ModelConfig();
        this.gson = new Gson();

        // Ensure base URL ends with /v1
        this.baseUrl = this.config.getBaseUrl();

        // Get API key
        this.apiKey = this.config.getApiKey();

        // Create OkHttpClient with timeout and authentication
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS);

        // Add authentication interceptor if API key is provided
        if (this.apiKey != null && !this.apiKey.isEmpty()) {
            final String finalApiKey = this.apiKey;
            clientBuilder.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + finalApiKey);
                    return chain.proceed(requestBuilder.build());
                }
            });
        }

        this.client = clientBuilder.build();
    }

    /**
     * Send a request to the model.
     *
     * This method directly sends JSON requests, similar to the Python version,
     * to support multimodal messages with array content.
     */
    public ModelResponse request(List<JsonObject> messages) throws IOException {
        try {
            // Build request body as JSON, similar to Python version
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", this.config.getModelName());
            requestBody.add("messages", convertMessagesToJsonArray(messages));
            requestBody.addProperty("max_tokens", this.config.getMaxTokens());
            requestBody.addProperty("temperature", this.config.getTemperature());
            requestBody.addProperty("top_p", this.config.getTopP());
            requestBody.addProperty("frequency_penalty", this.config.getFrequencyPenalty());
            requestBody.addProperty("stream", false);

            // Add extra_body parameters if any
            if (!this.config.getExtraBody().isEmpty()) {
                for (Map.Entry<String, Object> entry : this.config.getExtraBody().entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        requestBody.addProperty(entry.getKey(), (String) value);
                    } else if (value instanceof Number) {
                        requestBody.addProperty(entry.getKey(), (Number) value);
                    } else if (value instanceof Boolean) {
                        requestBody.addProperty(entry.getKey(), (Boolean) value);
                    } else {
                        // For complex objects, convert to JSON
                        requestBody.add(entry.getKey(), gson.toJsonTree(value));
                    }
                }
            }

            // Create HTTP request
            String jsonBody = gson.toJson(requestBody);
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    jsonBody
            );

            Request request = new Request.Builder()
                    .url(this.baseUrl + "/chat/completions")
                    .post(body)
                    .build();

            // Execute request
            try (Response response = this.client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error details";
                    String errorMsg = "Model request failed with HTTP error: " + response.code();

                    // Try to parse error message from response
                    try {
                        JsonObject errorJson = JsonParser.parseString(errorBody).getAsJsonObject();
                        if (errorJson.has("error") && errorJson.get("error").isJsonObject()) {
                            JsonObject error = errorJson.getAsJsonObject("error");
                            if (error.has("message")) {
                                errorMsg += " - " + error.get("message").getAsString();
                            }
                        } else if (errorJson.has("errors") && errorJson.get("errors").isJsonObject()) {
                            JsonObject errors = errorJson.getAsJsonObject("errors");
                            if (errors.has("message")) {
                                errorMsg += " - " + errors.get("message").getAsString();
                            }
                        }
                    } catch (Exception e) {
                        // If parsing fails, use raw error body
                        if (errorBody.length() > 0 && errorBody.length() < 500) {
                            errorMsg += " - " + errorBody;
                        }
                    }

                    // Provide helpful error messages for common issues
                    if (response.code() == 401) {
                        errorMsg += "\nAuthentication failed. Please check:";
                        errorMsg += "\n1. API key is correct and not expired";
                        errorMsg += "\n2. For ModelScope: Ensure token starts with 'ms-'";
                        errorMsg += "\n3. For ModelScope: Token should be from https://modelscope.cn";
                        errorMsg += "\n4. Base URL: " + this.baseUrl;
                    } else if (response.code() == 404) {
                        errorMsg += "\nEndpoint not found. Please check:";
                        errorMsg += "\n1. Base URL: " + this.baseUrl;
                        errorMsg += "\n2. Model name: " + this.config.getModelName();
                    } else if (response.code() == 400) {
                        errorMsg += "\nBad request. Please check:";
                        errorMsg += "\n1. Request parameters are valid";
                        errorMsg += "\n2. Model name is correct";
                        if (errorBody.contains("context length")) {
                            errorMsg += "\n3. Context length exceeded. Please reduce the length of input messages.";
                        }
                    }

                    throw new IOException(errorMsg);
                }

                // Parse response
                String responseBody = response.body() != null ? response.body().string() : "{}";
                JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();

                if (!responseJson.has("choices") || !responseJson.get("choices").isJsonArray()) {
                    throw new IOException("Invalid response format: no choices");
                }

                JsonArray choices = responseJson.getAsJsonArray("choices");
                if (choices.size() == 0) {
                    throw new IOException("Invalid response format: empty choices");
                }

                JsonObject choice = choices.get(0).getAsJsonObject();
                if (!choice.has("message") || !choice.get("message").isJsonObject()) {
                    throw new IOException("Invalid response format: no message");
                }

                JsonObject message = choice.getAsJsonObject("message");
                if (!message.has("content") || message.get("content").isJsonNull()) {
                    throw new IOException("Invalid response format: message content is null");
                }

                String rawContent = message.get("content").getAsString();

                // Parse thinking and action from response
                String[] parsed = parseResponse(rawContent);

                // If action is empty, try to use the full content as action
                if (parsed[1] == null || parsed[1].trim().isEmpty() || parsed[1].trim().equals("[]")) {
                    System.err.println("WARNING: Parsed action is empty, using full content as action");
                    System.err.println("Raw content preview (first 300 chars): " +
                            (rawContent != null && rawContent.length() > 300 ?
                                    rawContent.substring(0, 300) + "..." : rawContent));
                    parsed[1] = rawContent != null ? rawContent : "";
                }

                return new ModelResponse(parsed[0], parsed[1], rawContent);
            }

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            String errorMsg = "Model request failed: " + e.getMessage();
            throw new IOException(errorMsg, e);
        }
    }

    /**
     * Convert list of JsonObject messages to JsonArray.
     */
    private JsonArray convertMessagesToJsonArray(List<JsonObject> messages) {
        JsonArray array = new JsonArray();
        for (JsonObject msg : messages) {
            array.add(msg);
        }
        return array;
    }

    /**
     * Parse the model response into thinking and action parts.
     *
     * Parsing rules (matching Python version):
     * 1. If content contains 'finish(message=', everything before is thinking,
     *    everything from 'finish(message=' onwards is action.
     * 2. If rule 1 doesn't apply but content contains 'do(action=',
     *    everything before is thinking, everything from 'do(action=' onwards is action.
     * 3. Fallback: If content contains '<answer>', use legacy parsing with XML tags.
     * 4. Otherwise, return empty thinking and full content as action.
     */
    private String[] parseResponse(String content) {
        if (content == null || content.isEmpty()) {
            return new String[]{"", ""};
        }

        // Rule 1: Check for finish(message=
        int finishIndex = content.indexOf("finish(message=");
        if (finishIndex >= 0) {
            String thinking = content.substring(0, finishIndex).trim();
            String action = content.substring(finishIndex);
            return new String[]{thinking, action};
        }

        // Rule 2: Check for do(action=
        int doIndex = content.indexOf("do(action=");
        if (doIndex >= 0) {
            String thinking = content.substring(0, doIndex).trim();
            String action = content.substring(doIndex);
            return new String[]{thinking, action};
        }

        // Rule 3: Fallback to legacy XML tag parsing
        int answerStart = content.indexOf("<answer>");
        if (answerStart >= 0) {
            String thinking = content.substring(0, answerStart)
                    .replace("<think>", "")
                    .replace("</think>", "").trim();
            int answerEnd = content.indexOf("</answer>", answerStart);
            if (answerEnd >= 0) {
                String action = content.substring(answerStart + "<answer>".length(), answerEnd).trim();
                return new String[]{thinking, action};
            } else {
                // No closing tag, take everything after <answer>
                String action = content.substring(answerStart + "<answer>".length()).trim();
                return new String[]{thinking, action};
            }
        }

        // Rule 4: No markers found, return content as action
        return new String[]{"", content};
    }
}
