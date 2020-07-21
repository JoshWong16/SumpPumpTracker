package com.example.sumppumptracker;

import android.content.Context;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.regions.Regions;

public class CognitoSettings {

    private Regions cognitoRegion = Regions.US_WEST_2;

    private Context context;

    public CognitoSettings(Context context){
        this.context = context;
    }

    //returns user pool object so user can sign up
    //App Settings is privatized class with aws credentials
    public CognitoUserPool getUserPool(){
        return new CognitoUserPool(context, AppSettings.userPoolId, AppSettings.clientId, AppSettings.clientSecret, cognitoRegion);
    }
}
