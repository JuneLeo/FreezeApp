package android.hardware.input;

public interface IInputDeviceBatteryListener {

    /**
     * Called when there is a change in battery state for a monitored device. This will be called
     * immediately after the listener is successfully registered for a new device via IInputManager.
     * The parameters are values exposed through {@link android.hardware.BatteryState}.
     */
    void onBatteryStateChanged(int deviceId, boolean isBatteryPresent, int status, float capacity,
                               long eventTime);
}
