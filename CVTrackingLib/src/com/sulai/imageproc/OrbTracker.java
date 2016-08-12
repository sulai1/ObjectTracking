package com.sulai.imageproc;

import java.util.ArrayList;
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

public class OrbTracker extends AbstractTracker {

	private FeatureDetector detector;
	private DescriptorExtractor extractor;
	private DescriptorMatcher objMatcher;
	private DescriptorMatcher bgMatcher;
	private boolean bgEmpty = true;

	private UIProperty<Integer> kNearest = new UIProperty<Integer>(10);
	private UIProperty<Integer> maxClusters = new UIProperty<Integer>(5);
	private UIProperty<Integer> attempts = new UIProperty<Integer>(3);

	public UIProperty<Integer> getMaxClusters() {
		return maxClusters;
	}

	public UIProperty<Integer> getAttempts() {
		return attempts;
	}

	public OrbTracker(Mat[] objSamples, Mat[] bgSamples) {
		// init matching
		detector = FeatureDetector.create(FeatureDetector.FAST);
		extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		objMatcher = DescriptorMatcher
				.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
		bgMatcher = DescriptorMatcher
				.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

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
	}

	@Override
	public Mat trackObj(Mat frame) {

		// calculate key points
		Mat img = new Mat();
		Imgproc.cvtColor(frame, img, Imgproc.COLOR_RGB2GRAY);
		MatOfKeyPoint keypoints = new MatOfKeyPoint();
		detector.detect(img, keypoints);
		Mat descriptors = new Mat();
		extractor.compute(img, keypoints, descriptors);

		// match the key points from the background
		MatOfDMatch matches = new MatOfDMatch();
		objMatcher.match(descriptors, matches);
		List<DMatch> bgMatch = matches.toList();
		bgMatch.sort((a, b) -> {
			return (int) (b.distance - a.distance);
		});

		// remove background matching key points
		Mat nDescriptors;
		List<KeyPoint> kpList = keypoints.toList();
		int rows = descriptors.rows() - kNearest.get();
		KeyPoint[] retainedKP = null;
		KeyPoint[] removedKP = null;
		if (rows>0) {
			retainedKP = new KeyPoint[rows];
			removedKP = new KeyPoint[kNearest.get()];
			nDescriptors = new Mat(rows, descriptors.cols(), descriptors.type());
			DMatch[] m = matches.toArray();
			// copy remaining descriptors and keypoints
			for (int i = kNearest.get(); i < keypoints.rows(); i++) {
				retainedKP[i-kNearest.get()] = kpList.get(m[i].queryIdx);
				for (int j = 0; j < descriptors.cols(); j++)
					nDescriptors.put(i-kNearest.get(), j, descriptors.get(i, j));
			}
			//copy removed keypoints to draw debug information
			for (int i = 0; i < Math.min(kNearest.get(),kpList.size()); i++) {
				removedKP[i] = kpList.get(m[i].queryIdx);
			}
		} else {
			nDescriptors = descriptors;
		}

		// match the key points from the object
		matches = new MatOfDMatch();
		objMatcher.match(nDescriptors, matches);
		List<DMatch> objMatch = matches.toList();
		objMatch.sort((a, b) -> {
			return (int) (b.distance - a.distance);
		});

		// calculate clusters
		if (retainedKP!= null) {
			Point[] pts = new Point[retainedKP.length];
			for (int i = 0; i < retainedKP.length; i++) {
				pts[i] = retainedKP[i].pt;
			}
			Mat labels = cluster(new MatOfPoint(pts), maxClusters.get(),
					attempts.get());
			
			
			ArrayList<ArrayList<Point>> clusters = groupClusters(labels, pts,
					maxClusters.get());
			//get cluster with highest response
			for(int i=0; i<pts.length;i++){
				double clusterId = labels.get(i, 0)[0];
				
				
			}
			drawClusters(img, clusters);

			// split key points
			int k = Math.min(getK().get(), objMatch.size());
			KeyPoint[] lMKeyPts = new KeyPoint[k];
			Point[] points = new Point[k];
			DMatch[] m = new DMatch[k];
			for (int i = 0; i < k; i++) {
				m[i] = objMatch.get(i);
				lMKeyPts[i] = retainedKP[objMatch.get(i).queryIdx];
				points[i] = lMKeyPts[i].pt;
			}

			// draw key points
			Features2d.drawKeypoints(frame, new MatOfKeyPoint(retainedKP), frame,
					CVUtils.BLACK, Features2d.DRAW_OVER_OUTIMG);
			Features2d.drawKeypoints(frame, new MatOfKeyPoint(lMKeyPts), frame,
					CVUtils.GREEN, Features2d.DRAW_OVER_OUTIMG);
			Features2d.drawKeypoints(frame, new MatOfKeyPoint(removedKP), frame,
					CVUtils.RED, Features2d.DRAW_OVER_OUTIMG);
		}
		// CVUtils.drawEnclosingCircle(frame, new MatOfPoint(points),
		// CVUtils.RED);
		// Features2d.drawMatches(img, keypoints, refImg, this.keypoints, new
		// MatOfDMatch(m), res);
		return frame;
	}

	public UIProperty<Integer> getK() {
		return maxClusters;
	}

	@Override
	public void addObjectRefference(Mat obj) {
		Mat refImg = new Mat();
		Imgproc.cvtColor(obj, refImg, Imgproc.COLOR_RGB2GRAY);
		MatOfKeyPoint keypoints = new MatOfKeyPoint();
		detector.detect(refImg, keypoints);
		List<Mat> descriptors = new ArrayList<>();
		descriptors.add(new Mat());
		extractor.compute(refImg, keypoints, descriptors.get(0));
		objMatcher.add(descriptors);
	}

	@Override
	public void addBackgroundRefference(Mat bg) {
		Mat refImg = new Mat();
		Imgproc.cvtColor(bg, refImg, Imgproc.COLOR_RGB2GRAY);
		MatOfKeyPoint keypoints = new MatOfKeyPoint();
		detector.detect(refImg, keypoints);
		List<Mat> descriptors = new ArrayList<>();
		descriptors.add(new Mat());
		extractor.compute(refImg, keypoints, descriptors.get(0));
		bgMatcher.add(descriptors);
		bgEmpty = false;
	}

}
