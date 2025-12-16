package android.hardware.input;

import android.graphics.Rect;
import android.hardware.input.InputDeviceIdentifier;
import android.hardware.input.KeyboardLayout;
import android.hardware.input.IInputDevicesChangedListener;
import android.hardware.input.IInputDeviceBatteryListener;
import android.hardware.input.ITabletModeChangedListener;
import android.hardware.input.TouchCalibration;
import android.os.CombinedVibration;
import android.hardware.input.IInputSensorEventListener;
import android.hardware.input.InputSensorInfo;
import android.hardware.lights.Light;
import android.hardware.lights.LightState;
import android.os.IBinder;
import android.os.IVibratorStateListener;
import android.os.VibrationEffect;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.InputMonitor;
import android.view.PointerIcon;
import android.view.VerifiedInputEvent;

import java.util.List;

interface IInputManager {
    // Gets input device information.
    InputDevice getInputDevice(int deviceId);
    int[] getInputDeviceIds();

    // Enable/disable input device.
    boolean isInputDeviceEnabled(int deviceId);
    void enableInputDevice(int deviceId);
    void disableInputDevice(int deviceId);

    // Reports whether the hardware supports the given keys; returns true if successful
    boolean hasKeys(int deviceId, int sourceMask, int[] keyCodes,  boolean[] keyExists);

    // Returns the keyCode produced when pressing the key at the specified location, given the
    // active keyboard layout.
    int getKeyCodeForKeyLocation(int deviceId, int locationKeyCode);

    // Temporarily changes the pointer speed.
    void tryPointerSpeed(int speed);

    // Injects an input event into the system. The caller must have the INJECT_EVENTS permssion.
    // This method exists only for compatibility purposes and may be removed a future release.
    boolean injectInputEvent(InputEvent ev, int mode);

    // Injects an input event into the system. The caller must have the INJECT_EVENTS permission.
    // The caller can target windows owned by a certaUID by providing a valid UID, or by
    // providing {@link android.os.Process#INVALID_UID} to target all windows.
    boolean injectInputEventToTarget(InputEvent ev, int mode, int targetUid);

    VerifiedInputEvent verifyInputEvent(InputEvent ev);

    // Calibrate input device position
    TouchCalibration getTouchCalibrationForInputDevice(String inputDeviceDescriptor, int rotation);
    void setTouchCalibrationForInputDevice(String inputDeviceDescriptor, int rotation,
            TouchCalibration calibration);

    // Keyboard layouts configuration.
    KeyboardLayout[] getKeyboardLayouts();
    KeyboardLayout[] getKeyboardLayoutsForInputDevice(InputDeviceIdentifier identifier);
    KeyboardLayout getKeyboardLayout(String keyboardLayoutDescriptor);
    String getCurrentKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier);
    void setCurrentKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier,
            String keyboardLayoutDescriptor);
    String[] getEnabledKeyboardLayoutsForInputDevice(InputDeviceIdentifier identifier);
    void addKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier,
            String keyboardLayoutDescriptor);
    void removeKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier,
            String keyboardLayoutDescriptor);

    // Registers an input devices changed listener.
    void registerInputDevicesChangedListener(IInputDevicesChangedListener listener);

    // Queries whether the device is currently tablet mode
    int isInTabletMode();
    // Registers a tablet mode change listener
    void registerTabletModeChangedListener(ITabletModeChangedListener listener);

    // Queries whether the device's microphone is muted by switch
    int isMicMuted();

    // Input device vibrator control.
    void vibrate(int deviceId, VibrationEffect effect, IBinder token);
    void vibrateCombined(int deviceId, CombinedVibration vibration, IBinder token);
    void cancelVibrate(int deviceId, IBinder token);
    int[] getVibratorIds(int deviceId);
    boolean isVibrating(int deviceId);
    boolean registerVibratorStateListener(int deviceId, IVibratorStateListener listener);
    boolean unregisterVibratorStateListener(int deviceId, IVibratorStateListener listener);

    // Input device battery query.
    int getBatteryStatus(int deviceId);
    int getBatteryCapacity(int deviceId);

    void setPointerIconType(int typeId);
    void setCustomPointerIcon(PointerIcon icon);

    void requestPointerCapture(IBinder inputChannelToken, boolean enabled);

    /** Create an input monitor for gestures. */
    InputMonitor monitorGestureInput(IBinder token, String name, int displayId);

    // Add a runtime association between the input port and the display port. This overrides any
    // static associations.
    void addPortAssociation(String inputPort, int displayPort);
    // Remove the runtime association between the input port and the display port. Any existing
    // static association for the cleared input port will be restored.
    void removePortAssociation(String inputPort);

    // Add a runtime association between the input device and display.
    void addUniqueIdAssociation(String inputPort, String displayUniqueId);
    // Remove the runtime association between the input device and display.
    void removeUniqueIdAssociation(String inputPort);

    InputSensorInfo[] getSensorList(int deviceId);

    boolean registerSensorListener(IInputSensorEventListener listener);

    void unregisterSensorListener(IInputSensorEventListener listener);

    boolean enableSensor(int deviceId, int sensorType, int samplingPeriodUs,
                int maxBatchReportLatencyUs);

    void disableSensor(int deviceId, int sensorType);

    boolean flushSensor(int deviceId, int sensorType);

    List<Light> getLights(int deviceId);

    LightState getLightState(int deviceId, int lightId);

    void setLightStates(int deviceId, int[] lightIds, LightState[] states, IBinder token);

    void openLightSession(int deviceId, String opPkg, IBinder token);

    void closeLightSession(int deviceId, IBinder token);

    void cancelCurrentTouch();

    void registerBatteryListener(int deviceId, IInputDeviceBatteryListener listener);

    void unregisterBatteryListener(int deviceId, IInputDeviceBatteryListener listener);

    void pilferPointers(IBinder inputChannelToken);
}