package com.john.freezeapp.client;

import android.os.RemoteException;

import com.john.freezeapp.IClientLogBinder;

public class ClientLogBinderStub extends IClientLogBinder.Stub {


    @Override
    public void log(String msg) throws RemoteException {
        ClientLogBinderManager.notifyLog( msg);
    }

    @Override
    public void toast(String msg) throws RemoteException {

    }
}