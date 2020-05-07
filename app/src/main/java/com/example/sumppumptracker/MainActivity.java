package com.example.sumppumptracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
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
    Mat mat1, mat2, mat3;
    int counter = 0;

    //methods act as triggers that the app looks for
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
