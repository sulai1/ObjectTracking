package imageproc;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class CVUtils {

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

}
