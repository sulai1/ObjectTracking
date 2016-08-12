#CVTrackingLib
This library shows some tracking algorithms using OpenCV.
Its mainly a tool to visualize the impact of the parameters and show the performance of the different algorithms.
Based on this visualizattion the tracking algorithm can be compared for different usecases.
There is an interface to visualize the user interface for changing the parameters.

#Tracking Algorithms
this section contains a brief description of the tracking algorithms.

##CVCorner Tracker
This algorithm uses the opencv goodFeaturesToTrack to find corners and simply displays them to see the corner response.

##CVDiffTracker
This algorithm uses the absolute Difference between frames to track a moving object. The algorithm works best when the camera is not moving.
The algorithm first callculates the difference between two frames.
Optionaly it is possible to apply a blur to the difference to reduce the detected noise.
Then a threshold is applied and the resulting binary image is used to calculate the contours of the image.
Next for every conours the minimum enclosing circle is calculated.
Then the circles are displayd. 

##CVThreshTracker
This algorithm applies a threshold on the image to find the object. This is usefull for objects that are easy distinguishable
from the background.
First the the image is converted to either RGB or HSV.
Now one of the channels is used for further processing.
An optional blur can be applied now.
Next the threshold is calculated.
The resulting binary image is used to calculate the contours of the image.
Now the minimum enclosing circle for every contour is shown.
Optionally all contours and the corresponding circles are drawn.
The circle of the biggest conour is then drawn .

##CVMultiThreshTracker
This algorithm only displays the conours of different uniformly distributed thresholds.
First the frame is optionally blurred.
Now for every given interval the threshold is calculated. 
The different thresholds are added together using a bitwise or.
Now the contours are calculated and drawn

##ORBTracker
This algorihm tries to use the ORB features to find an object using a set of samples.
First the sample key points and descriptors are calculated.
Now they are used to train a bruteforce matcher.
Now for every frame the keypoints and descriptors are calculated.
Now they are matched with the sample key points/descriptors using a bruteforce knn matcher(Hamming Distance).
The best matches are considered as the object.
Now the minimum enclosing circle for the key points is calculated and displayed.

##ObjectTracker
Tries to use the best technique or a combination to track the object.
