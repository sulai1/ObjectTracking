package gui;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

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
	private int k = 20;
	private Rectangle oldtrack;

	public DetectorTester(FeatureExtractor etractor, int width, int height) {
		this.extractor = etractor;
		this.width = width;
		this.height = height;
		setPrefSize(width, height);
	}
	
	public void test(String img1, String img2, String reference) throws IOException {
		
		//load images
		BufferedImage img = ImageIO.read(new File(img1));
		Mat mimg1 = CVUtils.bufferedImageToMat(img);

		img = ImageIO.read(new File(img2));
		Mat mimg2 = CVUtils.bufferedImageToMat(img);

		img = ImageIO.read(new File(reference));
		Mat refimg = CVUtils.bufferedImageToMat(img);
		
		// calculate ROI
		Rectangle roi = roi(mimg1, mimg2);
		// calculate border for ORB Detector
		if(roi.x>32)
			roi.x=roi.x-32;
		else
			roi.x=0;
		if(roi.y>32)
			roi.y=roi.y-32;
		else
			roi.y=0;

		if(roi.y+roi.height+64<mimg1.rows())
			roi.height += 64;
		if(roi.x+roi.width+64<mimg1.cols())
			roi.width += 64;
		
		mimg1.adjustROI(roi.y, roi.y+roi.height, roi.x, roi.x+roi.height);
		
		//extract features
		Features f1 = extractor.detectAndCompute(mimg1);
		Features fref = extractor.detectAndCompute(refimg);
		
		MatOfDMatch matches = new MatOfDMatch();
		Mat outImg = findMatches(mimg1, refimg, f1, fref, matches, roi);
		Imgproc.rectangle(outImg, new Point(roi.x, roi.y), new Point(roi.x+roi.getWidth(), roi.y+roi.getHeight()), new Scalar(1,0,0));
		
		findHomography(mimg1, refimg, f1, fref, matches, outImg);
		
		drawImage(outImg);

	}

	private Rectangle roi(Mat frame1, Mat frame2) {
		Mat diff = CVUtils.difference(frame1, frame2, 6, 15);
		Moments mom = Imgproc.moments(diff);
		Point centroid = new Point(mom.m10 / mom.m00, mom.m01 / mom.m00);

		// calculate the region of interest
		double arrea = Math.sqrt(mom.m00);
		int size = (int) Math.round(Math.sqrt(arrea));
		Rectangle rectangle = new Rectangle((int) centroid.x - size, (int) centroid.y - size, size * 2, size * 2);
		if(oldtrack!=null)
			return CVUtils.interpolate(oldtrack, rectangle);
		else 
			return rectangle; 
		
		
	}
	
	private Mat findMatches(Mat mimg1, Mat mimg2, Features f1, Features f2, MatOfDMatch matches, Rectangle roi) {
		Mat d1 = new Mat();
		Mat d2 = new Mat();
		d1 = f1.descriptors;
		d2 = f2.descriptors;

		extractor.getMatcher().match(d1, d2, matches);
		Mat outImg = new Mat();
		
		LinkedList<DMatch> goodMatches = new LinkedList<>();
		KeyPoint[] kpArray = f1.keypoints.toArray();
		for(DMatch m:matches.toArray()){
			KeyPoint kp = kpArray[m.queryIdx];
			if(roi.contains(new java.awt.Point((int)kp.pt.x, (int)kp.pt.y)))
				goodMatches.add(m);
		}
		goodMatches.sort((a,b) -> {
			if(a.distance>b.distance)
				return 1;
			else if(a.distance<b.distance)
				return -1;
			else
				return 0;
		});
		
		while(goodMatches.size()>k)
			goodMatches.removeLast();
		
		MatOfDMatch match = new MatOfDMatch();
		match.fromList(goodMatches);
		Features2d.drawMatches(mimg1, f1.keypoints, mimg2, f2.keypoints, match, outImg, new Scalar(0, 255, 0),
				new Scalar(1, 0, 0), new MatOfByte(),Features2d.DRAW_RICH_KEYPOINTS);
		return outImg;
	}

	private void findHomography(Mat scene, Mat object, Features f1, Features f2, MatOfDMatch matches, Mat outImg) {
		MatOfPoint2f pts1 = new MatOfPoint2f(CVUtils.toPoints(goodMatches(matches, f1.keypoints, k, true)));
		MatOfPoint2f pts2 = new MatOfPoint2f(CVUtils.toPoints(goodMatches(matches, f2.keypoints, k, false)));
		
		Mat transform = Calib3d.findHomography(pts1, pts2);

		Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
		Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

		obj_corners.put(0, 0, new double[] { 0, 0 });
		obj_corners.put(1, 0, new double[] { object.cols(), 0 });
		obj_corners.put(2, 0, new double[] { object.cols(), object.rows() });
		obj_corners.put(3, 0, new double[] { 0, object.rows() });

		
		Core.perspectiveTransform(obj_corners,scene_corners, transform);

		Imgproc.line(outImg, new Point(scene_corners.get(0,0)), new Point(scene_corners.get(1,0)), new Scalar(0, 255, 0),4);
		Imgproc.line(outImg, new Point(scene_corners.get(1,0)), new Point(scene_corners.get(2,0)), new Scalar(0, 255, 0),4);
		Imgproc.line(outImg, new Point(scene_corners.get(2,0)), new Point(scene_corners.get(3,0)), new Scalar(0, 255, 0),4);
		Imgproc.line(outImg, new Point(scene_corners.get(3,0)), new Point(scene_corners.get(0,0)), new Scalar(0, 255, 0),4);
		
		Imgproc.line(outImg, new Point(obj_corners.get(0,0)), new Point(obj_corners.get(1,0)), new Scalar(0, 255, 0),4);
		Imgproc.line(outImg, new Point(obj_corners.get(1,0)), new Point(obj_corners.get(2,0)), new Scalar(0, 255, 0),4);
		Imgproc.line(outImg, new Point(obj_corners.get(2,0)), new Point(obj_corners.get(3,0)), new Scalar(0, 255, 0),4);
		Imgproc.line(outImg, new Point(obj_corners.get(3,0)), new Point(obj_corners.get(0,0)), new Scalar(0, 255, 0),4);
	}

	private MatOfKeyPoint goodMatches(MatOfDMatch matches, MatOfKeyPoint keypoints, int k, boolean bQuery) {
		// sort out matches that are not close enough
		DMatch[] matchs = matches.toArray();
		Arrays.sort(matchs, new Comparator<DMatch>() {

			@Override
			public int compare(DMatch arg0, DMatch arg1) {
				if (arg0.distance < arg1.distance)
					return -1;
				else if (arg0.distance > arg1.distance)
					return 1;
				else
					return 0;
			}

		});
		MatOfDMatch goodMatches;
		if (matchs.length > k)
			goodMatches = new MatOfDMatch(Arrays.copyOfRange(matchs, 0, k));
		else
			goodMatches = new MatOfDMatch(matchs);
		KeyPoint[] kplist = keypoints.toArray();

		KeyPoint[] kpres = new KeyPoint[k];
		List<DMatch> list = goodMatches.toList();
		if (bQuery)
			for (int i = 0; i < k; i++)
				kpres[i] = kplist[list.get(i).queryIdx];
		else
			for (int i = 0; i < k; i++)
				kpres[i] = kplist[list.get(i).trainIdx];
		return new MatOfKeyPoint(kpres);
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
