package android.hardware.display;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

public interface IVirtualDisplayCallback extends IInterface {

    /**
     * Called when the virtual display video projection has been
     * paused by the system or when the surface has been detached
     * by the application by calling setSurface(null).
     * The surface will not receive any more buffers while paused.
     */
    void onPaused();

    /**
     * Called when the virtual display video projection has been
     * resumed after having been paused.
     */
    void onResumed();

    /**
     * Called when the virtual display video projection has been
     * stopped by the system.  It will no longer receive frames
     * and it will never be resumed.  It is still the responsibility
     * of the application to release() the virtual display.
     */
    void onStopped();

    abstract class Stub extends Binder implements IVirtualDisplayCallback {

        public static IVirtualDisplayCallback asInterface(IBinder binder) {
            throw new RuntimeException();
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException();
        }
    }
}
