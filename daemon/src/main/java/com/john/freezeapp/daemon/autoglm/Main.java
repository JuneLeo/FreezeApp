package com.john.freezeapp.daemon.autoglm;

import com.john.freezeapp.daemon.autoglm.config.Apps;
import com.john.freezeapp.daemon.autoglm.model.ModelConfig;

import java.util.Scanner;

/**
 * Phone Agent CLI - AI-powered phone automation.
 */
public class Main {
    public static void main(String[] args) {
        // Parse command line arguments (simplified version)
        String baseUrl = System.getenv("PHONE_AGENT_BASE_URL");
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "https://api-inference.modelscope.cn/v1";
        }

        String modelName = System.getenv("PHONE_AGENT_MODEL");
        if (modelName == null || modelName.isEmpty()) {
            modelName = "ZhipuAI/AutoGLM-Phone-9B";
        }

        String apiKey = System.getenv("PHONE_AGENT_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "";
        }

        String deviceId = System.getenv("PHONE_AGENT_DEVICE_ID");
        String lang = System.getenv("PHONE_AGENT_LANG");
        if (lang == null || lang.isEmpty()) {
            lang = "cn";
        }

        String maxStepsStr = System.getenv("PHONE_AGENT_MAX_STEPS");
        int maxSteps = 100;
        if (maxStepsStr != null && !maxStepsStr.isEmpty()) {
            try {
                maxSteps = Integer.parseInt(maxStepsStr);
            } catch (NumberFormatException e) {
                // Use default
            }
        }

        // Simple argument parsing
        String task = null;
        boolean listApps = false;
        for (int i = 0; i < args.length; i++) {
            if ("--base-url".equals(args[i]) && i + 1 < args.length) {
                baseUrl = args[++i];
            } else if ("--model".equals(args[i]) && i + 1 < args.length) {
                modelName = args[++i];
            } else if ("--apikey".equals(args[i]) && i + 1 < args.length) {
                apiKey = args[++i];
            } else if ("--device-id".equals(args[i]) && i + 1 < args.length) {
                deviceId = args[++i];
            } else if ("--lang".equals(args[i]) && i + 1 < args.length) {
                lang = args[++i];
            } else if ("--list-apps".equals(args[i])) {
                listApps = true;
            } else if (!args[i].startsWith("--")) {
                task = args[i];
            }
        }

        task = "打开高德导航去天安门";

        // Handle --list-apps
        if (listApps) {
            System.out.println("Supported apps:");
            for (String app : Apps.listSupportedApps()) {
                System.out.println("  - " + app);
            }
            return;
        }

        // Create configurations
        ModelConfig modelConfig = new ModelConfig(baseUrl, modelName, apiKey);
        AgentConfig agentConfig = new AgentConfig(maxSteps, deviceId, lang, true);

        // Create agent
        PhoneAgent agent = new PhoneAgent(modelConfig, agentConfig);

        // Print header
        System.out.println("=".repeat(50));
        System.out.println("Phone Agent - AI-powered phone automation");
        System.out.println("=".repeat(50));
        System.out.println("Model: " + modelConfig.getModelName());
        System.out.println("Base URL: " + modelConfig.getBaseUrl());
        System.out.println("Max Steps: " + agentConfig.getMaxSteps());
        System.out.println("Language: " + agentConfig.getLang());
        if (agentConfig.getDeviceId() != null) {
            System.out.println("Device: " + agentConfig.getDeviceId());
        }
        System.out.println("=".repeat(50));

        // Run with provided task or enter interactive mode
        if (task != null && !task.isEmpty()) {
            System.out.println("\nTask: " + task + "\n");
            String result = agent.run(task, null);
            System.out.println("\nResult: " + result);
        } else {
            // Interactive mode
            System.out.println("\nEntering interactive mode. Type 'quit' to exit.\n");
            Scanner scanner = new Scanner(System.in);

            while (true) {
                try {
                    System.out.print("Enter your task: ");
                    String inputTask = scanner.nextLine().trim();

                    if (inputTask.toLowerCase().matches("quit|exit|q")) {
                        System.out.println("Goodbye!");
                        break;
                    }

                    if (inputTask.isEmpty()) {
                        continue;
                    }

                    System.out.println();
                    String result = agent.run(inputTask, null);
                    System.out.println("\nResult: " + result + "\n");
                    agent.reset();

                } catch (Exception e) {
                    System.out.println("\nError: " + e.getMessage() + "\n");
                }
            }
        }
    }
}

