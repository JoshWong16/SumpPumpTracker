package com.example.test;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    int iLowH = 45;
    int iLowS = 20;
    int iLowV = 10;

    int iHighH = 75;
    int iHighS = 225;
    int iHighV = 225;

    int red, green, blue;

    Mat imgHSV, imgThreshold;
    Scalar scLow, scHigh;
    JavaCameraView cameraView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = (JavaCameraView)findViewById(R.id.cameraView);
        cameraView.setVisibility(SurfaceView.VISIBLE);
        cameraView.setCameraIndex(0);//0 is back 1 is front
        cameraView.setCvCameraViewListener(MainActivity.this);

        Button btnRed = (Button) findViewById(R.id.btnRed);
        Button btnGreen = (Button) findViewById(R.id.btnGreen);
        Button btnYellow = (Button) findViewById(R.id.btnYellow);

        btnRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView colorSelec = (TextView) findViewById(R.id.colourSelec);
                colorSelec.setText("Red");
                colorSelec.setTextColor(Color.RED);

                iLowH = 110;
                iLowS = 30;
                iLowV = 10;

                iHighH = 140;
                iHighS = 200;
                iHighV = 225;
            }
        });

        btnGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView colorSelec = (TextView) findViewById(R.id.colourSelec);
                colorSelec.setText("Green");
                colorSelec.setTextColor(Color.GREEN);

                iLowH = 45;
                iLowS = 10;
                iLowV = 10;

                iHighH = 75;
                iHighS = 225;
                iHighV = 225;
            }
        });

        btnYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView colorSelec = (TextView) findViewById(R.id.colourSelec);
                colorSelec.setText("Yellow");
                colorSelec.setTextColor(Color.YELLOW);

                iLowH = 65;
                iLowS = 20;
                iLowV = 10;

                iHighH = 100;
                iHighS = 200;
                iHighV = 225;
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
        scLow = new Scalar(iLowH, iLowS, iLowV);
        scHigh = new Scalar(iHighH, iHighS, iHighV);

        Imgproc.cvtColor(inputFrame.rgba(), imgHSV, Imgproc.COLOR_BGR2HSV);//process image and convert it to hsv
        Core.inRange(imgHSV, scLow, scHigh, imgThreshold);


        getPercBW();

        return imgThreshold;
    }




    public void getPercBW(){

        int totWhite = 0;
        int totBlack = 0;
        int percBW = 0;

        Bitmap bmp = Bitmap.createBitmap(imgThreshold.width(), imgThreshold.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgThreshold, bmp,false);

        for(int i = 0; i < bmp.getHeight(); i++)
        {
            for(int j = 0; j < bmp.getWidth(); j++)
            {
                int pixel = bmp.getPixel(j, i);
                //bmp.recycle();
                red = Color.red(pixel);
                green = Color.green(pixel);
                blue = Color.blue(pixel);

                Log.d("MainActivity", red);

                if(red == 255 && green == 255 && blue == 255){
                    totWhite++;
                }else {
                    totBlack++;
                }
            }
        }

        percBW = (totWhite/(totBlack+totWhite))*100;//total percentage of black and white

        if (percBW > 50){
            Log.d("MainActivity", " UH oh LIGHTS ARE ON");
        }

    }
}

