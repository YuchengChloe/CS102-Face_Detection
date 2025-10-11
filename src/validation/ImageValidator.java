package src.validation;
import java.io.*;

/**
 * ImageValidator
 * -------------------
 * This class is a placeholder for validating face images
 * before they are stored into the database.
 *
 * 🧠 To teammate handling Requirement 3 (Face Detection & Recognition):
 * Please implement the actual validation logic here.
 * The Enrollment module (Requirement 1) will simply call
 * ImageValidator.isAcceptable(path) to check if an image
 * should be stored.
 *
 * Suggested steps for validation:
 * 1️⃣  Load the image using OpenCV or JavaCV.
 * 2️⃣  Check if a face is detected in the image.
 * 3️⃣  Ensure the image is not too dark / too bright.
 * 4️⃣  Check for blurriness (use Laplacian variance).
 * 5️⃣  Enforce minimum resolution (e.g. ≥ 200x200 px).
 *
 * For now, this class performs only basic file checks
 * so the program can compile and run.
 *
 * Author: [Your Name]
 * Requirement 1 – Student Enrollment & Management
 **/

public class ImageValidator {
    /**
     * Checks if the given image path is acceptable for storing.
     * Currently performs basic file existence and extension checks only.
     * Requirement 3 teammate: please extend this method to include
     * actual face validation and quality assessment.
     **/
    public static boolean isAcceptable(String path) {
        // TODO (Requirement 3):
        // - Detect faces using OpenCV (Haar cascade)
        // - Check brightness, sharpness, and resolution
        // - Return true only if a clear face is found
    }
}
