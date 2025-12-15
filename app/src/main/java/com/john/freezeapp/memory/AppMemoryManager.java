package com.john.freezeapp.memory;

import android.os.Debug;
import android.os.DebugHidden;
import android.os.IBinder;
import android.os.RemoteException;

import com.john.freezeapp.client.ClientBinderManager;
import com.john.freezeapp.client.ClientSystemService;
import com.john.freezeapp.daemon.DaemonHelper;
import com.john.freezeapp.daemon.memory.IMemoryMonitorBinder;
import com.john.freezeapp.daemon.memory.MemoryData;
import com.john.freezeapp.util.SharedPrefUtil;
import com.john.hidden.api.ReplaceRef;

public class AppMemoryManager {
    /**
     * if (!dumpSummaryOnly) {
     *             if (dumpFullInfo) {
     *                 printRow(pw, HEAP_FULL_COLUMN, "", "Pss", "Pss", "Shared", "Private",
     *                         "Shared", "Private", memInfo.hasSwappedOutPss ? "SwapPss" : "Swap",
     *                         "Rss", "Heap", "Heap", "Heap");
     *                 printRow(pw, HEAP_FULL_COLUMN, "", "Total", "Clean", "Dirty", "Dirty",
     *                         "Clean", "Clean", "Dirty", "Total",
     *                         "Size", "Alloc", "Free");
     *                 printRow(pw, HEAP_FULL_COLUMN, "", "------", "------", "------", "------",
     *                         "------", "------", "------", "------", "------", "------", "------");
     *                 printRow(pw, HEAP_FULL_COLUMN, "Native Heap", memInfo.nativePss,
     *                         memInfo.nativeSwappablePss, memInfo.nativeSharedDirty,
     *                         memInfo.nativePrivateDirty, memInfo.nativeSharedClean,
     *                         memInfo.nativePrivateClean, memInfo.hasSwappedOutPss ?
     *                         memInfo.nativeSwappedOutPss : memInfo.nativeSwappedOut,
     *                         memInfo.nativeRss, nativeMax, nativeAllocated, nativeFree);
     *                 printRow(pw, HEAP_FULL_COLUMN, "Dalvik Heap", memInfo.dalvikPss,
     *                         memInfo.dalvikSwappablePss, memInfo.dalvikSharedDirty,
     *                         memInfo.dalvikPrivateDirty, memInfo.dalvikSharedClean,
     *                         memInfo.dalvikPrivateClean, memInfo.hasSwappedOutPss ?
     *                         memInfo.dalvikSwappedOutPss : memInfo.dalvikSwappedOut,
     *                         memInfo.dalvikRss, dalvikMax, dalvikAllocated, dalvikFree);
     *             } else {
     *                 printRow(pw, HEAP_COLUMN, "", "Pss", "Private",
     *                         "Private", memInfo.hasSwappedOutPss ? "SwapPss" : "Swap",
     *                         "Rss", "Heap", "Heap", "Heap");
     *                 printRow(pw, HEAP_COLUMN, "", "Total", "Dirty",
     *                         "Clean", "Dirty", "Total", "Size", "Alloc", "Free");
     *                 printRow(pw, HEAP_COLUMN, "", "------", "------", "------",
     *                         "------", "------", "------", "------", "------", "------");
     *                 printRow(pw, HEAP_COLUMN, "Native Heap", memInfo.nativePss,
     *                         memInfo.nativePrivateDirty,
     *                         memInfo.nativePrivateClean,
     *                         memInfo.hasSwappedOutPss ? memInfo.nativeSwappedOutPss :
     *                         memInfo.nativeSwappedOut, memInfo.nativeRss,
     *                         nativeMax, nativeAllocated, nativeFree);
     *                 printRow(pw, HEAP_COLUMN, "Dalvik Heap", memInfo.dalvikPss,
     *                         memInfo.dalvikPrivateDirty,
     *                         memInfo.dalvikPrivateClean,
     *                         memInfo.hasSwappedOutPss ? memInfo.dalvikSwappedOutPss :
     *                         memInfo.dalvikSwappedOut, memInfo.dalvikRss,
     *                         dalvikMax, dalvikAllocated, dalvikFree);
     *             }
     *
     *             int otherPss = memInfo.otherPss;
     *             int otherSwappablePss = memInfo.otherSwappablePss;
     *             int otherSharedDirty = memInfo.otherSharedDirty;
     *             int otherPrivateDirty = memInfo.otherPrivateDirty;
     *             int otherSharedClean = memInfo.otherSharedClean;
     *             int otherPrivateClean = memInfo.otherPrivateClean;
     *             int otherSwappedOut = memInfo.otherSwappedOut;
     *             int otherSwappedOutPss = memInfo.otherSwappedOutPss;
     *             int otherRss = memInfo.otherRss;
     *
     *             for (int i=0; i<Debug.MemoryInfo.NUM_OTHER_STATS; i++) {
     *                 final int myPss = memInfo.getOtherPss(i);
     *                 final int mySwappablePss = memInfo.getOtherSwappablePss(i);
     *                 final int mySharedDirty = memInfo.getOtherSharedDirty(i);
     *                 final int myPrivateDirty = memInfo.getOtherPrivateDirty(i);
     *                 final int mySharedClean = memInfo.getOtherSharedClean(i);
     *                 final int myPrivateClean = memInfo.getOtherPrivateClean(i);
     *                 final int mySwappedOut = memInfo.getOtherSwappedOut(i);
     *                 final int mySwappedOutPss = memInfo.getOtherSwappedOutPss(i);
     *                 final int myRss = memInfo.getOtherRss(i);
     *                 if (myPss != 0 || mySharedDirty != 0 || myPrivateDirty != 0
     *                         || mySharedClean != 0 || myPrivateClean != 0 || myRss != 0
     *                         || (memInfo.hasSwappedOutPss ? mySwappedOutPss : mySwappedOut) != 0) {
     *                     if (dumpFullInfo) {
     *                         printRow(pw, HEAP_FULL_COLUMN, Debug.MemoryInfo.getOtherLabel(i),
     *                                 myPss, mySwappablePss, mySharedDirty, myPrivateDirty,
     *                                 mySharedClean, myPrivateClean,
     *                                 memInfo.hasSwappedOutPss ? mySwappedOutPss : mySwappedOut,
     *                                 myRss, "", "", "");
     *                     } else {
     *                         printRow(pw, HEAP_COLUMN, Debug.MemoryInfo.getOtherLabel(i),
     *                                 myPss, myPrivateDirty,
     *                                 myPrivateClean,
     *                                 memInfo.hasSwappedOutPss ? mySwappedOutPss : mySwappedOut,
     *                                 myRss, "", "", "");
     *                     }
     *                     otherPss -= myPss;
     *                     otherSwappablePss -= mySwappablePss;
     *                     otherSharedDirty -= mySharedDirty;
     *                     otherPrivateDirty -= myPrivateDirty;
     *                     otherSharedClean -= mySharedClean;
     *                     otherPrivateClean -= myPrivateClean;
     *                     otherSwappedOut -= mySwappedOut;
     *                     otherSwappedOutPss -= mySwappedOutPss;
     *                     otherRss -= myRss;
     *                 }
     *             }
     *
     *             if (dumpFullInfo) {
     *                 printRow(pw, HEAP_FULL_COLUMN, "Unknown", otherPss, otherSwappablePss,
     *                         otherSharedDirty, otherPrivateDirty, otherSharedClean, otherPrivateClean,
     *                         memInfo.hasSwappedOutPss ? otherSwappedOutPss : otherSwappedOut,
     *                         otherRss, "", "", "");
     *                 printRow(pw, HEAP_FULL_COLUMN, "TOTAL", memInfo.getTotalPss(),
     *                         memInfo.getTotalSwappablePss(),
     *                         memInfo.getTotalSharedDirty(), memInfo.getTotalPrivateDirty(),
     *                         memInfo.getTotalSharedClean(), memInfo.getTotalPrivateClean(),
     *                         memInfo.hasSwappedOutPss ? memInfo.getTotalSwappedOutPss() :
     *                         memInfo.getTotalSwappedOut(), memInfo.getTotalRss(),
     *                         nativeMax+dalvikMax, nativeAllocated+dalvikAllocated,
     *                         nativeFree+dalvikFree);
     *             } else {
     *                 printRow(pw, HEAP_COLUMN, "Unknown", otherPss,
     *                         otherPrivateDirty, otherPrivateClean,
     *                         memInfo.hasSwappedOutPss ? otherSwappedOutPss : otherSwappedOut,
     *                         otherRss, "", "", "");
     *                 printRow(pw, HEAP_COLUMN, "TOTAL", memInfo.getTotalPss(),
     *                         memInfo.getTotalPrivateDirty(),
     *                         memInfo.getTotalPrivateClean(),
     *                         memInfo.hasSwappedOutPss ? memInfo.getTotalSwappedOutPss() :
     *                         memInfo.getTotalSwappedOut(), memInfo.getTotalRss(),
     *                         nativeMax+dalvikMax,
     *                         nativeAllocated+dalvikAllocated, nativeFree+dalvikFree);
     *             }
     *
     *             if (dumpDalvik) {
     *                 pw.println(" ");
     *                 pw.println(" Dalvik Details");
     *
     *                 for (int i=Debug.MemoryInfo.NUM_OTHER_STATS;
     *                      i<Debug.MemoryInfo.NUM_OTHER_STATS + Debug.MemoryInfo.NUM_DVK_STATS; i++) {
     *                     final int myPss = memInfo.getOtherPss(i);
     *                     final int mySwappablePss = memInfo.getOtherSwappablePss(i);
     *                     final int mySharedDirty = memInfo.getOtherSharedDirty(i);
     *                     final int myPrivateDirty = memInfo.getOtherPrivateDirty(i);
     *                     final int mySharedClean = memInfo.getOtherSharedClean(i);
     *                     final int myPrivateClean = memInfo.getOtherPrivateClean(i);
     *                     final int mySwappedOut = memInfo.getOtherSwappedOut(i);
     *                     final int mySwappedOutPss = memInfo.getOtherSwappedOutPss(i);
     *                     final int myRss = memInfo.getOtherRss(i);
     *                     if (myPss != 0 || mySharedDirty != 0 || myPrivateDirty != 0
     *                             || mySharedClean != 0 || myPrivateClean != 0
     *                             || (memInfo.hasSwappedOutPss ? mySwappedOutPss : mySwappedOut) != 0) {
     *                         if (dumpFullInfo) {
     *                             printRow(pw, HEAP_FULL_COLUMN, Debug.MemoryInfo.getOtherLabel(i),
     *                                     myPss, mySwappablePss, mySharedDirty, myPrivateDirty,
     *                                     mySharedClean, myPrivateClean,
     *                                     memInfo.hasSwappedOutPss ? mySwappedOutPss : mySwappedOut,
     *                                     myRss, "", "", "");
     *                         } else {
     *                             printRow(pw, HEAP_COLUMN, Debug.MemoryInfo.getOtherLabel(i),
     *                                     myPss, myPrivateDirty,
     *                                     myPrivateClean,
     *                                     memInfo.hasSwappedOutPss ? mySwappedOutPss : mySwappedOut,
     *                                     myRss, "", "", "");
     *                         }
     *                     }
     *                 }
     *             }
     *         }
     *
     *         pw.println(" ");
     *         pw.println(" App Summary");
     *         printRow(pw, TWO_COUNT_COLUMN_HEADER, "", "Pss(KB)", "", "Rss(KB)");
     *         printRow(pw, TWO_COUNT_COLUMN_HEADER, "", "------", "", "------");
     *         printRow(pw, TWO_COUNT_COLUMNS,
     *                 "Java Heap:", memInfo.getSummaryJavaHeap(), "", memInfo.getSummaryJavaHeapRss());
     *         printRow(pw, TWO_COUNT_COLUMNS,
     *                 "Native Heap:", memInfo.getSummaryNativeHeap(), "",
     *                 memInfo.getSummaryNativeHeapRss());
     *         printRow(pw, TWO_COUNT_COLUMNS,
     *                 "Code:", memInfo.getSummaryCode(), "", memInfo.getSummaryCodeRss());
     *         printRow(pw, TWO_COUNT_COLUMNS,
     *                 "Stack:", memInfo.getSummaryStack(), "", memInfo.getSummaryStackRss());
     *         printRow(pw, TWO_COUNT_COLUMNS,
     *                 "Graphics:", memInfo.getSummaryGraphics(), "", memInfo.getSummaryGraphicsRss());
     *         printRow(pw, ONE_COUNT_COLUMN,
     *                 "Private Other:", memInfo.getSummaryPrivateOther());
     *         printRow(pw, ONE_COUNT_COLUMN,
     *                 "System:", memInfo.getSummarySystem());
     *         printRow(pw, ONE_ALT_COUNT_COLUMN,
     *                 "Unknown:", "", "", memInfo.getSummaryUnknownRss());
     *         pw.println(" ");
     *         if (memInfo.hasSwappedOutPss) {
     *             printRow(pw, THREE_COUNT_COLUMNS,
     *                     "TOTAL PSS:", memInfo.getSummaryTotalPss(),
     *                     "TOTAL RSS:", memInfo.getTotalRss(),
     *                     "TOTAL SWAP PSS:", memInfo.getSummaryTotalSwapPss());
     *         } else {
     *             printRow(pw, THREE_COUNT_COLUMNS,
     *                     "TOTAL PSS:", memInfo.getSummaryTotalPss(),
     *                     "TOTAL RSS:", memInfo.getTotalRss(),
     *                     "TOTAL SWAP (KB):", memInfo.getSummaryTotalSwap());
     *         }
     */


