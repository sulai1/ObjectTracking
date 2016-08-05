package com.sulai.imageproc;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import com.sulai.gui.UIProperty;

public class OrbTracker extends AbstractTracker {

	private FeatureDetector detector;
	private DescriptorExtractor extractor;
	private DescriptorMatcher matcher;
	
	private UIProperty<Integer> k = new UIProperty<Integer>(10);

	public OrbTracker(Mat[] samples) {
		//init matching
		detector = FeatureDetector.create(FeatureDetector.ORB);
		extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

		//train matcher
		for (int i = 0; i < 1; i++) {
			Mat refImg = new Mat();
			Imgproc.cvtColor(samples[i], refImg, Imgproc.COLOR_RGB2GRAY);
			MatOfKeyPoint keypoints = new MatOfKeyPoint();
			detector.detect(refImg, keypoints );
			List<Mat> descriptors = new ArrayList<>();
			descriptors .add(new Mat());
			extractor.compute(refImg, keypoints, descriptors.get(0));
			matcher.add(descriptors);
		}
		matcher.train();
	}

	@Override
	public void start(Mat frame) {
	}

	@Override
	public Mat trackObj(Mat frame) {
		Mat img = new Mat();
		Imgproc.cvtColor(frame, img, Imgproc.COLOR_RGB2GRAY);
		MatOfKeyPoint keypoints = new MatOfKeyPoint();
		detector.detect(img, keypoints);
		Mat descriptors = new Mat();
		extractor.compute(img, keypoints, descriptors);
		MatOfDMatch matches = new MatOfDMatch();
		matcher.match(descriptors, matches);
		List<DMatch> lMatch = matches.toList();
		lMatch.sort((a, b) -> {
			return (int) (b.distance - a.distance);
		});

		int k = Math.min(getK().get(), lMatch.size());
		KeyPoint[] lKeyPts = keypoints.toArray();
		KeyPoint[] lMKeyPts = new KeyPoint[k];
		DMatch[] m = new DMatch[k];
		for (int i = 0; i < k; i++) {
			m[i] = lMatch.get(i);
			lMKeyPts[i] = lKeyPts[lMatch.get(i).queryIdx];
		}
		Features2d.drawKeypoints(frame, new MatOfKeyPoint(lKeyPts), frame, CVUtils.BLACK,
				Features2d.DRAW_OVER_OUTIMG);
		Features2d.drawKeypoints(frame, new MatOfKeyPoint(lMKeyPts), frame, CVUtils.RED,
				Features2d.DRAW_OVER_OUTIMG);
		// Features2d.drawMatches(img, keypoints, refImg, this.keypoints, new
		// MatOfDMatch(m), res);
		return frame;
	}

	public UIProperty<Integer> getK() {
		return k;
	}

}
