package com.john.freezeapp.daemon.memory;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class MemoryData implements Parcelable{
    public int mPid;
    public String mPackageName;
    public long mJavaHeapPssSize;
    public long mNativeHeapPssSize;
    public long mCodePssSize;
    public long mStackPssSize;
    public long mGraphicsPssSize;
    public long mPrivateOtherPssSize;
    public long mSystemPssSize;
    public long mTotalPssSize;
    public long mTotalSwapPssSize;

    public MemoryData() {
    }


    protected MemoryData(Parcel in) {
        mPid = in.readInt();
        mPackageName = in.readString();
        mJavaHeapPssSize = in.readLong();
        mNativeHeapPssSize = in.readLong();
        mCodePssSize = in.readLong();
        mStackPssSize = in.readLong();
        mGraphicsPssSize = in.readLong();
        mPrivateOtherPssSize = in.readLong();
        mSystemPssSize = in.readLong();
        mTotalPssSize = in.readLong();
        mTotalSwapPssSize = in.readLong();
    }

    public static final Creator<MemoryData> CREATOR = new Creator<MemoryData>() {
        @Override
        public MemoryData createFromParcel(Parcel in) {
            return new MemoryData(in);
        }

        @Override
        public MemoryData[] newArray(int size) {
            return new MemoryData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mPid);
        dest.writeString(mPackageName);
        dest.writeLong(mJavaHeapPssSize);
        dest.writeLong(mNativeHeapPssSize);
        dest.writeLong(mCodePssSize);
        dest.writeLong(mStackPssSize);
        dest.writeLong(mGraphicsPssSize);
        dest.writeLong(mPrivateOtherPssSize);
        dest.writeLong(mSystemPssSize);
        dest.writeLong(mTotalPssSize);
        dest.writeLong(mTotalSwapPssSize);
    }
}
