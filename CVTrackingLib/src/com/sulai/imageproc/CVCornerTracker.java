package com.sulai.imageproc;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.sulai.gui.UIProperty;

public class CVCornerTracker extends AbstractTracker {

	public UIProperty<Integer> maxCorners = new UIProperty<>(50);
	public UIProperty<Double> qualityLevel = new UIProperty<>(0.01);
	public UIProperty<Double> minDistance = new UIProperty<>(0.01);

	private static final Scalar COLOR = new Scalar(0, 0, 255);

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
		for (Point p : corners.toArray()) {
			Imgproc.circle(f1, p, 2, COLOR);
		}
		// Features2d.drawKeypoints(f1, keypoints,
		// f1,COLOR, 1);
		return f1;
	}
	//*********************GETTER 4 PROPRTIES'''''''''''''''''''//

	public UIProperty<Integer> getMaxCorners() {
		return maxCorners;
	}

	public UIProperty<Double> getQualityLevel() {
		return qualityLevel;
	}

	public UIProperty<Double> getMinDistance() {
		return minDistance;
	}

}
