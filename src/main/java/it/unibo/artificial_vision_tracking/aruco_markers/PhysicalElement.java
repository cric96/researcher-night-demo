package it.unibo.artificial_vision_tracking.aruco_markers;

public class PhysicalElement {
    private final int id;
    private final double x;
    private final double y;
    private final double rotation;

    public PhysicalElement(final int id, final double x, final double y, final double rotation) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
    }

    public int getId() {
        return this.id;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getRotation() {
        return this.rotation;
    }

    public String toString() {
        return "it.unibo.artificial_vision_tracking.aruco_markers.PhysicalElement [id=" + this.id + ", x=" + this.x + ", y=" + this.y + ", rotation=" + this.rotation + "]";
    }
}
