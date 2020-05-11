package com.example.test;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    /*int iLowH = 45;
    int iLowS = 20;
    int iLowV = 10;

    int iHighH = 75;
    int iHighS = 225;
    int iHighV = 225;*/

    int red, green, blue;
    Mat imgHSV, imgThreshold;
    //Scalar scLow, scHigh;
    float redPerc, greenPerc, yellowPerc;
    int windowwidth, windowheight;
    int xRed, yRed, xGreen, yGreen, xYellow, yYellow;

    //app ui objects
    JavaCameraView cameraView;
    ImageView boxRed, boxGreen, boxYellow;
    ViewGroup rootLayout;
    TextView text;

    RelativeLayout.LayoutParams layoutParams1, layoutParams2, layoutParams3;



    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                {
                    cameraView.enableView();
                    break;
                }
                default:
                {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    static {
        if(OpenCVLoader.initDebug()){
            Log.d("MainActivity","OpenCV loaded");
        }else{
            Log.d("MainActivity", "OpenCV not loaded");
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        cameraView = (JavaCameraView)findViewById(R.id.cameraView);
        cameraView.setVisibility(SurfaceView.VISIBLE);
        cameraView.setCameraIndex(0);//0 is back 1 is front
        cameraView.setCvCameraViewListener(MainActivity.this);

        rootLayout = (ViewGroup) findViewById(R.id.activity_main);
        boxRed = (ImageView) findViewById(R.id.boxRed);
        boxGreen = (ImageView) findViewById(R.id.boxGreen);
        boxYellow = (ImageView) findViewById(R.id.boxYellow);

        text = (TextView) findViewById(R.id.colourSelec);


        //windowwidth = getWindowManager().getDefaultDisplay().getWidth();
        //windowheight = getWindowManager().getDefaultDisplay().getHeight();

        layoutParams1 = new RelativeLayout.LayoutParams(100, 100);
        layoutParams2 = new RelativeLayout.LayoutParams(100, 100);
        layoutParams3 = new RelativeLayout.LayoutParams(100, 100);

        layoutParams1.leftMargin = 60;
        layoutParams1.topMargin = 0;
        layoutParams2.leftMargin = 60;
        layoutParams2.topMargin = 110;
        layoutParams3.leftMargin = 60;
        layoutParams3.topMargin = 220;

        boxRed.setLayoutParams(layoutParams1);
        boxRed.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                final int X = (int) event.getRawX();
                final int Y = (int) event.getRawY();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) boxRed.getLayoutParams();
                        xRed = X - lParams.leftMargin;
                        yRed = Y - lParams.topMargin;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) boxRed.getLayoutParams();
                        layoutParams.leftMargin = X - xRed;
                        layoutParams.topMargin = Y - yRed;
                        layoutParams.rightMargin = -250;
                        layoutParams.bottomMargin = -250;
                        boxRed.setLayoutParams(layoutParams);

                        if(layoutParams.leftMargin < 60){
                            layoutParams.leftMargin = 60;

                        }
                        if(layoutParams.topMargin < 0) {
                            layoutParams.topMargin = 0;

                        }
                        if(layoutParams.topMargin > 980) {
                            layoutParams.topMargin = 980;

                        }
                        if(layoutParams.leftMargin > 1880) {
                            layoutParams.leftMargin = 1880;
                        }
                    break;
                }

                //RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) boxRed.getLayoutParams();


                rootLayout.invalidate();
                return true;
            }
        });
        boxGreen.setLayoutParams(layoutParams2);
        boxGreen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                final int X = (int) event.getRawX();
                final int Y = (int) event.getRawY();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) boxGreen.getLayoutParams();
                        xGreen = X - lParams.leftMargin;
                        yGreen = Y - lParams.topMargin;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) boxGreen.getLayoutParams();
                        layoutParams.leftMargin = X - xGreen;
                        layoutParams.topMargin = Y - yGreen;
                        layoutParams.rightMargin = -250;
                        layoutParams.bottomMargin = -250;
                        boxGreen.setLayoutParams(layoutParams);

                        if(layoutParams.leftMargin < 60){
                            layoutParams.leftMargin = 60;

                        }
                        if(layoutParams.topMargin < 0) {
                            layoutParams.topMargin = 0;

                        }
                        if(layoutParams.topMargin > 980) {
                            layoutParams.topMargin = 980;

                        }
                        if(layoutParams.leftMargin > 1880) {
                            layoutParams.leftMargin = 1880;
                        }

                        break;
                }
                rootLayout.invalidate();
                return true;
            }
        });
        boxYellow.setLayoutParams(layoutParams3);
        boxYellow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                final int X = (int) event.getRawX();
                final int Y = (int) event.getRawY();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) boxYellow.getLayoutParams();
                        xYellow = X - lParams.leftMargin;
                        yYellow = Y - lParams.topMargin;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) boxYellow.getLayoutParams();
                        layoutParams.leftMargin = X - xYellow;
                        layoutParams.topMargin = Y - yYellow;
                        layoutParams.rightMargin = -250;
                        layoutParams.bottomMargin = -250;
                        boxYellow.setLayoutParams(layoutParams);

                        if(layoutParams.leftMargin < 60){
                            layoutParams.leftMargin = 60;

                        }
                        if(layoutParams.topMargin < 0) {
                            layoutParams.topMargin = 0;

                        }
                        if(layoutParams.topMargin > 980) {
                            layoutParams.topMargin = 980;

                        }
                        if(layoutParams.leftMargin > 1880) {
                            layoutParams.leftMargin = 1880;
                        }

                        break;
                }
                rootLayout.invalidate();
                return true;
            }
        });
    }




    @Override
    protected void onPause() {
        super.onPause();
        if(cameraView != null){
            cameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraView != null){
            cameraView.disableView();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()){
            Log.d("MainActivity","OpenCV loaded");
            mLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }else{
            Log.d("MainActivity", "OpenCV not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        }
    }






    @Override
    public void onCameraViewStarted(int width, int height) {

        imgHSV = new Mat(width, height, CvType.CV_8UC4);
        imgThreshold = new Mat(width, height, CvType.CV_8UC4);

    }






    @Override
    public void onCameraViewStopped() {
        imgHSV.release();
    }




    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        //float redPerc, greenPerc, yellowPerc;


        //scLow = new Scalar(iLowH, iLowS, iLowV);
        //scHigh = new Scalar(iHighH, iHighS, iHighV);

        Imgproc.cvtColor(inputFrame.rgba(), imgHSV, Imgproc.COLOR_BGR2HSV);//process image and convert it to hsv

        Core.inRange(imgHSV, new Scalar(110, 30, 10), new Scalar(140, 200, 225), imgThreshold);
        redPerc = getPercBW((boxRed.getLeft()-60), boxRed.getTop());

        Core.inRange(imgHSV, new Scalar(65, 20, 10), new Scalar(100, 200, 225), imgThreshold);
        yellowPerc = getPercBW((boxYellow.getLeft()-60), boxYellow.getTop());

        Core.inRange(imgHSV, new Scalar(45, 10, 10), new Scalar(75, 225, 225), imgThreshold);
        greenPerc = getPercBW((boxGreen.getLeft()-60), boxGreen.getTop());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText("Green: "+(int)greenPerc+",\nRed: "+(int)redPerc+",\nYellow: "+(int)yellowPerc);
            }
        });

        Imgproc.rectangle(imgThreshold, new Point((boxRed.getLeft()-60), boxRed.getTop()), new Point((boxRed.getLeft()+38),(boxRed.getTop()+99)), new Scalar(255,0,255), 1);
        Imgproc.rectangle(imgThreshold, new Point((boxGreen.getLeft()-60), boxGreen.getTop()), new Point((boxGreen.getLeft()+38),(boxGreen.getTop()+99)), new Scalar(255,0,255), 1);
        Imgproc.rectangle(imgThreshold, new Point((boxYellow.getLeft()-60), boxYellow.getTop()), new Point((boxYellow.getLeft()+38),(boxYellow.getTop()+99)), new Scalar(255,0,255), 1);
        Imgproc.rectangle(imgThreshold, new Point(0, 0), new Point(imgThreshold.width()-1,imgThreshold.height()-1), new Scalar(255,0,255), 2);//draw border

        //Log.d("MainActivity:", "rx"+imgThreshold.width()+" ry"+imgThreshold.height());
        //Log.d("MainActivity:", "gx"+xGreen+"gy"+yGreen);
        //Log.d("MainActivity:", "yx"+xYellow+" yy"+yYellow);
        return imgThreshold;
    }




    public float getPercBW(int xstart, int ystart){

        float totWhite = 0;
        float totBlack = 0;
        float percBW;

        Bitmap bmp = Bitmap.createBitmap(imgThreshold.width(), imgThreshold.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgThreshold, bmp,false);

        for(int i = ystart; i <= (ystart + 99); i++)
        {
            for(int j = xstart; j <= (xstart + 98); j++)
            {
                int pixel = bmp.getPixel(j, i);

                red = Color.red(pixel);
                green = Color.green(pixel);
                blue = Color.blue(pixel);

                //Log.d("MainActivity", "red:"+red);
                //Log.d("MainActivity", "grn:"+green);
                //Log.d("MainActivity", "bl:"+blue);

                if(red == 0 && green == 0 && blue == 0){
                    totBlack++;
                }else {
                    totWhite++;
                }
            }
        }

        //Log.d("MainActivity", "bl:"+totBlack);
        //Log.d("MainActivity", "wh:"+totWhite);

        percBW = (totWhite/(totBlack+totWhite))*100;//total percentage of black and white

        /*if (percBW > 20){
            Log.d("MainActivity", " UH oh LIGHTS ARE ON");
        }*/

        return percBW;
    }
}




class CustomizableCameraView extends JavaCameraView {

    public CustomizableCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPreviewFPS(double min, double max){
        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewFpsRange((int)(min*100), (int)(max*100));
        mCamera.setParameters(params);
    }
}
