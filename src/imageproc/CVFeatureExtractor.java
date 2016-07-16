package imageproc;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

public class CVFeatureExtractor extends FeatureExtractor {

	private FeatureDetector det;
	private DescriptorExtractor ext;
	private DescriptorMatcher matcher;

	/**
	 * Initializes the feature extractor from the CV_Type and uses the specified
	 * parameters like specified in the OpenCV documentation
	 * 
	 * @param type
	 *            the type of the descriptor
	 * @param params
	 *            XML or YAML file containing the parameters
	 */
	public CVFeatureExtractor(int type, String params) {
		this(type);
		setParams(params, params);
	}

	public CVFeatureExtractor(int dettype, String detparams, int exttype, String extparams) {
		this(dettype, exttype);
		setParams(detparams, extparams);
	}

	public CVFeatureExtractor(int type) {
		det = FeatureDetector.create(type);
		ext = DescriptorExtractor.create(type);
		matcher = DescriptorMatcher.create(type);
	}

	public CVFeatureExtractor(int dettype, int exttype) {
		det = FeatureDetector.create(dettype);
		ext = DescriptorExtractor.create(exttype);
		matcher = DescriptorMatcher.create(exttype);
	}

	public void setParams(String detparams, String extparams) {
		ext.read(extparams);
		det.read(detparams);
		
		det.write(
				detparams.substring(0, detparams.length() - 4) + "_out" + detparams.substring(detparams.length() - 4));
		ext.write(
				extparams.substring(0, extparams.length() - 4) + "_out" + extparams.substring(extparams.length() - 4));
	}

	@Override
	public DescriptorMatcher getMatcher() {
		return matcher;
	}

	@Override
	public Mat compute(Mat img, MatOfKeyPoint keypoints) {
		Mat descriptors = new Mat();
		ext.compute(img, keypoints, descriptors);
		return descriptors;
	}

	@Override
	public MatOfKeyPoint detect(Mat img) {
		MatOfKeyPoint keypoints = new MatOfKeyPoint();
		det.detect(img, keypoints);
		return keypoints;
	}

}