    /**
     * ** MEMINFO in pid 28161 [com.autonavi.minimap] **
     * Pss  Private  Private  SwapPss      Rss     Heap     Heap     Heap
     * Total    Dirty    Clean    Dirty    Total     Size    Alloc     Free
     * ------   ------   ------   ------   ------   ------   ------   ------
     * Native Heap    27958    17008    10944   333252    29516   467984   418064    43151
     * Dalvik Heap     9540     1704     7804    17493    12876    40258    20129    20129
     * Dalvik Other    22192     1520     6292     2096    36952
     * Stack     2048     1924      124     6248     2056
     * Ashmem      170      156        0        0     1180
     * Other dev      136        0      136        0      616
     * .so mmap    74732       84    59432     5542   145676
     * .jar mmap     3017        0        0        0    55072
     * .apk mmap    16375        0    11964     1036    34104
     * .ttf mmap      127        0       60        0      556
     * .dex mmap    17124        0      960        0    54016
     * .oat mmap       14        0        0        0     2032
     * .art mmap     4912      132     4592     2880    16932
     * Other mmap    30611       36    29328        0    35108
     * GL mtrack    16112    16112        0        0    16112
     * Unknown     1898      908      756     9584     9392
     * TOTAL   605097    39584   132392   378131   452196   508242   438193    63280
     * <p>
     * App Summary
     * Pss(KB)                        Rss(KB)
     * ------                         ------
     * Java Heap:     6428                          29808
     * Native Heap:    17008                          29516
     * Code:    72788                         320504
     * Stack:     1924                           2056
     * Graphics:    16112                          16112
     * Private Other:    57716
     * System:   433121
     * Unknown:                                   54200
     * <p>
     * TOTAL PSS:   605097            TOTAL RSS:   452196       TOTAL SWAP PSS:   378131
     * <p>
     * Objects
     * Views:      773         ViewRootImpl:        1
     * AppContexts:       13           Activities:        1
     * Assets:       32        AssetManagers:        0
     * Local Binders:      155        Proxy Binders:       87
     * Parcel memory:       97         Parcel count:      242
     * Death Recipients:        8             WebViews:        0
     * <p>
     * Native Allocations
     * Count                       Total(kB)
     * ------                         ------
     * Bitmap (malloced):       64                          11830
     * Other (malloced):     3230                            307
     * Other (nonmalloced):      282                            170
     * <p>
     * SQL
     * MEMORY_USED:     1454
     * PAGECACHE_OVERFLOW:      694          MALLOC_SIZE:       46
     * <p>
     * DATABASES
     * pgsz     dbsz   Lookaside(b) cache hits cache misses cache size  Dbname
     * PER CONNECTION STATS
     * 4       16             33     2    17     3  /data/user/0/com.autonavi.minimap/databases/amap_mini_app_env_rpc.db
     * 4       20             35     1    16     2  /data/user/0/com.autonavi.minimap/databases/accs.db
     * 4       88            123    17    38    14  /data/user/0/com.autonavi.minimap/databases/deviceML.db
     * 4      172            123    12    23     7  /data/user/0/com.autonavi.minimap/databases/aMap.db
     * 4       36             77    84    25     9  /data/user/0/com.autonavi.minimap/databases/com.autonavi.minimap_uptunnel.db
     * 4       96            123    44    64    10  /data/user/0/com.autonavi.minimap/no_backup/androidx.work.workdb
     * 4        8                    0     0     0    (attached) temp
     * 4       96             32     2    15     3  /data/user/0/com.autonavi.minimap/no_backup/androidx.work.workdb (2)
     * 4       52             44     3    18     4  /data/user/0/com.autonavi.minimap/databases/location_cache_new.db
     * POOL STATS
     * cache hits  cache misses    cache size  Dbname
     * 2            18            20  /data/user/0/com.autonavi.minimap/databases/amap_mini_app_env_rpc.db
     * 1            17            18  /data/user/0/com.autonavi.minimap/databases/accs.db
     * 17            39            56  /data/user/0/com.autonavi.minimap/databases/deviceML.db
     * 12            24            36  /data/user/0/com.autonavi.minimap/databases/aMap.db
     * 84            26           110  /data/user/0/com.autonavi.minimap/databases/com.autonavi.minimap_uptunnel.db
     * 48           100           148  /data/user/0/com.autonavi.minimap/no_backup/androidx.work.workdb
     * 3            19            22  /data/user/0/com.autonavi.minimap/databases/location_cache_new.db
     * <p>
     * Asset Allocations
     * : 19K
     */

