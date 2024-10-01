package it.unibo.artificial_vision_tracking.aruco_markers;

public final class Costants {

    static final String MS_STRING = " ms";

    //Costants for the ArucoDetector
    static final int ADAPTIVE_THRESH_WIN_SIZE_MIN = 3;
    static final int ADAPTIVE_THRESH_WIN_SIZE_MAX = 23;
    static final int ADAPTIVE_THRESH_WIN_SIZE_STEP = 10;
    static final int ADAPTIVE_THRESH_CONSTANT = 7;
    static final int MARKER_BORDER_BITS = 1;
    static final int PERSPECTIVE_REMOVE_PIXEL_PER_CELL = 16;
    static final double PERSPECTIVE_REMOVE_IGNORED_MARGIN_PER_CELL = 0.1;

    //Costants for the camera
    static final int CAMERA_EXPORSURE = -2;

    //Costants for the calcPose methods
    static final int CORNER_NUMBER = 4;
    static final int SECOND_IN_MILLIS = 1000;

    //Costants for the drawAxes method
    static final int X_NEGATIVE_DELTA = -50;
    static final int Y_POSITIVE_DELTA = 75;
    static final int Y_NEGATIVE_DELTA = -15;

    //Parameter to scale the frame size to speed up the marker detection
    static final int SCALE = 2;
    static final int SCALE_CANVAS = 2;

}
