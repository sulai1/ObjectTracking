package imageproc;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class CVUtils {

	public static Mat loadImage(String path) throws IOException{
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
