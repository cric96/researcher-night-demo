package it.unibo.artificial_vision_tracking.aruco_markers;

/**
 * Enum to define the resolution of the camera.
 */
public enum ResolutionEnum {
    /**
     * Resolution 3840x2160.
     */
    RESOLUTION_3840_2160(3840, 2160),
    /**
     * Resolution 1920x1080.
     */
    RESOLUTION_1920_1080(1920, 1080),
    /**
     * Resolution 1280x720.
     */
    RESOLUTION_1280_720(1280, 720),
    /**
     * Resolution 1024x768.
     */
    RESOLUTION_1024_768(1024, 768),
    /**
     * Resolution 800x600.
     */
    RESOLUTION_800_600(800, 600),
    /**
     * Resolution 640x480.
     */
    RESOLUTION_640_480(640, 480);

    private final int width;
    private final int height;

    /**
     * Constructor of the enum.
     * @param width
     * @param height
     */
    ResolutionEnum(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Get the width of the resolution.
     * @return int
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the height of the resolution.
     * @return int
     */
    public int getHeight() {
        return height;
    }
}
