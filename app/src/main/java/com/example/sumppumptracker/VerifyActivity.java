package com.example.sumppumptracker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;

import androidx.appcompat.app.AppCompatActivity;

public class VerifyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        final EditText editTextCode = findViewById(R.id.verificationCode);
        final EditText editTextUsername = findViewById(R.id.username);

        Button buttonVerify = findViewById(R.id.verify);
        buttonVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ConfirmTask().execute(String.valueOf(editTextCode.getText()),
                        String.valueOf(editTextUsername.getText()));
            }
        });
    }

    private class ConfirmTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            final String[] result = new String[1];

            //Callback Handler for confirmSignUp API
            final GenericHandler confirmationCallback = new GenericHandler() {
                @Override
                public void onSuccess() {
                    //User was successfully confirmed
                    result[0] = "Success!";

                    onVerifyClicked(getIntent().getStringExtra("username"), getIntent().getStringExtra("phone"));
                }

                @Override
                public void onFailure(Exception exception) {
                    //User confirmation failed. Check exception for the cause.
                    result[0] = "Failed: " + exception.getMessage();
                }
            };

            CognitoSettings cognitoSettings = new CognitoSettings(VerifyActivity.this);

            CognitoUser thisUser = cognitoSettings.getUserPool().getUser(strings[1]);
            //this will cause confirmation to fail if the user attribute (alias) has been verified
            //for another user in the same pool
            thisUser.confirmSignUp(strings[0], false, confirmationCallback);
            return result[0];
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.d(AppSettings.tag, "Confirmation Result" + result);
        }
    }

    /**
     * Creates intent to start new Login activity
     */
    private void onVerifyClicked(String username, String phone){
        Log.d(AppSettings.tag, "onRegisterClicked");
        Intent intent = new Intent("android.intent.action.FirstLoginActivity");
        intent.putExtra("username", username);
        intent.putExtra("phone", phone);
        startActivity(intent);
    }

}