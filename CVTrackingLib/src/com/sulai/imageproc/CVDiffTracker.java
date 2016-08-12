package com.sulai.imageproc;


import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.sulai.gui.UIProperty;

public class CVDiffTracker extends AbstractTracker {

	private static final Scalar COLOR = new Scalar(0, 0, 255);
	private Mat refframe = null;
	private UIProperty<Double> thresh = new UIProperty<>(75.);
	private UIProperty<Double> blur = new UIProperty<>(1.);

	@Override
	public void start(Mat frame) {
	}

	@Override
	public Mat trackObj(Mat frame) {
		if (refframe == null) {
			refframe = new Mat();
			Imgproc.cvtColor(frame, refframe, Imgproc.COLOR_RGB2GRAY);
			return frame;
		}
		Mat f1 = frame.clone();
		Mat frame1 = new Mat();
		Imgproc.cvtColor(f1, frame1, Imgproc.COLOR_RGB2GRAY);
		Mat res = new Mat();
		// calculate diff and threshold
		Core.absdiff(frame1, refframe, res);
		Imgproc.blur(res, res, new Size(getBlur().get(), getBlur().get()));
		Imgproc.threshold(res, res, thresh.get(), 255, 0);

		// calculate the contours
		ArrayList<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(res, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		if(showDebug.get())
			Imgproc.drawContours(f1, contours, -1, COLOR);
		int l = Math.min(contours.size(), 200);
		for (int i = 0; i < l; i++) {
			MatOfPoint c = contours.get(i);
			Point center = new Point();
			float[] radius = new float[1];
			MatOfPoint2f contours2f = new MatOfPoint2f();
			c.convertTo(contours2f, CvType.CV_32FC2);
			Imgproc.minEnclosingCircle(contours2f, center, radius);
			Imgproc.circle(f1, center, (int) radius[0], COLOR);
		}
		return f1;
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

}
