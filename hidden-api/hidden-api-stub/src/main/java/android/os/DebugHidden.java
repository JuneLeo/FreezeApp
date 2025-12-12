package android.os;


import com.john.hidden.api.Replace;

import java.util.HashMap;
import java.util.Map;

@Replace(Debug.class)
public class DebugHidden {
    public static boolean getMemoryInfo(int pid, Debug.MemoryInfo memoryInfo) {
        return true;
    }


    @Replace(Debug.MemoryInfo.class)
    public static class MemoryInfoHidden {
        public int dalvikPss;
        /** The proportional set size that is swappable for dalvik heap. */
        /**
         * We may want to expose this, eventually.
         */

        public int dalvikSwappablePss;
        /**
         * The resident set size for dalvik heap.  (Without other Dalvik overhead.)
         */
        public int dalvikRss;
        /**
         * The private dirty pages used by dalvik heap.
         */
        public int dalvikPrivateDirty;
        /**
         * The shared dirty pages used by dalvik heap.
         */
        public int dalvikSharedDirty;
        /** The private clean pages used by dalvik heap. */
        /**
         * We may want to expose this, eventually.
         */

        public int dalvikPrivateClean;
        /** The shared clean pages used by dalvik heap. */
        /**
         * We may want to expose this, eventually.
         */

        public int dalvikSharedClean;
        /** The dirty dalvik pages that have been swapped out. */
        /**
         * We may want to expose this, eventually.
         */

        public int dalvikSwappedOut;
        /** The dirty dalvik pages that have been swapped out, proportional. */
        /**
         * We may want to expose this, eventually.
         */
        public int dalvikSwappedOutPss;

        /**
         * The proportional set size for the native heap.
         */
        public int nativePss;
        /** The proportional set size that is swappable for the native heap. */
        /**
         * We may want to expose this, eventually.
         */

        public int nativeSwappablePss;
        /**
         * The resident set size for the native heap.
         */
        public int nativeRss;
        /**
         * The private dirty pages used by the native heap.
         */
        public int nativePrivateDirty;
        /**
         * The shared dirty pages used by the native heap.
         */
        public int nativeSharedDirty;
        /** The private clean pages used by the native heap. */
        /**
         * We may want to expose this, eventually.
         */

        public int nativePrivateClean;
        /** The shared clean pages used by the native heap. */
        /**
         * We may want to expose this, eventually.
         */

        public int nativeSharedClean;
        /** The dirty native pages that have been swapped out. */
        /**
         * We may want to expose this, eventually.
         */

        public int nativeSwappedOut;
        /** The dirty native pages that have been swapped out, proportional. */
        /**
         * We may want to expose this, eventually.
         */
        public int nativeSwappedOutPss;

        /**
         * The proportional set size for everything else.
         */
        public int otherPss;
        /** The proportional set size that is swappable for everything else. */
        /**
         * We may want to expose this, eventually.
         */

        public int otherSwappablePss;
        /**
         * The resident set size for everything else.
         */
        public int otherRss;
        /**
         * The private dirty pages used by everything else.
         */
        public int otherPrivateDirty;
        /**
         * The shared dirty pages used by everything else.
         */
        public int otherSharedDirty;
        /** The private clean pages used by everything else. */
        /**
         * We may want to expose this, eventually.
         */

        public int otherPrivateClean;
        /** The shared clean pages used by everything else. */
        /**
         * We may want to expose this, eventually.
         */

        public int otherSharedClean;
        /** The dirty pages used by anyting else that have been swapped out. */
        /**
         * We may want to expose this, eventually.
         */

        public int otherSwappedOut;
        /** The dirty pages used by anyting else that have been swapped out, proportional. */
        /**
         * We may want to expose this, eventually.
         */
        public int otherSwappedOutPss;

        /** Whether the kernel reports proportional swap usage */
        /**
         *
         */
        public boolean hasSwappedOutPss;

