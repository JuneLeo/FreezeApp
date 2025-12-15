package com.john.freezeapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.john.freezeapp.client.ClientBinderManager;

public class BaseService extends Service {

    private IDaemonBinder daemonBinder;

    ClientBinderManager.IDaemonBinderListener iDaemonBinderListener = new ClientBinderManager.IDaemonBinderListener() {
        @Override
        public void bind(IDaemonBinder daemonBinder) {
            toBindDaemon(daemonBinder);
        }

        @Override
        public void unbind() {
            toUnbindDaemon();
        }
    };

    protected void bindDaemon(IDaemonBinder daemonBinder) {

    }

    protected void unbindDaemon() {

    }

    public IDaemonBinder getDaemonBinder() {
        return daemonBinder;
    }

    private void toBindDaemon(IDaemonBinder daemonBinder) {
        this.daemonBinder = daemonBinder;
        bindDaemon(daemonBinder);
    }

    private void toUnbindDaemon() {
        unbindDaemon();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ClientBinderManager.unregisterDaemonBinderListener(iDaemonBinderListener);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        daemonBinder = ClientBinderManager.getDaemonBinder();
        ClientBinderManager.registerDaemonBinderListener(iDaemonBinderListener);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
