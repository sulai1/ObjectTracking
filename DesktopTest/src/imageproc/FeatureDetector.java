package imageproc;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

public interface FeatureDetector {
	public MatOfKeyPoint detect(Mat image);
}
