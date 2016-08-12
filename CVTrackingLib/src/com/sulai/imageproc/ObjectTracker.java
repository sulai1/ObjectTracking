package com.sulai.imageproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import com.sulai.gui.UIProperty;

public class ObjectTracker extends AbstractTracker {

	private FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
	private DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
	private DescriptorMatcher objMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
	private DescriptorMatcher bgMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

	public UIProperty<Integer> getAttempts() {
		return attempts;
	}

	public UIProperty<Integer> getkNearest() {
		return kNearest;
	}

	public UIProperty<Integer> getMaxClusters() {
		return maxClusters;
	}

	private UIProperty<Integer> kNearest = new UIProperty<Integer>(10);
	private UIProperty<Integer> maxClusters = new UIProperty<Integer>(5);
	private UIProperty<Integer> attempts = new UIProperty<Integer>(3);

	public ObjectTracker(Mat[] objSamples, Mat[] bgSamples) {
		// train matcher
		for (int i = 0; i < 1; i++) {
			addObjectRefference(objSamples[i]);
		}
		objMatcher.train();
		// train background matcher
		for (int i = 0; i < 1; i++) {
			addBackgroundRefference(objSamples[i]);
		}
		bgMatcher.train();
	}

	@Override
	public void start(Mat frame) {
		objMatcher.train();
		bgMatcher.train();
	}

	@Override
	public void addObjectRefference(Mat obj) {
		MatOfKeyPoint keypoints = new MatOfKeyPoint();
		detector.detect(obj, keypoints);
		Mat descriptors = new Mat();
		extractor.compute(obj, keypoints, descriptors);
		List<Mat> dArr = new ArrayList<Mat>();
		dArr.add(descriptors);
		objMatcher.add(dArr);
	}

	@Override
	public void addBackgroundRefference(Mat bg) {
		MatOfKeyPoint keypoints = new MatOfKeyPoint();
		detector.detect(bg, keypoints);
		Mat descriptors = new Mat();
		extractor.compute(bg, keypoints, descriptors);
		List<Mat> dArr = new ArrayList<Mat>();
		dArr.add(descriptors);
		bgMatcher.add(dArr);
	}

	@Override
	public Mat trackObj(Mat frame) {
		// extract the key points and compute descriptors
		Mat img = new Mat();
		Imgproc.cvtColor(frame, img, Imgproc.COLOR_RGB2GRAY);
		MatOfKeyPoint keypoints = new MatOfKeyPoint();
		detector.detect(frame, keypoints);
		Mat descriptors = new Mat();
		extractor.compute(frame, keypoints, descriptors);

		// match the descriptors with the background
		MatOfDMatch bgMatches = new MatOfDMatch();
		bgMatcher.match(descriptors, bgMatches);
		DMatch[] bgArray = bgMatches.toArray();
		Arrays.sort(bgArray, (a, b) -> {
			return (int) (a.distance - b.distance);
		});

		// match the descriptors with the background
		MatOfDMatch objMatches = new MatOfDMatch();
		bgMatcher.match(descriptors, objMatches);
		DMatch[] objArray = objMatches.toArray();
		Arrays.sort(objArray, (a, b) -> {
			return (int) (b.distance - a.distance);
		});

		// find k best matches
		int k = Math.min(kNearest.get(), bgArray.length);
		KeyPoint[] kpts = keypoints.toArray();
		ArrayList<KeyPoint> nkpts = new ArrayList<>();
		for (int i = 0; i < k; i++) {
			nkpts.add(kpts[bgArray[i].queryIdx]);
		}

		// find worst matches
		k = Math.min(kNearest.get(), objArray.length);
		ArrayList<KeyPoint> obj_nkpts = new ArrayList<>();
		for (int i = 0; i < k; i++) {
			obj_nkpts.add(kpts[objArray[i].queryIdx]);
		}

		Point[] pts = new Point[kpts.length];
		for (int i = 0; i < kpts.length; i++) {
			pts[i] = kpts[i].pt;
		}
		if (pts.length != 0) {
			MatOfPoint corners = new MatOfPoint(pts);
			int nClusters = clusterSize(corners);
			Mat labels = cluster(corners, nClusters, attempts.get());
			ArrayList<ArrayList<Point>> clusters = groupClusters(labels, pts, nClusters);
			drawClusters(frame, clusters);
		}
		// Features2d.drawKeypoints(frame, new MatOfKeyPoint(keypoints), img,
		// CVUtils.BLACK, Features2d.DRAW_RICH_KEYPOINTS);
		if (showDebug.get()) {
			Features2d.drawKeypoints(frame, new MatOfKeyPoint(obj_nkpts.toArray(new KeyPoint[obj_nkpts.size()])), img,
					CVUtils.RED, 0);
			Features2d.drawKeypoints(img, new MatOfKeyPoint(nkpts.toArray(new KeyPoint[nkpts.size()])), img,
					CVUtils.GREEN, 0);
			return img;
		}
		return frame;
	}

	private int clusterSize(MatOfPoint corners) {
//		int sqrt = (int) Math.sqrt(corners.rows() / 4.0);
//		if (sqrt > 0)
//			return sqrt;
//		else
//			return 1;
		return maxClusters.get();
	}

}
