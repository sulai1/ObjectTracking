package com.sulai.imageproc;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

public class CVThreshTracker extends AbstractTracker {

	private CheckBox bContours;
	private CheckBox showAll;
	private Pair<DoubleProperty, IntegerProperty> threshold;
	private Pair<DoubleProperty, BooleanProperty> blur;

	public CVThreshTracker(Pane parent) {
		super(parent);

		threshold = threshChooser("Threashold");
		blur = blurChooser("Blur");

		// create blur option
		bContours = new CheckBox("Contours");
		parent.getChildren().add(bContours);

		// show all option
		showAll = new CheckBox("Show All");
		parent.getChildren().add(showAll);

	}


	@Override
	public void start(Mat frame) {

	}

	@Override
	public Mat trackObj(Mat f1) {
		Mat frame1 = new Mat(); 
		Imgproc.cvtColor(f1, frame1, Imgproc.COLOR_RGB2GRAY);
		// apply optional blur
		if (blur.getValue().get()) {
			Imgproc.blur(frame1, frame1, new Size(blur.getKey().get(), blur.getKey().get()));
		}

		// apply threshold and remove small blobs
		Mat thresh = new Mat();
		Imgproc.threshold(frame1, thresh, threshold.getKey().get(), 255, threshold.getValue().get());

		// find contours
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

		// optionally draw the contours
		if (bContours.isSelected()) {
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
			if (showAll.isSelected())
				CVUtils.drawEnclosingCircle(f1, c,CVUtils.RED);
			// draw every circle
		}

		// draw biggest circle
		if (maxContour != null) {
			CVUtils.drawEnclosingCircle(f1, maxContour,CVUtils.RED);
		}
		return f1;
	}

	

}