        // LINT.IfChange
        /**
         *
         */
        public static final int HEAP_UNKNOWN = 0;
        /**
         *
         */
        public static final int HEAP_DALVIK = 1;
        /**
         *
         */
        public static final int HEAP_NATIVE = 2;

        /**
         *
         */
        public static final int OTHER_DALVIK_OTHER = 0;
        /**
         *
         */
        public static final int OTHER_STACK = 1;
        /**
         *
         */
        public static final int OTHER_CURSOR = 2;
        /**
         *
         */
        public static final int OTHER_ASHMEM = 3;
        /**
         *
         */
        public static final int OTHER_GL_DEV = 4;
        /**
         *
         */
        public static final int OTHER_UNKNOWN_DEV = 5;
        /**
         *
         */
        public static final int OTHER_SO = 6;
        /**
         *
         */
        public static final int OTHER_JAR = 7;
        /**
         *
         */
        public static final int OTHER_APK = 8;
        /**
         *
         */
        public static final int OTHER_TTF = 9;
        /**
         *
         */
        public static final int OTHER_DEX = 10;
        /**
         *
         */
        public static final int OTHER_OAT = 11;
        /**
         *
         */
        public static final int OTHER_ART = 12;
        /**
         *
         */
        public static final int OTHER_UNKNOWN_MAP = 13;
        /**
         *
         */
        public static final int OTHER_GRAPHICS = 14;
        /**
         *
         */
        public static final int OTHER_GL = 15;
        /**
         *
         */
        public static final int OTHER_OTHER_MEMTRACK = 16;

        // Needs to be declared here for the DVK_STAT ranges below.
        /**
         *
         */

        public static final int NUM_OTHER_STATS = 17;

        // Dalvik subsections.
        /**
         *
         */
        public static final int OTHER_DALVIK_NORMAL = 17;
        /**
         *
         */
        public static final int OTHER_DALVIK_LARGE = 18;
        /**
         *
         */
        public static final int OTHER_DALVIK_ZYGOTE = 19;
        /**
         *
         */
        public static final int OTHER_DALVIK_NON_MOVING = 20;
        // Section begins and ends for dumpsys, relative to the DALVIK categories.
        /**
         *
         */
        public static final int OTHER_DVK_STAT_DALVIK_START =
                OTHER_DALVIK_NORMAL - NUM_OTHER_STATS;
        /**
         *
         */
        public static final int OTHER_DVK_STAT_DALVIK_END =
                OTHER_DALVIK_NON_MOVING - NUM_OTHER_STATS;

        // Dalvik Other subsections.
        /**
         *
         */
        public static final int OTHER_DALVIK_OTHER_LINEARALLOC = 21;
        /**
         *
         */
        public static final int OTHER_DALVIK_OTHER_ACCOUNTING = 22;
        /**
         *
         */
        public static final int OTHER_DALVIK_OTHER_ZYGOTE_CODE_CACHE = 23;
        /**
         *
         */
        public static final int OTHER_DALVIK_OTHER_APP_CODE_CACHE = 24;
        /**
         *
         */
        public static final int OTHER_DALVIK_OTHER_COMPILER_METADATA = 25;
        /**
         *
         */
        public static final int OTHER_DALVIK_OTHER_INDIRECT_REFERENCE_TABLE = 26;
        /**
         *
         */
        public static final int OTHER_DVK_STAT_DALVIK_OTHER_START =
                OTHER_DALVIK_OTHER_LINEARALLOC - NUM_OTHER_STATS;
        /**
         *
         */
        public static final int OTHER_DVK_STAT_DALVIK_OTHER_END =
                OTHER_DALVIK_OTHER_INDIRECT_REFERENCE_TABLE - NUM_OTHER_STATS;

