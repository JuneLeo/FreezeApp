package com.john.freezeapp.daemon.autoglm;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.john.freezeapp.daemon.DaemonLog;
import com.john.freezeapp.daemon.autoglm.actions.ActionHandler;
import com.john.freezeapp.daemon.autoglm.actions.ActionParser;
import com.john.freezeapp.daemon.autoglm.actions.ActionResult;
import com.john.freezeapp.daemon.autoglm.adb.DeviceUtils;
import com.john.freezeapp.daemon.autoglm.adb.Screenshot;
import com.john.freezeapp.daemon.autoglm.adb.ScreenshotUtils;
import com.john.freezeapp.daemon.autoglm.config.Config;
import com.john.freezeapp.daemon.autoglm.config.Messages;
import com.john.freezeapp.daemon.autoglm.model.MessageBuilder;
import com.john.freezeapp.daemon.autoglm.model.ModelClient;
import com.john.freezeapp.daemon.autoglm.model.ModelConfig;
import com.john.freezeapp.daemon.autoglm.model.ModelResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * AI-powered agent for automating Android phone interactions.
 */
public class PhoneAgent {
    private ModelConfig modelConfig;
    private AgentConfig agentConfig;
    private ModelClient modelClient;
    private ActionHandler actionHandler;
    private List<JsonObject> context;
    private int stepCount;

    public PhoneAgent() {
        this(new ModelConfig(), new AgentConfig());
    }

    public PhoneAgent(ModelConfig modelConfig) {
        this(modelConfig, new AgentConfig());
    }

    public PhoneAgent(ModelConfig modelConfig, AgentConfig agentConfig) {
        this(modelConfig, agentConfig, null, null);
    }

    public PhoneAgent(ModelConfig modelConfig, AgentConfig agentConfig,
                      Function<String, Boolean> confirmationCallback,
                      Function<String, Void> takeoverCallback) {
        this.modelConfig = modelConfig != null ? modelConfig : new ModelConfig();
        this.agentConfig = agentConfig != null ? agentConfig : new AgentConfig();
        this.modelClient = new ModelClient(this.modelConfig);
        this.actionHandler = new ActionHandler(
                this.agentConfig.getDeviceId(),
                confirmationCallback,
                takeoverCallback
        );
        this.context = new ArrayList<>();
        this.stepCount = 0;
    }

    /**
     * Run the agent to complete a task.
     */
    public String run(String task, AgentCallback callback) {
        context.clear();
        stepCount = 0;
        if (callback != null) {
            callback.start();
        }
        // First step with user prompt
        StepResult result = executeStep(task, true, callback);

        if (result.isFinished()) {
            if (callback != null) {
                callback.end();
            }
            return result.getMessage() != null ? result.getMessage() : "Task completed";
        }

        // Continue until finished or max steps reached
        while (stepCount < agentConfig.getMaxSteps()) {
            result = executeStep(null, false, callback);

            if (result.isFinished()) {
                if (callback != null) {
                    callback.end();
                }
                return result.getMessage() != null ? result.getMessage() : "Task completed";
            }
        }

        return "Max steps reached";
    }

    /**
     * Reset the agent state for a new task.
     */
    public void reset() {
        context.clear();
        stepCount = 0;
    }

