package android.net;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.SparseBooleanArray;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Collection of active network statistics. Can contain summary details across
 * all interfaces, or details with per-UID granularity. Internally stores data
 * as a large table, closely matching {@code /proc/} data format. This structure
 * optimizes for rapid in-memory comparison, but consider using
 * {@link NetworkStatsHistory} when persisting.
 *
 * @hide
 */
// @NotThreadSafe
public final class NetworkStats {
    private static final String TAG = "NetworkStats";

    /**
     * {@link #iface} value when interface details unavailable.
     * @hide
     */
     public static final String IFACE_ALL = null;

    /**
     * Virtual network interface for video telephony. This is for VT data usage counting
     * purpose.
     */
    public static final String IFACE_VT = "vt_data0";

    /** {@link #uid} value when UID details unavailable. */
    public static final int UID_ALL = -1;
    /** Special UID value for data usage by tethering. */
    public static final int UID_TETHERING = -5;

    /**
     * {@link #tag} value matching any tag.
     * @hide
     */
    // TODO: Rename TAG_ALL to TAG_ANY.
    public static final int TAG_ALL = -1;
    /** {@link #set} value for all sets combined, not including debug sets. */
    public static final int SET_ALL = -1;
    /** {@link #set} value where background data is accounted. */
    public static final int SET_DEFAULT = 0;
    /** {@link #set} value where foreground data is accounted. */
    public static final int SET_FOREGROUND = 1;
    /**
     * All {@link #set} value greater than SET_DEBUG_START are debug {@link #set} values.
     * @hide
     */
    public static final int SET_DEBUG_START = 1000;
    /**
     * Debug {@link #set} value when the VPN stats are moved in.
     * @hide
     */
    public static final int SET_DBG_VPN_IN = 1001;
    /**
     * Debug {@link #set} value when the VPN stats are moved out of a vpn UID.
     * @hide
     */
    public static final int SET_DBG_VPN_OUT = 1002;

    /** @hide */
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    /**
     * Include all interfaces when filtering
     * @hide
     */
    public  static final String[] INTERFACES_ALL = null;

    /** {@link #tag} value for total data across all tags. */
    public static final int TAG_NONE = 0;

    /** {@link #metered} value to account for all metered states. */
    public static final int METERED_ALL = -1;
    /** {@link #metered} value where native, unmetered data is accounted. */
    public static final int METERED_NO = 0;
    /** {@link #metered} value where metered data is accounted. */
    public static final int METERED_YES = 1;

    /** @hide */
    @Retention(RetentionPolicy.SOURCE)
    public @interface Meteredness {
    }


    /** {@link #roaming} value to account for all roaming states. */
    public static final int ROAMING_ALL = -1;
    /** {@link #roaming} value where native, non-roaming data is accounted. */
    public static final int ROAMING_NO = 0;
    /** {@link #roaming} value where roaming data is accounted. */
    public static final int ROAMING_YES = 1;

    /** @hide */
    @Retention(RetentionPolicy.SOURCE)
    
    public @interface Roaming {
    }

    public static final int DEFAULT_NETWORK_ALL = -1;
    public static final int DEFAULT_NETWORK_NO = 0;
    public static final int DEFAULT_NETWORK_YES = 1;

    /** @hide */
    @Retention(RetentionPolicy.SOURCE)
    public @interface DefaultNetwork {
    }

    /**
     * Denotes a request for stats at the interface level.
     * @hide
     */
    public static final int STATS_PER_IFACE = 0;
    /**
     * Denotes a request for stats at the interface and UID level.
     * @hide
     */
    public static final int STATS_PER_UID = 1;

    /** @hide */
    @Retention(RetentionPolicy.SOURCE)
    public @interface StatsType {
    }

    private static final String CLATD_INTERFACE_PREFIX = "v4-";
    // Delta between IPv4 header (20b) and IPv6 header (40b).
    // Used for correct stats accounting on clatd interfaces.
    private static final int IPV4V6_HEADER_DELTA = 20;

    // TODO: move fields to "mVariable" notation

    
    private long elapsedRealtime;
    private int size;
    private int capacity;
    private String[] iface;
    private int[] uid;
    private int[] set;
    private int[] tag;
    private int[] metered;
    private int[] roaming;
    private int[] defaultNetwork;
    private long[] rxBytes;
    private long[] rxPackets;
    private long[] txBytes;
    private long[] txPackets;
    private long[] operations;

    /**
     * Basic element of network statistics. Contains the number of packets and number of bytes
     * transferred on both directions in a given set of conditions. See
     * {@link Entry#Entry(String, int, int, int, int, int, int, long, long, long, long, long)}.
     *
     * @hide
     */
    
    public static class Entry {
        /** @hide */
        public String iface;
        /** @hide */
        public int uid;
        /** @hide */
        public int set;
        /** @hide */
        public int tag;
        /**
         * Note that this is only populated w/ the default value when read from /proc or written
         * to disk. We merge in the correct value when reporting this value to clients of
         * getSummary().
         * @hide
         */
        public int metered;
        /**
         * Note that this is only populated w/ the default value when read from /proc or written
         * to disk. We merge in the correct value when reporting this value to clients of
         * getSummary().
         * @hide
         */
        public int roaming;
        /**
         * Note that this is only populated w/ the default value when read from /proc or written
         * to disk. We merge in the correct value when reporting this value to clients of
         * getSummary().
         * @hide
         */
        public int defaultNetwork;
        /** @hide */
        public long rxBytes;
        /** @hide */
        public long rxPackets;
        /** @hide */
        public long txBytes;
        /** @hide */
        public long txPackets;
        /** @hide */
        public long operations;

        /** @hide */
        public Entry() {
            this(IFACE_ALL, UID_ALL, SET_DEFAULT, TAG_NONE, METERED_NO, ROAMING_NO,
                    DEFAULT_NETWORK_NO, 0L, 0L, 0L, 0L, 0L);
        }

