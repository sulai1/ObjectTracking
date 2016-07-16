package imageproc;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import imageproc.FeatureExtractor.Features;

public class ObjectTracker {

	private static final double BLUR = 6;
	private double threshold = 15;
	private Rectangle oldtrack = new Rectangle();
	private FeatureExtractor extractor;

	public ObjectTracker() throws IOException {
		this(15);
	}

	public ObjectTracker(double threshold) throws IOException {
		this.threshold = threshold;
		extractor = new CVFeatureExtractor(FeatureDetector.FAST, FeatureDetector.ORB);
	}

	public Point trackCenter(Mat frame1, Mat frame2) {
		Mat diff = difference(frame1, frame2);
		return centroid(diff);
	}

	public Mat track(Mat frame1, Mat frame2) {

		// calculate the region of interrest
		Mat diff = difference(frame1, frame2);
		Moments mom = Imgproc.moments(diff);
		Point centroid = new Point(mom.m10 / mom.m00, mom.m01 / mom.m00);

		// calculate the region of interest
//		double arrea = Math.sqrt(mom.m00);
//		int size = (int) Math.round(Math.sqrt(arrea));
//		Rectangle rectangle = new Rectangle((int) centroid.x - size, (int) centroid.y - size, size * 2, size * 2);
//		Rectangle track = interpolate(oldtrack, rectangle);
//		Mat subImg = frame1.adjustROI(track.x, track.y, track.x + track.width, track.y + track.height);

		// calculate the keypoints and descriptors
		Features f1 = extractor.detectAndCompute(frame1);
		Features f2 = extractor.detectAndCompute(frame2);

		f1.descriptors.convertTo(f1.descriptors, CvType.CV_32F);
//		f2.descriptors.convertTo(f2.descriptors, CvType.CV_32F);
//		MatOfDMatch matches = new MatOfDMatch();
//		extractor.getMatcher().match(f1.descriptors, matches );
//		MatOfKeyPoint kp = getGoodMatches(matches, f1.keypoints);

		System.out.println(f1.keypoints.size());
		Mat outImage = new Mat();
		Features2d.drawKeypoints(frame1, f1.keypoints, outImage);
		return outImage;
//		return frame1;
	}

	private MatOfKeyPoint getGoodMatches(MatOfDMatch matches, MatOfKeyPoint keypoints) {
		DMatch[] dMatches = matches.toArray();
		float min = Float.MAX_VALUE;
		MatOfKeyPoint newkp = new MatOfKeyPoint();

		for (int i = 0; i < dMatches.length; i++) {
			min = Math.min(dMatches[i].distance, min);
		}
		ArrayList<KeyPoint> kp = new ArrayList<>();
		KeyPoint[] kporig = keypoints.toArray();

		for (int i = 0; i < dMatches.length; i++) {
			if (dMatches[i].distance <= 2 * min) {
				kp.add(kporig[dMatches[i].queryIdx]);
			}
		}
		newkp.fromList(kp);
		System.out.println(kp.size()+" "+dMatches.length+" "+min);
		return newkp;
	}

	private Mat toImage(Mat frame1, Rectangle rectangle, MatOfKeyPoint keypoints) {
		Mat outImage = new Mat();
		Features2d.drawKeypoints(frame1, keypoints, outImage);
		return outImage;
	}

	private Rectangle interpolate(Rectangle oldtrack, Rectangle rectangle) {
		return new Rectangle((oldtrack.x + rectangle.x) / 2, (oldtrack.y + rectangle.y) / 2,
				(oldtrack.width + rectangle.width) / 2, (oldtrack.height + rectangle.height) / 2);
		// return rectangle;
	}

	/**
	 * @param frame1
	 * @param frame2
	 * @return
	 */
	public Mat difference(Mat frame1, Mat frame2) {
		Mat A = new Mat();
		Imgproc.cvtColor(frame1, A, Imgproc.COLOR_RGB2GRAY);

		Mat B = new Mat();
		Imgproc.cvtColor(frame2, B, Imgproc.COLOR_RGB2GRAY);

		Mat C = new Mat();
		Core.absdiff(A, B, C);
		Mat D = new Mat();
		Imgproc.blur(C, D, new Size(new Point(BLUR, BLUR)));
		setThreshold(Imgproc.threshold(D, C, threshold, 255, Imgproc.THRESH_BINARY));
		return C;
	}

	public static Point centroid(Mat m) {
		Moments mom = Imgproc.moments(m);
		Point point = new Point(mom.m10 / mom.m00, mom.m01 / mom.m00);
		return point;
	}

	public double getThreshold() {
		return this.threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public BufferedImage track(BufferedImage frame1, BufferedImage frame2) throws Exception {
		return CVUtils.toBufferedImage(track(CVUtils.bufferedImageToMat(frame1), CVUtils.bufferedImageToMat(frame2)));
	}

//	public void add(List<BufferedImage> img) throws Exception {
//		LinkedList<Mat> list = new LinkedList<>();
//		for (BufferedImage i : img) {
//			Mat mat = CVUtils.bufferedImageToMat(i);
//			MatOfKeyPoint keypoints = new MatOfKeyPoint();
//			Mat descriptors = new Mat();
//			detector.detect(mat, keypoints);
//			extractor.compute(mat, keypoints, descriptors);
//			int c = descriptors.cols();
//			int r = descriptors.rows();
//			Mat d = new Mat();
//			descriptors.convertTo(d , CvType.CV_32F);
//			list.add(d);
//		}
//		matcher.add(list);
//	}
//
//	public void train() {
//		matcher.train();
//	}

}
