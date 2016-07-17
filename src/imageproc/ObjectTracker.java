package imageproc;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import javax.imageio.ImageIO;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
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
	private int k = 4;
	private Features lastFeatures;
	private Mat lastFrame;
	private Mat reference;
	private Features refFeatures;

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

	public void start(Mat frame, Mat reference) {
		this.reference = reference;
		this.refFeatures = extractor.detectAndCompute(reference);
		this.lastFrame = frame;
		this.lastFeatures = extractor.detectAndCompute(frame);
	}

	public Mat track(Mat frame1, Mat frame2) {

		// calculate the region of interrest
		// roi(frame1, frame2,frame1, frame2);

		// calculate the keypoints and descriptors
		Features f1 = extractor.detectAndCompute(frame1);
		Features f2 = extractor.detectAndCompute(frame2);

		MatOfDMatch matches = new MatOfDMatch();
		extractor.getMatcher().match(f1.descriptors, f2.descriptors, matches);
		MatOfKeyPoint goodMatches;
		if(matches.toArray().length>k )
			goodMatches = goodMatches(matches, f1.keypoints, k);
		else
			goodMatches = f1.keypoints;

		Mat outImage = new Mat();
		Features2d.drawKeypoints(frame1, goodMatches, outImage);
		return outImage;
	}

	private void roi(Mat frame1, Mat frame2, Mat subImg1, Mat subImg2) {
		Mat diff = difference(frame1, frame2);
		Moments mom = Imgproc.moments(diff);
		Point centroid = new Point(mom.m10 / mom.m00, mom.m01 / mom.m00);

		// calculate the region of interest
		double arrea = Math.sqrt(mom.m00);
		int size = (int) Math.round(Math.sqrt(arrea));
		Rectangle rectangle = new Rectangle((int) centroid.x - size, (int) centroid.y - size, size * 2, size * 2);
		Rectangle track = interpolate(oldtrack, rectangle);
		subImg1 = frame1.adjustROI(track.x, track.y, track.x + track.width, track.y + track.height);
		subImg2 = frame2.adjustROI(track.x, track.y, track.x + track.width, track.y + track.height);
	}

	private MatOfKeyPoint goodMatches(MatOfDMatch matches, MatOfKeyPoint keypoints, int k) {
		// sort out matches that are not close enough
		DMatch[] matchs = matches.toArray();
		Arrays.sort(matchs, new Comparator<DMatch>() {

			@Override
			public int compare(DMatch arg0, DMatch arg1) {
				if (arg0.distance < arg1.distance)
					return -1;
				else if (arg0.distance > arg1.distance)
					return 1;
				else
					return 0;
			}

		});
		MatOfDMatch goodMatches;
		if (matchs.length > k)
			goodMatches = new MatOfDMatch(Arrays.copyOfRange(matchs, 0, k));
		else
			goodMatches = new MatOfDMatch(matchs);
		KeyPoint[] kplist = keypoints.toArray();

		KeyPoint[] kpres = new KeyPoint[k];
		int i = 0;
		for (DMatch m : goodMatches.toArray()) {
			kpres[i++] = kplist[m.queryIdx];
		}
		return new MatOfKeyPoint(kpres);
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

	// public void add(List<BufferedImage> img) throws Exception {
	// LinkedList<Mat> list = new LinkedList<>();
	// for (BufferedImage i : img) {
	// Mat mat = CVUtils.bufferedImageToMat(i);
	// MatOfKeyPoint keypoints = new MatOfKeyPoint();
	// Mat descriptors = new Mat();
	// detector.detect(mat, keypoints);
	// extractor.compute(mat, keypoints, descriptors);
	// int c = descriptors.cols();
	// int r = descriptors.rows();
	// Mat d = new Mat();
	// descriptors.convertTo(d , CvType.CV_32F);
	// list.add(d);
	// }
	// matcher.add(list);
	// }
	//
	// public void train() {
	// matcher.train();
	// }

}
