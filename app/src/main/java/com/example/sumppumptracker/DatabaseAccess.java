package com.example.sumppumptracker;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.document.ScanOperationConfig;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Search;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table;
import com.amazonaws.mobileconnectors.dynamodbv2.document.UpdateItemOperationConfig;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Primitive;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Purpose of class is to initialize connection to DynamoDB
 * Defines CRUD methods
 */
public class DatabaseAccess {

    private String TAG = "SumpPumpDB";

    private final String COGNITO_IDENTITY_POOL_ID = "us-west-2:8f70a2b5-fb95-452b-8240-4cfe1ce3974a";
    private final Regions COGNITO_ITENTITY_POOL_REGION = Regions.US_WEST_2;
    private final String DYNAMODB_TABLE = "SumpPumpUsers";
    private Context context;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private AmazonDynamoDBClient dbClient;
    private Table dbTable;

    /*
    This class is a singleton - storage for the current instance
    */
    private static volatile DatabaseAccess instance;

    private DatabaseAccess(Context context, HashMap<String, String> logins){
        this.context = context;

        //Create a new credentials provider
        credentialsProvider =  new CognitoCachingCredentialsProvider(context, COGNITO_IDENTITY_POOL_ID, COGNITO_ITENTITY_POOL_REGION);
        credentialsProvider.setLogins(logins);
        //Create a connection to the DynamoDB service
        dbClient = new AmazonDynamoDBClient(credentialsProvider);
        //Must set dbClient region here or else it defaults to us_east_1
        dbClient.setRegion(Region.getRegion(Regions.US_WEST_2));
        //Create a table reference
        dbTable = Table.loadTable(dbClient, DYNAMODB_TABLE);
    }

    /**
     * Creates a singleton DatabaseAccess object
     * Singleton pattern - retrieve an instance of the DatabaseAccess
     * Ensures we always use the same instance of the DatabaseAccess class
     * Object is synchronized so that only one thread can run the instance at a time
     */
    public static synchronized DatabaseAccess getInstance(Context context, HashMap<String, String> logins){
        if (instance == null) {
            instance = new DatabaseAccess(context, logins);
        }
        return instance;
    }

    /**
     * method called to update a given lightID's status
     * @param lightID
     */
    public boolean updateLightStatus(String lightID, String lightStatus, Document retrievedDoc){
        Log.i(TAG, "in updateLightStatus");


        if (retrievedDoc != null){

            //updates or switches the current light status
            retrievedDoc.put(lightID, lightStatus);

            //creates a document object with the updated result
            Document updateResult = dbTable.updateItem(retrievedDoc, new Primitive(lightID),
                    new UpdateItemOperationConfig().withReturnValues(ReturnValue.UPDATED_NEW));

            try{
                Log.i(TAG, "updateResult: " + Document.toJson(updateResult));
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "updateResult json error: " + e.getLocalizedMessage());
            }
            return true;
        }else{
            return false;
        }

    }


    /**
     * Get light status of all lightID's
     * @param
     * @return List of document objects containing each LightID's lightstatus
     */
    public List<Document> getAllLightStatus(){
        //initialize scanconfig object
        ScanOperationConfig scanOperationConfig = new ScanOperationConfig();
        //create array of attributes to retrieve from each item
        List<String> attributeList = new ArrayList<>();
        attributeList.add("LightStatus");

        //Access table and return scan results
        scanOperationConfig.withAttributesToGet(attributeList);
        Search searchResult = dbTable.scan(scanOperationConfig);
        return searchResult.getAllResults();
    }

    /**
     * Get light status of specified light
     * @param: LightStatus#
     * @return: state of the light
     */
    public String getLightStatus(String lightID, Document user){
        String lightStatus = String.valueOf(user.get(lightID));
        return lightStatus;
    }

    /**
     * Get User item from database
     * @param: sub of user (from IDtoken)
     * @return: User item form DynamoDB as document object
     */
    public Document getUserItem(String sub){
        Document retrievedDoc = dbTable.getItem(new Primitive(sub));
        if(retrievedDoc != null){
            return retrievedDoc;
        }
        else{
            Log.e(AppSettings.tag, "error retrieving userItem from Dynamo");
            return null;
        }
    }

    /**
     * updates the array of pump times in dynamoDB
     * @param time it took for pump to empty water
     * @param numPump number of pump
     * @param userItem
     */
    public void updatePumpTime(int time, int numPump, Document userItem){

    }

}