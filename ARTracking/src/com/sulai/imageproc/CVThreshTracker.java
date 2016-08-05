package com.sulai.imageproc;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import com.sulai.gui.UIProperty;

public class CVThreshTracker extends AbstractTracker {

	private UIProperty<Double> thresh = new UIProperty<>(125.);
	private UIProperty<Double> blur = new UIProperty<>(1.);
	private UIProperty<Boolean> bContours = new UIProperty<>(false);
	private UIProperty<Boolean> showAll = new UIProperty<>(false);
	
	@Override
	public void start(Mat frame) {

	}

	@Override
	public Mat trackObj(Mat f1) {
		Mat frame1 = new Mat(); 
		Imgproc.cvtColor(f1, frame1, Imgproc.COLOR_RGB2GRAY);
		// apply optional blur
		if (true) {
			Imgproc.blur(frame1, frame1, new Size(blur.get(), blur.get()));
		}

		// apply threshold and remove small blobs
		Mat thresh = new Mat();
		Imgproc.threshold(frame1, thresh, this.thresh.get(), 255, 0);

		// find contours
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

		// optionally draw the contours
		if (bContours.get()) {
			Imgproc.drawContours(f1, contours, -1, CVUtils.RED);
		}

		// find biggest contour
		double maxArea = 0;
		MatOfPoint maxContour = null;
		for (int i = 1; i < contours.size(); i++) { // the first one seems to be
													// not usable
			MatOfPoint c = contours.get(i);

			// find the biggest contour
			Moments m = Imgproc.moments(c);
			if (m.m00 > maxArea) {
				maxArea = m.m00;
				maxContour = c;
			}
			if (showAll.get())
				CVUtils.drawEnclosingCircle(f1, c,CVUtils.RED);
			// draw every circle
		}

		// draw biggest circle
		if (maxContour != null) {
			CVUtils.drawEnclosingCircle(f1, maxContour,CVUtils.RED);
		}
		return f1;
	}


	public UIProperty<Double> getThresh() {
		return thresh;
	}
	
	public UIProperty<Double> getBlur() {
		return blur;
	}
	
	
	

	

}
