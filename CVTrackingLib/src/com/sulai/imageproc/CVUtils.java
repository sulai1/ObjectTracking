package com.sulai.imageproc;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class CVUtils {

	public static final Scalar RED = new Scalar(0, 0, 255);
	public static final Scalar BLUE = new Scalar(255, 0, 0);
	public static final Scalar GREEN = new Scalar(0, 255, 0);
	public static final Scalar BLACK = new Scalar(0, 0, 0);

	public static List<String> observableThreshList() {
		List<String> items = new ArrayList<>();
		items.add("Binary");
		items.add("Binary Inverse");
		items.add("Otsu");
		items.add("To Zero");
		items.add("Triangle");
		items.add("Trunc");
		return items;
	}

	public static int getThreshType(int index) {
		int type;
		switch (index) {
		case 1:
			type = Imgproc.THRESH_BINARY_INV;
			break;
		case 2:
			type = Imgproc.THRESH_OTSU;
			break;
		case 3:
			type = Imgproc.THRESH_TOZERO;
			break;
		case 4:
			type = Imgproc.THRESH_TRIANGLE;
			break;
		case 5:
			type = Imgproc.THRESH_TRUNC;
			break;
		default:
			type = 0;
		}
		return type;

	}

	public static Mat loadImage(String path) throws IOException {
		BufferedImage image = ImageIO.read(new File(path));
		return bufferedImageToMat(image);
	}

	public static Mat bufferedImageToMat(BufferedImage bi) {
		Mat mat;
		mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		BufferedImage img = toBufferedImageOfType(bi, BufferedImage.TYPE_3BYTE_BGR);
		byte[] idata = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, idata);
		return mat;
	}

	public static Mat bufferedImageToGrayMat(BufferedImage bi) {
		Mat mat;
		mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC1);
		BufferedImage img = toBufferedImageOfType(bi, BufferedImage.TYPE_BYTE_GRAY);
		byte[] idata = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, idata);
		return mat;
	}

	public static void matToString(Mat transform) {
		for (int row = 0; row < transform.rows(); row++) {
			for (int col = 0; col < transform.cols(); col++) {
				System.out.print(transform.get(row, col)[0] + " ");
			}
			System.out.println();
		}
	}

	public static Point[] toPoints(MatOfKeyPoint keypoints) {
		KeyPoint[] kpList = keypoints.toArray();
		Point[] pts = new Point[kpList.length];
		int i = 0;
		for (KeyPoint kp : kpList) {
			pts[i++] = kp.pt;
		}
		return pts;
	}

	public static Mat difference(Mat frame1, Mat frame2, int blur, double threshold) {
		Mat A = new Mat();
		Imgproc.cvtColor(frame1, A, Imgproc.COLOR_RGB2GRAY);

		Mat B = new Mat();
		Imgproc.cvtColor(frame2, B, Imgproc.COLOR_RGB2GRAY);

		Mat C = new Mat();
		Core.absdiff(A, B, C);
		Mat D = new Mat();

		Imgproc.blur(C, D, new Size(new Point(blur, blur)));
		Imgproc.threshold(D, C, threshold, 255, Imgproc.THRESH_BINARY);
		return C;
	}

	public static BufferedImage toBufferedImageOfType(BufferedImage original, int type) {
		if (original == null) {
			throw new IllegalArgumentException("original == null");
		}

		// Don't convert if it already has correct type
		if (original.getType() == type) {
			return original;
		}

		// Create a buffered image
		BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), type);

		// Draw the image onto the new buffer
		Graphics2D g = image.createGraphics();
		try {
			g.setComposite(AlphaComposite.Src);
			g.drawImage(original, 0, 0, null);
		} finally {
			g.dispose();
		}

		return image;
	}

	public static Rectangle interpolate(Rectangle oldtrack, Rectangle rectangle) {
		return new Rectangle((oldtrack.x + rectangle.x) / 2, (oldtrack.y + rectangle.y) / 2,
				(oldtrack.width + rectangle.width) / 2, (oldtrack.height + rectangle.height) / 2);
	}

	public static BufferedImage toBufferedImage(Mat m) {
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (m.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b); // get all the pixels
		BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return image;

	}

	public static BufferedImage toBufferedImageOfSize(Mat track, double d, double e) {
		Imgproc.resize(track, track, new Size(d, e));
		return toBufferedImage(track);
	}

	public static void drawEnclosingCircle(Mat frame, MatOfPoint contour, Scalar color) {
		// find the enclosing circle
		Point center = new Point();
		float[] radius = new float[1];
		MatOfPoint2f maxC2F = new MatOfPoint2f();
		contour.convertTo(maxC2F, CvType.CV_32F);
		Imgproc.minEnclosingCircle(maxC2F, center, radius);

		// draw contour overlay and the circle
		Imgproc.circle(frame, center, (int) radius[0], color);
	}
}
