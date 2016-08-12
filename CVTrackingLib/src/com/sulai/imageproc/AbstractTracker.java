package com.sulai.imageproc;

import java.util.ArrayList;
import java.util.Random;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;

import com.sulai.gui.UIProperty;

public abstract class AbstractTracker {
	public static final Scalar[] colors;

	static {
		colors = new Scalar[255];
		Random rnd = new Random();
		for (int i = 0; i < 255; i++) {
			colors[i] = new Scalar(rnd.nextInt(256), rnd.nextInt(256),
					rnd.nextInt(256));
		}
	}

	protected UIProperty<Boolean> showDebug = new UIProperty<>(false);
	private UIProperty<Long> performance = new UIProperty<Long>(0L);
	
	public abstract void start(Mat frame);

	public abstract void addObjectRefference(Mat obj);
		
	public abstract void addBackgroundRefference(Mat bg);

	public UIProperty<Boolean> getShowDebug() {
		return showDebug;
	}
	
	
	public Mat track(Mat frame1) {
		long startTime = System.nanoTime();
		Mat res = trackObj(frame1.clone());
		performance.set(System.nanoTime() - startTime);
		return res;
	}

	public abstract Mat trackObj(Mat frame1);

	public UIProperty<Long> getPerformance() {
		return performance;
	}

	public static Mat cluster(MatOfPoint corners, int maxCorners, int attempts) {
		Mat labels = new Mat();
		TermCriteria criteria = new TermCriteria(TermCriteria.EPS
				+ TermCriteria.MAX_ITER, 100, 1.0);
		MatOfPoint centers = new MatOfPoint();
		Mat c = new Mat();
		corners.convertTo(c, CvType.CV_32FC2);
		Core.kmeans(c, Math.min(maxCorners, corners.rows()), labels,
				criteria, attempts, Core.KMEANS_PP_CENTERS, centers);
		return labels;
	}
	public static void drawClusters(Mat f1, ArrayList<ArrayList<Point>> clusters) {
		for(int i=0; i<clusters.size();i++){
			if(!clusters.get(i).isEmpty())
				CVUtils.drawEnclosingCircle(f1, new MatOfPoint(clusters.get(i).toArray(new Point[clusters.get(i).size()])), colors[i]);
		}
	}

	/**
	 * 
	 * @param labels
	 * @param cArr
	 * @param maxCorners
	 * @return
	 */
	public static ArrayList<ArrayList<Point>> groupClusters(Mat labels, Point[] cArr, int maxCorners) {
		ArrayList<ArrayList<Point>> clusters = new ArrayList<ArrayList<Point>>();
		for(int i=0; i<maxCorners;i++)
			clusters.add(new ArrayList<Point>());
		for (int i = 0; i < labels.rows(); i++) {
			int index = (int)labels.get(i, 0)[0];
			clusters.get(index).add(cArr[i]);
		}
		return clusters;
	}

}
