package com.john.freezeapp.daemon.autoglm;

import android.os.RemoteException;

import com.john.freezeapp.daemon.DaemonLog;
import com.john.freezeapp.daemon.autoglm.model.ModelConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutoGLMBinder extends IAutoGLMBinder.Stub {


    final List<IAutoGLMListener> iAutoGLMListeners = new ArrayList<>();
    ExecutorService executorService;

    boolean isActive = false;

    @Override
    public void execute(String query, String url, String model, String apiKey) throws RemoteException {
        stop();

        executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            isActive = true;
            try {
                innerExecute(query, url, model, apiKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
            isActive = false;
        });
        DaemonLog.toClient("AutoGLMBinder start");
    }

    @Override
    public boolean isActive() throws RemoteException {
        return this.isActive;
    }

    @Override
    public void stop() throws RemoteException {
        if (this.executorService != null) {
            this.executorService.shutdownNow();
            this.isActive = false;
            DaemonLog.toClient("AutoGLMBinder stop");
        }
    }

    @Override
    public void addListener(IAutoGLMListener listener) throws RemoteException {
        synchronized (listener) {
            iAutoGLMListeners.add(listener);
            listener.asBinder().linkToDeath(() -> {
                iAutoGLMListeners.remove(listener);
                DaemonLog.toClient("AutoGLMBinder listener death length=" + iAutoGLMListeners.size());
            }, 0);
            DaemonLog.toClient("AutoGLMBinder add listener length=" + iAutoGLMListeners.size());
        }
    }

    @Override
    public void removeListener(IAutoGLMListener listener) throws RemoteException {
        iAutoGLMListeners.remove(listener);
        DaemonLog.toClient("AutoGLMBinder remove listener length=" + iAutoGLMListeners.size());
    }

    private void innerExecute(String query, String url, String model, String apiKey) {
        String lang = "cn";
        int maxSteps = 100;
        String task = query;
        boolean listApps = false;
        // Create configurations
        ModelConfig modelConfig = new ModelConfig(url, model, apiKey);
        AgentConfig agentConfig = new AgentConfig(maxSteps, null, lang, true);

        // Create agent
        PhoneAgent agent = new PhoneAgent(modelConfig, agentConfig);

        // Print header
        DaemonLog.log("=".repeat(50));
        DaemonLog.log("Phone Agent - AI-powered phone automation");
        DaemonLog.log("=".repeat(50));
        DaemonLog.log("Model: " + modelConfig.getModelName());
        DaemonLog.log("Base URL: " + modelConfig.getBaseUrl());
        DaemonLog.log("Max Steps: " + agentConfig.getMaxSteps());
        DaemonLog.log("Language: " + agentConfig.getLang());
        if (agentConfig.getDeviceId() != null) {
            DaemonLog.log("Device: " + agentConfig.getDeviceId());
        }
        DaemonLog.log("=".repeat(50));

        DaemonLog.log("\nTask: " + task + "\n");
        String result = agent.run(task, new AgentCallback() {
            @Override
            public void start() {
                notifyStart();
            }

            @Override
            public void action(String action) {
                notifyAction(action);
            }

            @Override
            public void end() {
                notifyEnd();
            }
        });
        DaemonLog.log("\nResult: " + result);
    }


    private void notifyStart() {
        for (IAutoGLMListener iAutoGLMListener : iAutoGLMListeners) {
            try {
                iAutoGLMListener.start();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyEnd() {
        for (IAutoGLMListener iAutoGLMListener : iAutoGLMListeners) {
            try {
                iAutoGLMListener.end();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyAction(String action) {
        for (IAutoGLMListener iAutoGLMListener : iAutoGLMListeners) {
            try {
                iAutoGLMListener.process(action);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


}
