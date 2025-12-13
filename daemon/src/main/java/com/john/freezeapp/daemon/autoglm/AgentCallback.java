package com.john.freezeapp.daemon.autoglm;

public interface AgentCallback {
    void start();

    void action(String action);

    void end();
}