        /**
         * Construct a {@link Entry} object by giving statistics of packet and byte transferred on
         * both direction, and associated with a set of given conditions.
         *
         * @param iface interface name of this {@link Entry}. Or null if not specified.
         * @param uid uid of this {@link Entry}. {@link #UID_TETHERING} if this {@link Entry} is
         *            for tethering. Or {@link #UID_ALL} if this {@link NetworkStats} is only
         *            counting iface stats.
         * @param set usage state of this {@link Entry}.
         * @param tag tag of this {@link Entry}.
         * @param metered metered state of this {@link Entry}.
         * @param roaming roaming state of this {@link Entry}.
         * @param defaultNetwork default network status of this {@link Entry}.
         * @param rxBytes Number of bytes received for this {@link Entry}. Statistics should
         *                represent the contents of IP packets, including IP headers.
         * @param rxPackets Number of packets received for this {@link Entry}. Statistics should
         *                  represent the contents of IP packets, including IP headers.
         * @param txBytes Number of bytes transmitted for this {@link Entry}. Statistics should
         *                represent the contents of IP packets, including IP headers.
         * @param txPackets Number of bytes transmitted for this {@link Entry}. Statistics should
         *                  represent the contents of IP packets, including IP headers.
         * @param operations count of network operations performed for this {@link Entry}. This can
         *                   be used to derive bytes-per-operation.
         */
        public Entry( String iface, int uid, @State int set, int tag,
                     @Meteredness int metered, @Roaming int roaming, @DefaultNetwork int defaultNetwork,
                     long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
            this.iface = iface;
            this.uid = uid;
            this.set = set;
            this.tag = tag;
            this.metered = metered;
            this.roaming = roaming;
            this.defaultNetwork = defaultNetwork;
            this.rxBytes = rxBytes;
            this.rxPackets = rxPackets;
            this.txBytes = txBytes;
            this.txPackets = txPackets;
            this.operations = operations;
        }

        /** @hide */
        public boolean isNegative() {
            return rxBytes < 0 || rxPackets < 0 || txBytes < 0 || txPackets < 0 || operations < 0;
        }

        /** @hide */
        public boolean isEmpty() {
            return rxBytes == 0 && rxPackets == 0 && txBytes == 0 && txPackets == 0
                    && operations == 0;
        }

        /** @hide */
        public void add(Entry another) {
            this.rxBytes += another.rxBytes;
            this.rxPackets += another.rxPackets;
            this.txBytes += another.txBytes;
            this.txPackets += another.txPackets;
            this.operations += another.operations;
        }

        /**
         * @return interface name of this entry.
         * @hide
         */
         public String getIface() {
            return iface;
        }

        /**
         * @return the uid of this entry.
         */
        public int getUid() {
            return uid;
        }

        /**
         * @return the set state of this entry.
         */
        @State public int getSet() {
            return set;
        }

        /**
         * @return the tag value of this entry.
         */
        public int getTag() {
            return tag;
        }

        /**
         * @return the metered state.
         */
        @Meteredness
        public int getMetered() {
            return metered;
        }

        /**
         * @return the roaming state.
         */
        @Roaming
        public int getRoaming() {
            return roaming;
        }

        /**
         * @return the default network state.
         */
        @DefaultNetwork
        public int getDefaultNetwork() {
            return defaultNetwork;
        }

        /**
         * @return the number of received bytes.
         */
        public long getRxBytes() {
            return rxBytes;
        }

        /**
         * @return the number of received packets.
         */
        public long getRxPackets() {
            return rxPackets;
        }

        /**
         * @return the number of transmitted bytes.
         */
        public long getTxBytes() {
            return txBytes;
        }

        /**
         * @return the number of transmitted packets.
         */
        public long getTxPackets() {
            return txPackets;
        }

        /**
         * @return the count of network operations performed for this entry.
         */
        public long getOperations() {
            return operations;
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("iface=").append(iface);
            builder.append(" uid=").append(uid);
            builder.append(" set=").append(setToString(set));
            builder.append(" tag=").append(tagToString(tag));
            builder.append(" metered=").append(meteredToString(metered));
            builder.append(" roaming=").append(roamingToString(roaming));
            builder.append(" defaultNetwork=").append(defaultNetworkToString(defaultNetwork));
            builder.append(" rxBytes=").append(rxBytes);
            builder.append(" rxPackets=").append(rxPackets);
            builder.append(" txBytes=").append(txBytes);
            builder.append(" txPackets=").append(txPackets);
            builder.append(" operations=").append(operations);
            return builder.toString();
        }

        /** @hide */
        @Override
        public boolean equals( Object o) {
            if (o instanceof Entry) {
                final Entry e = (Entry) o;
                return uid == e.uid && set == e.set && tag == e.tag && metered == e.metered
                        && roaming == e.roaming && defaultNetwork == e.defaultNetwork
                        && rxBytes == e.rxBytes && rxPackets == e.rxPackets
                        && txBytes == e.txBytes && txPackets == e.txPackets
                        && operations == e.operations && TextUtils.equals(iface, e.iface);
            }
            return false;
        }

        /** @hide */
        @Override
        public int hashCode() {
            return Objects.hash(uid, set, tag, metered, roaming, defaultNetwork, iface);
        }
    }

    public NetworkStats(long elapsedRealtime, int initialSize) {
        this.elapsedRealtime = elapsedRealtime;
        this.size = 0;
        if (initialSize > 0) {
            this.capacity = initialSize;
            this.iface = new String[initialSize];
            this.uid = new int[initialSize];
            this.set = new int[initialSize];
            this.tag = new int[initialSize];
            this.metered = new int[initialSize];
            this.roaming = new int[initialSize];
            this.defaultNetwork = new int[initialSize];
            this.rxBytes = new long[initialSize];
            this.rxPackets = new long[initialSize];
            this.txBytes = new long[initialSize];
            this.txPackets = new long[initialSize];
            this.operations = new long[initialSize];
        } else {
            // Special case for use by NetworkStatsFactory to start out *really* empty.
            clear();
        }
    }

    /** @hide */
    public NetworkStats(Parcel parcel) {
        elapsedRealtime = parcel.readLong();
        size = parcel.readInt();
        capacity = parcel.readInt();
        iface = parcel.createStringArray();
        uid = parcel.createIntArray();
        set = parcel.createIntArray();
        tag = parcel.createIntArray();
        metered = parcel.createIntArray();
        roaming = parcel.createIntArray();
        defaultNetwork = parcel.createIntArray();
        rxBytes = parcel.createLongArray();
        rxPackets = parcel.createLongArray();
        txBytes = parcel.createLongArray();
        txPackets = parcel.createLongArray();
        operations = parcel.createLongArray();
    }

