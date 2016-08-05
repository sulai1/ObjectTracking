package com.sulai.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import imageproc.AbstractTracker;
import imageproc.CVUtils;
import javafx.animation.AnimationTimer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

public class TrackingBenchmark extends VBox {

	private MediaView view;
	private AbstractTracker tracker;
	private Slider slider;

	public TrackingBenchmark(String source, AbstractTracker tracker) throws IOException {
		this.tracker = tracker;

		//Open Viodeo file
		MediaPlayer player = new MediaPlayer(new Media(new File(source).toURI().toString()));
		view = new MediaView(player);
		player.currentTimeProperty().addListener(c->{slider.valueProperty().set(player.getCurrentTime().toSeconds());});
		
		slider = new Slider(0,player.getMedia().getDuration().toSeconds(),0);
		this.getChildren().add(slider);
			//add to training set
//			tracker.add(bis);
//			tracker.train();
	}

	protected boolean isImage(String name) {
		switch (name.substring(name.length() - 3)) {
		case "png":
			return true;
		case "jpg":
			return true;
		default:
			return false;
		}
	}

	public void start() {
		view.getMediaPlayer().play();
		AnimationTimer timer = new AnimationTimer() {
			boolean bfirst = true;

			/**
			 * This handle is called every frame to capture it calculate the
			 * bounds of the object and draws a bounding around the object
			 * 
			 * @param now
			 */
			@Override
			public void handle(long now) {
				WritableImage curframe = view.snapshot(new SnapshotParameters(),
						SwingFXUtils.toFXImage(new BufferedImage(500, 500, BufferedImage.TYPE_BYTE_GRAY), null));
				if (bfirst) {
					bfirst = false;
					setWidth(curframe.getWidth());
					setHeight(curframe.getHeight());
					drawImage(curframe);
					tracker.start(CVUtils.bufferedImageToMat(SwingFXUtils.fromFXImage(curframe, null)));
				} else {
					BufferedImage img;
					try {
						img = CVUtils.toBufferedImageOfSize(tracker.track(CVUtils.bufferedImageToMat(SwingFXUtils.fromFXImage(curframe, null))),getWidth(),getHeight());
						drawImage(SwingFXUtils.toFXImage(img, null));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				start();
			}

		};
		timer.start();

	}

	private void drawImage(Image frame) {
		Canvas c = new Canvas(frame.getWidth(), frame.getHeight());
		GraphicsContext gc = c.getGraphicsContext2D();
		gc.drawImage(frame, 0, 0);
		if (getChildren().size() > 0)
			getChildren().remove(0);
		getChildren().add(c);
	}

	@Override
	public WritableImage snapshot(SnapshotParameters params, WritableImage image) {
		return super.snapshot(params, image);
	}

	public void setOnFrameChange() {
	}

	public void setTracker(AbstractTracker tracker) {
		this.tracker = tracker;
	}

}
