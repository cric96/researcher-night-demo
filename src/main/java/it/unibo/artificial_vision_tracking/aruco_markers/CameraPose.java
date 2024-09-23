package it.unibo.artificial_vision_tracking.aruco_markers;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.ArucoDetector;
import org.opencv.objdetect.DetectorParameters;
import org.opencv.objdetect.Dictionary;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class to calculate the pose of the camera.
 */
public class CameraPose {
    private boolean running = true;
    private static final Logger LOGGER = Logger.getLogger(CameraPose.class.getName());
    private static final String MS_STRING = " ms";

    //Costants for the ArucoDetector
    private static final int ADAPTIVE_THRESH_WIN_SIZE_MIN = 3;
    private static final int ADAPTIVE_THRESH_WIN_SIZE_MAX = 23;
    private static final int ADAPTIVE_THRESH_WIN_SIZE_STEP = 10;
    private static final int ADAPTIVE_THRESH_CONSTANT = 7;
    private static final int MARKER_BORDER_BITS = 1;
    private static final int PERSPECTIVE_REMOVE_PIXEL_PER_CELL = 16;
    private static final double PERSPECTIVE_REMOVE_IGNORED_MARGIN_PER_CELL = 0.1;

    //Costants for the camera
    private static final int CAMERA_EXPORSURE = -2;

    //Costants for the calcPose methods
    private static final int CORNER_NUMBER = 4;
    private static final int SECOND_IN_MILLIS = 1000;

    //Costants for the drawAxes method
    private static final int X_NEGATIVE_DELTA = -50;
    private static final int Y_POSITIVE_DELTA = 75;
    private static final int Y_NEGATIVE_DELTA = -15;

    //Parameter to scale the frame size to speed up the marker detection
    private static final int SCALE = 2;
    private static final int SCALE_CANVAS = 2;

    private final Mat cameraMatrix; 
    private final Mat distCoeffs;
    private final float markerLength;
    private final Dictionary dictionary;
    private final int dictionaryType; 
    private final int selectedcamera;
    private final OpenCVFrameConverter.ToMat converterToMat;

    /**
     * Constructor of the class.
     * @param cameraMatrix
     * @param distCoeffs
     * @param markerLength
     * @param dictionaryType
     * @param selectedcamera
     */
    public CameraPose(final Mat cameraMatrix, final Mat distCoeffs, final float markerLength, 
        final int dictionaryType, final int selectedcamera) {
        this.cameraMatrix = cameraMatrix.clone();
        this.distCoeffs = distCoeffs.clone();
        this.markerLength = markerLength;
        this.dictionaryType = dictionaryType;
        this.dictionary = Objdetect.getPredefinedDictionary(this.dictionaryType);
        this.selectedcamera = selectedcamera;
        this.converterToMat = new OpenCVFrameConverter.ToMat();
    }

    //Getters
    /**
     * Method to get the scale.
     * @return int
     */
    public static int getScale() {
        return SCALE;
    }

    /**
     * Method to get the scale canvas.
     * @return int
     */
    public static int getScaleCanvas() {
        return SCALE_CANVAS;
    }

    /**
     * Method to get the cameraMatrix.
     * @return Mat
     */
    public Mat getCameraMatrix() {
        return cameraMatrix.clone();
    }

    /**
     * Method to get the distCoeffs.
     * @return Mat
     */
    public Mat getDistCoeffs() {
        return distCoeffs.clone();
    }

    /**
     * Method to get the marker length.
     * @return float
     */
    public float getMarkerLength() {
        return markerLength;
    }

    /**
     * Method to get the dictionary.
     * @return Dictionary
     */
    public Dictionary getDictionary() {
        return Objdetect.getPredefinedDictionary(this.dictionaryType);
    }

    /**
     * Method to get the selected camera.
     * @return int
     */
    public int getSelectedcamera() {
        return selectedcamera;
    }

