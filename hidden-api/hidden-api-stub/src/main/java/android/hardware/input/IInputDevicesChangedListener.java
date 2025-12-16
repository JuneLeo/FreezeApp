package android.hardware.input;

public interface IInputDevicesChangedListener {
    /* Called when input devices changed, such as a device being added,
     * removed or changing configuration.
     *
     * The parameter is an array of pairs (deviceId, generation) indicating the current
     * device id and generation of all input devices.  The client can determine what
     * has happened by comparing the result to its prior observations.
     */
    void onInputDevicesChanged(int[] deviceIdAndGeneration);
}