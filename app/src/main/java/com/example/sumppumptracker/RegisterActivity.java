package com.example.sumppumptracker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerUser();
    }

    private void registerUser(){
        final EditText inputEmail = findViewById(R.id.email);
        final EditText inputPassword = findViewById(R.id.password);
        final EditText inputUsername = findViewById(R.id.username);
        final EditText inputName = findViewById(R.id.name);
        final EditText inputPhone = findViewById(R.id.phone);

        //Create a CognitoUserAttributes object and add user attributes
        final CognitoUserAttributes userAttributes = new CognitoUserAttributes();

        //object that executes onSuccess or onFailure methods based on if registration by user is successful
        final SignUpHandler signupCallback = new SignUpHandler() {
            @Override
            public void onSuccess(CognitoUser user, boolean signUpConfirmationState, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                //sign up successful
                Log.d(AppSettings.tag, "sign up success...is confirmed: " + signUpConfirmationState);
                //check if this user (cognitoUser) needs to be confirmed
                if (!signUpConfirmationState){
                    Log.d(AppSettings.tag, "sign up success...not confirmed, verification code sent to: " + cognitoUserCodeDeliveryDetails.getDestination());
                    onRegisterClicked(String.valueOf(inputUsername.getText()), String.valueOf(inputPhone.getText()));
                }
                else{
                    //user already confirmed
                    Log.d(AppSettings.tag, "sign up success...confirmed");
                }
            }

            @Override
            public void onFailure(Exception exception) {
                Log.e(AppSettings.tag, "sign up failure: " + exception.getLocalizedMessage());
            }
        };

        Button buttonRegister = findViewById(R.id.register);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //add user attributes
                userAttributes.addAttribute("given_name", String.valueOf(inputName.getText()));
                userAttributes.addAttribute("email", String.valueOf(inputEmail.getText()));
                userAttributes.addAttribute("phone_number", "+1" + String.valueOf(inputPhone.getText()));

                //access custom cognitoSettings class and call getUserPool() to sign up user
                CognitoSettings cognitoSettings = new CognitoSettings(RegisterActivity.this);
                cognitoSettings.getUserPool().signUpInBackground(String.valueOf(inputUsername.getText()),
                        String.valueOf(inputPassword.getText()), userAttributes, null, signupCallback);
            }
        });

        Button buttonLogin = findViewById(R.id.login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLoginClicked();
            }
        });

    }

    /**
     * Creates intent to start new verification activity
     */
    private void onRegisterClicked(String username, String phone){
        Log.d(AppSettings.tag, "onRegisterClicked");
        Intent intent = new Intent("android.intent.action.VerifyActivity");
        intent.putExtra("username", username);
        intent.putExtra("phone", phone);
        startActivity(intent);
    }

    /**
     * Creates intent to start new Login activity
     */
    private void onLoginClicked(){
        Log.d(AppSettings.tag, "onRegisterClicked");
        Intent intent = new Intent("android.intent.action.LoginActivity");
        startActivity(intent);
    }



}