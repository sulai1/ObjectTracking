package com.sulai.gui;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FXUI extends UI {

	private BorderPane root;
	private TabPane pane;
	private FXThread fxThread = null;
	private ArrayList<VBox> tabs = new ArrayList<>();

	@Override
	public void init(String args) {
		if (fxThread == null)
			fxThread = new FXThread(args);
	}

	private class FXThread extends Application {
		public FXThread(String args) {
			launch(args);
		}

		@Override
		public void start(Stage primaryStage) throws Exception {
			root = new BorderPane();
			Scene scene = new Scene(root, 800, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();

			pane = new TabPane();
			root.setBottom(pane);
		}

	}

	@Override
	public void createUIDouble(int index, String name, UIProperty<Double> property, double min, double max) {
		createMissingTabs(index);
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
		createMissingTabs(index);
		CheckBox box = new CheckBox(name);
		box.selectedProperty().addListener(c -> {
			property.set(box.isSelected());
		});
		tabs.get(index).getChildren().add(box);
	}

	private void createMissingTabs(int index) {
		while (index > pane.getTabs().size()) {
			pane.getTabs().add(new Tab());
			VBox vBox = new VBox();
			pane.getTabs().get(index).setContent(vBox);
			this.tabs.add(vBox);
		}
	}

}
