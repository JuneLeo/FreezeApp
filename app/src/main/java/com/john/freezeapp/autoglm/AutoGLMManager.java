package com.john.freezeapp.autoglm;

import android.os.IBinder;
import android.os.RemoteException;

import com.john.freezeapp.client.ClientBinderManager;
import com.john.freezeapp.daemon.DaemonHelper;
import com.john.freezeapp.daemon.autoglm.IAutoGLMBinder;
import com.john.freezeapp.util.SharedPrefUtil;

public class AutoGLMManager {
    public static IAutoGLMBinder getAutoGLMBinder() {
        try {
            IBinder service = ClientBinderManager.getDaemonBinder().getService(DaemonHelper.DAEMON_BINDER_AUTO_GLM);
            if (service != null) {
                return IAutoGLMBinder.Stub.asInterface(service);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void stopAutoGLM() {
        IAutoGLMBinder autoGLMBinder = getAutoGLMBinder();
        if (autoGLMBinder != null) {
            try {
                autoGLMBinder.stop();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void execute(String query, String url, String model, String apiKey) {
        IAutoGLMBinder autoGLMBinder = AutoGLMManager.getAutoGLMBinder();
        if (autoGLMBinder == null) {
            return;
        }

        try {
            autoGLMBinder.execute(query, url, model, apiKey);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static boolean isActive() {
        IAutoGLMBinder autoGLMBinder = getAutoGLMBinder();
        boolean isActive = false;
        if (autoGLMBinder != null) {
            try {
                isActive = autoGLMBinder.isActive();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
        return isActive;
    }

    private static final String APP_AUTO_GLM_URL = "app_auto_glm_url";
    private static final String APP_AUTO_GLM_MODEL = "app_auto_glm_model";
    private static final String APP_AUTO_GLM_APIKEY = "app_auto_glm_apikey";


    public static String getUrl() {
        return SharedPrefUtil.getString(APP_AUTO_GLM_URL, "");
    }

    public static String getModel() {
        return SharedPrefUtil.getString(APP_AUTO_GLM_MODEL, "");
    }

    public static String getApiKey() {
        return SharedPrefUtil.getString(APP_AUTO_GLM_APIKEY, "");
    }

    public static void setUrl(String url) {
        SharedPrefUtil.setString(APP_AUTO_GLM_URL, url);
    }

    public static void setModel(String url) {
        SharedPrefUtil.setString(APP_AUTO_GLM_MODEL, url);
    }

    public static void setApiKey(String url) {
        SharedPrefUtil.setString(APP_AUTO_GLM_APIKEY, url);
    }

}