        // Dex subsections (Boot vdex, App dex, and App vdex).
        /**
         *
         */
        public static final int OTHER_DEX_BOOT_VDEX = 27;
        /**
         *
         */
        public static final int OTHER_DEX_APP_DEX = 28;
        /**
         *
         */
        public static final int OTHER_DEX_APP_VDEX = 29;
        /**
         *
         */
        public static final int OTHER_DVK_STAT_DEX_START = OTHER_DEX_BOOT_VDEX - NUM_OTHER_STATS;
        /**
         *
         */
        public static final int OTHER_DVK_STAT_DEX_END = OTHER_DEX_APP_VDEX - NUM_OTHER_STATS;

        // Art subsections (App image, boot image).
        /**
         *
         */
        public static final int OTHER_ART_APP = 30;
        /**
         *
         */
        public static final int OTHER_ART_BOOT = 31;
        // LINT.ThenChange(/system/memory/libmeminfo/include/meminfo/androidprocheaps.h)
        /**
         *
         */
        public static final int OTHER_DVK_STAT_ART_START = OTHER_ART_APP - NUM_OTHER_STATS;
        public static final int OTHER_DVK_STAT_ART_END = OTHER_ART_BOOT - NUM_OTHER_STATS;
        public static final int NUM_DVK_STATS = OTHER_ART_BOOT + 1 - OTHER_DALVIK_NORMAL;
        public static final int NUM_CATEGORIES = 9;
        public static final int OFFSET_PSS = 0;
        public static final int OFFSET_SWAPPABLE_PSS = 1;
        public static final int OFFSET_RSS = 2;
        public static final int OFFSET_PRIVATE_DIRTY = 3;
        public static final int OFFSET_SHARED_DIRTY = 4;
        public static final int OFFSET_PRIVATE_CLEAN = 5;
        public static final int OFFSET_SHARED_CLEAN = 6;
        public static final int OFFSET_SWAPPED_OUT = 7;
        public static final int OFFSET_SWAPPED_OUT_PSS = 8;


        private int[] otherStats = new int[(NUM_OTHER_STATS + NUM_DVK_STATS) * NUM_CATEGORIES];


        /**
         * Copy contents from another object.
         */
        public void set(Debug.MemoryInfo other) {
            throw new RuntimeException();
        }

        /**
         * Return total PSS memory usage in kB.
         */
        public int getTotalPss() {
            throw new RuntimeException();
        }

        /**
         * Return total PSS memory usage in kB.
         */

        public int getTotalUss() {
            throw new RuntimeException();
        }

        /**
         * Return total PSS memory usage in kB mapping a file of one of the following extension:
         * .so, .jar, .apk, .ttf, .dex, .odex, .oat, .art .
         */
        public int getTotalSwappablePss() {
            throw new RuntimeException();
        }

        /**
         * Return total RSS memory usage in kB.
         */
        public int getTotalRss() {
            throw new RuntimeException();
        }

        /**
         * Return total private dirty memory usage in kB.
         */
        public int getTotalPrivateDirty() {
            throw new RuntimeException();
        }

        /**
         * Return total shared dirty memory usage in kB.
         */
        public int getTotalSharedDirty() {
            throw new RuntimeException();
        }

        /**
         * Return total shared clean memory usage in kB.
         */
        public int getTotalPrivateClean() {
            throw new RuntimeException();
        }

        /**
         * Return total shared clean memory usage in kB.
         */
        public int getTotalSharedClean() {
            throw new RuntimeException();
        }

        /**
         * Return total swapped out memory in kB.
         */
        public int getTotalSwappedOut() {
            throw new RuntimeException();
        }

        /**
         * Return total swapped out memory in kB, proportional.
         */
        public int getTotalSwappedOutPss() {
            throw new RuntimeException();
        }

        /**
         *
         */

        public int getOtherPss(int which) {
            throw new RuntimeException();
        }

        /**
         *
         */
        public int getOtherSwappablePss(int which) {
            throw new RuntimeException();
        }

