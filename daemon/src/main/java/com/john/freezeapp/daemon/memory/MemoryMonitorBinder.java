package com.john.freezeapp.daemon.memory;

import android.os.RemoteException;
import android.text.TextUtils;

import com.john.freezeapp.IClientLogBinder;
import com.john.freezeapp.daemon.CommonShellUtils;
import com.john.freezeapp.daemon.DaemonLog;

import java.util.ArrayList;
import java.util.Iterator;
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
        DaemonLog.toClient("MemoryMonitorBinder Task start");
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
            listener.asBinder().linkToDeath(() -> {
                try {
                    removeListener(listener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                DaemonLog.toClient("MemoryMonitorBinder listener death length=" + iMemoryMonitorListeners.size());
            }, 0);
            DaemonLog.toClient("MemoryMonitorBinder add listener length=" + iMemoryMonitorListeners.size());
        }
    }

    @Override
    public void removeListener(IMemoryMonitorListener listener) throws RemoteException {
        synchronized (iMemoryMonitorListeners) {
            iMemoryMonitorListeners.removeIf(next -> next.asBinder() == listener.asBinder());
            DaemonLog.toClient("MemoryMonitorBinder remove listener length=" + iMemoryMonitorListeners.size());
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
            DaemonLog.toClient("MemoryMonitorBinder Task stop");
        }
    }
}
