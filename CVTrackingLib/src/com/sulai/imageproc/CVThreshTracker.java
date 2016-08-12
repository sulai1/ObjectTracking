package com.sulai.imageproc;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
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
	private UIProperty<Integer> colorChannel = new UIProperty<Integer>(6);
	private String[] labels = { "R", "G", "B", "H", "S", "V" };

	public UIProperty<Boolean> getShowContours() {
		return bContours;
	}

	@Override
	public void start(Mat frame) {

	}

	@Override
	public Mat trackObj(Mat f1) {
		Mat frame1;
		switch (colorChannel.get()) {
		case 0:
			frame1 = getRGBChannel(f1, 0);
			break;
		case 1:
			frame1 = getRGBChannel(f1, 1);
			break;
		case 2:
			frame1 = getRGBChannel(f1, 2);
			break;
		case 3:
			frame1 = getHSVChannel(f1, 0);
			break;
		case 4:
			frame1 = getHSVChannel(f1, 1);
			break;
		default:
			frame1 = new Mat();
			Imgproc.cvtColor(f1, frame1, Imgproc.COLOR_RGB2GRAY);
			break;

		}
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

		if (showDebug.get())
			f1 = frame1;
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
			if (showDebug.get())
				CVUtils.drawEnclosingCircle(f1, c, CVUtils.RED);
			// draw every circle
		}

		// draw biggest circle
		if (maxContour != null) {
			CVUtils.drawEnclosingCircle(f1, maxContour, CVUtils.RED);
		}
		return f1;
	}

	private Mat getHSVChannel(Mat f1, int channel) {
		Mat res = new Mat();
		Imgproc.cvtColor(f1, res, Imgproc.COLOR_RGB2HSV);
		List<Mat> channels = new ArrayList<>();
		Core.split(res, channels);
		return channels.get(channel);
	}

	private Mat getRGBChannel(Mat f1, int channel) {
		Mat frame1;
		List<Mat> channels = new ArrayList<>();
		Core.split(f1, channels);
		frame1 = channels.get(channel);
		return frame1;
	}

	public UIProperty<Double> getThresh() {
		return thresh;
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

	public UIProperty<Integer> getColorChannel() {
		return colorChannel;
	}

	public String[] getLabels() {
		return labels;
	}

}
