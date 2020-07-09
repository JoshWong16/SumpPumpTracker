package com.example.sumppumptracker;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document;

import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class ShowLightStatus extends AppCompatActivity {

    TextView Light1, Light2, Light3, Light4, Light5, Light6;

    TextView[] textViewArr = new TextView[]{Light2, Light3, Light4, Light5, Light6, Light1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_light_status);



        //cast textView objects to actual textViews
        textViewArr[0] = findViewById(R.id.light2);
        textViewArr[1] = findViewById(R.id.light3);
        textViewArr[2] = findViewById(R.id.light4);
        textViewArr[3] = findViewById(R.id.light5);
        textViewArr[4] = findViewById(R.id.light6);
        textViewArr[5] = findViewById(R.id.light1);

        postRefresh();
    }

    /**
     * Async Task to get all light statuses
     */
    private class GetAllAsyncTask extends AsyncTask<Void, Void, List<Document>> {
        List<Document> documents;

        @Override
        protected List<Document> doInBackground(Void... voids) {
            Log.d(AppSettings.tag, "In GetAllAsyncTask DoInBackground");

            String idToken = getIntent().getStringExtra("idToken");
            HashMap<String, String> logins = new HashMap<String, String>();
            logins.put("cognito-idp.us-west-2.amazonaws.com/us-west-2_kZujWKyqd", idToken);

            //create instance of DatabaseAccess
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(ShowLightStatus.this, logins);

            try {
                //call getAllLightStatus method
                documents = databaseAccess.getAllLightStatus();
                System.out.println(documents);

            }catch (Exception e){
                Log.e(AppSettings.tag, "error getting light statuses");
            }

            return documents;
        }

        @Override
        protected void onPostExecute(List<Document> documents) {
            super.onPostExecute(documents);
            Log.d(AppSettings.tag, "In GetAllAsyncTask onPostExecute");
            //parse through documents list and assign values to respective textviews
            for(int lightNum = 0; lightNum<documents.size(); lightNum++){
                Document lightDoc = documents.get(lightNum);
                String lightStatus = String.valueOf(lightDoc.get("LightStatus").asBoolean());
                textViewArr[lightNum].setText(lightStatus);
            }

        }


    }


    /**
     * Refresh method
     */
    private void refresh(int milliseconds){
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                postRefresh();
            }
        };
    }

    public void postRefresh(){
        //Get all LightStatuses for each LightID and return as a list of Document objects
        final GetAllAsyncTask getAllAsyncTask = new GetAllAsyncTask();
        getAllAsyncTask.execute();

        refresh(1000);
    }
}