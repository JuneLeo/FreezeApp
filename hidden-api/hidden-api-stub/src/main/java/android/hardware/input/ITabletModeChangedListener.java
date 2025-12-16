package android.hardware.input;

public interface ITabletModeChangedListener {
    /* Called when the device enters or exits tablet mode. */
    void onTabletModeChanged(long whenNanos, boolean inTabletMode);
}