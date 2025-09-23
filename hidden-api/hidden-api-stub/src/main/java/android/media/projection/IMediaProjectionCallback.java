package android.media.projection;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

public interface IMediaProjectionCallback extends IInterface {

    void onStop();

    abstract class Stub extends Binder implements IMediaProjectionCallback {

        public static IMediaProjectionCallback asInterface(IBinder binder) {
            throw new RuntimeException();
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException();
        }
    }
}
