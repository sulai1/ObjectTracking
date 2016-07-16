package gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import imageproc.CVUtils;
import imageproc.FeatureExtractor;
import imageproc.FeatureExtractor.Features;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

public class DetectorTester extends HBox {

	private FeatureExtractor extractor;
	private int width, height;
	private int k = 10;

	public DetectorTester(FeatureExtractor etractor, int width, int height) {
		this.extractor = etractor;
		this.width = width;
		this.height = height;
		setPrefSize(width, height);
	}

	public void test(String imgPath) throws IOException {
		BufferedImage img = ImageIO.read(new File(imgPath));
		Mat mimg = CVUtils.bufferedImageToMat(img);
		Features f1 = extractor.detectAndCompute(mimg);

		Mat out = new Mat();
		Features2d.drawKeypoints(mimg, f1.keypoints, out);
		drawImage(out);
	}

	public void testMatch(String img1, String img2) throws IOException {
		BufferedImage img = ImageIO.read(new File(img1));
		Mat mimg1 = CVUtils.bufferedImageToMat(img);
		Features f1 = extractor.detectAndCompute(mimg1);

		img = ImageIO.read(new File(img2));
		Mat mimg2 = CVUtils.bufferedImageToMat(img);
		Features f2 = extractor.detectAndCompute(mimg2);

		MatOfDMatch matches = new MatOfDMatch();
		Mat d1 = new Mat();
		Mat d2 = new Mat();
		// f1.descriptors.convertTo(d1 , CvType.CV_32F);
		// f2.descriptors.convertTo(d2, CvType.CV_32F);
		d1 = f1.descriptors;
		d2 = f2.descriptors;
		System.out.println(d1.type() + " " + d2.type());

		extractor.getMatcher().match(d1, d2, matches);
		Mat outImg = new Mat();
		Features2d.drawMatches(mimg1, f1.keypoints, mimg2, f2.keypoints, matches, outImg, new Scalar(0, 255, 0),
				new Scalar(1, 0, 0), new MatOfByte(), 0);

		drawImage(outImg);
	}

	public void testTraining(String samples, String img) {
		try {
			Mat qimg = CVUtils.loadImage(img);
			File folder = new File(samples);

			// train the matcher with the samples
			List<Mat> descriptors = new ArrayList<>();
			ArrayList<Mat> simg = new ArrayList<>();
			ArrayList<Features> sfs = new ArrayList<>();
			int idx = 1;
			Mat sample = CVUtils.loadImage(folder.listFiles()[idx].toString());
			simg.add(sample);
			Features sf = extractor.detectAndCompute(sample);
			sfs.add(sf);
			descriptors.add(sf.descriptors);
//			for (File f : folder.listFiles()) {
//				Mat sample = CVUtils.loadImage(f.toString());
//				simg.add(sample);
//				Features sf = extractor.detectAndCompute(sample);
//				sfs.add(sf);
//				descriptors.add(sf.descriptors);
//			}
			extractor.getMatcher().add(descriptors);
			extractor.getMatcher().train();
			
			// match the query with the training data
			Features features = extractor.detectAndCompute(qimg);
			MatOfDMatch matches = new MatOfDMatch();
			extractor.getMatcher().match(features.descriptors, matches);
			KeyPoint[] kpquery = features.keypoints.toArray();
			ArrayList<KeyPoint> kpres = new ArrayList<KeyPoint>();
			
			// sort out matches that are not close enough
			DMatch[] matchs = matches.toArray();
			Arrays.sort(matchs, new Comparator<DMatch>(){

				@Override
				public int compare(DMatch arg0, DMatch arg1) {
					if(arg0.distance < arg1.distance)
						return -1;
					else if(arg0.distance > arg1.distance) 
						return 1;
					else return 0;
				}
				
			});
			
			DMatch m[] = new DMatch[k];
			
			for(int i=0; i<k;i++){
				m[i] = matchs[i];
				kpres.add(kpquery[m[i].queryIdx]);
			}
			
//			matches = new MatOfDMatch(m);
			
			System.out.println(m.length +","+ kpres.size());
			// draw the result
			MatOfKeyPoint kp = new MatOfKeyPoint(kpres.toArray(new KeyPoint[kpres.size()]));
			Mat out = new Mat();
			Features2d.drawMatches(qimg, kp, simg.get(0), sfs.get(0).keypoints, matches, out );
////			Features2d.drawKeypoints(qimg, kp, out);
			drawImage(out);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void drawImage(Mat src) {
		Mat out = new Mat();
		Imgproc.resize(src, out, new Size(width, height));
		drawImage(SwingFXUtils.toFXImage(CVUtils.toBufferedImage(out), null));
	}

	private void drawImage(Image frame) {
		Canvas c = new Canvas(frame.getWidth(), frame.getHeight());
		GraphicsContext gc = c.getGraphicsContext2D();
		gc.strokeRect(10, 100, 10, 10);
		gc.drawImage(frame, 0, 0);
		if (getChildren().size() > 0)
			getChildren().remove(0);
		getChildren().add(c);
	}

}
