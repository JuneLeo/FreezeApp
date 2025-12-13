package com.john.freezeapp.daemon.autoglm.config;

import java.util.*;

/**
 * App name to package name mapping for supported applications.
 */
public class Apps {
    private static final Map<String, String> APP_PACKAGES = new HashMap<>();

    static {
        // Social & Messaging
        APP_PACKAGES.put("微信", "com.tencent.mm");
        APP_PACKAGES.put("QQ", "com.tencent.mobileqq");
        APP_PACKAGES.put("微博", "com.sina.weibo");
        
        // E-commerce
        APP_PACKAGES.put("淘宝", "com.taobao.taobao");
        APP_PACKAGES.put("京东", "com.jingdong.app.mall");
        APP_PACKAGES.put("拼多多", "com.xunmeng.pinduoduo");
        
        // Lifestyle & Social
        APP_PACKAGES.put("小红书", "com.xingin.xhs");
        APP_PACKAGES.put("豆瓣", "com.douban.frodo");
        APP_PACKAGES.put("知乎", "com.zhihu.android");
        
        // Maps & Navigation
        APP_PACKAGES.put("高德地图", "com.autonavi.minimap");
        APP_PACKAGES.put("百度地图", "com.baidu.BaiduMap");
        
        // Food & Services
        APP_PACKAGES.put("美团", "com.sankuai.meituan");
        APP_PACKAGES.put("大众点评", "com.dianping.v1");
        APP_PACKAGES.put("饿了么", "me.ele");
        
        // Travel
        APP_PACKAGES.put("携程", "ctrip.android.view");
        APP_PACKAGES.put("铁路12306", "com.MobileTicket");
        APP_PACKAGES.put("12306", "com.MobileTicket");
        APP_PACKAGES.put("滴滴出行", "com.sdu.did.psnger");
        
        // Video & Entertainment
        APP_PACKAGES.put("bilibili", "tv.danmaku.bili");
        APP_PACKAGES.put("抖音", "com.ss.android.ugc.aweme");
        APP_PACKAGES.put("快手", "com.smile.gifmaker");
        APP_PACKAGES.put("腾讯视频", "com.tencent.qqlive");
        APP_PACKAGES.put("爱奇艺", "com.qiyi.video");
        
        // Music & Audio
        APP_PACKAGES.put("网易云音乐", "com.netease.cloudmusic");
        APP_PACKAGES.put("QQ音乐", "com.tencent.qqmusic");
        APP_PACKAGES.put("喜马拉雅", "com.ximalaya.ting.android");
        
        // Add more apps as needed...
        APP_PACKAGES.put("Chrome", "com.android.chrome");
        APP_PACKAGES.put("chrome", "com.android.chrome");
        APP_PACKAGES.put("WeChat", "com.tencent.mm");
        APP_PACKAGES.put("wechat", "com.tencent.mm");
        APP_PACKAGES.put("Settings", "com.android.settings");
    }

    /**
     * Get the package name for an app.
     */
    public static String getPackageName(String appName) {
        return APP_PACKAGES.get(appName);
    }

    /**
     * Get the app name from a package name.
     */
    public static String getAppName(String packageName) {
        for (Map.Entry<String, String> entry : APP_PACKAGES.entrySet()) {
            if (entry.getValue().equals(packageName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Get a list of all supported app names.
     */
    public static List<String> listSupportedApps() {
        return new ArrayList<>(APP_PACKAGES.keySet());
    }

    /**
     * Get the app packages map.
     */
    public static Map<String, String> getAppPackages() {
        return new HashMap<>(APP_PACKAGES);
    }
}

