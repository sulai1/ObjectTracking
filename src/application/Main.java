package application;
	
import org.opencv.core.Core;

import gui.TrackingBenchmark;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class Main extends Application {
	private static final String SPHERO_MOVING = "res\\sphero_moving_cam.mp4";
	private static final String SPHERO_STILL = "res\\sphero_still_cam.mp4";

	@Override
	public void start(Stage primaryStage) {
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,400,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
			TrackingBenchmark benchmark = new TrackingBenchmark(SPHERO_MOVING, "res/samples");
			benchmark.start();
			root.setCenter(benchmark);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