    /**
     * Clear all data stored in this object.
     * @hide
     */
    public void clear() {
       
    }

    /** @hide */
    
    public NetworkStats insertEntry(
            String iface, long rxBytes, long rxPackets, long txBytes, long txPackets) {
        return insertEntry(
                iface, UID_ALL, SET_DEFAULT, TAG_NONE, METERED_NO, ROAMING_NO, DEFAULT_NETWORK_NO,
                rxBytes, rxPackets, txBytes, txPackets, 0L);
    }

    /** @hide */
    
    public NetworkStats insertEntry(String iface, int uid, int set, int tag, long rxBytes,
                                    long rxPackets, long txBytes, long txPackets, long operations) {
        return insertEntry(new Entry(
                iface, uid, set, tag,  METERED_NO, ROAMING_NO, DEFAULT_NETWORK_NO,
                rxBytes, rxPackets, txBytes, txPackets, operations));
    }

    /** @hide */
    
    public NetworkStats insertEntry(String iface, int uid, int set, int tag, int metered,
                                    int roaming, int defaultNetwork, long rxBytes, long rxPackets, long txBytes,
                                    long txPackets, long operations) {
        return insertEntry(new Entry(
                iface, uid, set, tag, metered, roaming, defaultNetwork, rxBytes, rxPackets,
                txBytes, txPackets, operations));
    }

    /**
     * Add new stats entry, copying from given {@link Entry}. The {@link Entry}
     * object can be recycled across multiple calls.
     * @hide
     */
    public NetworkStats insertEntry(Entry entry) {
        if (size >= capacity) {
            final int newLength = Math.max(size, 10) * 3 / 2;
            iface = Arrays.copyOf(iface, newLength);
            uid = Arrays.copyOf(uid, newLength);
            set = Arrays.copyOf(set, newLength);
            tag = Arrays.copyOf(tag, newLength);
            metered = Arrays.copyOf(metered, newLength);
            roaming = Arrays.copyOf(roaming, newLength);
            defaultNetwork = Arrays.copyOf(defaultNetwork, newLength);
            rxBytes = Arrays.copyOf(rxBytes, newLength);
            rxPackets = Arrays.copyOf(rxPackets, newLength);
            txBytes = Arrays.copyOf(txBytes, newLength);
            txPackets = Arrays.copyOf(txPackets, newLength);
            operations = Arrays.copyOf(operations, newLength);
            capacity = newLength;
        }

        setValues(size, entry);
        size++;

        return this;
    }

    private void setValues(int i, Entry entry) {
        iface[i] = entry.iface;
        uid[i] = entry.uid;
        set[i] = entry.set;
        tag[i] = entry.tag;
        metered[i] = entry.metered;
        roaming[i] = entry.roaming;
        defaultNetwork[i] = entry.defaultNetwork;
        rxBytes[i] = entry.rxBytes;
        rxPackets[i] = entry.rxPackets;
        txBytes[i] = entry.txBytes;
        txPackets[i] = entry.txPackets;
        operations[i] = entry.operations;
    }

    /**
     * Iterate over Entry objects.
     *
     * Return an iterator of this object that will iterate through all contained Entry objects.
     *
     * This iterator does not support concurrent modification and makes no guarantee of fail-fast
     * behavior. If any method that can mutate the contents of this object is called while
     * iteration is in progress, either inside the loop or in another thread, then behavior is
     * undefined.
     * The remove() method is not implemented and will throw UnsupportedOperationException.
     * @hide
     */
    
     public Iterator<Entry> iterator() {
        return new Iterator<Entry>() {
            int mIndex = 0;

            @Override
            public boolean hasNext() {
                return mIndex < size;
            }

            @Override
            public Entry next() {
                return getValues(mIndex++, null);
            }
        };
    }

    /**
     * Return specific stats entry.
     * @hide
     */
    
    public Entry getValues(int i,  Entry recycle) {
        final Entry entry = recycle != null ? recycle : new Entry();
        entry.iface = iface[i];
        entry.uid = uid[i];
        entry.set = set[i];
        entry.tag = tag[i];
        entry.metered = metered[i];
        entry.roaming = roaming[i];
        entry.defaultNetwork = defaultNetwork[i];
        entry.rxBytes = rxBytes[i];
        entry.rxPackets = rxPackets[i];
        entry.txBytes = txBytes[i];
        entry.txPackets = txPackets[i];
        entry.operations = operations[i];
        return entry;
    }

    /**
     * If @{code dest} is not equal to @{code src}, copy entry from index @{code src} to index
     * @{code dest}.
     */
    private void maybeCopyEntry(int dest, int src) {
        if (dest == src) return;
        iface[dest] = iface[src];
        uid[dest] = uid[src];
        set[dest] = set[src];
        tag[dest] = tag[src];
        metered[dest] = metered[src];
        roaming[dest] = roaming[src];
        defaultNetwork[dest] = defaultNetwork[src];
        rxBytes[dest] = rxBytes[src];
        rxPackets[dest] = rxPackets[src];
        txBytes[dest] = txBytes[src];
        txPackets[dest] = txPackets[src];
        operations[dest] = operations[src];
    }

    /** @hide */
    public long getElapsedRealtime() {
        return elapsedRealtime;
    }

    /** @hide */
    public void setElapsedRealtime(long time) {
        elapsedRealtime = time;
    }

    /**
     * Return age of this {@link NetworkStats} object with respect to
     * {@link SystemClock#elapsedRealtime()}.
     * @hide
     */
    public long getElapsedRealtimeAge() {
        return SystemClock.elapsedRealtime() - elapsedRealtime;
    }

    /** @hide */
    
    public int size() {
        return size;
    }

    /** @hide */
    
    public int internalSize() {
        return capacity;
    }

    /** @hide */
    @Deprecated
    public NetworkStats combineValues(String iface, int uid, int tag, long rxBytes, long rxPackets,
                                      long txBytes, long txPackets, long operations) {
        return combineValues(
                iface, uid, SET_DEFAULT, tag, rxBytes, rxPackets, txBytes,
                txPackets, operations);
    }

    /** @hide */
    public NetworkStats combineValues(String iface, int uid, int set, int tag,
                                      long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
        return combineValues(new Entry(
                iface, uid, set, tag, METERED_NO, ROAMING_NO, DEFAULT_NETWORK_NO,
                rxBytes, rxPackets, txBytes, txPackets, operations));
    }

