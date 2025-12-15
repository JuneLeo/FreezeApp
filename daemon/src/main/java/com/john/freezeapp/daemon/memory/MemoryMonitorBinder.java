package com.john.freezeapp.daemon.memory;

import android.os.RemoteException;
import android.text.TextUtils;

import com.john.freezeapp.daemon.CommonShellUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MemoryMonitorBinder extends IMemoryMonitorBinder.Stub {

    final List<IMemoryMonitorListener> iMemoryMonitorListeners = new ArrayList<>();
    ScheduledExecutorService executorService;

    boolean isActive = false;

    @Override
    public boolean start(String packageName, long delay) throws RemoteException {
        stop();
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleWithFixedDelay(() -> requestMemoryData(packageName), 0, delay, TimeUnit.MILLISECONDS);
        return false;
    }

    private void innerStop() {
        try {
            stop();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void requestMemoryData(String packageName) {
        CommonShellUtils.execCommand(String.format("dumpsys meminfo --package %s", packageName), false, new CommonShellUtils.ShellCommandResultCallback() {
            @Override
            public void callback(CommonShellUtils.ShellCommandResult commandResult) {
                if (!TextUtils.isEmpty(commandResult.errorMsg)) {
                    innerStop();
                    return;
                }

                if (!TextUtils.isEmpty(commandResult.successMsg)) {
                    List<MemoryData> memoryDatas = MemoryMonitorParse.parse(commandResult.successMsg);

                    for (MemoryData memoryData : memoryDatas) {
                        if (TextUtils.equals(packageName, memoryData.mPackageName)) {
                            notifyListener(memoryData);
                            return;
                        }
                    }
                }
            }
        });
    }

    private void notifyListener(MemoryData memoryData) {
        synchronized (iMemoryMonitorListeners) {
            for (IMemoryMonitorListener listener : iMemoryMonitorListeners) {
                try {
                    listener.process(memoryData);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void addListener(IMemoryMonitorListener listener) throws RemoteException {
        synchronized (iMemoryMonitorListeners) {
            iMemoryMonitorListeners.add(listener);
            listener.asBinder().linkToDeath(() -> iMemoryMonitorListeners.remove(listener), 0);
        }
    }

    @Override
    public void removeListener(IMemoryMonitorListener listener) throws RemoteException {
        synchronized (iMemoryMonitorListeners) {
            iMemoryMonitorListeners.remove(listener);
        }
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
        }
    }
}
