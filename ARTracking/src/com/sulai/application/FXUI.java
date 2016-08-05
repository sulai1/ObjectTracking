package com.sulai.application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import com.sulai.gui.UI;
import com.sulai.gui.UIProperty;
import com.sulai.imageproc.AbstractTracker;
import com.sulai.imageproc.CVUtils;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

public class FXUI extends Application implements UI {
	private static final String[] SAMPLES = { "res\\sphero_still_cam.mp4", "res\\sphero_moving_cam.mp4",
			"res\\query.png", "res/samples/sphero1.png", "res/samples/sphero2.png", "res/samples/sphero3.png",
			"res/samples/sphero4.png" };

	private ArrayList<VBox> tabs = new ArrayList<>();

	private TabPane pane = new TabPane();
	private List<AbstractTracker> trackers = new ArrayList<>();

	@Override
	public void start(Stage primaryStage) {
		try {
			// FXSetup
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

			BorderPane root = new BorderPane();
			Scene scene = new Scene(root, 800, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();

			pane = new TabPane();
			root.setBottom(pane);

			// UI setup
			Mat[] samples = new Mat[5];
			for (int i = 0; i < samples.length - 2; i++) {
				samples[i] = CVUtils.bufferedImageToMat(ImageIO.read(new File(SAMPLES[i + 2])));
			}
			trackers = UI.initUI(samples, this);
			TrackingBenchmark benchmark = new TrackingBenchmark(SAMPLES[0], trackers.get(0));
			pane.getSelectionModel().selectedItemProperty().addListener(c -> {
				benchmark.setTracker(trackers.get(pane.getSelectionModel().getSelectedIndex()));
			});
			root.setCenter(benchmark);

			HBox player = new HBox();
			root.setTop(player);
			initPlayer(player, benchmark);
			benchmark.start();
			root.setBottom(pane);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void initPlayer(HBox player, TrackingBenchmark benchmark) {
		// player setup
		MediaPlayer mp = benchmark.getView().getMediaPlayer();
		
		// play button
		Button playButton = new Button(">");
		playButton.setOnAction(c -> {
			switch(mp.getStatus()){
			case DISPOSED:
				break;
			case HALTED:
				break;
			case PAUSED:
				mp.play();
				break;
			case PLAYING:
				mp.pause();
				break;
			case READY:
				mp.play();
				break;
			case STALLED:
				break;
			case STOPPED:
				mp.play();
				break;
			case UNKNOWN:
				break;
			default:
				break;
			}
		});
		player.getChildren().add(playButton);
		
		//stop button
		Button stopButton = new Button("#");
		stopButton.setOnAction(c->{
			mp.stop();
		});
		player.getChildren().add(stopButton);
	}

	@Override
	public void createTab(AbstractTracker tracker, String name) {
		VBox box = new VBox();
		tabs.add(box);

		Tab tab = new Tab(name);
		tab.setContent(box);

		pane.getTabs().add(tab);
		trackers.add(tracker);
	}

	@Override
	public void createUIInt(int index, String name, UIProperty<Integer> property, int min, int max) {
		Label label = new Label(name);
		Slider slider = new Slider(min, max, property.get());
		slider.valueProperty().addListener(c -> {
			property.set((int) slider.getValue());
		});
		this.tabs.get(index).getChildren().add(label);
		this.tabs.get(index).getChildren().add(slider);

	}

	@Override
	public void createUIDouble(int index, String name, UIProperty<Double> property, double min, double max) {
		Label label = new Label(name);
		Slider slider = new Slider(min, max, property.get());
		slider.valueProperty().addListener(c -> {
			property.set(slider.getValue());
		});
		tabs.get(index).getChildren().add(label);
		tabs.get(index).getChildren().add(slider);
	}

	@Override
	public void createUIBool(int index, String name, UIProperty<Boolean> property) {
		CheckBox box = new CheckBox(name);
		box.selectedProperty().addListener(c -> {
			property.set(box.isSelected());
		});
		tabs.get(index).getChildren().add(box);
	}

	public static void main(String[] args) {
		launch(args);
	}

}
