package com.john.freezeapp.daemon;

import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Keep;

import com.john.freezeapp.daemon.runas.RunAsManager;
import com.john.freezeapp.util.UserHandleCompat;

@Keep
public class Daemon {


    private Handler mHandler = null;
    public ActivityThread mActivityThread = null;

    private static Daemon sDaemon;


    public static void main(String[] args) {
        DaemonLog.log("Daemon start");
        Looper.prepareMainLooper();
        sDaemon = new Daemon();
        test();
        Looper.loop();
        DaemonLog.toClient("Daemon close");
        DaemonLog.log("Daemon finish");
        System.exit(0);
    }

    private static void test() {

    }


    public static Daemon getDaemon() {
        return sDaemon;
    }

    public Application getApplication() {
        return mActivityThread.getApplication();
    }

    public Context getContext() {
        return mActivityThread.getSystemContext();
    }

    private void killOtherDaemon() {
        CommonShellUtils.execCommand(String.format("ps | grep '%s' | awk '{print $2}'", DaemonHelper.DAEMON_NICKNAME), false, commandResult -> {
            int cPid = Process.myPid();
            DaemonLog.log("killOtherDaemon commandResult=" + commandResult.toString());
            if (commandResult.result && !TextUtils.isEmpty(commandResult.successMsg)) {
                String[] pids = commandResult.successMsg.split("\\s+");
                for (String pid : pids) {
                    if (!TextUtils.equals(pid, cPid + "")) {
                        CommonShellUtils.execCommand("kill -9 " + pid, false, null);
                    }
                }
            }
        });
    }

    public Daemon() {
        printDaemon();
        killOtherDaemon();
        RunAsManager.killAllRunAsProcess();
        mHandler = new Handler(Looper.getMainLooper());
        mActivityThread = ActivityThread.systemMain();
        DaemonBinderManager.register(mActivityThread.getSystemContext());
        DaemonBinderManager.sendBinderContainer();

        new Thread(new Runnable() {
            @Override
            public void run() {
                CommonShellUtils.execCommand("am instrument -w com.john.freezeapp/com.john.freezeapp.ins.DebugInstrumentation ", false, new CommonShellUtils.ShellCommandResultCallback() {
                    @Override
                    public void callback(CommonShellUtils.ShellCommandResult commandResult) {
                        DaemonLog.log("ShellCommandResult finish");
                    }
                });
            }
        }).start();

        printDaemon();
    }

    private void printDaemon() {
        DaemonLog.log("-----------------------------Daemon-----------------------------");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\npid=");
        stringBuilder.append(android.os.Process.myPid());
        stringBuilder.append("\n");

        stringBuilder.append("uid=");
        stringBuilder.append(android.os.Process.myUid());
        stringBuilder.append("\n");

        stringBuilder.append("userId=");
        stringBuilder.append(UserHandleCompat.myUserId());
        stringBuilder.append("\n");

        stringBuilder.append("uname=");
        stringBuilder.append(Os.uname());
        stringBuilder.append("\n");
        DaemonLog.log(stringBuilder.toString());
        DaemonLog.toClient(stringBuilder.toString());
        DaemonLog.log("-----------------------------------------------------------------");
    }


    public Handler getHandler() {
        return mHandler;
    }

    public void stop() {
        System.exit(0);
    }

}
