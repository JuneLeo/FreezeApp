package com.john.freezeapp.daemon;

import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;


import java.io.FileDescriptor;

/**
 * https://xrefandroid.com/
 *
 * @param <T>
 */
public class SystemServiceBinder<T extends IInterface> implements IBinder, IBinder.DeathRecipient {

    public interface OnGetBinderListener {
        IBinder onGetBinder(IBinder binder);
    }

    private static OnGetBinderListener onGetBinderListener;

    public static void setOnGetBinderListener(OnGetBinderListener onGetBinderListener) {
        SystemServiceBinder.onGetBinderListener = onGetBinderListener;
    }

    public interface ServiceCreator<T> {
        T create(IBinder binder);
    }

    private final String name;
    private final ServiceCreator<T> serviceCreator;

    private IBinder binderCache;
    private T serviceCache;

    public SystemServiceBinder(String name, ServiceCreator<T> serviceCreator) {
        this.name = name;
        this.serviceCreator = serviceCreator;
    }

    public IBinder getBinder() {
        if (binderCache != null) {
            return binderCache;
        }

        IBinder binder = ServiceManager.getService(name);
        if (binder == null) {
            return null;
        }

        if (onGetBinderListener != null) {
            binder = onGetBinderListener.onGetBinder(binder);
        }

        try {
            binder.linkToDeath(this, 0);
        } catch (Throwable ignored) {
        }

        binderCache = binder;
        return binder;
    }

    public T get() {
        if (serviceCache != null) {
            return serviceCache;
        }

        IBinder binder = getBinder();
        if (binder == null) {
            return null;
        }

        serviceCache = serviceCreator.create(binder);
        return serviceCache;
    }

    @Override
    public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        IBinder binder = getBinder();
        if (binder == null) {
            return false;
        }

        try {
            return binder.transact(code, data, reply, flags);
        } catch (DeadObjectException e) {
            binderCache = null;

            IBinder newBinder = getBinder();
            if (newBinder != null) {
                return binder.transact(code, data, reply, flags);
            }
        }
        return false;
    }

    @Override
    public void binderDied() {
        binderCache.unlinkToDeath(this, 0);
        binderCache = null;
        serviceCache = null;
    }


    @Override
    public String getInterfaceDescriptor() throws RemoteException {
        IBinder binder = getBinder();
        if (binder != null) {
            return binder.getInterfaceDescriptor();
        }
        return null;
    }

    @Override
    public boolean pingBinder() {
        IBinder binder = getBinder();
        if (binder != null) {
            return binder.pingBinder();
        }
        return false;
    }

    @Override
    public boolean isBinderAlive() {
        IBinder binder = getBinder();
        if (binder != null) {
            return binder.isBinderAlive();
        }
        return false;
    }


    @Override
    public IInterface queryLocalInterface(String s) {
        IBinder binder = getBinder();
        if (binder != null) {
            return binder.queryLocalInterface(s);
        }
        return null;
    }

    @Override
    public void dump(FileDescriptor fd, String[] args) throws RemoteException {
        IBinder binder = getBinder();
        if (binder != null) {
            binder.dump(fd, args);
        }
    }

    @Override
    public void dumpAsync(FileDescriptor fd, String[] args) throws RemoteException {
        IBinder binder = getBinder();
        if (binder != null) {
            binder.dumpAsync(fd, args);
        }
    }

    @Override
    public void linkToDeath(DeathRecipient recipient, int flags) throws RemoteException {
        IBinder binder = getBinder();
        if (binder != null) {
            binder.linkToDeath(recipient, flags);
        }
    }

    @Override
    public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
        IBinder binder = getBinder();
        if (binder != null) {
            return binder.unlinkToDeath(recipient, flags);
        }
        return false;
    }
}