    public static MemoryData getAppMemoryModel(int pid) {
        Debug.MemoryInfo[] memoryInfos = ClientSystemService.getActivityManager().getProcessMemoryInfo(new int[]{pid});
        if (memoryInfos == null || memoryInfos.length == 0) {
            return null;
        }

        DebugHidden.MemoryInfoHidden memInfo = ReplaceRef.<DebugHidden.MemoryInfoHidden>unsafeCast(memoryInfos[0]);
        /**
         *  printRow(pw, TWO_COUNT_COLUMNS,
         *      *                 "Java Heap:", memInfo.getSummaryJavaHeap(), "", memInfo.getSummaryJavaHeapRss());
         *      *         printRow(pw, TWO_COUNT_COLUMNS,
         *      *                 "Native Heap:", memInfo.getSummaryNativeHeap(), "",
         *      *                 memInfo.getSummaryNativeHeapRss());
         *      *         printRow(pw, TWO_COUNT_COLUMNS,
         *      *                 "Code:", memInfo.getSummaryCode(), "", memInfo.getSummaryCodeRss());
         *      *         printRow(pw, TWO_COUNT_COLUMNS,
         *      *                 "Stack:", memInfo.getSummaryStack(), "", memInfo.getSummaryStackRss());
         *      *         printRow(pw, TWO_COUNT_COLUMNS,
         *      *                 "Graphics:", memInfo.getSummaryGraphics(), "", memInfo.getSummaryGraphicsRss());
         *      *         printRow(pw, ONE_COUNT_COLUMN,
         *      *                 "Private Other:", memInfo.getSummaryPrivateOther());
         *      *         printRow(pw, ONE_COUNT_COLUMN,
         *      *                 "System:", memInfo.getSummarySystem());
         *      *         printRow(pw, ONE_ALT_COUNT_COLUMN,
         *      *                 "Unknown:", "", "", memInfo.getSummaryUnknownRss());
         *      *         pw.println(" ");
         *      *         if (memInfo.hasSwappedOutPss) {
         *      *             printRow(pw, THREE_COUNT_COLUMNS,
         *      *                     "TOTAL PSS:", memInfo.getSummaryTotalPss(),
         *      *                     "TOTAL RSS:", memInfo.getTotalRss(),
         *      *                     "TOTAL SWAP PSS:", memInfo.getSummaryTotalSwapPss());
         *      *         } else {
         *      *             printRow(pw, THREE_COUNT_COLUMNS,
         *      *                     "TOTAL PSS:", memInfo.getSummaryTotalPss(),
         *      *                     "TOTAL RSS:", memInfo.getTotalRss(),
         *      *                     "TOTAL SWAP (KB):", memInfo.getSummaryTotalSwap());
         *      *         }
         */
        MemoryData model = new MemoryData();
        model.mJavaHeapPssSize = memInfo.getSummaryJavaHeap(); // Java Heap
        model.mNativeHeapPssSize = memInfo.getSummaryNativeHeap(); //Native Heap
        model.mCodePssSize = memInfo.getSummaryCode(); //Code
        model.mStackPssSize = memInfo.getSummaryStack(); //Stack
        model.mGraphicsPssSize = memInfo.getSummaryGraphics(); //Graphics
        model.mPrivateOtherPssSize = memInfo.getSummaryPrivateOther(); //Private Other
        model.mSystemPssSize = memInfo.getSummarySystem(); //System
        model.mTotalPssSize = memInfo.getSummaryTotalPss(); //TOTAL PSS
        model.mTotalSwapPssSize = memInfo.getSummaryTotalSwapPss(); //TOTAL SWAP PSS
        return model;


    }


    public static boolean isOpen() {
        return SharedPrefUtil.getInt(SharedPrefUtil.KEY_MEMORY_MONITOR_SWITCH, 0) == 1;
    }

    public static void setSwitch(boolean isOpen) {
        SharedPrefUtil.setInt(SharedPrefUtil.KEY_MEMORY_MONITOR_SWITCH, isOpen ? 1 : 0);
    }

    public static String getProcessName() {
        return SharedPrefUtil.getString(SharedPrefUtil.KEY_MEMORY_MONITOR_PROCESS_NAME, "");
    }

    public static void setProcessName(String processName) {
        SharedPrefUtil.setString(SharedPrefUtil.KEY_MEMORY_MONITOR_PROCESS_NAME, processName);
    }


    public static IMemoryMonitorBinder getMemoryMonitorBinder() {
        try {
            IBinder service = ClientBinderManager.getDaemonBinder().getService(DaemonHelper.DAEMON_BINDER_MEMORY_MONITOR);
            if (service != null) {
                return IMemoryMonitorBinder.Stub.asInterface(service);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

}