        /**
         *
         */
        public int getOtherRss(int which) {
            throw new RuntimeException();
        }

        /**
         *
         */

        public int getOtherPrivateDirty(int which) {
            throw new RuntimeException();
        }

        /**
         *
         */

        public int getOtherSharedDirty(int which) {
            throw new RuntimeException();
        }

        /**
         *
         */
        public int getOtherPrivateClean(int which) {
            throw new RuntimeException();
        }

        /**
         *
         */

        public int getOtherPrivate(int which) {
            throw new RuntimeException();
        }

        /**
         *
         */
        public int getOtherSharedClean(int which) {
            throw new RuntimeException();
        }

        /**
         *
         */
        public int getOtherSwappedOut(int which) {
            throw new RuntimeException();
        }

        /**
         *
         */
        public int getOtherSwappedOutPss(int which) {
            throw new RuntimeException();
        }

        /**
         *
         */

        public static String getOtherLabel(int which) {
            switch (which) {
                case OTHER_DALVIK_OTHER:
                    return "Dalvik Other";
                case OTHER_STACK:
                    return "Stack";
                case OTHER_CURSOR:
                    return "Cursor";
                case OTHER_ASHMEM:
                    return "Ashmem";
                case OTHER_GL_DEV:
                    return "Gfx dev";
                case OTHER_UNKNOWN_DEV:
                    return "Other dev";
                case OTHER_SO:
                    return ".so mmap";
                case OTHER_JAR:
                    return ".jar mmap";
                case OTHER_APK:
                    return ".apk mmap";
                case OTHER_TTF:
                    return ".ttf mmap";
                case OTHER_DEX:
                    return ".dex mmap";
                case OTHER_OAT:
                    return ".oat mmap";
                case OTHER_ART:
                    return ".art mmap";
                case OTHER_UNKNOWN_MAP:
                    return "Other mmap";
                case OTHER_GRAPHICS:
                    return "EGL mtrack";
                case OTHER_GL:
                    return "GL mtrack";
                case OTHER_OTHER_MEMTRACK:
                    return "Other mtrack";
                case OTHER_DALVIK_NORMAL:
                    return ".Heap";
                case OTHER_DALVIK_LARGE:
                    return ".LOS";
                case OTHER_DALVIK_ZYGOTE:
                    return ".Zygote";
                case OTHER_DALVIK_NON_MOVING:
                    return ".NonMoving";
                case OTHER_DALVIK_OTHER_LINEARALLOC:
                    return ".LinearAlloc";
                case OTHER_DALVIK_OTHER_ACCOUNTING:
                    return ".GC";
                case OTHER_DALVIK_OTHER_ZYGOTE_CODE_CACHE:
                    return ".ZygoteJIT";
                case OTHER_DALVIK_OTHER_APP_CODE_CACHE:
                    return ".AppJIT";
                case OTHER_DALVIK_OTHER_COMPILER_METADATA:
                    return ".CompilerMetadata";
                case OTHER_DALVIK_OTHER_INDIRECT_REFERENCE_TABLE:
                    return ".IndirectRef";
                case OTHER_DEX_BOOT_VDEX:
                    return ".Boot vdex";
                case OTHER_DEX_APP_DEX:
                    return ".App dex";
                case OTHER_DEX_APP_VDEX:
                    return ".App vdex";
                case OTHER_ART_APP:
                    return ".App art";
                case OTHER_ART_BOOT:
                    return ".Boot art";
                default:
                    return "????";
            }
        }

