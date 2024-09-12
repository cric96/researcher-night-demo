package it.unibo.artificial_vision_tracking;/*
 * This Java source file was generated by the Gradle 'init' task.
 */

import it.unibo.artificial_vision_tracking.aruco_markers.CameraPose;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.opencv.opencv_java;
import org.opencv.core.Mat;
import org.opencv.objdetect.Objdetect;

import java.util.ArrayList;
import java.util.List;

/**
 * Main class of the project.
 */
public final class App {
    /**
     * Loading the OpenCV native library
     */
    static {
        Loader.load(opencv_java.class);
    }

    /**
     * Main method.
     * @param args
     * @throws FrameGrabber.Exception
     * @throws InterruptedException
     */
    public static void main(final String[] args) throws FrameGrabber.Exception, InterruptedException {
        final int markersX = 11; // Numero di marker sull'asse X
        final int markersY = 8; // Numero di marker sull'asse Y
        final float markerLength = 0.07f; // Lunghezza del marker (in metri)
        final String directoryPath = "calibration";
        //final Dictionary dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_100);
        final int selectedCamera = 0;
        /*final int markerSheetMarkersX = 8; // Numero di marker sull'asse X
        final int markerSheetMarkersY = 10; // Numero di marker sull'asse Y
        final int markerSheetMarkerLength = 50; // Lunghezza del marker (in pixel)
        final int markerSheetMarkerSeparation = 10; // Separazione tra i marker (in pixel)*/
        final int dictionaryType = Objdetect.DICT_4X4_100;
        //final String fileName = "markersSheet";

        /*it.unibo.artificial_vision_tracking.aruco_markers.GenerateMarkersSheet gms = new it.unibo.artificial_vision_tracking.aruco_markers.GenerateMarkersSheet(markerSheetMarkersX, markerSheetMarkersY,
            markerSheetMarkerLength, markerSheetMarkerSeparation, dictionaryType, fileName);*/
        //gms.generateMarkersSheet();
        final it.unibo.artificial_vision_tracking.aruco_markers.CameraCalibrator cc = new it.unibo.artificial_vision_tracking.aruco_markers.CameraCalibrator(markersX, markersY, directoryPath);
        final List<Mat> cameraParam = cc.calibration();


        final CameraPose cp = new CameraPose(cameraParam.get(0), cameraParam.get(1),
            markerLength, dictionaryType, selectedCamera);
        cp.calcPose();
        var camera = cp.getCamera();
        while  (true) {
            var result = cp.capturePositioning(camera);
            System.out.println("-----");
            result.forEach(System.out::println);
            Thread.sleep(300);
        }
        //Test to calculate the pose of a single frame
        /*VideoCapture capture = cp.getCamera();
        long startTime = System.currentTimeMillis(); 
        int i = 0;
        //A FRAME LIMITER MAY BE REQUIRED (not sure about this)
        while(i < 100){
            System.out.println("\n" + cp.calcSinglePose(capture)[0].dump() + "\n");
            i++;
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Avg frame time: " + (endTime - startTime) / i + "ms");
        */

        //Test of the RobotScreenSaver
        //RobotScreenSaver.screenSaver(cameraParam.get(0), cameraParam.get(1), markerLength, dictionary, selectedCamera);
    }
}
