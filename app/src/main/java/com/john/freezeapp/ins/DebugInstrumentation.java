package com.john.freezeapp.ins;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DebugInstrumentation extends Instrumentation {

    public static final String TAG = "songpengfei";

    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Log.d("songpengfei", "newApplication - " + Log.getStackTraceString(new Throwable()));
        installMultidex();
        // 在主线程中启动 Activity

        Log.d(TAG, "class = " + className + ", classloader=" + cl.getClass().getName());
        try {
            Class clz = cl.loadClass("androidx/lifecycle/ProcessLifecycleOwner");
            Log.d(TAG, "class = " + className + ", classloader=" + cl.getClass().getName());
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

        return super.newApplication(cl, className, context);
    }


    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        Log.d("songpengfei", "DebugInstrumentation started");
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        super.callActivityOnCreate(activity, icicle);


    }


    @Override
    public void callActivityOnResume(Activity activity) {
        super.callActivityOnResume(activity);
        Log.d("songpengfei", "callActivityOnResume - " + Log.getStackTraceString(new Throwable()));


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


        Log.d("songpengfei", "onDestroy - " + Log.getStackTraceString(new Throwable()));


    }

    @Override
    public void finish(int resultCode, Bundle results) {
        Log.d("songpengfei", "finish - " + Log.getStackTraceString(new Throwable()));
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
