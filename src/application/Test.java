package application;

import java.io.IOException;

import org.opencv.core.Core;
import org.opencv.features2d.FeatureDetector;

import gui.DetectorTester;
import imageproc.CVFeatureExtractor;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Test extends Application {
	private static final int HEIGHT = 800;
	private static final int WIDTH = 1000;
	private static final String PARAMS = "res/orb.YAML";
	private static final String SAMPLES = "res/samples";
	private static final String SPHERO = "res/board1.jpg";
	private static final String SPHERO1 = "res/samples/sphero3.png";
	private static final String SPHERO2 = "res/samples/sphero4.png";


	@Override
	public void start(Stage primaryStage) throws IOException {

		BorderPane root = new BorderPane();
		Scene scene = new Scene(root, 400, 400);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();
		
		CVFeatureExtractor ext = new CVFeatureExtractor(FeatureDetector.FAST,FeatureDetector.ORB);
		DetectorTester tester = new DetectorTester(ext,WIDTH,HEIGHT);
		tester.testMatch(SPHERO, SPHERO1);
//		tester.testTraining(SAMPLES, SPHERO);
		root.setCenter(tester);
	}

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch(args);
	}
}
