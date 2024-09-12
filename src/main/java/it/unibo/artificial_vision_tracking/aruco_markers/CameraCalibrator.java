package it.unibo.artificial_vision_tracking.aruco_markers;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.nio.file.Path;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Class to calibrate the camera.
 */
public class CameraCalibrator {
    private static final Logger LOGGER = Logger.getLogger(CameraCalibrator.class.getName());

    private static final float INITIAL_VALUE = 0.0f;

    private static final int WIN_X_SIZE = 11;
    private static final int WIN_Y_SIZE = 11;
    private static final int ZERO_X_ZONE = -1;
    private static final int ZERO_Y_ZONE = -1;
    private static final int MAX_ITERATION = 30;
    private static final double ACCURACY = 0.001;

    private static final int CAMERA_MATRIX_ROWS = 3;
    private static final int CAMERA_MATRIX_COLUMNS = 3;
    private static final int DIST_COEFFS_ROWS = 8;
    private static final int DIST_COEFFS_COLUMNS = 1;

    private final int boardWidth;
    private final int boardHeight;
    private final String directoryPath;

    /**
     * Constructor of the class.
     * @param boardWidth width of the chessboard
     * @param boardHeight height of the chessboard
     * @param directoryPath path of the images
     */
    public CameraCalibrator(final int boardWidth, final int boardHeight, final String directoryPath) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.directoryPath = directoryPath;
    }

    /**
     * Method to calculate the calibration of the camera given a set of images.
     * @return a collection of cameraMatrix and distCoeffs
     */
    public List<Mat> calibration() {
        final Size boardSize = new Size(boardWidth, boardHeight);

        final List<Mat> objectPoints = new ArrayList<>();
        final List<Mat> imagePoints = new ArrayList<>();
        final List<String> imageFiles = getImageFiles(directoryPath);

        final MatOfPoint3f objectPoint = new MatOfPoint3f();
        for (int i = 0; i < boardHeight; i++) {
            for (int j = 0; j < boardWidth; j++) {
                objectPoint.push_back(new MatOfPoint3f(new Point3(j, i, INITIAL_VALUE)));
            }
        }

        //Scrolling through each image in the given path 
        for (final String filePath : imageFiles) {
            LOGGER.info("Processing " + filePath);
            final Mat image = Imgcodecs.imread(filePath);
            final Mat grayImage = new Mat();
            //Converting to a gray scale image to reduce computational complexity
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

            final MatOfPoint2f imageCorners = new MatOfPoint2f();
            //Scanning the image to extract the corners
            final boolean found = Calib3d.findChessboardCorners(grayImage, boardSize, imageCorners);

            //finishing the corners with the cornerSubPix method
            if (found) {
                Imgproc.cornerSubPix(grayImage, imageCorners, new Size(WIN_X_SIZE, WIN_Y_SIZE), 
                        new Size(ZERO_X_ZONE, ZERO_Y_ZONE),
                        new TermCriteria(TermCriteria.EPS + TermCriteria.COUNT, MAX_ITERATION, ACCURACY));

                imagePoints.add(imageCorners);
                objectPoints.add(objectPoint);
                //methods to draw a pattern on the image if the chessboard is recognized and then save the new image 
                //Calib3d.drawChessboardCorners(image, boardSize, imageCorners, found);
                //Imgcodecs.imwrite("output_" + new File(filePath).getName(), image);
            }
        }

        //Creating the two calibration matrix
        final Mat cameraMatrix = Mat.eye(CAMERA_MATRIX_ROWS, CAMERA_MATRIX_COLUMNS, CvType.CV_64F);
        final Mat distCoeffs = Mat.zeros(DIST_COEFFS_ROWS, DIST_COEFFS_COLUMNS, CvType.CV_64F);
        //Collections to calculate the reprojection error
        final List<Mat> rvecs = new ArrayList<>();
        final List<Mat> tvecs = new ArrayList<>();

        //calibration of the camera
        final double rms = Calib3d.calibrateCamera(objectPoints, imagePoints, boardSize, cameraMatrix, distCoeffs, rvecs, tvecs);

        //Printing of the result obtained
        LOGGER.info("RMS error: " + rms);
        LOGGER.info("Camera Matrix: \n" + cameraMatrix.dump());
        LOGGER.info("Distortion Coefficients: \n" + distCoeffs.dump());


        //Reprojection error calculation
        double totalError = 0;
        double totalPoints = 0;
        for (int i = 0; i < objectPoints.size(); i++) {
            final MatOfPoint2f projectedPoints = new MatOfPoint2f();
            Calib3d.projectPoints(new MatOfPoint3f(objectPoints.get(i)), rvecs.get(i), tvecs.get(i), 
                cameraMatrix, new MatOfDouble(distCoeffs), projectedPoints);
            final MatOfPoint2f imgPoints = new MatOfPoint2f(imagePoints.get(i));
            final double error = Core.norm(imgPoints, projectedPoints, Core.NORM_L2);
            totalError += error * error;
            totalPoints += objectPoints.get(i).total();
        }

        final double meanError = Math.sqrt(totalError / totalPoints);
        LOGGER.info("Mean Reprojection Error: " + meanError);

        return List.of(cameraMatrix, distCoeffs);
    }


    /**
     * Method for getting a list of images at a specified path.
     * @param directoryPath
     * @return the list of the absolute path of each image
     */
    private static List<String> getImageFiles(final String directoryPath){
        try {
            var path = Path.of(ClassLoader.getSystemClassLoader().getResource(directoryPath).toURI());
            final File dir = path.toFile();
            final File[] files = dir.listFiles((dir1, name) -> name.toLowerCase(Locale.getDefault()).endsWith(".jpg")
                    || name.toLowerCase(Locale.getDefault()).endsWith(".png"));
            final List<String> imageFiles = new ArrayList<>();
            if (files != null) {
                for (final File file : files) {
                    imageFiles.add(file.getAbsolutePath());
                }
            }
            return imageFiles;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
