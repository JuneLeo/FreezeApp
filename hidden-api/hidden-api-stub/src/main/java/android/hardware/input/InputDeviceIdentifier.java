package android.hardware.input;

public final class InputDeviceIdentifier  {
    private String mDescriptor;
    private int mVendorId;
    private int mProductId;


    public String getDescriptor() {
        return mDescriptor;
    }

    public int getVendorId() {
        return mVendorId;
    }

    public int getProductId() {
        return mProductId;
    }


}