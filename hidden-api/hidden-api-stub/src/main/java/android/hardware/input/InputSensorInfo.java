package android.hardware.input;

public class InputSensorInfo {

    private String mName;
    private String mVendor;
    private int mVersion;
    private int mHandle;
    private int mType;
    private float mMaxRange;
    private float mResolution;
    private float mPower;
    private int mMinDelay;
    private int mFifoReservedEventCount;
    private int mFifoMaxEventCount;
    private String mStringType;
    private String mRequiredPermission;
    private int mMaxDelay;
    private int mFlags;
    private int mId;


    public String getName() {
        return mName;
    }


    public String getVendor() {
        return mVendor;
    }


    public int getVersion() {
        return mVersion;
    }


    public int getHandle() {
        return mHandle;
    }


    public int getType() {
        return mType;
    }


    public float getMaxRange() {
        return mMaxRange;
    }


    public float getResolution() {
        return mResolution;
    }


    public float getPower() {
        return mPower;
    }


    public int getMinDelay() {
        return mMinDelay;
    }


    public int getFifoReservedEventCount() {
        return mFifoReservedEventCount;
    }


    public int getFifoMaxEventCount() {
        return mFifoMaxEventCount;
    }


    public String getStringType() {
        return mStringType;
    }


    public String getRequiredPermission() {
        return mRequiredPermission;
    }


    public int getMaxDelay() {
        return mMaxDelay;
    }


    public int getFlags() {
        return mFlags;
    }


    public int getId() {
        return mId;
    }

}