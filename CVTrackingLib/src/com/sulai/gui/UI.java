package com.sulai.gui;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Mat;

import com.sulai.imageproc.AbstractTracker;
import com.sulai.imageproc.CVCornerTracker;
import com.sulai.imageproc.CVDiffTracker;
import com.sulai.imageproc.CVThreshTracker;
import com.sulai.imageproc.ContourTracker;
import com.sulai.imageproc.MultiThreshTracker;
import com.sulai.imageproc.ObjectTracker;
import com.sulai.imageproc.OrbTracker;

/**
 * UI is a abstraction layer for the user interface
 * 
 * @author sascha
 *
 */
public interface UI {
	
	public static List<AbstractTracker> initUI(Mat[] objSamples,Mat[] bgSamples, UI ui) throws IOException {
		List<AbstractTracker> tracker = new LinkedList<>();
		CVDiffTracker t = new CVDiffTracker();
		ui.createTab(t, "Diff");
		ui.createLabel(0,"Performance", t.getPerformance());
		ui.createUIDouble(0, "threshold", t.getThresh(), 0, 255);
		ui.createUIDouble(0, "blur", t.getBlur(), 1, 32);
		tracker.add(t);

		CVCornerTracker t2 = new CVCornerTracker();
		ui.createTab(t2, "Corner");
		ui.createLabel(1,"Performance", t2.getPerformance());
		ui.createUIInt(1, "maxCorners", t2.getMaxCorners(), 1, 255);
		ui.createUIInt(1, "attempts", t2.getMaxCorners(), 1, 16);
		ui.createUIDouble(1, "qualityLevel", t2.getQualityLevel(), 0.000001, 10);
		ui.createUIDouble(1, "minDistance", t2.getMinDistance(), 0.000001, 10);
		tracker.add(t2);

		CVThreshTracker t3 = new CVThreshTracker();
		ui.createTab(t3, "Threshold");
		ui.createLabel(2,"Performance", t3.getPerformance());
		ui.createUIDouble(2, "thresh", t3.getThresh(), 0, 255);
		ui.createUIDouble(2, "blur", t3.getBlur(), 1, 32);
		ui.createUIBool(2, "show contours", t3.getShowContours());
		ui.createUIBool(2, "show debug", t3.getShowDebug());
		ui.createCombobox(2, t3.getLabels(), t3.getColorChannel());
		tracker.add(t3);

		MultiThreshTracker t4 = new MultiThreshTracker();
		ui.createTab(t4, "MultiThresh");
		ui.createLabel(3,"Performance", t4.getPerformance());
		ui.createUIDouble(3, "thresh", t4.getThesh(), 0, 255);
		ui.createUIDouble(3, "blur", t4.getBlur(), 1, 32);
		tracker.add(t4);

		// read the samples
		OrbTracker orbTracker = new OrbTracker(objSamples, bgSamples);
		ui.createTab(orbTracker, "ORB");
		ui.createLabel(4,"Performance", orbTracker.getPerformance());
		ui.createUIInt(4, "maxClusters", orbTracker.getMaxClusters(), 1, 32);
		ui.createUIInt(4, "attempts", orbTracker.getAttempts(), 1, 16);
		tracker.add(orbTracker);
		
		ObjectTracker t6 = new ObjectTracker(objSamples, bgSamples);
		ui.createTab(t6, "Tracker");
		ui.createLabel(5,"Performance", t6.getPerformance());
		ui.createUIInt(5, "maxClusters",t6.getMaxClusters() , 1, 16);
		ui.createUIInt(5, "kNearest",t6.getkNearest() , 1, 32);
		ui.createUIInt(5, "attempts",t6.getAttempts() , 1, 16);
		ui.createUIBool(5, "showAll", t6.getShowDebug());
		tracker.add(t6);
		
		ContourTracker t7 = new ContourTracker();
		ui.createTab(t7, "Canny");
		ui.createLabel(6,"Performance", t7.getPerformance());
		ui.createUIInt(6, "bThresh",t7.getbTresh() , 1, 255);
		ui.createUIInt(6, "tThresh",t7.gettTresh() , 1, 255);
		tracker.add(t7);
		return tracker;
	}

	void createLabel(int tab, String string, UIProperty<Long> performance);

	void createTab(AbstractTracker tracker, String string);
	
	void createCombobox(int tab, String[] labels, UIProperty<Integer> value);

	void createUIInt(int index, String name, UIProperty<Integer> property, int min, int max);

	void createUIDouble(int index, String name, UIProperty<Double> property, double min, double max);

	void createUIBool(int index, String name, UIProperty<Boolean> property);

}
