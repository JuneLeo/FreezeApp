package com.john.freezeapp.adb;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.john.adb.AdbClient;
import com.john.adb.AdbKey;
import com.john.adb.AdbMdns;
import com.john.freezeapp.client.ClientLog;
import com.john.freezeapp.util.FreezeUtil;
import com.john.freezeapp.util.SharedPrefUtil;
import com.john.freezeapp.daemon.util.UIExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdbStartDialog extends AlertDialog {

    private AdbMdns adbMdns = null;

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public AdbStartDialog(Context context) {
        super(context);
        init();
    }

    public AdbStartDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    public AdbStartDialog(Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    private void init() {

        adbMdns = new AdbMdns(getContext(), -1, AdbMdns.TLS_CONNECT, new AdbMdns.Callback() {
            @Override
            public void callback(int port) {
                doAdbClient(port);
            }
        });
        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                adbMdns.start();
            }
        });

        setButton(BUTTON_POSITIVE, "开发者选项", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isNormal) {
                    Toast.makeText(getContext(), "FreezeApp服务正在连接中...", Toast.LENGTH_SHORT).show();
                    return;
                }
                FreezeUtil.toDevelopPage(getContext());
            }
        });

        setButton(BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        updateTitle("正在搜索无线调试服务", false);
        setMessage("请在“开发者选项”中启用“无线调试”功能。当网络变化时“无线调试”会被自动禁用。\n\n 注意：请不要禁用“开发者选项”或者“USB调试”，否则Freeze服务会被停止。\n\n 如果一直搜索，请尝试禁用并启用“无线调试”。");
    }

    private boolean isNormal = true;

    private void updateTitle(String title, boolean isNormal) {
        this.isNormal = isNormal;
        UIExecutor.postUI(new Runnable() {
            @Override
            public void run() {
                setTitle(title);
            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (adbMdns != null) {
            adbMdns.stop();
        }
    }


    AdbClient adbClient;

    private void doAdbClient(int port) {
        if (!isShowing()) {
            return;
        }
        if (port > 65535 || port < 1) {
            updateTitle("匹配码错误", false);
            return;
        }
        updateTitle("正在连接中", true);
        String host = "127.0.0.1";
        AdbKey key = new AdbKey(new AdbKey.PreferenceAdbKeyStore(SharedPrefUtil.getSharedPref()), "freezeapp");

        executorService.execute(() -> {
            adbClient = new AdbClient(host, port, key);
            try {
                adbClient.connect();
                String cmd = "sh " + FreezeUtil.getShellFilePath(getContext());
                adbClient.shellCommand(cmd, new AdbClient.ShellCommandCallback() {
                    @Override
                    public void callback(byte[] bytes) {
                        ClientLog.log("adb client shell reslt - " + new String(bytes));
                        updateTitle("启动成功", true);
                    }
                });
                try {
                    adbClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                UIExecutor.postUI(new Runnable() {
                    @Override
                    public void run() {
                        if (isShowing()) {
                            dismiss();
                        }
                    }
                });

            } catch (Throwable e) {
                e.printStackTrace();
                updateTitle("启动失败", false);
            }
        });

    }


}
