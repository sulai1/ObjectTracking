package imageproc;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class ObjectTracker {

	private double threshold = 15;
	private Rectangle oldtrack = new Rectangle();

	public ObjectTracker() {}

	public ObjectTracker(double threshold) {
		this.threshold = threshold;
	}

	public Point trackCenter(Mat frame1, Mat frame2) {
		Mat diff = difference(frame1, frame2);
		return centroid(diff);
	}

	public Rectangle track(Mat frame1, Mat frame2) {
		Mat diff = difference(frame1, frame2);
		Moments mom = Imgproc.moments(diff);
		Point centroid = new Point(mom.m10 / mom.m00, mom.m01 / mom.m00);
		double arrea = Math.sqrt(mom.m00);
		int size = (int) Math.round(Math.sqrt(arrea));
		Rectangle rectangle = new Rectangle((int) centroid.x-size, (int) centroid.y-size, size*2, size*2);
		Rectangle track = interpolate(oldtrack,rectangle);
		if(rectangle.x==0&&rectangle.y==0){
			return oldtrack;
		}else{
			oldtrack = track;
			return track;
		}
	}

	private Rectangle  interpolate(Rectangle oldtrack, Rectangle rectangle) {
		return new Rectangle((oldtrack.x+rectangle.x)/2, (oldtrack.y+rectangle.y)/2, (oldtrack.width+rectangle.width)/2,(oldtrack.height+rectangle.height)/2);
//		return rectangle;
	}

	/**
	 * @param frame1
	 * @param frame2
	 * @return
	 */
	public Mat difference(Mat frame1, Mat frame2) {
		Mat A = new Mat();
		Imgproc.cvtColor(frame1, A, Imgproc.COLOR_RGB2GRAY);

		Mat B = new Mat();
		Imgproc.cvtColor(frame2, B, Imgproc.COLOR_RGB2GRAY);

		Mat C = new Mat();
		Core.absdiff(A, B, C);
		Mat D = new Mat();
		Imgproc.blur(C, D, new Size(new Point(3, 3)));
		setThreshold(Imgproc.threshold(D, C, threshold, 255, Imgproc.THRESH_BINARY));
		return C;
	}

	public static Point centroid(Mat m) {
		Moments mom = Imgproc.moments(m);
		System.out.println(mom);
		Point point = new Point(mom.m10 / mom.m00, mom.m01 / mom.m00);
		return point;
	}

	public static Mat bufferedImageToMat(BufferedImage bi) throws Exception {
		Mat mat;
		mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		BufferedImage img = toBufferedImageOfType(bi, BufferedImage.TYPE_3BYTE_BGR);
		byte[] idata = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, idata);
		return mat;
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
	    }
	    finally {
	        g.dispose();
	    }

	    return image;
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

	public double getThreshold() {
		return this.threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public Rectangle track(BufferedImage frame1, BufferedImage frame2) throws Exception {
		return track(bufferedImageToMat(frame1), bufferedImageToMat(frame2));
	}

}
