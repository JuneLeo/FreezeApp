package com.john.freezeapp.daemon.traffic;

import android.os.RemoteException;

public class DaemonTrafficBinder extends IDaemonTrafficBinder.Stub {
    @Override
    public void start(long threshold, int trafficType) throws RemoteException {
        try {
            TrafficMonitor.start(threshold, trafficType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws RemoteException {
        try {
            TrafficMonitor.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isActive() throws RemoteException {
        return TrafficMonitor.isActive();
    }
}
