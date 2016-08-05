package imageproc;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.sulai.gui.UIProperty;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

public class CVDiffTracker extends AbstractTracker {

	private static final Scalar COLOR = new Scalar(0, 0, 255);
	private Mat refframe = null;
	private UIProperty<Double> thresh;
	private Pair<DoubleProperty, IntegerProperty> threshold;
	private Pair<DoubleProperty, BooleanProperty> blur;

	public CVDiffTracker(Pane parent) {
		super(parent);
		threshold = threshChooser("Threashold");
		blur = blurChooser("Blur");
	}


	@Override
	public void start(Mat frame) {
	}

	@Override
	public Mat trackObj(Mat frame) {
		if (refframe == null) {
			refframe = new Mat();
			Imgproc.cvtColor(frame, refframe, Imgproc.COLOR_RGB2GRAY);
			return frame;
		}
		Mat f1 = frame.clone();
		Mat frame1 = new Mat();
		Imgproc.cvtColor(f1, frame1, Imgproc.COLOR_RGB2GRAY);
		Mat res = new Mat();
		// calculate diff and threshold
		Core.absdiff(frame1, refframe, res);
		if(blur.getValue().get())
			Imgproc.blur(res, res, new Size(blur.getKey().get(), blur.getKey().get()));
		Imgproc.threshold(res, res, threshold.getKey().get(), 255, threshold.getValue().get());

		// calculate the contours
		ArrayList<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(res, contours, new Mat(), Imgproc.RETR_TREE,
				Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.drawContours(f1, contours, -1, COLOR);
		int l = Math.min(contours.size(), 200);
		for (int i=0;i < l;i++) {
			MatOfPoint c = contours.get(i);
			Point center = new Point();
			float[] radius = new float[1];
			MatOfPoint2f contours2f = new MatOfPoint2f();
			c.convertTo(contours2f, CvType.CV_32FC2);
			Imgproc.minEnclosingCircle(contours2f, center, radius);
			Imgproc.circle(f1, center, (int) radius[0], COLOR);
		}
		refframe = new Mat();
		Imgproc.cvtColor(frame, refframe, Imgproc.COLOR_RGB2GRAY);
		return f1;
	}

}
