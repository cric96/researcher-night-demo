package it.unibo.artificial_vision_tracking.aruco_markers;

public class DefaultCameraSettings {
    public static double[] getCameraMatrix() {
        return new double[] { 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0 };
    }
}
