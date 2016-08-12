package com.sulai.imageproc;


import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.sulai.gui.UIProperty;

public class MultiThreshTracker extends AbstractTracker {

	private int type;
	private UIProperty<Double> blur = new UIProperty<Double>(125.0);
	private UIProperty<Double> threshold = new UIProperty<Double>(125.0);

	@Override
	public void start(Mat frame) {

	}

	@Override
	public Mat trackObj(Mat f1) {
		Mat frame1 = new Mat();
		Imgproc.cvtColor(f1, frame1, Imgproc.COLOR_RGB2GRAY);
		// apply optional blur
		Imgproc.blur(frame1, frame1, new Size(blur.get(), blur.get()));

		// apply threshold and remove small blobs
		Mat thresh = new Mat();
		for (int i = 0; i < 255; i += threshold.get()) {
			Imgproc.threshold(frame1, thresh, i, i, type);
			List<MatOfPoint> contours = new ArrayList<>();
			Mat hierarchy = new Mat();
			Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_TREE,
					Imgproc.CHAIN_APPROX_SIMPLE);
			Imgproc.drawContours(f1, contours, -1, CVUtils.RED);
		}
		return f1;
	}

	public UIProperty<Double> getThesh() {
		return threshold;
	}

	public UIProperty<Double> getBlur() {
		return blur;
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
