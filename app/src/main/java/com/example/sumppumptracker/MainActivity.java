package com.example.sumppumptracker;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;



    //item in DynamoDB to update (hardcoded)
    private String lightStatus = "1";
    private String TAG = "DynamoDB";
    private TextView textViewItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //declare cameraBridgeViewBase. reference ID declared in activiy_main.xml
        cameraBridgeViewBase = (JavaCameraView)findViewById(R.id.myCameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
//        cameraBridgeViewBase.enableView();

        //enable view if loading is successful
        baseLoaderCallback = new BaseLoaderCallback(this ) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                switch(status){
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };


        //Button to update lightID: 1
        Button buttonUpdate1 = findViewById(R.id.button1);
        buttonUpdate1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Updating lightID: 1");
                //create a new AsyncTask
                UpdateAsyncTask updateAsyncTask = new UpdateAsyncTask();
                //execute AsyncTask and passing it the primary key
                updateAsyncTask.execute("1");
            }
        });

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
                isSuccess = databaseAccess.updateLightStatus("1");
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













    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        Mat frame = inputFrame.rgba();
//        mat1 = inputFrame.rgba();
        return frame;
    }

    @Override
    public void onCameraViewStopped(){
//        mat1.release();
//        mat2.release();
//        mat3.release();
    }

    //function is called essentially every frame
    @Override
    public void onCameraViewStarted(int width, int height){
//        mat1 = new Mat(width, height, CvType.CV_8UC4); //basically the channel of the image
//        mat2 = new Mat(width, height, CvType.CV_8UC4);
//        mat3 = new Mat(width, height, CvType.CV_8UC4);
    }

    //turns off camera or pauses camera
    @Override
    protected  void onPause() {
        super.onPause();
        if(cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        //checks if CV loads correctly
        if(!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(),"There is a problem in OpenCV", Toast.LENGTH_SHORT).show();
        }
        else{
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if (cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
    }

}
