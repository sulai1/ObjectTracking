package com.sulai.imageproc;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.sulai.gui.UIProperty;

public class ContourTracker extends AbstractTracker {

	private UIProperty<Integer> bTresh = new UIProperty<Integer>(200);
	private UIProperty<Integer> tTresh = new UIProperty<Integer>(250);

	@Override
	public void start(Mat frame) {

	}

	@Override
	public void addObjectRefference(Mat obj) {

	}

	@Override
	public void addBackgroundRefference(Mat bg) {

	}

	@Override
	public Mat trackObj(Mat frame1) {
		Mat res = new Mat();
		// / Convert image to gray and blur it
		Imgproc.cvtColor(frame1, res, Imgproc.COLOR_BGR2GRAY);
		Imgproc.blur(res, res, new Size(3, 3));
		Mat edges = new Mat();
		Imgproc.Canny(res, edges, bTresh.get(), tTresh.get());
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(res, contours,hierarchy , Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.drawContours(frame1, contours, -1, CVUtils.RED);
		return res;
	}

	public UIProperty<Integer> getbTresh() {
		return bTresh;
	}

	public UIProperty<Integer> gettTresh() {
		return tTresh;
	}

}
