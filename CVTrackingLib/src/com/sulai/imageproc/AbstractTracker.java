package com.sulai.imageproc;

import org.opencv.core.Mat;

import com.sulai.gui.UIProperty;

public abstract class AbstractTracker {


	private UIProperty<Long> performance = new UIProperty<Long>(0L);
	
	public abstract void start(Mat frame);

	public Mat track(Mat frame1) {
		long startTime = System.nanoTime();
		Mat res = trackObj(frame1.clone());
		performance.set(System.nanoTime()-startTime);
		return res;
	}

	public abstract Mat trackObj(Mat frame1);

	public UIProperty<Long> getPerformance() {
		return performance;
	}

}
