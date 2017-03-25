package ferozepurwale.run;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CameraTest extends AppCompatActivity {

    private static Bitmap b1, b2;
    private static int descriptor = DescriptorExtractor.BRISK;
    private static String descriptorType, text;
    private static int min_dist = 10;
    private static int min_matches = 750;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("CameraTest", "OpenCV loaded successfully");
                    try {
                        doStuff();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("CameraTest", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d("CameraTest", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);

        b1 = loadImageFromStorage(new File(Environment.getExternalStorageDirectory() + "/Run/1.jpg"));
        b2 = loadImageFromStorage(new File(Environment.getExternalStorageDirectory() + "/Run/2.jpg"));

        b1 = Bitmap.createScaledBitmap(b1, 500, 500, true);
        b2 = Bitmap.createScaledBitmap(b2, 500, 500, true);

    }

    private void doStuff() {
        Mat img1 = new Mat();
        Utils.bitmapToMat(b1, img1);
        Mat img2 = new Mat();
        Utils.bitmapToMat(b2, img2);
        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.cvtColor(img2, img2, Imgproc.COLOR_RGBA2GRAY);
        img1.convertTo(img1, CvType.CV_32F);
        img2.convertTo(img2, CvType.CV_32F);
        Log.d("ImageComparator", "img1:" + img1.rows() + "x" + img1.cols() + " img2:" + img2.rows() + "x" + img2.cols());
        Mat hist1 = new Mat();
        Mat hist2 = new Mat();
        MatOfInt histSize = new MatOfInt(180);
        MatOfInt channels = new MatOfInt(0);
        ArrayList<Mat> bgr_planes1 = new ArrayList<Mat>();
        ArrayList<Mat> bgr_planes2 = new ArrayList<Mat>();
        Core.split(img1, bgr_planes1);
        Core.split(img2, bgr_planes2);
        MatOfFloat histRanges = new MatOfFloat(0f, 180f);
        boolean accumulate = false;
        Imgproc.calcHist(bgr_planes1, channels, new Mat(), hist1, histSize, histRanges, accumulate);
        Core.normalize(hist1, hist1, 0, hist1.rows(), Core.NORM_MINMAX, -1, new Mat());
        Imgproc.calcHist(bgr_planes2, channels, new Mat(), hist2, histSize, histRanges, accumulate);
        Core.normalize(hist2, hist2, 0, hist2.rows(), Core.NORM_MINMAX, -1, new Mat());
        img1.convertTo(img1, CvType.CV_32F);
        img2.convertTo(img2, CvType.CV_32F);
        hist1.convertTo(hist1, CvType.CV_32F);
        hist2.convertTo(hist2, CvType.CV_32F);

        double compare = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CHISQR);
        Log.d("ImageComparator", "compare: " + compare);
        if (compare > 0 && compare < 2200) {
            Toast.makeText(CameraTest.this, "Images may be possible duplicates, verifying", Toast.LENGTH_LONG).show();
            new asyncTask(CameraTest.this).execute();
        } else if (compare == 0)
            Toast.makeText(CameraTest.this, "Images are exact duplicates", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(CameraTest.this, "Images are not duplicates", Toast.LENGTH_LONG).show();
    }

    public static class asyncTask extends AsyncTask<Void, Void, Void> {
        private static Mat img1, img2, descriptors, dupDescriptors;
        private static FeatureDetector detector;
        private static DescriptorExtractor DescExtractor;
        private static DescriptorMatcher matcher;
        private static MatOfKeyPoint keypoints, dupKeypoints;
        private static MatOfDMatch matches, matches_final_mat;
        private static boolean isDuplicate = false;
        private CameraTest asyncTaskContext = null;
        private static Scalar RED = new Scalar(255, 0, 0);
        private static Scalar GREEN = new Scalar(0, 255, 0);

        public asyncTask(CameraTest context) {
            asyncTaskContext = context;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // TODO Auto-generated method stub
            compare();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                Mat img3 = new Mat();
                MatOfByte drawnMatches = new MatOfByte();
                Features2d.drawMatches(img1, keypoints, img2, dupKeypoints,
                        matches_final_mat, img3, GREEN, RED, drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
                Bitmap bmp = Bitmap.createBitmap(img3.cols(), img3.rows(),
                        Bitmap.Config.ARGB_8888);
                Imgproc.cvtColor(img3, img3, Imgproc.COLOR_BGR2RGB);
                Utils.matToBitmap(img3, bmp);
                List<DMatch> finalMatchesList = matches_final_mat.toList();
                if (finalMatchesList.size() > min_matches)// dev discretion for
                // number of matches to
                // be found for an image
                // to be judged as
                // duplicate
                {
                    text = finalMatchesList.size()
                            + " matches were found. Possible duplicate image.";
                    isDuplicate = true;
                } else {
                    text = finalMatchesList.size()
                            + " matches were found. Images aren't similar.";
                    isDuplicate = false;
                }
                Log.d("CameraTest", text);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(asyncTaskContext, e.toString(),
                        Toast.LENGTH_LONG).show();
            }
        }

        void compare() {
            try {
                b1 = b1.copy(Bitmap.Config.ARGB_8888, true);
                b2 = b2.copy(Bitmap.Config.ARGB_8888, true);
                img1 = new Mat();
                img2 = new Mat();
                Utils.bitmapToMat(b1, img1);
                Utils.bitmapToMat(b2, img2);
                Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2RGB);
                Imgproc.cvtColor(img2, img2, Imgproc.COLOR_BGR2RGB);
                detector = FeatureDetector.create(FeatureDetector.PYRAMID_FAST);
                DescExtractor = DescriptorExtractor.create(descriptor);
                matcher = DescriptorMatcher
                        .create(DescriptorMatcher.BRUTEFORCE_HAMMING);

                keypoints = new MatOfKeyPoint();
                dupKeypoints = new MatOfKeyPoint();
                descriptors = new Mat();
                dupDescriptors = new Mat();
                matches = new MatOfDMatch();
                detector.detect(img1, keypoints);
                Log.d("LOG!", "number of query Keypoints= " + keypoints.size());
                detector.detect(img2, dupKeypoints);
                Log.d("LOG!", "number of dup Keypoints= " + dupKeypoints.size());
                // Descript keypoints
                DescExtractor.compute(img1, keypoints, descriptors);
                DescExtractor.compute(img2, dupKeypoints, dupDescriptors);
                Log.d("LOG!", "number of descriptors= " + descriptors.size());
                Log.d("LOG!", "number of dupDescriptors= " + dupDescriptors.size());
                // matching descriptors
                matcher.match(descriptors, dupDescriptors, matches);
                Log.d("LOG!", "Matches Size " + matches.size());
                // New method of finding best matches
                List<DMatch> matchesList = matches.toList();
                List<DMatch> matches_final = new ArrayList<DMatch>();
                for (int i = 0; i < matchesList.size(); i++) {
                    if (matchesList.get(i).distance <= min_dist) {
                        matches_final.add(matches.toList().get(i));
                    }
                }

                matches_final_mat = new MatOfDMatch();
                matches_final_mat.fromList(matches_final);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Nullable
    private Bitmap loadImageFromStorage(File path) {
        Bitmap b;
        try {
            File f = new File(path.getAbsolutePath());
            b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
