package com.sulai.imageproc;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MultiThreshTracker extends AbstractTracker {

	private ComboBox<String> typeChooser;
	private Slider threshold;
	private CheckBox bBlur;
	private Slider blur;
	private CheckBox bContours;
	private CheckBox showAll;
	private int type;

	public MultiThreshTracker(Pane parent) {
		super(parent);

		// threshold
		Label label = new Label("Threshold");
		parent.getChildren().add(label);
		// ad threshold type chooser
		typeChooser = new ComboBox<String>(CVUtils.observableThreshList());
		typeChooser
				.getSelectionModel()
				.selectedItemProperty()
				.addListener(
						c -> {
							type = CVUtils.getThreshType(typeChooser
									.getSelectionModel().getSelectedIndex());
						});
		parent.getChildren().add(typeChooser);
		// create threshold slider
		threshold = new Slider(0, 255, 64);
		threshold.setShowTickMarks(true);
		parent.getChildren().add(threshold);

		// create blur option
		bBlur = new CheckBox("Blur");
		parent.getChildren().add(bBlur);
		blur = new Slider(2, 32, 2);
		parent.getChildren().add(blur);

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
		int blur = (int) this.blur.getValue();
		if (bBlur.isSelected()) {
			Imgproc.blur(frame1, frame1, new Size(blur, blur));
		}

		// apply threshold and remove small blobs
		Mat thresh = new Mat();
		for (int i = 0; i < 255; i += threshold.getValue()) {
			Imgproc.threshold(frame1, thresh, i, i, type);
			List<MatOfPoint> contours = new ArrayList<>();
			Mat hierarchy = new Mat();
			Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_TREE,
					Imgproc.CHAIN_APPROX_SIMPLE);
			Imgproc.drawContours(f1, contours, -1, CVUtils.RED);
		}
		return f1;
	}

}
