package com.example.sumppumptracker;

import android.content.Context;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.regions.Regions;

public class CognitoSettings {

    private String userPoolId = "us-west-2_kZujWKyqd";
    private String clientId = "6206cdmoi1re0jgi3gljkltrg5";
    private String clientSecret = "rr2kf0d72llm9l0t50rae0fjbb7b1n3a0agnu1gtur9kq6jbigu";
    private Regions cognitoRegion = Regions.US_WEST_2;

    private Context context;

    public CognitoSettings(Context context){
        this.context = context;
    }

    //returns user pool object so user can sign up
    public CognitoUserPool getUserPool(){
        return new CognitoUserPool(context, userPoolId, clientId, clientSecret, cognitoRegion);
    }
}
