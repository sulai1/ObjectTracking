package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import com.sulai.gui.TrackingBenchmark;

import imageproc.AbstractTracker;
import imageproc.CVCornerTracker;
import imageproc.CVDiffTracker;
import imageproc.CVThreshTracker;
import imageproc.CVUtils;
import imageproc.MultiThreshTracker;
import imageproc.OrbTracker;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
	private static final String[] SAMPLES = { "res\\sphero_still_cam.mp4", "res\\sphero_moving_cam.mp4",
			"res\\query.png", "res/samples/sphero1.png", "res/samples/sphero2.png", "res/samples/sphero3.png" , "res/samples/sphero4.png"};

	@Override
	public void start(Stage primaryStage) {
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

			BorderPane root = new BorderPane();
			Scene scene = new Scene(root, 800, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();

			TabPane pane = new TabPane();
			root.setBottom(pane);
			ArrayList<AbstractTracker> tracker = new ArrayList<>();

			Tab diff = new Tab("Diff");
			VBox box = new VBox();
			diff.setContent(box);
			pane.getTabs().add(diff);
			tracker.add(new CVDiffTracker(box));

			Tab thresh = new Tab("Thresh");
			VBox box1 = new VBox();
			thresh.setContent(box1);
			pane.getTabs().add(thresh);
			tracker.add(new CVThreshTracker(box1));

			Tab corner = new Tab("Corner");
			VBox box2 = new VBox();
			corner.setContent(box2);
			pane.getTabs().add(corner);
			tracker.add(new CVCornerTracker(box2));

			Tab mThresh = new Tab("MultiThresh");
			VBox box3 = new VBox();
			mThresh.setContent(box3);
			pane.getTabs().add(mThresh);
			tracker.add(new MultiThreshTracker(box3));

			Tab orb = new Tab("ORB");
			VBox box4 = new VBox();
			orb.setContent(box4);
			pane.getTabs().add(orb);
			
			//read the samples
			Mat[] samples = new Mat[5];

			BufferedImage image = ImageIO.read(new File(SAMPLES[2]));
			samples[0] = CVUtils.bufferedImageToMat(image);

			image = ImageIO.read(new File(SAMPLES[3]));
			samples[1] = CVUtils.bufferedImageToMat(image);

			image = ImageIO.read(new File(SAMPLES[4]));
			samples[2] = CVUtils.bufferedImageToMat(image);

			image = ImageIO.read(new File(SAMPLES[5]));
			samples[3] = CVUtils.bufferedImageToMat(image);

			image = ImageIO.read(new File(SAMPLES[6]));
			samples[4] = CVUtils.bufferedImageToMat(image);
			
			tracker.add(new OrbTracker(box4,samples));

			// TrackingBenchmark benchmark = new TrackingBenchmark(SAMPLES[0],
			// new CVCornerTracker("res/samples",settings));
			// TrackingBenchmark benchmark = new TrackingBenchmark(SAMPLES[0],
			// new CVThreshTracker(settings));
			TrackingBenchmark benchmark = new TrackingBenchmark(SAMPLES[1], tracker.get(0));
			benchmark.start();
			root.setCenter(benchmark);
			pane.getSelectionModel().selectedItemProperty().addListener(c -> {
				benchmark.setTracker(tracker.get(pane.getSelectionModel().getSelectedIndex()));
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
