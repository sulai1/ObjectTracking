package com.sulai.imageproc;

import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import javafx.scene.layout.Pane;

public class CVCornerTracker extends AbstractTracker {

	private static final Scalar COLOR = new Scalar(0,0,255);
	public CVCornerTracker(Pane parent) throws IOException {
		super(parent);

	}

	@Override
	public void start(Mat frame) {
	}

	@Override
	public Mat trackObj(Mat f1) {
		Mat frame1 = new Mat(); 
		Imgproc.cvtColor(f1, frame1, Imgproc.COLOR_RGB2GRAY);

		// corners of the current frame
		MatOfPoint corners = new MatOfPoint();
		Imgproc.goodFeaturesToTrack(frame1, corners , 50, 0.1, 0.01);
		for(Point p:corners.toArray()){
			Imgproc.circle(f1, p , 2, COLOR);
		}
//		Features2d.drawKeypoints(f1, keypoints,
//				f1,COLOR, 1);
		return f1;
	}
}