    private StepResult executeStep(String userPrompt, boolean isFirst, AgentCallback callback) {
        stepCount++;

        try {
            // Capture current screen state
            Screenshot screenshot = ScreenshotUtils.getScreenshot(agentConfig.getDeviceId());
            String currentApp = DeviceUtils.getCurrentApp(agentConfig.getDeviceId());

            // Build messages
            if (isFirst) {
                context.add(MessageBuilder.createSystemMessage(agentConfig.getSystemPrompt()));

                String screenInfo = MessageBuilder.buildScreenInfo(currentApp, null);
                String textContent = userPrompt + "\n\n" + screenInfo;

                context.add(MessageBuilder.createUserMessage(
                        textContent, screenshot.getBase64Data()));
            } else {
                String screenInfo = MessageBuilder.buildScreenInfo(currentApp, null);
                String textContent = "** Screen Info **\n\n" + screenInfo;

                context.add(MessageBuilder.createUserMessage(
                        textContent, screenshot.getBase64Data()));
            }

            // Remove images from previous messages to save context space
            // Only keep images in the most recent user message
            // Also limit context history to prevent token overflow
            List<JsonObject> contextToSend = new ArrayList<>();

            // Keep only system message and last 3 messages (very aggressive to save tokens)
            // This ensures we stay well under the token limit
            int maxHistoryMessages = 3;
            int startIndex = Math.max(0, context.size() - maxHistoryMessages - 1);

            // Always keep system message if it exists
            if (context.size() > 0 && "system".equals(context.get(0).get("role").getAsString())) {
                contextToSend.add(context.get(0));
                if (startIndex == 0) {
                    startIndex = 1;
                }
            }

            // Add recent messages, removing images from all except the last one
            for (int i = startIndex; i < context.size(); i++) {
                JsonObject msg = context.get(i);
                // Skip system message if already added
                if (i == 0 && "system".equals(msg.get("role").getAsString()) &&
                        contextToSend.size() > 0 &&
                        "system".equals(contextToSend.get(0).get("role").getAsString())) {
                    continue;
                }

                // Deep copy the message
                JsonObject msgCopy = new JsonObject();
                msgCopy.addProperty("role", msg.get("role").getAsString());

                if (i == context.size() - 1) {
                    // Keep the last message (current screenshot) as is
                    msgCopy.add("content", msg.get("content"));
                } else {
                    // Remove images from all previous messages
                    JsonObject cleanedMsg = MessageBuilder.removeImagesFromMessage(msg);
                    msgCopy.add("content", cleanedMsg.get("content"));
                }
                contextToSend.add(msgCopy);
            }

            if (agentConfig.isVerbose()) {
                DaemonLog.log("DEBUG: Sending " + contextToSend.size() + " messages to model");
                // Debug: check if last message has image
                if (contextToSend.size() > 0) {
                    JsonObject lastMsg = contextToSend.get(contextToSend.size() - 1);
                    if (lastMsg.has("content") && lastMsg.get("content").isJsonArray()) {
                        JsonArray content = lastMsg.get("content").getAsJsonArray();
                        for (int i = 0; i < content.size(); i++) {
                            JsonObject item = content.get(i).getAsJsonObject();
                            if (item.has("type") && "image_url".equals(item.get("type").getAsString())) {
                                String url = item.getAsJsonObject("image_url").get("url").getAsString();
                                int imageSize = url.length();
                                DaemonLog.log("DEBUG: Last message contains image, size: ~" +
                                        (imageSize / 1024) + " KB (base64)");
                            }
                        }
                    }
                }
            }

            // Get model response
            ModelResponse response;
            try {
                response = modelClient.request(contextToSend);
            } catch (IOException e) {
                if (agentConfig.isVerbose()) {
                    e.printStackTrace();
                }
                return new StepResult(false, true, null, "",
                        "Model error: " + e.getMessage());
            }


            // Parse action from response
            JsonObject action;
            try {
                if (agentConfig.isVerbose()) {
                    DaemonLog.log("Raw action string: [" + response.getAction() + "]");
                }

                // If action is empty, log warning and use raw content
                if (response.getAction() == null || response.getAction().trim().isEmpty() ||
                        response.getAction().trim().equals("[]")) {
                    System.err.println("WARNING: Action is empty, checking raw content...");
                    if (response.getRawContent() != null && !response.getRawContent().trim().isEmpty()) {
                        System.err.println("Attempting to parse from raw content...");
                        // Try to parse from raw content directly
                        action = ActionParser.parseAction(response.getRawContent());
                    } else {
                        // Create finish action as fallback
                        action = new JsonObject();
                        action.addProperty("_metadata", "finish");
                        action.addProperty("message", "Model returned empty action");
                    }
                } else {
                    action = ActionParser.parseAction(response.getAction());
                }
            } catch (IllegalArgumentException e) {
                if (agentConfig.isVerbose()) {
                    System.err.println("Failed to parse action. Raw action: [" + response.getAction() + "]");
                    e.printStackTrace();
                }
                // Create finish action as fallback
                action = new JsonObject();
                action.addProperty("_metadata", "finish");
                action.addProperty("message", response.getAction() != null ?
                        response.getAction() : "Action parsing failed");
            }

            if (agentConfig.isVerbose()) {
                // Print thinking process
                Messages msgs = Config.getMessages(agentConfig.getLang());
                DaemonLog.log("\n" + "=".repeat(50));
                DaemonLog.log("💭 " + msgs.getThinking() + ":");
                DaemonLog.log("-".repeat(50));
                DaemonLog.log(response.getThinking());
                DaemonLog.log("-".repeat(50));
                DaemonLog.log("🎯 " + msgs.getAction() + ":");
                DaemonLog.log(action.toString());
                DaemonLog.log("=".repeat(50) + "\n");
            }

            // Remove image from context to save space
            JsonObject lastMessage = context.get(context.size() - 1);
            context.set(context.size() - 1,
                    MessageBuilder.removeImagesFromMessage(lastMessage));
            try {
                String actionName = action.has("action") ?
                        action.get("action").getAsString() : null;
                if (callback != null) {
                    callback.action(actionName);
                }
            } catch (Exception e) {
                //
            }
            // Execute action
            ActionResult actionResult;
            try {
                actionResult = actionHandler.execute(action,
                        screenshot.getWidth(), screenshot.getHeight());
            } catch (Exception e) {
                if (agentConfig.isVerbose()) {
                    e.printStackTrace();
                }
                JsonObject finishAction = new JsonObject();
                finishAction.addProperty("_metadata", "finish");
                finishAction.addProperty("message", e.getMessage());
                actionResult = actionHandler.execute(finishAction,
                        screenshot.getWidth(), screenshot.getHeight());
            }

            // Add assistant response to context
            String assistantContent = "<think>" +
                    response.getThinking() + "</think>" +
                    "<answer>" + response.getAction() + "</answer>";
            context.add(MessageBuilder.createAssistantMessage(assistantContent));

            // Check if finished
            boolean finished = (action.has("_metadata") &&
                    "finish".equals(action.get("_metadata").getAsString())) ||
                    actionResult.isShouldFinish();

            if (finished && agentConfig.isVerbose()) {
                Messages msgs = Config.getMessages(agentConfig.getLang());
                DaemonLog.log("\n" + "🎉 " + "=".repeat(48));
                String message = actionResult.getMessage() != null ?
                        actionResult.getMessage() :
                        (action.has("message") && !action.get("message").isJsonNull() ?
                                action.get("message").getAsString() :
                                msgs.getDone());
                DaemonLog.log("✅ " + msgs.getTaskCompleted() + ": " + message);
                DaemonLog.log("=".repeat(50) + "\n");
            }

            return new StepResult(
                    actionResult.isSuccess(),
                    finished,
                    action,
                    response.getThinking(),
                    actionResult.getMessage() != null ? actionResult.getMessage() :
                            (action.has("message") && !action.get("message").isJsonNull() ?
                                    action.get("message").getAsString() : null)
            );

        } catch (Exception e) {
            if (agentConfig.isVerbose()) {
                e.printStackTrace();
            }
            return new StepResult(false, true, null, "",
                    "Step execution error: " + e.getMessage());
        }
    }

    public List<JsonObject> getContext() {
        return new ArrayList<>(context);
    }

    public int getStepCount() {
        return stepCount;
    }
}