        public String getMemoryStat(String statName) {
            switch (statName) {
                case "summary.java-heap":
                    return Integer.toString(getSummaryJavaHeap());
                case "summary.native-heap":
                    return Integer.toString(getSummaryNativeHeap());
                case "summary.code":
                    return Integer.toString(getSummaryCode());
                case "summary.stack":
                    return Integer.toString(getSummaryStack());
                case "summary.graphics":
                    return Integer.toString(getSummaryGraphics());
                case "summary.private-other":
                    return Integer.toString(getSummaryPrivateOther());
                case "summary.system":
                    return Integer.toString(getSummarySystem());
                case "summary.total-pss":
                    return Integer.toString(getSummaryTotalPss());
                case "summary.total-swap":
                    return Integer.toString(getSummaryTotalSwap());
                default:
                    return null;
            }
        }

        public Map<String, String> getMemoryStats() {
            Map<String, String> stats = new HashMap<String, String>();
            stats.put("summary.java-heap", Integer.toString(getSummaryJavaHeap()));
            stats.put("summary.native-heap", Integer.toString(getSummaryNativeHeap()));
            stats.put("summary.code", Integer.toString(getSummaryCode()));
            stats.put("summary.stack", Integer.toString(getSummaryStack()));
            stats.put("summary.graphics", Integer.toString(getSummaryGraphics()));
            stats.put("summary.private-other", Integer.toString(getSummaryPrivateOther()));
            stats.put("summary.system", Integer.toString(getSummarySystem()));
            stats.put("summary.total-pss", Integer.toString(getSummaryTotalPss()));
            stats.put("summary.total-swap", Integer.toString(getSummaryTotalSwap()));
            return stats;
        }


        public int getSummaryJavaHeap() {
            throw new RuntimeException();
        }

        /**
         * Pss of Native Heap bytes in KB due to the application.
         * Notes:
         * * Includes private dirty malloc space.
         * * We don't include nativePrivateClean, because there should be no
         * such thing as private clean for the Native Heap.
         */

        public int getSummaryNativeHeap() {
            throw new RuntimeException();
        }

        /**
         * Pss of code and other static resource bytes in KB due to
         * the application.
         */

        public int getSummaryCode() {
            throw new RuntimeException();
        }

        /**
         * Pss in KB of the stack due to the application.
         * Notes:
         * * Includes private dirty stack, which includes both Java and Native
         * stack.
         * * Does not include private clean stack, because there should be no
         * such thing as private clean for the stack.
         */

        public int getSummaryStack() {
            throw new RuntimeException();
        }

        /**
         * Pss in KB of graphics due to the application.
         * Notes:
         * * Includes private Gfx, EGL, and GL.
         * * Warning: These numbers can be misreported by the graphics drivers.
         * * We don't include shared graphics. It may make sense to, because
         * shared graphics are likely buffers due to the application
         * anyway, but it's simpler to implement to just group all shared
         * memory into the System category.
         */

        public int getSummaryGraphics() {
            throw new RuntimeException();
        }

        /**
         * Pss in KB due to the application that haven't otherwise been
         * accounted for.
         */

        public int getSummaryPrivateOther() {
            throw new RuntimeException();
        }

        /**
         * Pss in KB due to the system.
         * Notes:
         * * Includes all shared memory.
         */

        public int getSummarySystem() {
            throw new RuntimeException();
        }

        public int getSummaryJavaHeapRss() {
            throw new RuntimeException();
        }


        public int getSummaryNativeHeapRss() {
            throw new RuntimeException();
        }

        /**
         * Rss of code and other static resource bytes in KB due to
         * the application.
         */
        public int getSummaryCodeRss() {
            throw new RuntimeException();
        }

        public int getSummaryStackRss() {
            throw new RuntimeException();
        }

        public int getSummaryGraphicsRss() {
            throw new RuntimeException();
        }


        public int getSummaryUnknownRss() {
            throw new RuntimeException();
        }

        public int getSummaryTotalPss() {
            throw new RuntimeException();
        }

        public int getSummaryTotalSwap() {
            throw new RuntimeException();
        }

        public int getSummaryTotalSwapPss() {
            throw new RuntimeException();
        }

        public boolean hasSwappedOutPss() {
            return hasSwappedOutPss;
        }
    }
}
