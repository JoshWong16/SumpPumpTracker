package com.example.sumppumptracker;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.AsyncTask;
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

import com.example.sumppumptracker.DatabaseAccess;

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

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener, View.OnClickListener{

    int colorRange[][] = {{40,50,40,85,255,255}, {115,50,40,125,255,255}, {85,60,40,100,255,255}};//0-green   1-red   2-yellow //(lowH,lowS,lowV,highH,highS,highV)
    int colorID = 3; //0-green   1-red   2-yellow   3-hsv
    int red, green, blue;
    Mat imgHSV, imgThreshold;
    float redPerc, greenPerc, yellowPerc;
    int xRed, yRed, xGreen, yGreen, xYellow, yYellow;
    int xdimension = 1920;
    int ydimension = 1080;
    int xdelta,ydelta;
    boolean timerIsOn = false;
    int counter;
    Timer timer;
    private String TAG = "SumpPumpDB";

    //app ui objects
    JavaCameraView cameraView;
    ImageView boxRed, boxGreen, boxYellow;
    ViewGroup rootLayout;
    TextView text;
    Button btnRed, btnGreen, btnYellow, btnHSV;

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

        btnRed = (Button) findViewById(R.id.btnRed);
        btnGreen = (Button) findViewById(R.id.btnGreen);
        btnYellow = (Button) findViewById(R.id.btnYellow);
        btnHSV = (Button) findViewById(R.id.btnHSV);

        btnRed.setOnClickListener(this);
        btnGreen.setOnClickListener(this);
        btnYellow.setOnClickListener(this);
        btnHSV.setOnClickListener(this);

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
        boxRed.setOnTouchListener(this);
        boxGreen.setLayoutParams(layoutParams2);
        boxGreen.setOnTouchListener(this);
        boxYellow.setLayoutParams(layoutParams3);
        boxYellow.setOnTouchListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGreen:
                colorID = 0;
                break;
            case R.id.btnRed:
                colorID = 1;
                break;
            case R.id.btnYellow:
                colorID = 2;
                break;
            case R.id.btnHSV:
                colorID = 3;
                break;
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                xdelta = X - lParams.leftMargin;
                ydelta = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                layoutParams.leftMargin = X - xdelta;
                layoutParams.topMargin = Y - ydelta;
                layoutParams.rightMargin = -250;
                layoutParams.bottomMargin = -250;
                v.setLayoutParams(layoutParams);

                if(layoutParams.leftMargin < 60){
                    layoutParams.leftMargin = 60;

                }
                if(layoutParams.topMargin < 0) {
                    layoutParams.topMargin = 0;

                }
                if(layoutParams.topMargin > (ydimension - 100)) {
                    layoutParams.topMargin = (ydimension - 100);

                }
                if(layoutParams.leftMargin > (xdimension - 40)) {
                    layoutParams.leftMargin = (xdimension - 40);
                }

                break;
        }
        rootLayout.invalidate();
        return true;
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
        //create a new AsyncTask
        UpdateAsyncTask updateAsyncTask = new UpdateAsyncTask();


        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                Log.d("mainactivity","seconds passed " + counter);
                counter++;
            }
        };

        //scLow = new Scalar(iLowH, iLowS, iLowV);
        //scHigh = new Scalar(iHighH, iHighS, iHighV);

        Imgproc.cvtColor(inputFrame.rgba(), imgHSV, Imgproc.COLOR_BGR2HSV);//process image and convert it to hsv

        Core.inRange(imgHSV, new Scalar(colorRange[1][0], colorRange[1][1], colorRange[1][2]), new Scalar(colorRange[1][3], colorRange[1][4], colorRange[1][5]), imgThreshold);
        redPerc = getPercBW((boxRed.getLeft()-60), boxRed.getTop());

        if(!timerIsOn && redPerc >= 50){
            //start timer

            timer = new Timer("MyTimer");//create a new timer
            timer.scheduleAtFixedRate(timerTask, 30, 1000);//start timer in 30ms to increment  counter
            //execute AsyncTask and passing it the primary key
            updateAsyncTask.execute("1", "true");
            timerIsOn = true;

        }else if(timerIsOn && redPerc < 50){
            //stop timer

            timer.cancel();
            counter = 0;
            //execute AsyncTask and passing it the primary key
            updateAsyncTask.execute("1", "false");
            timerIsOn = false;

        }

        Core.inRange(imgHSV, new Scalar(colorRange[2][0], colorRange[2][1], colorRange[2][2]), new Scalar(colorRange[2][3], colorRange[2][4], colorRange[2][5]), imgThreshold);
        yellowPerc = getPercBW((boxYellow.getLeft()-60), boxYellow.getTop());

        Core.inRange(imgHSV, new Scalar(colorRange[0][0], colorRange[0][1], colorRange[0][2]), new Scalar(colorRange[0][3], colorRange[0][4], colorRange[0][5]), imgThreshold);
        greenPerc = getPercBW((boxGreen.getLeft()-60), boxGreen.getTop());



        runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                text.setText("Green: "+(int)greenPerc+",\nRed: "+(int)redPerc+",\nYellow: "+(int)yellowPerc);
            }
        });


        if(colorID != 3) {//green
            Core.inRange(imgHSV, new Scalar(colorRange[colorID][0], colorRange[colorID][1], colorRange[colorID][2]), new Scalar(colorRange[colorID][3], colorRange[colorID][4], colorRange[colorID][5]), imgThreshold);

            Imgproc.rectangle(imgThreshold, new Point((boxRed.getLeft() - 60), boxRed.getTop()), new Point((boxRed.getLeft() + 38), (boxRed.getTop() + 99)), new Scalar(255, 0, 255), 1);
            Imgproc.rectangle(imgThreshold, new Point((boxGreen.getLeft() - 60), boxGreen.getTop()), new Point((boxGreen.getLeft() + 38), (boxGreen.getTop() + 99)), new Scalar(255, 0, 255), 1);
            Imgproc.rectangle(imgThreshold, new Point((boxYellow.getLeft() - 60), boxYellow.getTop()), new Point((boxYellow.getLeft() + 38), (boxYellow.getTop() + 99)), new Scalar(255, 0, 255), 1);
            Imgproc.rectangle(imgThreshold, new Point(0, 0), new Point(imgThreshold.width() - 1, imgThreshold.height() - 1), new Scalar(255, 0, 255), 2);//draw border

            return imgThreshold;

        }else{//hsv
            Imgproc.rectangle(imgHSV, new Point((boxRed.getLeft() - 60), boxRed.getTop()), new Point((boxRed.getLeft() + 38), (boxRed.getTop() + 99)), new Scalar(255, 0, 255), 1);
            Imgproc.rectangle(imgHSV, new Point((boxGreen.getLeft() - 60), boxGreen.getTop()), new Point((boxGreen.getLeft() + 38), (boxGreen.getTop() + 99)), new Scalar(255, 0, 255), 1);
            Imgproc.rectangle(imgHSV, new Point((boxYellow.getLeft() - 60), boxYellow.getTop()), new Point((boxYellow.getLeft() + 38), (boxYellow.getTop() + 99)), new Scalar(255, 0, 255), 1);
            Imgproc.rectangle(imgHSV, new Point(0, 0), new Point(imgThreshold.width() - 1, imgThreshold.height() - 1), new Scalar(255, 0, 255), 2);//draw border

            return imgHSV;
        }

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

        return percBW;
    }


    /**
     * Async Task to update lightstatus
     */

    private class UpdateAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            boolean isSuccess = false;

            Log.i(TAG, "in UpdateAsyncTask doInBackground updating LightID: 1");
            //create instance of DatabaseAccess
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(MainActivity.this);

            try {
                //call updateLightStatus method
                isSuccess = databaseAccess.updateLightStatus(strings[0], Boolean.parseBoolean(strings[1]));
            }catch (Exception e){
                Log.i(TAG, "error updating contact: " + e.getMessage());
            }

            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            super.onPostExecute(isSuccess);
            Log.i(TAG, "in UpdateAsyncTask onPostExecute os success: " + isSuccess);

        }
    }

}



