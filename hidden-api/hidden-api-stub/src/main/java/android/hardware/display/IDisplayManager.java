package android.hardware.display;

import android.content.pm.ParceledListSlice;
import android.graphics.Point;
import android.media.projection.IMediaProjection;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.Surface;

public interface IDisplayManager extends IInterface {


    DisplayInfo getDisplayInfo(int displayId);
    int[] getDisplayIds(boolean includeDisabled);

    boolean isUidPresentOnDisplay(int uid, int displayId);

    void registerCallback(IDisplayManagerCallback callback);
    void registerCallbackWithEventMask(IDisplayManagerCallback callback, long eventsMask);

    // Requires CONFIGURE_WIFI_DISPLAY permission.
    // The process must have previously registered a callback.
    void startWifiDisplayScan();

    // Requires CONFIGURE_WIFI_DISPLAY permission.
    void stopWifiDisplayScan();

    // Requires CONFIGURE_WIFI_DISPLAY permission.
    void connectWifiDisplay(String address);

    // No permissions required.
    void disconnectWifiDisplay();

    // Requires CONFIGURE_WIFI_DISPLAY permission.
    void renameWifiDisplay(String address, String alias);

    // Requires CONFIGURE_WIFI_DISPLAY permission.
    void forgetWifiDisplay(String address);

    // Requires CONFIGURE_WIFI_DISPLAY permission.
    void pauseWifiDisplay();

    // Requires CONFIGURE_WIFI_DISPLAY permission.
    void resumeWifiDisplay();

    // No permissions required.
    WifiDisplayStatus getWifiDisplayStatus();

    // Requires WRITE_SECURE_SETTINGS permission.
    void setUserDisabledHdrTypes(int[] userDisabledTypes);

    // Requires WRITE_SECURE_SETTINGS permission.
    void setAreUserDisabledHdrTypesAllowed(boolean areUserDisabledHdrTypesAllowed);

    // No permissions required.
    boolean areUserDisabledHdrTypesAllowed();

    // No permissions required.
    int[] getUserDisabledHdrTypes();

    // Requires CONFIGURE_DISPLAY_COLOR_MODE
    void requestColorMode(int displayId, int colorMode);

    // Requires CAPTURE_VIDEO_OUTPUT, CAPTURE_SECURE_VIDEO_OUTPUT, or an appropriate
    // MediaProjection token for certain combinations of flags.
    int createVirtualDisplay(VirtualDisplayConfig virtualDisplayConfig,
                             IVirtualDisplayCallback callback, IMediaProjection projectionToken,
                             String packageName);

    // No permissions required, but must be same Uid as the creator.
    void resizeVirtualDisplay(IVirtualDisplayCallback token,
                              int width, int height, int densityDpi);

    // No permissions required but must be same Uid as the creator.
    void setVirtualDisplaySurface(IVirtualDisplayCallback token, Surface surface);

    // No permissions required but must be same Uid as the creator.
    void releaseVirtualDisplay(IVirtualDisplayCallback token);

    // No permissions required but must be same Uid as the creator.
    void setVirtualDisplayState(IVirtualDisplayCallback token, boolean isOn);

    // Get a stable metric for the device's display size. No permissions required.
    Point getStableDisplaySize();

    // Requires BRIGHTNESS_SLIDER_USAGE permission.
    ParceledListSlice getBrightnessEvents(String callingPackage);

    // Requires ACCESS_AMBIENT_LIGHT_STATS permission.
    ParceledListSlice getAmbientBrightnessStats();

    // Sets the global brightness configuration for a given user. Requires
    // CONFIGURE_DISPLAY_BRIGHTNESS, and INTERACT_ACROSS_USER if the user being configured is not
    // the same as the calling user.
    void setBrightnessConfigurationForUser(BrightnessConfiguration c, int userId,
                                           String packageName);

    // Sets the global brightness configuration for a given display. Requires
    // CONFIGURE_DISPLAY_BRIGHTNESS.
    void setBrightnessConfigurationForDisplay(BrightnessConfiguration c, String uniqueDisplayId,
                                              int userId, String packageName);

    // Gets the brightness configuration for a given display. Requires
    // CONFIGURE_DISPLAY_BRIGHTNESS.
    BrightnessConfiguration getBrightnessConfigurationForDisplay(String uniqueDisplayId,
                                                                 int userId);

    // Gets the global brightness configuration for a given user. Requires
    // CONFIGURE_DISPLAY_BRIGHTNESS, and INTERACT_ACROSS_USER if the user is not
    // the same as the calling user.
    BrightnessConfiguration getBrightnessConfigurationForUser(int userId);

    // Gets the default brightness configuration if configured.
    BrightnessConfiguration getDefaultBrightnessConfiguration();

    // Gets the last requested minimal post processing settings for display with displayId.
    boolean isMinimalPostProcessingRequested(int displayId);

    // Temporarily sets the display brightness.
    void setTemporaryBrightness(int displayId, float brightness);

    // Saves the display brightness.
    void setBrightness(int displayId, float brightness);

    // Retrieves the display brightness.
    float getBrightness(int displayId);

    // Temporarily sets the auto brightness adjustment factor.
    void setTemporaryAutoBrightnessAdjustment(float adjustment);

    // Get the minimum brightness curve.
//    Curve getMinimumBrightnessCurve();

    // Get Brightness Information for the specified display.
    BrightnessInfo getBrightnessInfo(int displayId);

    // Gets the id of the preferred wide gamut color space for all displays.
    // The wide gamut color space is returned from composition pipeline
    // based on hardware capability.
    int getPreferredWideGamutColorSpaceId();

    // Sets the user preferred display mode.
    // Requires MODIFY_USER_PREFERRED_DISPLAY_MODE permission.
    void setUserPreferredDisplayMode(int displayId, Display.Mode mode);
    Display.Mode getUserPreferredDisplayMode(int displayId);
    Display.Mode getSystemPreferredDisplayMode(int displayId);

    // When enabled the app requested display resolution and refresh rate is always selected
    // in DisplayModeDirector regardless of user settings and policies for low brightness, low
    // battery etc.
    void setShouldAlwaysRespectAppRequestedMode(boolean enabled);
    boolean shouldAlwaysRespectAppRequestedMode();

    // Sets the refresh rate switching type.
    void setRefreshRateSwitchingType(int newValue);

    // Returns the refresh rate switching type.
    int getRefreshRateSwitchingType();

    // Query for DISPLAY_DECORATION support.
//    DisplayDecorationSupport getDisplayDecorationSupport(int displayId);


    abstract class Stub extends Binder implements IDisplayManager {

        public static IDisplayManager asInterface(IBinder binder) {
            throw new RuntimeException();
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException();
        }
    }
}
