package imageproc;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;

import javafx.animation.AnimationTimer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;

public class VideoView extends HBox {

	private long starttime;
	private WritableImage lastFrame;
	private MediaView view;
	private ObjectTracker tracker;

	public VideoView(String source) throws MalformedURLException {
		view = new MediaView(new MediaPlayer(new Media(new File(source).toURI().toString())));
		tracker = new ObjectTracker();
	}

	public void play() {
		view.getMediaPlayer().play();
		starttime = System.currentTimeMillis();
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
					starttime = now;
					bfirst = false;
					lastFrame = curframe;
					setWidth(curframe.getWidth());
					setHeight(curframe.getHeight());
					drawImage(curframe);
				} else {
					double curtime = (now - starttime) / 1000.0;
					System.out.println(curtime);
					Rectangle rect;
					try {
						rect = tracker.track(SwingFXUtils.fromFXImage(lastFrame, null),
								SwingFXUtils.fromFXImage(curframe, null));
						drawImage(lastFrame, rect);
						System.out.println(rect);
					} catch (Exception e) {
						e.printStackTrace();
						drawImage(lastFrame);
					}
				}
				lastFrame = curframe;
				start();
			}

		};
		timer.start();

	}

	private void drawImage(WritableImage frame, Rectangle bounds) {
		Canvas c = new Canvas(frame.getWidth(), frame.getHeight());
		GraphicsContext gc = c.getGraphicsContext2D();
		gc.drawImage(frame, 0, 0);
		gc.setFill(Color.RED);
		gc.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
		if (getChildren().size() > 0)
			getChildren().remove(0);
		getChildren().add(c);
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

}
