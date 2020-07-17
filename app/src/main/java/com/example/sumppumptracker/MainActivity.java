package com.example.sumppumptracker;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document;
import com.auth0.android.jwt.JWT;

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


import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener, View.OnClickListener{

    int colorRange[][] = {{40,50,40,85,255,255}, {115,50,40,125,255,255}, {85,60,40,100,255,255}};//0-green   1-red   2-yellow //(lowH,lowS,lowV,highH,highS,highV)
    int colorID = 3; //0-green   1-red   2-yellow   3-hsv
    int red, green, blue;
    Mat imgHSV, imgThreshold;
    float redPerc, yellowPerc, float1Perc, float2Perc, float3Perc, float4Perc, pump1Perc, pump2Perc;
    int xRed, yRed, xGreen, yGreen, xYellow, yYellow;
    //int xdimension = 1920;
    //int ydimension = 1080;
    int xdimension, ydimension, xResolution, yResolution;
    int xdelta,ydelta;
    boolean timerIsOnP1 = false;
    boolean timerIsOnP2 = false;
    boolean HWAIsOn = false;
    boolean OVRIsOn = false;
    boolean float2IsOn = false;
    boolean float1IsOn = false;
    int counterP1, counterP2;
    Timer timerP1, timerP2;

    private static final String SERVER_IP = "192.168.0.13";
    private static final int SERVER_PORT = 11967;


    //app ui objects
    JavaCameraView cameraView;
    ImageView boxRed, boxYellow, boxStop, boxStart, boxOVR, boxHWA, boxPump1, boxPump2;
    ViewGroup rootLayout;
    TextView floatPerc, pumpPerc;
    Button btnRed, btnGreen, btnYellow, btnHSV;


    RelativeLayout.LayoutParams layoutParamsRed, layoutParamsYellow, layoutParamsF1, layoutParamsF2, layoutParamsF3, layoutParamsF4, layoutParamsP1, layoutParamsP2;

    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder builder;



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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        xResolution = displayMetrics.widthPixels;//1080
        yResolution = displayMetrics.heightPixels;//1794

        cameraView = (JavaCameraView)findViewById(R.id.cameraView);
        cameraView.setVisibility(SurfaceView.VISIBLE);
        cameraView.setCameraIndex(0);//0 is back 1 is front
        cameraView.setCvCameraViewListener(MainActivity.this);

        rootLayout = (ViewGroup) findViewById(R.id.activity_main);
        boxHWA = (ImageView) findViewById(R.id.boxHighWaterAlarm);
        boxOVR = (ImageView) findViewById(R.id.boxOverride);

        boxStop = (ImageView) findViewById(R.id.boxFloat1);
        boxStart = (ImageView) findViewById(R.id.boxFloat2);
        //boxFloat3 = (ImageView) findViewById(R.id.boxFloat3);
        //boxFloat4 = (ImageView) findViewById(R.id.boxFloat4);

        boxPump1 = (ImageView) findViewById(R.id.boxPump1);
        boxPump2 = (ImageView) findViewById(R.id.boxPump2);

        floatPerc = (TextView) findViewById(R.id.floatPerc);
        pumpPerc = (TextView) findViewById(R.id.pumpPerc);

        btnRed = (Button) findViewById(R.id.btnRed);
        btnGreen = (Button) findViewById(R.id.btnGreen);
        btnYellow = (Button) findViewById(R.id.btnYellow);
        btnHSV = (Button) findViewById(R.id.btnHSV);

        btnRed.setOnClickListener(this);
        btnGreen.setOnClickListener(this);
        btnYellow.setOnClickListener(this);
        btnHSV.setOnClickListener(this);

        layoutParamsRed = new RelativeLayout.LayoutParams(100, 100);
        layoutParamsYellow = new RelativeLayout.LayoutParams(100, 100);

        layoutParamsF1 = new RelativeLayout.LayoutParams(100, 100);
        layoutParamsF2 = new RelativeLayout.LayoutParams(100, 100);
        layoutParamsF3 = new RelativeLayout.LayoutParams(100, 100);
        layoutParamsF4 = new RelativeLayout.LayoutParams(100, 100);

        layoutParamsP1 = new RelativeLayout.LayoutParams(100,100);
        layoutParamsP2 = new RelativeLayout.LayoutParams(100,100);

        layoutParamsRed.leftMargin = 60;
        layoutParamsRed.topMargin = 0;
        layoutParamsYellow.leftMargin = 60;
        layoutParamsYellow.topMargin = 110;

        layoutParamsF1.leftMargin = 170;
        layoutParamsF1.topMargin = 0;
        layoutParamsF2.leftMargin = 170;
        layoutParamsF2.topMargin = 110;
        layoutParamsF3.leftMargin = 170;
        layoutParamsF3.topMargin = 220;
        layoutParamsF4.leftMargin = 170;
        layoutParamsF4.topMargin = 330;

        layoutParamsP1.leftMargin = 280;
        layoutParamsP1.topMargin = 0;
        layoutParamsP2.leftMargin = 280;
        layoutParamsP2.topMargin = 110;

/*        boxRed.setLayoutParams(layoutParamsRed);
        boxRed.setOnTouchListener(this);
        boxYellow.setLayoutParams(layoutParamsYellow);
        boxYellow.setOnTouchListener(this);*/

        boxStop.setLayoutParams(layoutParamsF1);
        boxStop.setOnTouchListener(this);
        boxStart.setLayoutParams(layoutParamsF2);
        boxStart.setOnTouchListener(this);
        boxOVR.setLayoutParams(layoutParamsF3);
        boxOVR.setOnTouchListener(this);
        boxHWA.setLayoutParams(layoutParamsF4);
        boxHWA.setOnTouchListener(this);

        boxPump1.setLayoutParams(layoutParamsP1);
        boxPump1.setOnTouchListener(this);
        boxPump2.setLayoutParams(layoutParamsP2);
        boxPump2.setOnTouchListener(this);

        Log.i("MainActivity", "App Initialized Successfully!");

        createNotificationChannel();

        builder = new NotificationCompat.Builder(this, "lemubitA")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("float 4 is on")
                .setContentText("float 4 is on")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager = NotificationManagerCompat.from(this);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "studentChannel";
            String description = "Channel for student notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("lemubitA", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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

                int x = (xResolution - xdimension)/2;
                int y = (yResolution - ydimension)/2;


                //keep the box inide the camera's area
                if(layoutParams.leftMargin < x){
                    layoutParams.leftMargin = x;

                }
                if(layoutParams.topMargin < 0) {
                    layoutParams.topMargin = 0;

                }
                if(layoutParams.topMargin > yResolution-100) {
                    layoutParams.topMargin = yResolution-100;

                }
                if(layoutParams.leftMargin > xdimension+x-100) {
                    layoutParams.leftMargin = xdimension+x-100;
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
        Log.d("mainactivity", "camera paused");
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraView != null){
            cameraView.disableView();
        }
        Log.d("mainactivity", "camera destroyed");
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
        Log.d("mainactivity", "camera resumed");
    }






    @Override
    public void onCameraViewStarted(int width, int height) {

        imgHSV = new Mat(width, height, CvType.CV_8UC4);
        imgThreshold = new Mat(width, height, CvType.CV_8UC4);

        xdimension = width;//960
        ydimension = height;//1280

        Log.d("mainactivity", "camera started");

    }






    @Override
    public void onCameraViewStopped() {
        imgHSV.release();
        Log.d("mainactivity", "camera stopped");
    }



    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        //float redPerc, greenPerc, yellowPerc;

        UpdateAsyncTask updateAsyncTask1 = new UpdateAsyncTask();
        UpdateAsyncTask updateAsyncTask2 = new UpdateAsyncTask();
        UpdateAsyncTask updateAsyncTask3 = new UpdateAsyncTask();
        UpdateAsyncTask updateAsyncTask4 = new UpdateAsyncTask();
        UpdateAsyncTask updateAsyncTask5 = new UpdateAsyncTask();
        UpdateAsyncTask updateAsyncTask6 = new UpdateAsyncTask();

        UpdatePumpAsyncTask updatePump1Task = new UpdatePumpAsyncTask();
        UpdatePumpAsyncTask updatePump2Task = new UpdatePumpAsyncTask();

       TimerTask timerTaskP1 = new TimerTask() {

            @Override
            public void run() {
                //Log.d("mainactivity","seconds passed " + counterP1);
                counterP1++;
            }
        };

        TimerTask timerTaskP2 = new TimerTask() {

            @Override
            public void run() {
                //Log.d("mainactivity","seconds passed " + counterP1);
                counterP2++;
            }
        };

        //scLow = new Scalar(iLowH, iLowS, iLowV);
        //scHigh = new Scalar(iHighH, iHighS, iHighV);

        Imgproc.cvtColor(inputFrame.rgba(), imgHSV, Imgproc.COLOR_BGR2HSV);//process image and convert it to hsv

        //Core.inRange(imgHSV, new Scalar(colorRange[1][0], colorRange[1][1], colorRange[1][2]), new Scalar(colorRange[1][3], colorRange[1][4], colorRange[1][5]), imgThreshold);
        //redPerc = getPercBW((boxRed.getLeft()-60), boxRed.getTop());

        if(!timerIsOnP1 && pump1Perc >= 50){
            //start timer
            //Date currentTime = Calendar.getInstance().getTime();
            timerP1 = new Timer("Pump1Timer");//create a new timer
            timerP1.scheduleAtFixedRate(timerTaskP1, 30, 1000);//start timer in 30ms to increment  counter
            timerIsOnP1 = true;
            updateAsyncTask1.execute("LightStatus3","true");

        }else if(timerIsOnP1 && pump1Perc < 50){
            //stop timer
            timerP1.cancel();
            timerIsOnP1 = false;
            updateAsyncTask1.execute("LightStatus3","false");
            updatePump1Task.execute(String.valueOf(counterP1), "PumpTimes1");
            counterP1 = 0;
        }

        if(!timerIsOnP2 && pump2Perc >= 50){
            //start timer
            //Date currentTime = Calendar.getInstance().getTime();
            timerP2 = new Timer("Pump2Timer");//create a new timer
            timerP2.scheduleAtFixedRate(timerTaskP2, 30, 1000);//start timer in 30ms to increment  counter
            timerIsOnP2 = true;
            updateAsyncTask2.execute("LightStatus4","true");

        }else if(timerIsOnP2 && pump2Perc < 50){
            //stop timer
            timerP2.cancel();
            timerIsOnP2 = false;
            updateAsyncTask2.execute("LightStatus4","false");
            updatePump2Task.execute(String.valueOf(counterP2), "PumpTimes2");
            counterP2 = 0;
        }


        //Core.inRange(imgHSV, new Scalar(colorRange[2][0], colorRange[2][1], colorRange[2][2]), new Scalar(colorRange[2][3], colorRange[2][4], colorRange[2][5]), imgThreshold);
        //yellowPerc = getPercBW((boxYellow.getLeft()-60), boxYellow.getTop());



        Core.inRange(imgHSV, new Scalar(colorRange[0][0], colorRange[0][1], colorRange[0][2]), new Scalar(colorRange[0][3], colorRange[0][4], colorRange[0][5]), imgThreshold);
        float1Perc = getPercBW((boxStop.getLeft()-60), boxStop.getTop());
        float2Perc = getPercBW((boxStart.getLeft()-60), boxStart.getTop());
        pump1Perc = getPercBW((boxPump1.getLeft()-60), boxPump1.getTop());
        pump2Perc = getPercBW((boxPump2.getLeft()-60), boxPump2.getTop());

        Core.inRange(imgHSV, new Scalar(colorRange[2][0], colorRange[2][1], colorRange[2][2]), new Scalar(colorRange[2][3], colorRange[2][4], colorRange[2][5]), imgThreshold);
        float3Perc = getPercBW((boxOVR.getLeft()-60), boxOVR.getTop());

        Core.inRange(imgHSV, new Scalar(colorRange[1][0], colorRange[1][1], colorRange[1][2]), new Scalar(colorRange[1][3], colorRange[1][4], colorRange[1][5]), imgThreshold);
        float4Perc = getPercBW((boxHWA.getLeft()-60), boxHWA.getTop());

        if(!HWAIsOn && float4Perc >= 50){
            //notificationManager.notify(100, builder.build());
            updateAsyncTask3.execute("LightStatus1","true");
            HWAIsOn = true;
        }else if (HWAIsOn && float4Perc < 50){
            updateAsyncTask3.execute("LightStatus1","false");
            HWAIsOn = false;
        }

        if(!OVRIsOn && float3Perc >= 50){
            //notificationManager.notify(100, builder.build());
            updateAsyncTask4.execute("LightStatus2","true");
            OVRIsOn = true;
        }else if (OVRIsOn && float3Perc < 50){
            updateAsyncTask4.execute("LightStatus2","false");
            OVRIsOn = false;
        }

        if(!float2IsOn && float2Perc >= 50){
            //notificationManager.notify(100, builder.build());
            updateAsyncTask5.execute("LightStatus5","true");
            float2IsOn = true;
        }else if (float2IsOn && float2Perc < 50){
            updateAsyncTask5.execute("LightStatus5","false");
            float2IsOn = false;
        }

        if(!float1IsOn && float1Perc >= 50){
            //notificationManager.notify(100, builder.build());
            updateAsyncTask6.execute("LightStatus6","true");
            float1IsOn = true;
        }else if (float1IsOn && float1Perc < 50){
            updateAsyncTask6.execute("LightStatus6","false");
            float1IsOn = false;
        }




        runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                floatPerc.setText("Stop: "+(int)float1Perc+"\nStart: "+(int)float2Perc+"\nOverride: "+(int)float3Perc+"\nHigh Water: "+(int)float4Perc);
                pumpPerc.setText("pump 1: "+(int)pump1Perc+"\npump 2: "+(int)pump2Perc);
            }
        });


        if(colorID != 3) {
            Core.inRange(imgHSV, new Scalar(colorRange[colorID][0], colorRange[colorID][1], colorRange[colorID][2]), new Scalar(colorRange[colorID][3], colorRange[colorID][4], colorRange[colorID][5]), imgThreshold);

            /*Imgproc.rectangle(imgThreshold, new Point((boxRed.getLeft() - 60), boxRed.getTop()), new Point((boxRed.getLeft() + 38), (boxRed.getTop() + 99)), new Scalar(255, 0, 255), 1);
            Imgproc.rectangle(imgThreshold, new Point((boxFloat1.getLeft() - 60), boxFloat1.getTop()), new Point((boxFloat1.getLeft() + 38), (boxFloat1.getTop() + 99)), new Scalar(255, 0, 255), 1);
            Imgproc.rectangle(imgThreshold, new Point((boxYellow.getLeft() - 60), boxYellow.getTop()), new Point((boxYellow.getLeft() + 38), (boxYellow.getTop() + 99)), new Scalar(255, 0, 255), 1);
            Imgproc.rectangle(imgThreshold, new Point(0, 0), new Point(imgThreshold.width() - 1, imgThreshold.height() - 1), new Scalar(255, 0, 255), 2);//draw border
*/
            return imgThreshold;

        }else{//hsv
            /*
            Imgproc.rectangle(imgHSV, new Point((boxRed.getLeft() - 60), boxRed.getTop()), new Point((boxRed.getLeft() + 38), (boxRed.getTop() + 99)), new Scalar(255, 0, 255), 1);
            Imgproc.rectangle(imgHSV, new Point((boxFloat1.getLeft() - 60), boxFloat1.getTop()), new Point((boxFloat1.getLeft() + 38), (boxFloat1.getTop() + 99)), new Scalar(255, 0, 255), 1);
            Imgproc.rectangle(imgHSV, new Point((boxYellow.getLeft() - 60), boxYellow.getTop()), new Point((boxYellow.getLeft() + 38), (boxYellow.getTop() + 99)), new Scalar(255, 0, 255), 1);
            Imgproc.rectangle(imgHSV, new Point(0, 0), new Point(imgThreshold.width() - 1, imgThreshold.height() - 1), new Scalar(255, 0, 255), 2);//draw border
            */
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
     * Async Task to update all light statuses
     */
    private class UpdateAsyncTask extends AsyncTask<String, Void, Boolean> {
        Document userItem;
        boolean isSuccess = false;
        @Override
        protected Boolean doInBackground(String... strings) {
            Log.d(AppSettings.tag, "In UpdateAsyncTask DoInBackground");

            String idToken = getIntent().getStringExtra("idToken");
            HashMap<String, String> logins = new HashMap<String, String>();
            logins.put("cognito-idp.us-west-2.amazonaws.com/us-west-2_kZujWKyqd", idToken);

            //create instance of DatabaseAccess and decode idToken
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(MainActivity.this, logins);
            JWT jwt = new JWT(idToken);
            String subject = jwt.getSubject();

            try {
                //retrieve userItem from database and update desired lightStatuses
//                userItem = databaseAccess.getUserItem(subject);
                isSuccess = databaseAccess.updateLightStatus(strings[0], strings[1], subject);

            }catch (Exception e){
                Log.e(AppSettings.tag, "error updating light statuses");
            }

            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            super.onPostExecute(isSuccess);
            //Log.d(AppSettings.tag, "In UpdateAsyncTask onPostExecute: " + isSuccess);

        }
    }


    /**
     * Async Task to update all light statuses
     * @param: list of strings containing (in order) pump time, pump number
     */
    private class UpdatePumpAsyncTask extends AsyncTask<String, Void, Boolean> {

        boolean isSuccess1 = false;
        boolean isSuccess2 = false;

        @Override
        protected Boolean doInBackground(String... strings) {
            Log.d(AppSettings.tag, "In UpdatePumpAsyncTask DoInBackground");

            String idToken = getIntent().getStringExtra("idToken");
            HashMap<String, String> logins = new HashMap<String, String>();
            logins.put("cognito-idp.us-west-2.amazonaws.com/us-west-2_kZujWKyqd", idToken);

            //create instance of DatabaseAccess and decode idToken
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(MainActivity.this, logins);
            JWT jwt = new JWT(idToken);
            String subject = jwt.getSubject();

            try {
                //retrieve userItem from database and update desired PumpTimes
                isSuccess1 = databaseAccess.updatePumpTime(strings[0], strings[1], subject);

//                Date currentTime = Calendar.getInstance().getTime();
//                isSuccess2 = databaseAccess.updatePumpDateTime(currentTime, "DateTimePump" + strings[1], subject);


            }catch (Exception e){
                Log.e(AppSettings.tag, "error updating pump times");
                Log.e(AppSettings.tag, e.getMessage());
            }

            return isSuccess2;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            super.onPostExecute(isSuccess);
            Log.d(AppSettings.tag, "In UpdatePumpAsyncTask onPostExecute: " + isSuccess);

        }


    }
}

