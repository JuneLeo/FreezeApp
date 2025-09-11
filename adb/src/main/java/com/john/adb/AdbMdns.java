package com.john.adb;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.text.TextUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;

public class AdbMdns {

    public static final String TLS_CONNECT = "_adb-tls-connect._tcp";
    public static final String TLS_PAIRING = "_adb-tls-pairing._tcp";
    public static final String TAG = "AdbMdns";
    private Context context;
    private String serviceType;
    private int port;
    private Callback callback;

    private boolean registered = false;
    private boolean running = false;
    private String serviceName = null;
    private NsdManager.DiscoveryListener listener = new NsdManager.DiscoveryListener() {
        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {

        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {

        }

        @Override
        public void onDiscoveryStarted(String serviceType) {
            AdbMdns.this.onDiscoveryStart();
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            AdbMdns.this.onDiscoveryStop();
        }

        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {
            AdbMdns.this.onServiceFound(serviceInfo);
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            AdbMdns.this.onServiceLost(serviceInfo);
        }
    };

    private NsdManager nsdManager;

    public AdbMdns(Context context, int port, String serviceType, Callback callback) {
        this.context = context;
        this.port = port;
        this.serviceType = serviceType;
        this.callback = callback;
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void start() {
        if (running) return;
        running = true;
        if (!registered) {
            nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener);
        }
    }

    public void stop() {
        if (!running) return;
        running = false;
        if (registered) {
            nsdManager.stopServiceDiscovery(listener);
        }
    }

    private void onDiscoveryStart() {
        registered = true;
    }

    private void onDiscoveryStop() {
        registered = false;
    }


    private void onServiceFound(NsdServiceInfo info) {
        nsdManager.resolveService(info, new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {

            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                try {
                    AdbMdns.this.onServiceResolved(serviceInfo);
                } catch (SocketException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void onServiceLost(NsdServiceInfo info) {
        if (TextUtils.equals(info.getServiceName(), serviceName)) {
            callback.callback(-1);
        }
    }

    private void onServiceResolved(NsdServiceInfo resolvedService) throws SocketException {

        boolean equalHostAddress = false;
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                if (TextUtils.equals(resolvedService.getHost().getHostAddress(), inetAddress.getHostAddress())) {
                    equalHostAddress = true;
                }
            }

        }

        if (running && equalHostAddress && isPortAvailable(resolvedService.getPort())) {
            serviceName = resolvedService.getServiceName();
            callback.callback(resolvedService.getPort());
        }
    }

    private boolean isPortAvailable(int port) {

        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress("127.0.0.1", port), 1);
            return false;
        } catch (Exception e) {
            return true;
        }

    }


    public interface Callback {
        void callback(int port);
    }

}
