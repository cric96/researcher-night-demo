package it.unibo.artificial_vision_tracking.aruco_markers;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.Dictionary;
import org.opencv.objdetect.GridBoard;
import org.opencv.objdetect.Objdetect;

import java.util.logging.Logger;

/**
 * Class to generate the Aruco markers sheet.
 */
public class GenerateMarkersSheet {
    private static final Logger LOGGER = Logger.getLogger(GenerateMarkersSheet.class.getName());
    /**
     * Constant to encode the value of "full color" in decimal.
     */
    public static final int FULL_COLOR = 255;
    private static final int IMAGE_MARGIN_SIZE = 10;
    private static final int MARKER_BORDER_BITS = 1;

    // Horizontal markers
    private final int markersX;
    // Vertical markers
    private final int markersY;
    // Marker length in pixels
    private final float markerLength;
    // Separation between markers in pixels
    private final float markerSeparation;
    // Aruco dictionary
    private final Dictionary dictionary;
    // Output file name
    private final String fileName;

    /**
     * Constructor of the class.
     * @param markersX
     * @param markersY
     * @param markerLength
     * @param markerSeparation
     * @param dictionaryType
     * @param fileName
     */
    public GenerateMarkersSheet(final int markersX, final int markersY, final float markerLength, 
        final float markerSeparation, final int dictionaryType, final String fileName) {
        this.markersX = markersX;
        this.markersY = markersY;
        this.markerLength = markerLength;
        this.markerSeparation = markerSeparation;
        this.dictionary = Objdetect.getPredefinedDictionary(dictionaryType);
        this.fileName = fileName;
    }

    /**
     * Method to generate the Aruco markers sheet.
     */
    public void generateMarkersSheet() {
        final GridBoard gridBoard = new GridBoard(new Size(markersX, markersY), markerLength, markerSeparation, dictionary);

        // Calculate the total width and height of the image
        final int totalWidth = (int) (markersX * (markerLength + markerSeparation) - markerSeparation);
        final int totalHeight = (int) (markersY * (markerLength + markerSeparation) - markerSeparation);
        final Size imageSize = new Size(totalWidth, totalHeight);

        // Create an image with a white background
        final Mat markerImage = new Mat(imageSize, CvType.CV_8UC1, new Scalar(FULL_COLOR));

        // Drawing the markers grid
        gridBoard.generateImage(imageSize, markerImage, IMAGE_MARGIN_SIZE, MARKER_BORDER_BITS);

        // Saving the image
        Imgcodecs.imwrite(fileName + ".png", markerImage);

        LOGGER.info("markers sheet image created and saved as " + fileName + ".png");
    }
}
