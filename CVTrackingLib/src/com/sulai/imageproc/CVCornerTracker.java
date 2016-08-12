package com.sulai.imageproc;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import com.sulai.gui.UIProperty;

public class CVCornerTracker extends AbstractTracker {

	public UIProperty<Integer> maxCorners = new UIProperty<>(50);
	public UIProperty<Integer> attempts = new UIProperty<>(4);
	public UIProperty<Double> qualityLevel = new UIProperty<>(0.01);
	public UIProperty<Double> minDistance = new UIProperty<>(0.01);

	@Override
	public void start(Mat frame) {
	}

	@Override
	public Mat trackObj(Mat f1) {
		Mat frame1 = new Mat();
		Imgproc.cvtColor(f1, frame1, Imgproc.COLOR_RGB2GRAY);

		// corners of the current frame
		MatOfPoint corners = new MatOfPoint();
		Imgproc.goodFeaturesToTrack(frame1, corners, 50, 0.1, 0.01);

		// calculate clusters
		Mat labels = cluster(corners);


		if (labels.empty())
			return f1;
		
		//group clusters than draw them
		ArrayList<ArrayList<Point>> clusters = groupClusters(f1, labels, corners.toArray());
		drawClusters(f1, clusters);
		
		return f1;
	}

	private Mat cluster(MatOfPoint corners) {
		Mat labels = new Mat();
		TermCriteria criteria = new TermCriteria(TermCriteria.EPS
				+ TermCriteria.MAX_ITER, 100, 1.0);
		MatOfPoint centers = new MatOfPoint();
		Mat c = new Mat();
		corners.convertTo(c, CvType.CV_32FC2);
		Core.kmeans(c, Math.min(maxCorners.get(),corners.rows()), labels, criteria, attempts.get(), Core.KMEANS_PP_CENTERS, centers);
		return labels;
	}

	private ArrayList<ArrayList<Point>> groupClusters(Mat f1, Mat labels, Point[] cArr) {
		ArrayList<ArrayList<Point>> clusters = new ArrayList<ArrayList<Point>>();
		for(int i=0; i<maxCorners.get();i++)
			clusters.add(new ArrayList<Point>());
		for (int i = 0; i < labels.rows(); i++) {
			int index = (int)labels.get(i, 0)[0];
			Imgproc.circle(f1, cArr[i], 3, colors[index]);
			clusters.get(index).add(cArr[i]);
		}
		return clusters;
	}

	// *********************GETTER 4 PROPRTIES'''''''''''''''''''//

	public UIProperty<Integer> getAttempts(){
		return attempts;
	}
	
	public UIProperty<Integer> getMaxCorners() {
		return maxCorners;
	}

	public UIProperty<Double> getQualityLevel() {
		return qualityLevel;
	}

	public UIProperty<Double> getMinDistance() {
		return minDistance;
	}

	@Override
	public void addObjectRefference(Mat obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addBackgroundRefference(Mat bg) {
		// TODO Auto-generated method stub
		
	}

}
