package com.example.sumppumptracker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;

import java.util.HashMap;

public class FirstLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText editTextUsername = findViewById(R.id.username);
        final EditText editTextPassword = findViewById(R.id.password);

        final AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                Log.d(AppSettings.tag, "Login successful, can get tokens here");

                String idToken = userSession.getIdToken().getJWTToken();

                CreateUserAsyncTask createUserAsyncTask = new CreateUserAsyncTask();
                createUserAsyncTask.execute(idToken);

                onLoginClicked(idToken);
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                Log.d(AppSettings.tag, "in getAuthenticationDetails");

                //need to get userID and password to continue
                AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId, String.valueOf(editTextPassword.getText()), null);

                //Pass user sign-in credentials to the continuation
                authenticationContinuation.setAuthenticationDetails(authenticationDetails);

                //Allow sign in to continue
                authenticationContinuation.continueTask();
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
                //not using and MFA
            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {
                //not using
            }

            @Override
            public void onFailure(Exception exception) {
                Log.e(AppSettings.tag, "Login failed: " + exception.getLocalizedMessage());
            }
        };

        Button btnlogin = findViewById(R.id.btnlogin);
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CognitoSettings cognitoSettings = new CognitoSettings(FirstLoginActivity.this);
                //retrieve given cognito user from the aws user pool
                CognitoUser thisUser = cognitoSettings.getUserPool().getUser(String.valueOf(editTextUsername.getText()));

                Log.d(AppSettings.tag, "Login button clicked");

                thisUser.getSessionInBackground(authenticationHandler);
            }
        });
    }

    /**
     * Creates intent to start new ShowLightStatus activity
     */
    private void onLoginClicked(String idToken){
        Log.d(AppSettings.tag, "onLoginClicked");
        Intent intent = new Intent("android.intent.action.MainActivity");
        intent.putExtra("idToken", idToken);

        startActivity(intent);
    }

    /**
     * Async Task to create new user item in dynamoDB
     * @param: list of strings containing (in order) idtoken
     */
    private class CreateUserAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            Log.d(AppSettings.tag, "In CreateUserAsynctask DoInBackground");

            HashMap<String, String> logins = new HashMap<>();
            logins.put(AppSettings.cognitoPoolURL, strings[0]);

            //create instance of DatabaseAccess and decode idToken
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(FirstLoginActivity.this, logins);

            try {

                databaseAccess.createUser(strings[0], getIntent().getStringExtra("username"), getIntent().getStringExtra("phone"));

            }catch (Exception e){
                Log.e(AppSettings.tag, "error creating user");
                Log.e(AppSettings.tag, e.getLocalizedMessage());
            }
            return null;
        }



    }
}