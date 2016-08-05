package imageproc;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.opencv.core.Mat;
import org.opencv.features2d.FeatureDetector;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

public abstract class AbstractTracker {

	private Pane parent;
	private Label label;

	public AbstractTracker(Pane parent) {
		this.parent = parent;
		label = new Label();
		parent.getChildren().add(label);
	}

	public Pane getParent() {
		return parent;
	}

	public abstract void start(Mat frame);

	public Mat track(Mat frame1) {
		long startTime = System.nanoTime();
		Mat res = trackObj(frame1.clone());
		long stopTime = System.nanoTime();
		label.setText("" + (stopTime - startTime));
		return res;
	}

	public abstract Mat trackObj(Mat frame1);

	// **************************************************//

	protected Pair<DoubleProperty, IntegerProperty> threshChooser(String name) {

		// ad threshold type chooser
		ComboBox<String> typeChooser = new ComboBox<String>(CVUtils.observableThreshList());
		SimpleIntegerProperty type = new SimpleIntegerProperty(0);
		typeChooser.getSelectionModel().selectedItemProperty().addListener(c -> {
			type.set(CVUtils.getThreshType(typeChooser.getSelectionModel().getSelectedIndex()));
		});
		parent.getChildren().add(typeChooser);

		// threshold label and slider
		Label label = new Label(name);
		parent.getChildren().add(label);
		Slider threshSlider = new Slider(1, 255, 50);
		threshSlider.setShowTickMarks(true);
		parent.getChildren().add(threshSlider);

		return new Pair<>(threshSlider.valueProperty(), type);
	}

	protected Pair<DoubleProperty, BooleanProperty> blurChooser(String name) {
		// create blur option
		CheckBox bBlur = new CheckBox(name);
		parent.getChildren().add(bBlur);
		Slider blur = new Slider(2, 32, 2);
		blur.setShowTickMarks(true);
		parent.getChildren().add(blur);
		return new Pair<DoubleProperty, BooleanProperty>(blur.valueProperty(), bBlur.selectedProperty());
	}

	protected class DetectorParams {
		
		private PrintStream out;
		public DetectorParams(String filename) throws FileNotFoundException {
			FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
			detector.write(filename);
		}
		
		protected void writeParams() {

		}

	}
}
