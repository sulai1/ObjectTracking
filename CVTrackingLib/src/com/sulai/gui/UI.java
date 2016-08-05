package com.sulai.gui;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Mat;

import com.sulai.imageproc.AbstractTracker;
import com.sulai.imageproc.CVCornerTracker;
import com.sulai.imageproc.CVDiffTracker;
import com.sulai.imageproc.CVThreshTracker;
import com.sulai.imageproc.MultiThreshTracker;
import com.sulai.imageproc.OrbTracker;

/**
 * UI is a abstraction layer for the user interface
 * 
 * @author sascha
 *
 */
public interface UI {
	
	public static List<AbstractTracker> initUI(Mat[] samples, UI ui) throws IOException {
		List<AbstractTracker> tracker = new LinkedList<>();
		CVDiffTracker t = new CVDiffTracker();
		ui.createTab(t, "Diff");
		ui.createUIDouble(0, "threshold", t.getThresh(), 0, 255);
		ui.createUIDouble(0, "blur", t.getBlur(), 1, 32);
		tracker.add(t);

		CVCornerTracker t2 = new CVCornerTracker();
		ui.createTab(t2, "Corner");
		ui.createUIInt(1, "maxCorners", t2.getMaxCorners(), 0, 255);
		ui.createUIDouble(1, "qualityLevel", t2.getQualityLevel(), 0.000001, 10);
		ui.createUIDouble(1, "minDistance", t2.getMinDistance(), 0.000001, 10);
		tracker.add(t2);

		CVThreshTracker t3 = new CVThreshTracker();
		ui.createTab(t3, "Threshold");
		ui.createUIDouble(2, "thresh", t3.getThresh(), 0, 255);
		ui.createUIDouble(2, "blur", t3.getBlur(), 1, 32);
		tracker.add(t3);

		MultiThreshTracker t4 = new MultiThreshTracker();
		ui.createTab(t4, "MultiThresh");
		ui.createUIDouble(3, "thresh", t4.getThesh(), 0, 255);
		ui.createUIDouble(3, "blur", t4.getBlur(), 1, 32);
		tracker.add(t4);

		// read the samples
		OrbTracker orbTracker = new OrbTracker(samples);
		ui.createTab(orbTracker, "ORB");
		ui.createUIInt(4, "k", orbTracker.getK(), 1, 500);
		tracker.add(orbTracker);
		return tracker;
	}

	void createTab(AbstractTracker tracker, String string);

	void createUIInt(int index, String name, UIProperty<Integer> property, int min, int max);

	void createUIDouble(int index, String name, UIProperty<Double> property, double min, double max);

	void createUIBool(int index, String name, UIProperty<Boolean> property);

}
