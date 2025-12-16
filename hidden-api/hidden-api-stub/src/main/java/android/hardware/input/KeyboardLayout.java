package android.hardware.input;

import android.os.LocaleList;

public final class KeyboardLayout {
    private String mDescriptor;
    private String mLabel;
    private String mCollection;
    private int mPriority;
    private LocaleList mLocales;
    private int mVendorId;
    private int mProductId;

    public String getDescriptor() {
        return mDescriptor;
    }

    /**
     * Gets the keyboard layout descriptive label to show in the user interface.
     * @return The keyboard layout descriptive label.
     */
    public String getLabel() {
        return mLabel;
    }

    /**
     * Gets the name of the collection to which the keyboard layout belongs.  This is
     * the label of the broadcast receiver or application that provided the keyboard layout.
     * @return The keyboard layout collection name.
     */
    public String getCollection() {
        return mCollection;
    }

    /**
     * Gets the locales that this keyboard layout is intended for.
     * This may be empty if a locale has not been assigned to this keyboard layout.
     * @return The keyboard layout's intended locale.
     */
    public LocaleList getLocales() {
        return mLocales;
    }

    /**
     * Gets the vendor ID of the hardware device this keyboard layout is intended for.
     * Returns -1 if this is not specific to any piece of hardware.
     * @return The hardware vendor ID of the keyboard layout's intended device.
     */
    public int getVendorId() {
        return mVendorId;
    }

    /**
     * Gets the product ID of the hardware device this keyboard layout is intended for.
     * Returns -1 if this is not specific to any piece of hardware.
     * @return The hardware product ID of the keyboard layout's intended device.
     */
    public int getProductId() {
        return mProductId;
    }
}