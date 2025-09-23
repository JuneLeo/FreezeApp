package android.media.projection;

import android.os.IBinder;
import android.os.IInterface;

public interface IMediaProjection extends IInterface {

    void start(IMediaProjectionCallback callback);
    void stop();
    boolean canProjectAudio();
    boolean canProjectVideo();
    boolean canProjectSecureVideo();
    int applyVirtualDisplayFlags(int flags);
    void registerCallback(IMediaProjectionCallback callback);
    void unregisterCallback(IMediaProjectionCallback callback);

    /**
     * Returns the {@link android.os.IBinder} identifying the task to record, or {@code null} if
     * there is none.
     */
    IBinder getLaunchCookie();

    /**
     * Updates the {@link android.os.IBinder} identifying the task to record, or {@code null} if
     * there is none.
     */
    void setLaunchCookie(IBinder launchCookie);
}
