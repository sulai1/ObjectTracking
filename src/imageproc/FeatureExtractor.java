package imageproc;

import java.util.ArrayList;

import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorMatcher;

public abstract class FeatureExtractor {

	public Features detectAndCompute(Mat img){
		MatOfKeyPoint keypoints = detect(img);
		Mat descriptors = compute(img,keypoints);
		return new Features(keypoints,descriptors);
	}

	public abstract DescriptorMatcher getMatcher();
	
	public abstract Mat compute(Mat img, MatOfKeyPoint keypoints);

	public abstract MatOfKeyPoint detect(Mat img);

	public static class Features{

		public MatOfKeyPoint keypoints;
		public Mat descriptors;
		
		public Features(MatOfKeyPoint keypoints, Mat descriptors) {
			this.keypoints = keypoints;
			this.descriptors = descriptors;
		}
		
		public MatOfKeyPoint match(Features f, int type){
			DescriptorMatcher matcher = DescriptorMatcher.create(type);
			MatOfDMatch matches = new MatOfDMatch();
			matcher.match(descriptors, f.descriptors, matches);
			float min = Float.MAX_VALUE;
			for(DMatch match:matches.toArray()){
				min = Math.min(match.distance,min);
			}
			
			KeyPoint[] k1 = f.keypoints.toArray();
			ArrayList<KeyPoint> keypoints =new ArrayList<>();
			for(DMatch match:matches.toArray()){
				if(match.distance <= min){
					keypoints.add( k1[match.imgIdx]);
				}
			}
			KeyPoint[] a = keypoints.toArray(new KeyPoint[keypoints.size()]);
			MatOfKeyPoint matOfKeyPoint = new MatOfKeyPoint(a);
			return matOfKeyPoint;
		}

	}

}