    /**
     * Method to get the converterToMat.
     * @return OpenCVFrameConverter.ToMat
     */
    public OpenCVFrameConverter.ToMat getConverterToMat() {
        return converterToMat;
    }

    /**
     * Method to get the ArucoDetector.
     * @return ArucoDetector
     */
    public ArucoDetector getArucoDetector() {
        final ArucoDetector arucoDetector = new ArucoDetector();
        //Setting the dictionary
        arucoDetector.setDictionary(this.dictionary);

        //Setting the detector parameters
        final DetectorParameters parameters = new DetectorParameters();
        //parameters to detect the markers with different thresholds
        parameters.set_adaptiveThreshWinSizeMin(ADAPTIVE_THRESH_WIN_SIZE_MIN);
        parameters.set_adaptiveThreshWinSizeMax(ADAPTIVE_THRESH_WIN_SIZE_MAX);
        parameters.set_adaptiveThreshWinSizeStep(ADAPTIVE_THRESH_WIN_SIZE_STEP);
        parameters.set_adaptiveThreshConstant(ADAPTIVE_THRESH_CONSTANT);
        //parameter to set the size of the black border around the marker
        parameters.set_markerBorderBits(MARKER_BORDER_BITS);
        //Augmented pixel per cell (reduce if the performance is too low)
        parameters.set_perspectiveRemovePixelPerCell(PERSPECTIVE_REMOVE_PIXEL_PER_CELL);
        //Margin of pixels to remove from the final image (0.1 is 10%)
        parameters.set_perspectiveRemoveIgnoredMarginPerCell(PERSPECTIVE_REMOVE_IGNORED_MARGIN_PER_CELL);
        arucoDetector.setDetectorParameters(parameters);

        return arucoDetector;
    }

    /**
     * Method to get the camera.
     * @return VideoCapture
     */
    public VideoCapture getCamera() {
        //Getting the camera
        final VideoCapture capture = new VideoCapture(this.selectedcamera, Videoio.CAP_V4L2); // Use 0 for the primary camera
        if (!capture.isOpened()) {
            LOGGER.warning("Error: impossible to open webcam.");
            converterToMat.close();
            return null;
        }

        //Setting the proper camera resolution
        boolean resolutionSet = false;
        for (final ResolutionEnum resolution : ResolutionEnum.values()) {
            if (capture.set(Videoio.CAP_PROP_FRAME_WIDTH, resolution.getWidth()) 
                && capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, resolution.getHeight())) {
                    LOGGER.info("Camera resolution set to: " + capture.get(Videoio.CAP_PROP_FRAME_WIDTH) 
                    + "x" + capture.get(Videoio.CAP_PROP_FRAME_HEIGHT));
                resolutionSet = true;
                break;
            }
        }

        //Setting the camera exposure to reduce the motion blur
        capture.set(Videoio.CAP_PROP_EXPOSURE, 100);

        //Getting the frame rate
        LOGGER.info("Frame rate: " + capture.get(Videoio.CAP_PROP_FPS));

        if (!resolutionSet) {
            LOGGER.warning("Error: impossible to set camera resolution.");
        }

