package com.john.freezeapp.daemon.autoglm.config;

/**
 * Internationalization messages.
 */
public class Messages {
    public static final Messages ZH = new Messages("思考过程", "执行动作", "任务完成", "完成");
    public static final Messages EN = new Messages("Thinking", "Action", "Task Completed", "Done");

    private final String thinking;
    private final String action;
    private final String taskCompleted;
    private final String done;

    private Messages(String thinking, String action, String taskCompleted, String done) {
        this.thinking = thinking;
        this.action = action;
        this.taskCompleted = taskCompleted;
        this.done = done;
    }

    public String getThinking() {
        return thinking;
    }

    public String getAction() {
        return action;
    }

    public String getTaskCompleted() {
        return taskCompleted;
    }

    public String getDone() {
        return done;
    }
}