    /**
     * Combine given values with an existing row, or create a new row if
     * {@link #findIndex(String, int, int, int, int, int, int)} is unable to find match. Can
     * also be used to subtract values from existing rows. This method mutates the referencing
     * {@link NetworkStats} object.
     *
     * @param entry the {@link Entry} to combine.
     * @return a reference to this mutated {@link NetworkStats} object.
     * @hide
     */
    public  NetworkStats combineValues( Entry entry) {
        final int i = findIndex(entry.iface, entry.uid, entry.set, entry.tag, entry.metered,
                entry.roaming, entry.defaultNetwork);
        if (i == -1) {
            // only create new entry when positive contribution
            insertEntry(entry);
        } else {
            rxBytes[i] += entry.rxBytes;
            rxPackets[i] += entry.rxPackets;
            txBytes[i] += entry.txBytes;
            txPackets[i] += entry.txPackets;
            operations[i] += entry.operations;
        }
        return this;
    }

    /**
     * Add given values with an existing row, or create a new row if
     * {@link #findIndex(String, int, int, int, int, int, int)} is unable to find match. Can
     * also be used to subtract values from existing rows.
     *
     * @param entry the {@link Entry} to add.
     * @return a new constructed {@link NetworkStats} object that contains the result.
     */
    public  NetworkStats addEntry( Entry entry) {
        throw new RuntimeException();
    }

    /**
     * Add the given {@link NetworkStats} objects.
     *
     * @return the sum of two objects.
     */
    public  NetworkStats add( NetworkStats another) {
        throw new RuntimeException();
    }

    /**
     * Combine all values from another {@link NetworkStats} into this object.
     * @hide
     */
    public void combineAllValues( NetworkStats another) {
        NetworkStats.Entry entry = null;
        for (int i = 0; i < another.size; i++) {
            entry = another.getValues(i, entry);
            combineValues(entry);
        }
    }