        return capture;
    }

    /**
     * Method to get the canvas.
     * @param title
     * @return CanvasFrame
     */
    public CanvasFrame getCanvas(final String title) {

        //Canvas to display the webcam feed
        final CanvasFrame canvas = new CanvasFrame(title);
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

        //Key listener to close the application
        canvas.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_Q) {
                    running = false;
                }
            }

            @Override
            public void keyTyped(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_Q) {
                    running = false;
                }
            }

            @Override
            public void keyReleased(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_Q) {
                    running = false;
                }
            }
        });

        return canvas;
    }

    /**
     * Calculate the pose for a single frame.
     * the param capture is used to not reopen every time the Videocapture because
     * it cost a lot in terms of time and resources and slow down the pose process
     * @param cap VideoCapture passed as parameter if it is null then the method will open a new one
     * @return Mat of tvec and tvec
     */
    public Mat[] calcSinglePose(final VideoCapture cap) {
        final VideoCapture capture;

        if (cap == null) {
            //Getting the camera
            capture = getCamera();
        } else {
            capture = cap;
        }

        //Create the object points of the marker
        final MatOfPoint3f objPoints = new MatOfPoint3f(
            new Point3(0, 0, 0),
            new Point3(markerLength, 0, 0),
            new Point3(markerLength, markerLength, 0),
            new Point3(0, markerLength, 0)
        );

        final Mat frame = new Mat();

        //Getting the frame
        capture.read(frame);
        if (frame.empty()) {
            return new Mat[] {new Mat(), new Mat()};
        }

        //Resize the frame to speed up the marker detection
        final Mat reducedFrame = new Mat();
        Imgproc.resize(frame, reducedFrame, new Size((double) frame.width() / SCALE, (double) frame.height() / SCALE));

        //Convert the frame to gray in order to detect the markers
        final Mat gray = new Mat();
        Imgproc.cvtColor(reducedFrame, gray, Imgproc.COLOR_BGR2GRAY);

        //Corners and ids of the detected markers
        final List<Mat> corners = new ArrayList<>();
        final Mat ids = new Mat();

        //Mat to store the rotation and translation vectors of all the markers
        final Mat rvecs = new Mat();
        final Mat tvecs = new Mat();

        //Getting the ArucoDetector
        final ArucoDetector arucoDetector = getArucoDetector();

        //Detecting the markers
        arucoDetector.detectMarkers(gray, corners, ids);
        if (!corners.isEmpty()) {
            rescalePoints(corners);
        }

        double totalReprojectionError = 0;
        int markerCount = 0;
        //Pose estimation
        if (!ids.empty()) {
            //Convert ID Mat to int array
            final int[] idArray = new int[(int) ids.total()];
            ids.get(0, 0, idArray);
            //Starting the pose estimation
            for (int i = 0; i < idArray.length; i++) {

                //Create MatOfPoint2f mat with the corner points of the marker
                final MatOfPoint2f cornerPoints = new MatOfPoint2f();
                //Mat to store the rotation and translation vectors
                final Mat rvec = new Mat();
                final Mat tvec = new Mat();

                prepareMatrixForPoseEstimation(corners, i, cornerPoints);

                //Pose estimation using solvePnP
                if (cornerPoints.rows() >= CORNER_NUMBER) {
                    Calib3d.solvePnP(objPoints, cornerPoints, cameraMatrix, new MatOfDouble(distCoeffs), 
                        rvec, tvec, false, Calib3d.SOLVEPNP_ITERATIVE);

                    rvecs.push_back(rvec);
                    tvecs.push_back(tvec);
                    // Calculate reprojection error
                    final double reprojectionError = calculateReprojectionError(objPoints, cornerPoints, rvec, tvec);
                    totalReprojectionError += reprojectionError * reprojectionError;
                    markerCount += cornerPoints.total();
                }

                //Mats cleanup
                cornerPoints.release();
            }

            ids.release();
            corners.clear();
        }

        //Mats cleanup
        gray.release();
        reducedFrame.release();
        frame.release();
        frame.release();

        if (markerCount > 0) {
            final double avgReprojectionError = Math.sqrt(totalReprojectionError / markerCount);
            LOGGER.info("Average reprojection error: " + avgReprojectionError);
        }
        if (cap == null) {
            capture.release();
        }
        converterToMat.close();

        return new Mat[] {tvecs, rvecs};
    }

    public List<PhysicalElement> capturePositioning(final VideoCapture capture) {

        //Create the object points of the marker
        final MatOfPoint3f objPoints = new MatOfPoint3f(
                new Point3(0, 0, 0),
                new Point3(markerLength, 0, 0),
                new Point3(markerLength, markerLength, 0),
                new Point3(0, markerLength, 0)
        );

        final Mat frame = new Mat();

        //Getting the frame
        capture.read(frame);
        if (frame.empty()) {
            return List.of();
        }

        //Resize the frame to speed up the marker detection
        final Mat reducedFrame = new Mat();
        Imgproc.resize(frame, reducedFrame, new Size((double) frame.width() / SCALE, (double) frame.height() / SCALE));

        //Convert the frame to gray in order to detect the markers
        final Mat gray = new Mat();
        Imgproc.cvtColor(reducedFrame, gray, Imgproc.COLOR_BGR2GRAY);

        //Corners and ids of the detected markers
        final List<Mat> corners = new ArrayList<>();
        final Mat ids = new Mat();

        //Mat to store the rotation and translation vectors of all the markers
        final Mat rvecs = new Mat();
        final Mat tvecs = new Mat();

        //Getting the ArucoDetector
        final ArucoDetector arucoDetector = getArucoDetector();

        //Detecting the markers
        arucoDetector.detectMarkers(gray, corners, ids);
        if (!corners.isEmpty()) {
            rescalePoints(corners);
        }

        double totalReprojectionError = 0;
        int markerCount = 0;
        //Pose estimation
        if (!ids.empty()) {
            //Convert ID Mat to int array
            final int[] idArray = new int[(int) ids.total()];
            ids.get(0, 0, idArray);
            //Starting the pose estimation
            var elements = new ArrayList<PhysicalElement>();
            for (int i = 0; i < idArray.length; i++) {
                //Create MatOfPoint2f mat with the corner points of the marker
                final MatOfPoint2f cornerPoints = new MatOfPoint2f();
                //Mat to store the rotation and translation vectors
                final Mat rvec = new Mat();
                final Mat tvec = new Mat();

                prepareMatrixForPoseEstimation(corners, i, cornerPoints);
                // Pose estimation using solvePnP
                if (cornerPoints.rows() >= CORNER_NUMBER) {
                    Calib3d.solvePnP(objPoints, cornerPoints, cameraMatrix, new MatOfDouble(distCoeffs),
                            rvec, tvec, false, Calib3d.SOLVEPNP_ITERATIVE);

                    drawAxes(frame, rvec, tvec, markerLength);
                    var element = new PhysicalElement(idArray[i], tvec.get(0, 0)[0], tvec.get(1, 0)[0], rvec.get(2, 0)[0]);
                    elements.add(element);
                    // Calculate reprojection error
                    final double reprojectionError = calculateReprojectionError(objPoints, cornerPoints, rvec, tvec);
                    totalReprojectionError += reprojectionError * reprojectionError;
                    markerCount += cornerPoints.total();
                }
                final Mat resizedFrame12 = new Mat();
                Imgproc.resize(frame, resizedFrame12, new Size((double) frame.width() / SCALE_CANVAS,
                        (double) frame.height() / SCALE_CANVAS));

                //Display the frame
                //Mats cleanup
                cornerPoints.release();
            }

            ids.release();
            corners.clear();

            //Mats cleanup
            gray.release();
            reducedFrame.release();
            frame.release();
            frame.release();
            converterToMat.close();
            return elements;
        }

        return List.of();
    }

    /**
     * Calculate the pose for a single frame.
     * very slow because of the opening of the Videocapture every call
     * @return Mat of tvec and tvec
     */
    public Mat[] calcSinglePose() {
        return calcSinglePose(null);
    }


    /**
     * Calculate the pose continously showing the result on a canvas.
     */
    public void calcPose() {
        final long startTime;
        int totalFrames = 0;

        double totalReprojectionError = 0;
        int markerCount = 0;

        //Getting the ArucoDetector
        final ArucoDetector arucoDetector = getArucoDetector();

        //Getting the camera
        final VideoCapture capture = getCamera();

        final long frameDuration = (long) (SECOND_IN_MILLIS / capture.get(Videoio.CAP_PROP_FPS));

        //Create the object points of the marker
        final MatOfPoint3f objPoints = new MatOfPoint3f(
            new Point3(0, 0, 0),
            new Point3(markerLength, 0, 0),
            new Point3(markerLength, markerLength, 0),
            new Point3(0, markerLength, 0)
        );

        //Canvas to display the webcam feed
        final CanvasFrame canvas = getCanvas("Webcam");

        final Mat frame = new Mat();

        //Variable to check if the marker zero is lost
        boolean lose = false;

        //Variables to calculate the time of the detection and pose estimation
        long totalTimeDetection = 0;
        long totalTimePose = 0;
        long totalGetFrameTime = 0;
        long startGetFrameTime;
        startTime = System.currentTimeMillis();
        long loseTime = System.currentTimeMillis();
        long startWhile;
        while (running) {
            startWhile = System.currentTimeMillis();
            //Getting the frame
            startGetFrameTime = System.currentTimeMillis();
            capture.read(frame);
            if (frame.empty()) {
                break;
            }
            totalGetFrameTime += System.currentTimeMillis() - startGetFrameTime;

            //Resize the frame to speed up the marker detection
            final Mat reducedFrame = new Mat();
            Imgproc.resize(frame, reducedFrame, new Size((double) frame.width() / SCALE, (double) frame.height() / SCALE));

            /*Mat undistorted = new Mat();
            Mat newCameraMatrix = Calib3d.getOptimalNewCameraMatrix(cameraMatrix, distCoeffs, frame.size(), 0);
            Calib3d.undistort(frame, undistorted, cameraMatrix, distCoeffs, newCameraMatrix);*/

            //Convert the frame to gray in order to detect the markers
            final Mat gray = new Mat();
            Imgproc.cvtColor(reducedFrame, gray, Imgproc.COLOR_BGR2GRAY);

            //Corners and ids of the detected markers
            final List<Mat> corners = new ArrayList<>();
            final Mat ids = new Mat();

            //Detecting the markers
            final long start = System.currentTimeMillis();
            arucoDetector.detectMarkers(gray, corners, ids);
            if (!corners.isEmpty()) {
                rescalePoints(corners);
            }
            totalTimeDetection += System.currentTimeMillis() - start;

            //Pose estimation
            if (!ids.empty()) {
                //Convert ID Mat to int array
                final int[] idArray = new int[(int) ids.total()];
                ids.get(0, 0, idArray);

                //Check if the zero marker is lost
                if (!containsZeroMarker(idArray)) {
                    if (!lose) {
                        loseTime = System.currentTimeMillis();
                        lose = true;
                    }
                } else {
                    if (lose) {
                        lose = false;
                        LOGGER.info("Lose: " + (System.currentTimeMillis() - loseTime));
                    }
                }

                //Starting the pose estimation
                final long startPose = System.currentTimeMillis();
                for (int i = 0; i < idArray.length; i++) {
                    //Create MatOfPoint2f mat with the corner points of the marker
                    final MatOfPoint2f cornerPoints = new MatOfPoint2f();
                    //Mat to store the rotation and translation vectors
                    final Mat rvec = new Mat();
                    final Mat tvec = new Mat();

                    prepareMatrixForPoseEstimation(corners, i, cornerPoints);

                    //Pose estimation using solvePnP
                    if (cornerPoints.rows() >= CORNER_NUMBER) {
                        Calib3d.solvePnP(objPoints, cornerPoints, cameraMatrix, new MatOfDouble(distCoeffs),
                            rvec, tvec, false, Calib3d.SOLVEPNP_ITERATIVE);

                        // Calculating distance from camera
                        /*double distance = Core.norm(tvec);
                        System.out.printf("Marker ID: %d - Distance: %.2f m%n", (int) ids.get(i, 0)[0], distance);*/
                        // Draw marker axis
                        drawAxes(frame, rvec, tvec, markerLength);
                        // Calculate reprojection error
                        final double reprojectionError = calculateReprojectionError(objPoints, cornerPoints, rvec, tvec);
                        totalReprojectionError += reprojectionError * reprojectionError;
                        markerCount += cornerPoints.total();
                    }

                    //Mats cleanup
                    rvec.release();
                    tvec.release();
                    cornerPoints.release();
                }
                totalTimePose += System.currentTimeMillis() - startPose;
                Objdetect.drawDetectedMarkers(frame, corners, ids);
                ids.release();
                corners.clear();
            } else {
                //Also if ids is empty I need to update the lose variable
                if (!lose) {
                    loseTime = System.currentTimeMillis();
                    lose = true;
                }
            }
            totalFrames++;

            //Resizing the frame to display it (only beacuse it looks better)
            final Mat resizedFrame12 = new Mat();
            Imgproc.resize(frame, resizedFrame12, new Size((double) frame.width() / SCALE_CANVAS,
                (double) frame.height() / SCALE_CANVAS));
            final Mat increasedContrat = new Mat();
            //Display the frame
            canvas.showImage(converterToMat.convert(resizedFrame12));

            //Mats cleanup
            gray.release();
            reducedFrame.release();
            frame.release();
            resizedFrame12.release();

            // Code to limit the frame rate to the camera frame rate
            if (System.currentTimeMillis() - startWhile < frameDuration) {
                try {
                    Thread.sleep(frameDuration - (System.currentTimeMillis() - startWhile));
                } catch (InterruptedException e) {
                    LOGGER.warning(e.getMessage());
                }
            }
        }

        frame.release();
        final long endTime = System.currentTimeMillis();
        LOGGER.info("Average time per getFrameTime: " + totalGetFrameTime / (double) totalFrames + MS_STRING);
        LOGGER.info("Average time per detection: " + totalTimeDetection / (double) totalFrames + MS_STRING);
        LOGGER.info("Average time per pose estimation: " + totalTimePose / (double) totalFrames + MS_STRING);
        LOGGER.info("Average time per frame: " + (endTime - startTime) / (double) totalFrames + MS_STRING);
        if (markerCount > 0) {
            final double avgReprojectionError = Math.sqrt(totalReprojectionError / markerCount);
            LOGGER.info("Average reprojection error: " + avgReprojectionError);
        }
        canvas.dispose();
        capture.release();
        converterToMat.close();
    }

    private void prepareMatrixForPoseEstimation(final List<Mat> corners, final int i, final MatOfPoint2f cornerPoints) {
        final Mat cornerMat = corners.get(i).clone();
        final List<double[]> cornerData = new ArrayList<>();
        //Extract the four corner points of the marker
        for (int h = 0; h < CORNER_NUMBER; h++) {
            cornerData.add(cornerMat.get(0, h));
        }

        //Save the corner points of the marker in an array
        final Point[] cornerPointsArray = new Point[CORNER_NUMBER];
        for (int j = 0; j < cornerData.size(); j++) {
            final double[] data = cornerData.get(j);
            cornerPointsArray[j] = new Point(data[0], data[1]);
        }

        //Create MatOfPoint2f mat with the corner points of the marker
        cornerPoints.fromArray(cornerPointsArray);
        cornerMat.release();
    }


    /**
     * Method to draw the axes of the marker and the position of the marker.
     * !IMPORTANT this method takes a lot of time to be executed so use it only for debugging
     * @param image
     * @param rvec
     * @param tvec
     * @param length
     */
    private void drawAxes(final Mat image, final Mat rvec, final Mat tvec, final float length) {
        final MatOfPoint3f axis = new MatOfPoint3f(
            new Point3(0, 0, 0),
            new Point3(length, 0, 0),
            new Point3(0, length, 0),
            new Point3(0, 0, -length)
        );

        final MatOfPoint2f projectedPoints = new MatOfPoint2f();
        Calib3d.projectPoints(axis, rvec, tvec, cameraMatrix, new MatOfDouble(distCoeffs), projectedPoints);

        final Point[] pts = projectedPoints.toArray();
        // Draw the X, Y, Z axis
        Imgproc.line(image, pts[0], pts[1], new Scalar(0, 0, GenerateMarkersSheet.FULL_COLOR), 2); // X axis in red
        Imgproc.line(image, pts[0], pts[2], new Scalar(0, GenerateMarkersSheet.FULL_COLOR, 0), 2); // Y axis in green
        Imgproc.line(image, pts[0], pts[3], new Scalar(GenerateMarkersSheet.FULL_COLOR, 0, 0), 2); // Z axis in blue 

        // Draw the text for the tvec and rvec
        final String tvecText = String.format("x: %.2f  y: %.2f  z: %.2f", tvec.get(0, 0)[0],
            tvec.get(1, 0)[0], tvec.get(2, 0)[0]);
        final String rvecText = String.format("z rotation: %.2f", rvec.get(2, 0)[0] * 180 / Math.PI);
        // Put the text on the top of the marker
        final Point tvectextPos = new Point(pts[0].x + X_NEGATIVE_DELTA, pts[0].y + Y_NEGATIVE_DELTA);
        // Put the text on the bottom of the marker
        final Point rvectextPos = new Point(pts[0].x + X_NEGATIVE_DELTA, pts[0].y + Y_POSITIVE_DELTA); 
        Imgproc.putText(image, tvecText, tvectextPos, Imgproc.FONT_HERSHEY_SIMPLEX, 1, 
            new Scalar(GenerateMarkersSheet.FULL_COLOR, GenerateMarkersSheet.FULL_COLOR, GenerateMarkersSheet.FULL_COLOR),
            2, Imgproc.LINE_AA);
        Imgproc.putText(image, rvecText, rvectextPos, Imgproc.FONT_HERSHEY_SIMPLEX, 1, 
            new Scalar(GenerateMarkersSheet.FULL_COLOR, GenerateMarkersSheet.FULL_COLOR, GenerateMarkersSheet.FULL_COLOR),
            2, Imgproc.LINE_AA);

        //Mats cleanup
        axis.release();
        projectedPoints.release();
    }

    /**
     * Method to calculate the reprojection error.
     * @param objPoints
     * @param imgPoints
     * @param rvec
     * @param tvec
     * @return Reprojection error
     */
    private double calculateReprojectionError(final MatOfPoint3f objPoints, 
        final MatOfPoint2f imgPoints, final Mat rvec, final Mat tvec) {
        final MatOfPoint2f projectedPoints = new MatOfPoint2f();
        Calib3d.projectPoints(objPoints, rvec, tvec, cameraMatrix, new MatOfDouble(distCoeffs), projectedPoints);

        // Calculating reprojection error
        final double error = Core.norm(imgPoints, projectedPoints, Core.NORM_L2);
        projectedPoints.release();
        return error;
    }

    /**
     * Method to rescale the points.
     * !IMPORTANT 
     *  if you resize the image before the detection 
     *  then you need to call this method to rescale the points
     *  detected by the marker detection to the right size
     *  otherwise the pose estimation will not work as expected
     * !IMPORTANT
     * @param corners
     */
    private static void rescalePoints(final List<Mat> corners) {
        for (final Mat corner : corners) {
            for (int i = 0; i < CORNER_NUMBER; i++) {
                final double[] data = corner.get(0, i);
                data[0] *= SCALE;
                data[1] *= SCALE;
                corner.put(0, i, data);
            }
        }
    }

    /**
     * Method to check if the marker array contains the zero marker.
     * @param idsArray
     * @return boolean value, true if the zero marker is present, false otherwise
     */
    private static boolean containsZeroMarker(final int[] idsArray) {
        for (final int i : idsArray) {
            if (i == 0) {
                return true;
            }
        }
        return false;
    }
}
