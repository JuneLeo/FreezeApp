package com.john.freezeapp.daemon;

import android.os.Binder;
import android.os.IBinder;
import android.os.IBinderHidden;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.SystemProperties;
import android.text.TextUtils;


import com.john.freezeapp.IDaemonBinder;
import com.john.freezeapp.IRemoteProcess;
import com.john.freezeapp.daemon.autoglm.AutoGLMBinder;
import com.john.freezeapp.daemon.clipboard.DaemonClipboardMonitorBinder;
import com.john.freezeapp.daemon.fs.DaemonFileServerBinder;
import com.john.freezeapp.daemon.memory.MemoryMonitorBinder;
import com.john.freezeapp.daemon.monitor.DaemonAppMonitorBinderStub;
import com.john.freezeapp.daemon.process.RemoteProcess;
import com.john.freezeapp.daemon.runas.DaemonRunAsBinder;
import com.john.freezeapp.daemon.traffic.DaemonTrafficBinder;
import com.john.hidden.api.ReplaceRef;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DaemonBinderStub extends IDaemonBinder.Stub {

    private final static Map<String, IBinder> sDaemonBinderMap = new HashMap<>();

    static {
        sDaemonBinderMap.put(DaemonHelper.DAEMON_BINDER_FRP, new DaemonFrpBinderStub());
        sDaemonBinderMap.put(DaemonHelper.DAEMON_BINDER_APP_MONITOR, new DaemonAppMonitorBinderStub());
        sDaemonBinderMap.put(DaemonHelper.DAEMON_BINDER_CLIPBOARD_MONITOR, new DaemonClipboardMonitorBinder());
        sDaemonBinderMap.put(DaemonHelper.DAEMON_BINDER_FILE_SERVER, new DaemonFileServerBinder());
        sDaemonBinderMap.put(DaemonHelper.DAEMON_BINDER_RUN_AS, new DaemonRunAsBinder());
        sDaemonBinderMap.put(DaemonHelper.DAEMON_BINDER_TRAFFIC_MONITOR, new DaemonTrafficBinder());
        sDaemonBinderMap.put(DaemonHelper.DAEMON_BINDER_AUTO_GLM, new AutoGLMBinder());
        sDaemonBinderMap.put(DaemonHelper.DAEMON_BINDER_MEMORY_MONITOR, new MemoryMonitorBinder());
    }

    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {

        if (code == android.os.IBinder.FIRST_CALL_TRANSACTION + 1) {
            data.enforceInterface(DESCRIPTOR);
            transactRemote(data, reply, flags);
            return true;
        }

        return super.onTransact(code, data, reply, flags);
    }

    private void transactRemote(Parcel data, Parcel reply, int flags) throws RemoteException {
        IBinder targetBinder = data.readStrongBinder();


        int targetCode = data.readInt();
        int targetFlags = data.readInt();

//        DaemonLog.toClient(Binder.getCallingUid(), Binder.getCallingPid(), "call transactRemote descriptor=" + targetBinder.getInterfaceDescriptor() + ", code=" + targetCode + ", flags=" + targetFlags);
        Parcel newData = Parcel.obtain();
        try {
            newData.appendFrom(data, data.dataPosition(), data.dataAvail());
        } catch (Throwable tr) {
            return;
        }
        try {
            long id = Binder.clearCallingIdentity();
            targetBinder.transact(targetCode, newData, reply, targetFlags);
            Binder.restoreCallingIdentity(id);
        } finally {
            newData.recycle();
        }
    }

    @Override
    public String getConfig(String module, String key) throws RemoteException {
        DaemonLog.toClient(Binder.getCallingUid(), Binder.getCallingPid(), "call getConfig key=" + key);

        if (TextUtils.equals(module, DaemonHelper.DAEMON_MODULE_CUSTOM)) {
            return DaemonConfig.getConfig(key);
        } else if (TextUtils.equals(module, DaemonHelper.DAEMON_MODULE_SYSTEM)) {
            return System.getProperty(key);
        } else if (TextUtils.equals(module, DaemonHelper.DAEMON_MODULE_SYSTEM_PROPERTIES)) {
            return SystemProperties.get(key);
        }
        return "";
    }

    @Override
    public IRemoteProcess newProcess(String[] cmd, String[] env, String dir) throws RemoteException {
        DaemonLog.toClient(Binder.getCallingUid(), Binder.getCallingPid(), "call newProcess cmd=" + Arrays.toString(cmd));
        Process process;
        try {
            process = Runtime.getRuntime().exec(cmd, env, dir != null ? new File(dir) : null);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }

        return new RemoteProcess(process);
    }

    @Override
    public void closeDaemon() throws RemoteException {
        DaemonLog.toClient(Binder.getCallingUid(), Binder.getCallingPid(), "call closeDaemon");
        Daemon.getDaemon().stop();
    }

    @Override
    public IBinder getService(String name) throws RemoteException {
        DaemonLog.toClient(Binder.getCallingUid(), Binder.getCallingPid(), "call getService name=" + name);
        if (sDaemonBinderMap.containsKey(name)) {
            return sDaemonBinderMap.get(name);
        }
        return null;
    }

    @Override
    public boolean registerClientBinder(IBinder iClientBinder) throws RemoteException {
        DaemonClientBinderProxy.registerClientBinder(Binder.getCallingUid(), Binder.getCallingPid(), iClientBinder);
        return true;
    }

    @Override
    public void unregisterClientBinder(IBinder iClientBinder) throws RemoteException {
        DaemonClientBinderProxy.unregisterClientBinder(Binder.getCallingUid(), Binder.getCallingPid(), iClientBinder);
    }


    @Override
    protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        super.dump(fd, fout, args);
        DaemonLog.toClient(Binder.getCallingUid(), Binder.getCallingPid(), "call dump args=" + Arrays.toString(args));

        if (args == null || args.length == 0) {

            dumpFail(fd, "dump args fail");
            return;
        }


        IBinder service = null;
        try {
            service = ServiceManager.getService(args[0]);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (service == null) {
            dumpFail(fd, "dump " + args[0] + " service not find");
            return;
        }

        String[] serverArgs = null;
        if (args.length > 1) {
            serverArgs = Arrays.copyOfRange(args, 1, args.length);
        }

        try {
            service.dump(fd, serverArgs);
        } catch (RemoteException e) {
            e.printStackTrace();
            dumpFail(fd, "dump exception " + e.getMessage());
        }
    }

    private void dumpFail(FileDescriptor fd, String msg) {
        DaemonLog.toClient(Binder.getCallingUid(), Binder.getCallingPid(), msg);
        DaemonLog.log(msg);

        FileOutputStream fileOutputStream = new FileOutputStream(fd);
        try {
            fileOutputStream.write(msg.getBytes(StandardCharsets.UTF_8));
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void shellCommand(FileDescriptor in, FileDescriptor out,
                             FileDescriptor err,
                             String[] args, ShellCallback shellCallback,
                             ResultReceiver resultReceiver) {
        DaemonLog.toClient(Binder.getCallingUid(), Binder.getCallingPid(), "call shellCommand args=" + Arrays.toString(args));

        if (args == null || args.length == 0) {
            dumpFail(err, "shellCommand args fail");
            return;
        }


        IBinder service = null;
        try {
            service = ServiceManager.getService(args[0]);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (service == null) {
            dumpFail(err, "shellCommand " + args[0] + " service not find");
            return;
        }

        String[] serverArgs = null;
        if (args.length > 1) {
            serverArgs = Arrays.copyOfRange(args, 1, args.length);
        }

        try {
            ReplaceRef.<IBinderHidden>unsafeCast(service).shellCommand(in, out, err, args, shellCallback, resultReceiver);
        } catch (Exception e) {
            e.printStackTrace();
            dumpFail(err, "shellCommand exception " + e.getMessage());
        }
    }
}
