package android.hardware.input;

public interface IInputSensorEventListener {
    /* Called when there is a new sensor event. */
    void onInputSensorChanged(int deviceId, int sensorId, int accuracy, long timestamp,
                              float[] values);

    /* Called when the accuracy of the registered sensor has changed. */
    void onInputSensorAccuracyChanged(int deviceId, int sensorId, int accuracy);
}