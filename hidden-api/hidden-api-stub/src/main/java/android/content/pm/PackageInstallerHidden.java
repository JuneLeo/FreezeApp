package android.content.pm;

import android.content.Context;

import androidx.annotation.RequiresApi;


public class PackageInstallerHidden {

    public PackageInstallerHidden(Context context, PackageManager pm, IPackageInstaller installer, String installerPackageName, int userId) {
        throw new RuntimeException();
    }

    @RequiresApi(26)
    public PackageInstallerHidden(IPackageInstaller installer, String installerPackageName, int userId) {
        throw new RuntimeException();
    }

    @RequiresApi(31)
    public PackageInstallerHidden(IPackageInstaller installer, String installerPackageName, String installerAttributionTag, int userId) {
        throw new RuntimeException();
    }

    public static class SessionHidden {
        public SessionHidden(IPackageInstallerSession session) {
            throw new RuntimeException();
        }
    }

    public static class SessionParamsHidden {
        public int installFlags;
    }
}