    /**
     * Find first stats index that matches the requested parameters.
     * @hide
     */
    public int findIndex(String iface, int uid, int set, int tag, int metered, int roaming,
                         int defaultNetwork) {
        for (int i = 0; i < size; i++) {
            if (uid == this.uid[i] && set == this.set[i] && tag == this.tag[i]
                    && metered == this.metered[i] && roaming == this.roaming[i]
                    && defaultNetwork == this.defaultNetwork[i]
                    && Objects.equals(iface, this.iface[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Find first stats index that matches the requested parameters, starting
     * search around the hinted index as an optimization.
     * @hide
     */
    
    public int findIndexHinted(String iface, int uid, int set, int tag, int metered, int roaming,
                               int defaultNetwork, int hintIndex) {
        for (int offset = 0; offset < size; offset++) {
            final int halfOffset = offset / 2;

            // search outwards from hint index, alternating forward and backward
            final int i;
            if (offset % 2 == 0) {
                i = (hintIndex + halfOffset) % size;
            } else {
                i = (size + hintIndex - halfOffset - 1) % size;
            }

            if (uid == this.uid[i] && set == this.set[i] && tag == this.tag[i]
                    && metered == this.metered[i] && roaming == this.roaming[i]
                    && defaultNetwork == this.defaultNetwork[i]
                    && Objects.equals(iface, this.iface[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Splice in {@link #operations} from the given {@link NetworkStats} based
     * on matching {@link #uid} and {@link #tag} rows. Ignores {@link #iface},
     * since operation counts are at data layer.
     * @hide
     */
    public void spliceOperationsFrom(NetworkStats stats) {
        for (int i = 0; i < size; i++) {
            final int j = stats.findIndex(iface[i], uid[i], set[i], tag[i], metered[i], roaming[i],
                    defaultNetwork[i]);
            if (j == -1) {
                operations[i] = 0;
            } else {
                operations[i] = stats.operations[j];
            }
        }
    }

    /**
     * Return list of unique interfaces known by this data structure.
     * @hide
     */
    public String[] getUniqueIfaces() {
        final HashSet<String> ifaces = new HashSet<String>();
        for (String iface : this.iface) {
            if (iface != IFACE_ALL) {
                ifaces.add(iface);
            }
        }
        return ifaces.toArray(new String[ifaces.size()]);
    }

    /**
     * Return list of unique UIDs known by this data structure.
     * @hide
     */
    
    public int[] getUniqueUids() {
        final SparseBooleanArray uids = new SparseBooleanArray();
        for (int uid : this.uid) {
            uids.put(uid, true);
        }

        final int size = uids.size();
        final int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = uids.keyAt(i);
        }
        return result;
    }

    /**
     * Return total bytes represented by this snapshot object, usually used when
     * checking if a {@link #subtract(NetworkStats)} delta passes a threshold.
     * @hide
     */
    
    public long getTotalBytes() {
        final Entry entry = getTotal(null);
        return entry.rxBytes + entry.txBytes;
    }

    /**
     * Return total of all fields represented by this snapshot object.
     * @hide
     */
    
    public Entry getTotal(Entry recycle) {
        return getTotal(recycle, null, UID_ALL, false);
    }

    /**
     * Return total of all fields represented by this snapshot object matching
     * the requested {@link #uid}.
     * @hide
     */
    
    public Entry getTotal(Entry recycle, int limitUid) {
        return getTotal(recycle, null, limitUid, false);
    }

    /**
     * Return total of all fields represented by this snapshot object matching
     * the requested {@link #iface}.
     * @hide
     */
    public Entry getTotal(Entry recycle, HashSet<String> limitIface) {
        return getTotal(recycle, limitIface, UID_ALL, false);
    }

    /** @hide */
    
    public Entry getTotalIncludingTags(Entry recycle) {
        return getTotal(recycle, null, UID_ALL, true);
    }

    /**
     * Return total of all fields represented by this snapshot object matching
     * the requested {@link #iface} and {@link #uid}.
     *
     * @param limitIface Set of {@link #iface} to include in total; or {@code
     *            null} to include all ifaces.
     */
    private Entry getTotal(
            Entry recycle, HashSet<String> limitIface, int limitUid, boolean includeTags) {
        final Entry entry = recycle != null ? recycle : new Entry();

        entry.iface = IFACE_ALL;
        entry.uid = limitUid;
        entry.set = SET_ALL;
        entry.tag = TAG_NONE;
        entry.metered = METERED_ALL;
        entry.roaming = ROAMING_ALL;
        entry.defaultNetwork = DEFAULT_NETWORK_ALL;
        entry.rxBytes = 0;
        entry.rxPackets = 0;
        entry.txBytes = 0;
        entry.txPackets = 0;
        entry.operations = 0;

        for (int i = 0; i < size; i++) {
            final boolean matchesUid = (limitUid == UID_ALL) || (limitUid == uid[i]);
            final boolean matchesIface = (limitIface == null) || (limitIface.contains(iface[i]));

            if (matchesUid && matchesIface) {
                // skip specific tags, since already counted in TAG_NONE
                if (tag[i] != TAG_NONE && !includeTags) continue;

                entry.rxBytes += rxBytes[i];
                entry.rxPackets += rxPackets[i];
                entry.txBytes += txBytes[i];
                entry.txPackets += txPackets[i];
                entry.operations += operations[i];
            }
        }
        return entry;
    }

    /**
     * Fast path for battery stats.
     * @hide
     */
    public long getTotalPackets() {
        long total = 0;
        for (int i = size - 1; i >= 0; i--) {
            total += rxPackets[i] + txPackets[i];
        }
        return total;
    }

    /**
     * Subtract the given {@link NetworkStats}, effectively leaving the delta
     * between two snapshots in time. Assumes that statistics rows collect over
     * time, and that none of them have disappeared. This method does not mutate
     * the referencing object.
     *
     * @return the delta between two objects.
     */
    public  NetworkStats subtract( NetworkStats right) {
        return subtract(this, right, null, null);
    }

    /**
     * Subtract the two given {@link NetworkStats} objects, returning the delta
     * between two snapshots in time. Assumes that statistics rows collect over
     * time, and that none of them have disappeared.
     * <p>
     * If counters have rolled backwards, they are clamped to {@code 0} and
     * reported to the given {@link NonMonotonicObserver}.
     * @hide
     */
    public static <C> NetworkStats subtract(NetworkStats left, NetworkStats right,
                                            NonMonotonicObserver<C> observer, C cookie) {
        return subtract(left, right, observer, cookie, null);
    }

    /**
     * Subtract the two given {@link NetworkStats} objects, returning the delta
     * between two snapshots in time. Assumes that statistics rows collect over
     * time, and that none of them have disappeared.
     * <p>
     * If counters have rolled backwards, they are clamped to {@code 0} and
     * reported to the given {@link NonMonotonicObserver}.
     * <p>
     * If <var>recycle</var> is supplied, this NetworkStats object will be
     * reused (and returned) as the result if it is large enough to contain
     * the data.
     * @hide
     */
    public static <C> NetworkStats subtract(NetworkStats left, NetworkStats right,
                                            NonMonotonicObserver<C> observer, C cookie, NetworkStats recycle) {
        long deltaRealtime = left.elapsedRealtime - right.elapsedRealtime;
        if (deltaRealtime < 0) {
            if (observer != null) {
                observer.foundNonMonotonic(left, -1, right, -1, cookie);
            }
            deltaRealtime = 0;
        }

        // result will have our rows, and elapsed time between snapshots
        final Entry entry = new Entry();
        final NetworkStats result;
        if (recycle != null && recycle.capacity >= left.size) {
            result = recycle;
            result.size = 0;
            result.elapsedRealtime = deltaRealtime;
        } else {
            result = new NetworkStats(deltaRealtime, left.size);
        }
        for (int i = 0; i < left.size; i++) {
            entry.iface = left.iface[i];
            entry.uid = left.uid[i];
            entry.set = left.set[i];
            entry.tag = left.tag[i];
            entry.metered = left.metered[i];
            entry.roaming = left.roaming[i];
            entry.defaultNetwork = left.defaultNetwork[i];
            entry.rxBytes = left.rxBytes[i];
            entry.rxPackets = left.rxPackets[i];
            entry.txBytes = left.txBytes[i];
            entry.txPackets = left.txPackets[i];
            entry.operations = left.operations[i];

            // find remote row that matches, and subtract
            final int j = right.findIndexHinted(entry.iface, entry.uid, entry.set, entry.tag,
                    entry.metered, entry.roaming, entry.defaultNetwork, i);
            if (j != -1) {
                // Found matching row, subtract remote value.
                entry.rxBytes -= right.rxBytes[j];
                entry.rxPackets -= right.rxPackets[j];
                entry.txBytes -= right.txBytes[j];
                entry.txPackets -= right.txPackets[j];
                entry.operations -= right.operations[j];
            }

            if (entry.isNegative()) {
                if (observer != null) {
                    observer.foundNonMonotonic(left, i, right, j, cookie);
                }
                entry.rxBytes = Math.max(entry.rxBytes, 0);
                entry.rxPackets = Math.max(entry.rxPackets, 0);
                entry.txBytes = Math.max(entry.txBytes, 0);
                entry.txPackets = Math.max(entry.txPackets, 0);
                entry.operations = Math.max(entry.operations, 0);
            }

            result.insertEntry(entry);
        }

        return result;
    }

    /**
     * Calculate and apply adjustments to captured statistics for 464xlat traffic.
     *
     * <p>This mutates stacked traffic stats, to account for IPv4/IPv6 header size difference.
     *
     * <p>UID stats, which are only accounted on the stacked interface, need to be increased
     * by 20 bytes/packet to account for translation overhead.
     *
     * <p>The potential additional overhead of 8 bytes/packet for ip fragments is ignored.
     *
     * <p>Interface stats need to sum traffic on both stacked and base interface because:
     *   - eBPF offloaded packets appear only on the stacked interface
     *   - Non-offloaded ingress packets appear only on the stacked interface
     *     (due to iptables raw PREROUTING drop rules)
     *   - Non-offloaded egress packets appear only on the stacked interface
     *     (due to ignoring traffic from clat daemon by uid match)
     * (and of course the 20 bytes/packet overhead needs to be applied to stacked interface stats)
     *
     * <p>This method will behave fine if {@code stackedIfaces} is an non-synchronized but add-only
     * {@code ConcurrentHashMap}
     * @param baseTraffic Traffic on the base interfaces. Will be mutated.
     * @param stackedTraffic Stats with traffic stacked on top of our ifaces. Will also be mutated.
     * @param stackedIfaces Mapping ipv6if -> ipv4if interface where traffic is counted on both.
     * @hide
     */
    public static void apply464xlatAdjustments(NetworkStats baseTraffic,
                                               NetworkStats stackedTraffic, Map<String, String> stackedIfaces) {
        // For recycling
        Entry entry = null;
        for (int i = 0; i < stackedTraffic.size; i++) {
            entry = stackedTraffic.getValues(i, entry);
            if (entry == null) continue;
            if (entry.iface == null) continue;
            if (!entry.iface.startsWith(CLATD_INTERFACE_PREFIX)) continue;

            // For 464xlat traffic, per uid stats only counts the bytes of the native IPv4 packet
            // sent on the stacked interface with prefix "v4-" and drops the IPv6 header size after
            // unwrapping. To account correctly for on-the-wire traffic, add the 20 additional bytes
            // difference for all packets (http://b/12249687, http:/b/33681750).
            //
            // Note: this doesn't account for LRO/GRO/GSO/TSO (ie. >mtu) traffic correctly, nor
            // does it correctly account for the 8 extra bytes in the IPv6 fragmentation header.
            //
            // While the ebpf code path does try to simulate proper post segmentation packet
            // counts, we have nothing of the sort of xt_qtaguid stats.
            entry.rxBytes += entry.rxPackets * IPV4V6_HEADER_DELTA;
            entry.txBytes += entry.txPackets * IPV4V6_HEADER_DELTA;
            stackedTraffic.setValues(i, entry);
        }
    }

    /**
     * Calculate and apply adjustments to captured statistics for 464xlat traffic counted twice.
     *
     * <p>This mutates the object this method is called on. Equivalent to calling
     * {@link #apply464xlatAdjustments(NetworkStats, NetworkStats, Map)} with {@code this} as
     * base and stacked traffic.
     * @param stackedIfaces Mapping ipv6if -> ipv4if interface where traffic is counted on both.
     * @hide
     */
    public void apply464xlatAdjustments(Map<String, String> stackedIfaces) {
        apply464xlatAdjustments(this, this, stackedIfaces);
    }

    /**
     * Return total statistics grouped by {@link #iface}; doesn't mutate the
     * original structure.
     * @hide
     */
    public NetworkStats groupedByIface() {
        final NetworkStats stats = new NetworkStats(elapsedRealtime, 10);

        final Entry entry = new Entry();
        entry.uid = UID_ALL;
        entry.set = SET_ALL;
        entry.tag = TAG_NONE;
        entry.metered = METERED_ALL;
        entry.roaming = ROAMING_ALL;
        entry.defaultNetwork = DEFAULT_NETWORK_ALL;
        entry.operations = 0L;

        for (int i = 0; i < size; i++) {
            // skip specific tags, since already counted in TAG_NONE
            if (tag[i] != TAG_NONE) continue;

            entry.iface = iface[i];
            entry.rxBytes = rxBytes[i];
            entry.rxPackets = rxPackets[i];
            entry.txBytes = txBytes[i];
            entry.txPackets = txPackets[i];
            stats.combineValues(entry);
        }

        return stats;
    }

    /**
     * Return total statistics grouped by {@link #uid}; doesn't mutate the
     * original structure.
     * @hide
     */
    public NetworkStats groupedByUid() {
        final NetworkStats stats = new NetworkStats(elapsedRealtime, 10);

        final Entry entry = new Entry();
        entry.iface = IFACE_ALL;
        entry.set = SET_ALL;
        entry.tag = TAG_NONE;
        entry.metered = METERED_ALL;
        entry.roaming = ROAMING_ALL;
        entry.defaultNetwork = DEFAULT_NETWORK_ALL;

        for (int i = 0; i < size; i++) {
            // skip specific tags, since already counted in TAG_NONE
            if (tag[i] != TAG_NONE) continue;

            entry.uid = uid[i];
            entry.rxBytes = rxBytes[i];
            entry.rxPackets = rxPackets[i];
            entry.txBytes = txBytes[i];
            entry.txPackets = txPackets[i];
            entry.operations = operations[i];
            stats.combineValues(entry);
        }

        return stats;
    }

    /**
     * Remove all rows that match one of specified UIDs.
     * This mutates the original structure in place.
     * @hide
     */
    public void removeUids(int[] uids) {
        throw new RuntimeException();
    }

    /**
     * Remove all rows that match one of specified UIDs.
     * @return the result object.
     * @hide
     */
    
    public NetworkStats removeEmptyEntries() {
        throw new RuntimeException();
    }

    /**
     * Removes the interface name from all entries.
     * This mutates the original structure in place.
     * @hide
     */
    public void clearInterfaces() {
        for (int i = 0; i < size; i++) {
            iface[i] = null;
        }
    }

    /**
     * Only keep entries that match all specified filters.
     *
     * <p>This mutates the original structure in place. After this method is called,
     * size is the number of matching entries, and capacity is the previous capacity.
     * @param limitUid UID to filter for, or {@link #UID_ALL}.
     * @param limitIfaces Interfaces to filter for, or {@link #INTERFACES_ALL}.
     * @param limitTag Tag to filter for, or {@link #TAG_ALL}.
     * @hide
     */
    public void filter(int limitUid, String[] limitIfaces, int limitTag) {
        throw new RuntimeException();
    }

    /**
     * Only keep entries with {@link #set} value less than {@link #SET_DEBUG_START}.
     *
     * <p>This mutates the original structure in place.
     * @hide
     */
    public void filterDebugEntries() {
        filter(e -> e.set < SET_DEBUG_START);
    }

    private void filter(Predicate<Entry> predicate) {
        Entry entry = new Entry();
        int nextOutputEntry = 0;
        for (int i = 0; i < size; i++) {
            entry = getValues(i, entry);
            if (predicate.test(entry)) {
                if (nextOutputEntry != i) {
                    setValues(nextOutputEntry, entry);
                }
                nextOutputEntry++;
            }
        }
        size = nextOutputEntry;
    }

    /** @hide */
    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("NetworkStats: elapsedRealtime="); pw.println(elapsedRealtime);
        for (int i = 0; i < size; i++) {
            pw.print(prefix);
            pw.print("  ["); pw.print(i); pw.print("]");
            pw.print(" iface="); pw.print(iface[i]);
            pw.print(" uid="); pw.print(uid[i]);
            pw.print(" set="); pw.print(setToString(set[i]));
            pw.print(" tag="); pw.print(tagToString(tag[i]));
            pw.print(" metered="); pw.print(meteredToString(metered[i]));
            pw.print(" roaming="); pw.print(roamingToString(roaming[i]));
            pw.print(" defaultNetwork="); pw.print(defaultNetworkToString(defaultNetwork[i]));
            pw.print(" rxBytes="); pw.print(rxBytes[i]);
            pw.print(" rxPackets="); pw.print(rxPackets[i]);
            pw.print(" txBytes="); pw.print(txBytes[i]);
            pw.print(" txPackets="); pw.print(txPackets[i]);
            pw.print(" operations="); pw.println(operations[i]);
        }
    }

    /**
     * Return text description of {@link #set} value.
     * @hide
     */
    public static String setToString(int set) {
        switch (set) {
            case SET_ALL:
                return "ALL";
            case SET_DEFAULT:
                return "DEFAULT";
            case SET_FOREGROUND:
                return "FOREGROUND";
            case SET_DBG_VPN_IN:
                return "DBG_VPN_IN";
            case SET_DBG_VPN_OUT:
                return "DBG_VPN_OUT";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Return text description of {@link #set} value.
     * @hide
     */
    public static String setToCheckinString(int set) {
        switch (set) {
            case SET_ALL:
                return "all";
            case SET_DEFAULT:
                return "def";
            case SET_FOREGROUND:
                return "fg";
            case SET_DBG_VPN_IN:
                return "vpnin";
            case SET_DBG_VPN_OUT:
                return "vpnout";
            default:
                return "unk";
        }
    }

    /**
     * @return true if the querySet matches the dataSet.
     * @hide
     */
    public static boolean setMatches(int querySet, int dataSet) {
        if (querySet == dataSet) {
            return true;
        }
        // SET_ALL matches all non-debugging sets.
        return querySet == SET_ALL && dataSet < SET_DEBUG_START;
    }

    /**
     * Return text description of {@link #tag} value.
     * @hide
     */
    public static String tagToString(int tag) {
        return "0x" + Integer.toHexString(tag);
    }

    /**
     * Return text description of {@link #metered} value.
     * @hide
     */
    public static String meteredToString(int metered) {
        switch (metered) {
            case METERED_ALL:
                return "ALL";
            case METERED_NO:
                return "NO";
            case METERED_YES:
                return "YES";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Return text description of {@link #roaming} value.
     * @hide
     */
    public static String roamingToString(int roaming) {
        switch (roaming) {
            case ROAMING_ALL:
                return "ALL";
            case ROAMING_NO:
                return "NO";
            case ROAMING_YES:
                return "YES";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Return text description of {@link #defaultNetwork} value.
     * @hide
     */
    public static String defaultNetworkToString(int defaultNetwork) {
        switch (defaultNetwork) {
            case DEFAULT_NETWORK_ALL:
                return "ALL";
            case DEFAULT_NETWORK_NO:
                return "NO";
            case DEFAULT_NETWORK_YES:
                return "YES";
            default:
                return "UNKNOWN";
        }
    }

    /** @hide */
    @Override
    public String toString() {
        final CharArrayWriter writer = new CharArrayWriter();
        dump("", new PrintWriter(writer));
        return writer.toString();
    }

    /** @hide */
    public interface NonMonotonicObserver<C> {
        public void foundNonMonotonic(
                NetworkStats left, int leftIndex, NetworkStats right, int rightIndex, C cookie);
        public void foundNonMonotonic(
                NetworkStats stats, int statsIndex, C cookie);
    }

    /**
     * VPN accounting. Move some VPN's underlying traffic to other UIDs that use tun0 iface.
     *
     * <p>This method should only be called on delta NetworkStats. Do not call this method on a
     * snapshot {@link NetworkStats} object because the tunUid and/or the underlyingIface may change
     * over time.
     *
     * <p>This method performs adjustments for one active VPN package and one VPN iface at a time.
     *
     * @param tunUid uid of the VPN application
     * @param tunIface iface of the vpn tunnel
     * @param underlyingIfaces underlying network ifaces used by the VPN application
     * @hide
     */
    public void migrateTun(int tunUid,  String tunIface,
                            List<String> underlyingIfaces) {
        // Combined usage by all apps using VPN.
        final Entry tunIfaceTotal = new Entry();
        // Usage by VPN, grouped by its {@code underlyingIfaces}.
        final Entry[] perInterfaceTotal = new Entry[underlyingIfaces.size()];
        // Usage by VPN, summed across all its {@code underlyingIfaces}.
        final Entry underlyingIfacesTotal = new Entry();

        for (int i = 0; i < perInterfaceTotal.length; i++) {
            perInterfaceTotal[i] = new Entry();
        }

        tunAdjustmentInit(tunUid, tunIface, underlyingIfaces, tunIfaceTotal, perInterfaceTotal,
                underlyingIfacesTotal);

        // If tunIface < underlyingIfacesTotal, it leaves the overhead traffic in the VPN app.
        // If tunIface > underlyingIfacesTotal, the VPN app doesn't get credit for data compression.
        // Negative stats should be avoided.
        final Entry[] moved =
                addTrafficToApplications(tunUid, tunIface, underlyingIfaces, tunIfaceTotal,
                        perInterfaceTotal, underlyingIfacesTotal);
        deductTrafficFromVpnApp(tunUid, underlyingIfaces, moved);
    }

    /**
     * Initializes the data used by the migrateTun() method.
     *
     * <p>This is the first pass iteration which does the following work:
     *
     * <ul>
     *   <li>Adds up all the traffic through the tunUid's underlyingIfaces (both foreground and
     *       background).
     *   <li>Adds up all the traffic through tun0 excluding traffic from the vpn app itself.
     * </ul>
     *
     * @param tunUid uid of the VPN application
     * @param tunIface iface of the vpn tunnel
     * @param underlyingIfaces underlying network ifaces used by the VPN application
     * @param tunIfaceTotal output parameter; combined data usage by all apps using VPN
     * @param perInterfaceTotal output parameter; data usage by VPN app, grouped by its {@code
     *     underlyingIfaces}
     * @param underlyingIfacesTotal output parameter; data usage by VPN, summed across all of its
     *     {@code underlyingIfaces}
     */
    private void tunAdjustmentInit(int tunUid,  String tunIface,
                                    List<String> underlyingIfaces,  Entry tunIfaceTotal,
                                    Entry[] perInterfaceTotal,  Entry underlyingIfacesTotal) {
        final Entry recycle = new Entry();
        for (int i = 0; i < size; i++) {
            getValues(i, recycle);
            if (recycle.uid == UID_ALL) {
                throw new IllegalStateException(
                        "Cannot adjust VPN accounting on an iface aggregated NetworkStats.");
            }
            if (recycle.set == SET_DBG_VPN_IN || recycle.set == SET_DBG_VPN_OUT) {
                throw new IllegalStateException(
                        "Cannot adjust VPN accounting on a NetworkStats containing SET_DBG_VPN_*");
            }
            if (recycle.tag != TAG_NONE) {
                // TODO(b/123666283): Take all tags for tunUid into account.
                continue;
            }

            if (tunUid == Process.SYSTEM_UID) {
                // Kernel-based VPN or VCN, traffic sent by apps on the VPN/VCN network
                //
                // Since the data is not UID-accounted on underlying networks, just use VPN/VCN
                // network usage as ground truth. Encrypted traffic on the underlying networks will
                // never be processed here because encrypted traffic on the underlying interfaces
                // is not present in UID stats, and this method is only called on UID stats.
                if (tunIface.equals(recycle.iface)) {
                    tunIfaceTotal.add(recycle);
                    underlyingIfacesTotal.add(recycle);

                    // In steady state, there should always be one network, but edge cases may
                    // result in the network being null (network lost), and thus no underlying
                    // ifaces is possible.
                    if (perInterfaceTotal.length > 0) {
                        // While platform VPNs and VCNs have exactly one underlying network, that
                        // network may have multiple interfaces (eg for 464xlat). This layer does
                        // not have the required information to identify which of the interfaces
                        // were used. Select "any" of the interfaces. Since overhead is already
                        // lost, this number is an approximation anyways.
                        perInterfaceTotal[0].add(recycle);
                    }
                }
            } else if (recycle.uid == tunUid) {
                // VpnService VPN, traffic sent by the VPN app over underlying networks
                for (int j = 0; j < underlyingIfaces.size(); j++) {
                    if (Objects.equals(underlyingIfaces.get(j), recycle.iface)) {
                        perInterfaceTotal[j].add(recycle);
                        underlyingIfacesTotal.add(recycle);
                        break;
                    }
                }
            } else if (tunIface.equals(recycle.iface)) {
                // VpnService VPN; traffic sent by apps on the VPN network
                tunIfaceTotal.add(recycle);
            }
        }
    }

    /**
     * Distributes traffic across apps that are using given {@code tunIface}, and returns the total
     * traffic that should be moved off of {@code tunUid} grouped by {@code underlyingIfaces}.
     *
     * @param tunUid uid of the VPN application
     * @param tunIface iface of the vpn tunnel
     * @param underlyingIfaces underlying network ifaces used by the VPN application
     * @param tunIfaceTotal combined data usage across all apps using {@code tunIface}
     * @param perInterfaceTotal data usage by VPN app, grouped by its {@code underlyingIfaces}
     * @param underlyingIfacesTotal data usage by VPN, summed across all of its {@code
     *     underlyingIfaces}
     */
    private Entry[] addTrafficToApplications(int tunUid,  String tunIface,
                                              List<String> underlyingIfaces,  Entry tunIfaceTotal,
                                              Entry[] perInterfaceTotal,  Entry underlyingIfacesTotal) {
        throw new RuntimeException();
    }

    private void deductTrafficFromVpnApp(
            int tunUid,
             List<String> underlyingIfaces,
             Entry[] moved) {
        if (tunUid == Process.SYSTEM_UID) {
            // No traffic recorded on a per-UID basis for in-kernel VPN/VCNs over underlying
            // networks; thus no traffic to deduct.
            return;
        }

        for (int i = 0; i < underlyingIfaces.size(); i++) {
            moved[i].uid = tunUid;
            // Add debug info
            moved[i].set = SET_DBG_VPN_OUT;
            moved[i].tag = TAG_NONE;
            moved[i].iface = underlyingIfaces.get(i);
            moved[i].metered = METERED_ALL;
            moved[i].roaming = ROAMING_ALL;
            moved[i].defaultNetwork = DEFAULT_NETWORK_ALL;
            combineValues(moved[i]);

            // Caveat: if the vpn software uses tag, the total tagged traffic may be greater than
            // the TAG_NONE traffic.
            //
            // Relies on the fact that the underlying traffic only has state ROAMING_NO and
            // METERED_NO, which should be the case as it comes directly from the /proc file.
            // We only blend in the roaming data after applying these adjustments, by checking the
            // NetworkIdentity of the underlying iface.
            final int idxVpnBackground = findIndex(underlyingIfaces.get(i), tunUid, SET_DEFAULT,
                    TAG_NONE, METERED_NO, ROAMING_NO, DEFAULT_NETWORK_NO);
            if (idxVpnBackground != -1) {
                // Note - tunSubtract also updates moved[i]; whatever traffic that's left is removed
                // from foreground usage.
                tunSubtract(idxVpnBackground, this, moved[i]);
            }

            final int idxVpnForeground = findIndex(underlyingIfaces.get(i), tunUid, SET_FOREGROUND,
                    TAG_NONE, METERED_NO, ROAMING_NO, DEFAULT_NETWORK_NO);
            if (idxVpnForeground != -1) {
                tunSubtract(idxVpnForeground, this, moved[i]);
            }
        }
    }

    private static void tunSubtract(int i,  NetworkStats left,  Entry right) {
        long rxBytes = Math.min(left.rxBytes[i], right.rxBytes);
        left.rxBytes[i] -= rxBytes;
        right.rxBytes -= rxBytes;

        long rxPackets = Math.min(left.rxPackets[i], right.rxPackets);
        left.rxPackets[i] -= rxPackets;
        right.rxPackets -= rxPackets;

        long txBytes = Math.min(left.txBytes[i], right.txBytes);
        left.txBytes[i] -= txBytes;
        right.txBytes -= txBytes;

        long txPackets = Math.min(left.txPackets[i], right.txPackets);
        left.txPackets[i] -= txPackets;
        right.txPackets -= txPackets;
    }
}