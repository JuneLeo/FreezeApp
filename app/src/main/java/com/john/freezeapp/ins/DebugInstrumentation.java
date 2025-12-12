package com.john.freezeapp.ins;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import top.canyie.pine.Pine;
import top.canyie.pine.PineConfig;
import top.canyie.pine.callback.MethodHook;

public class DebugInstrumentation extends Instrumentation {

    public static final String TAG = "DebugInstrumentation";

    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Log.d(TAG, "newApplication - " + Log.getStackTraceString(new Throwable()));
        installMultidex();
        PineConfig.debug = true;
        PineConfig.debuggable = true;
        return super.newApplication(cl, className, context);
    }


    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        Log.d(TAG, "onCreate");
        try {
            Pine.hook(Class.forName("com.autonavi.bundle.vui.util.CloudController").getDeclaredMethod("isLLMLogic"), new MethodHook() {
                @Override
                public void afterCall(Pine.CallFrame callFrame) throws Throwable {
                    callFrame.setResult(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        super.callActivityOnCreate(activity, icicle);
        Log.d(TAG, "callActivityOnCreate");
    }


    @Override
    public void callActivityOnResume(Activity activity) {
        super.callActivityOnResume(activity);
        Log.d(TAG, "callActivityOnResume - " + Log.getStackTraceString(new Throwable()));
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, "我是FreezeApp", Toast.LENGTH_LONG).show();
            }
        }, 5000);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        super.callActivityOnCreate(activity, icicle, persistentState);

    }


    @Override
    public void onStart() {
        super.onStart();
        waitForIdleSync();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy - " + Log.getStackTraceString(new Throwable()));
    }


    protected void installOldMultiDex(Class<?> multidexClass)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method install = multidexClass.getDeclaredMethod("install", Context.class);
        install.invoke(null, getTargetContext());
    }


    protected void installMultidex() {
        // Typically multidex is installed by inserting call at Application#attachBaseContext
        // However instrumentation#onCreate is called before Application#attachBaseContext. Thus
        // need to install it here, if its on classpath.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            try {
                Class<?> multidex = getMultiDexClass();
                try {
                    Method installInstrumentation =
                            multidex.getDeclaredMethod("installInstrumentation", Context.class, Context.class);
                    installInstrumentation.invoke(null, getContext(), getTargetContext());
                } catch (NoSuchMethodException nsme) {
                    Log.w(
                            TAG,
                            "Could not find MultiDex.installInstrumentation. Calling MultiDex.install instead."
                                    + " Is an old version of the multidex library being used? If test app is using"
                                    + " multidex, classes might not be found");
                    installOldMultiDex(multidex);
                }
            } catch (ClassNotFoundException ignored) {
                Log.i(TAG, "No multidex.");
            } catch (NoSuchMethodException nsme) {
                Log.i(TAG, "No multidex.", nsme);
            } catch (InvocationTargetException ite) {
                throw new RuntimeException("multidex is available at runtime, but calling it failed.", ite);
            } catch (IllegalAccessException iae) {
                throw new RuntimeException("multidex is available at runtime, but calling it failed.", iae);
            }
        }
    }

    private static Class<?> getMultiDexClass() throws ClassNotFoundException {
        try {
            return Class.forName("androidx.multidex.MultiDex");
        } catch (ClassNotFoundException e) {
            // check for support multidex
            return Class.forName("android.support.multidex.MultiDex");
        }
    }

}
