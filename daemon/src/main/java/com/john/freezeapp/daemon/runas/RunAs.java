package com.john.freezeapp.daemon.runas;

import android.os.Looper;
import android.os.Process;
import android.os.ProcessHidden;

import androidx.annotation.Keep;

import com.john.freezeapp.daemon.DaemonLog;
import com.john.freezeapp.daemon.fs.FileServerManager;
import com.john.freezeapp.util.CommonConstant;

import java.io.File;

@Keep
public class RunAs {

    public static void main(String[] args) {

        DaemonLog.log("RunAs 被启动了");
        Looper.prepareMainLooper();
        String currentDirectory = System.getProperty("user.dir");
        DaemonLog.log("RunAs currentDirectory = " + currentDirectory);
        FileServerManager fileServerManager = new FileServerManager();
        fileServerManager.startServer(CommonConstant.INTERNAL_APP_FILE_SERVER_PORT, new File(currentDirectory));

        DaemonLog.log("RunAs myPpid = " + ProcessHidden.myPpid());
        DaemonLog.log("RunAs myPid = " + Process.myPid());
        DaemonLog.log("RunAs myUid = " + Process.myUid());
        DaemonLog.log("RunAs myTid = " + Process.myTid());

        Looper.loop();

    }


}
