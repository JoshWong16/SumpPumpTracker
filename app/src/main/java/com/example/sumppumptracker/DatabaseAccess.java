package com.example.sumppumptracker;

import android.content.ClipData;
import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.document.ScanOperationConfig;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Search;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table;
import com.amazonaws.mobileconnectors.dynamodbv2.document.UpdateItemOperationConfig;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.DynamoDBEntry;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.DynamoDBList;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Primitive;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.PrimitiveList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ItemCollectionMetrics;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.auth0.android.jwt.JWT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Purpose of class is to initialize connection to DynamoDB
 * Defines CRUD methods
 */
public class DatabaseAccess {

    private String TAG = "SumpPumpDB";

    private final Regions COGNITO_ITENTITY_POOL_REGION = Regions.US_WEST_2;
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
        credentialsProvider =  new CognitoCachingCredentialsProvider(context, AppSettings.COGNITO_IDENTITY_POOL_ID, COGNITO_ITENTITY_POOL_REGION);
        credentialsProvider.setLogins(logins);
        //Create a connection to the DynamoDB service
        dbClient = new AmazonDynamoDBClient(credentialsProvider);
        //Must set dbClient region here or else it defaults to us_east_1
        dbClient.setRegion(Region.getRegion(Regions.US_WEST_2));
        //Create a table reference
        dbTable = Table.loadTable(dbClient, AppSettings.DYNAMODB_TABLE_NAME);
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
    public boolean updateLightStatus(String lightID, String lightStatus, String sub){
        Log.i(TAG, "in updateLightStatus");

        Document retrievedDoc = dbTable.getItem(new Primitive(sub));

        if (retrievedDoc != null){

            //updates or switches the current light status
            retrievedDoc.put(lightID, lightStatus);

            //creates a document object with the updated result
            Document updateResult = dbTable.updateItem(retrievedDoc,new Primitive(sub), new UpdateItemOperationConfig().withReturnValues(ReturnValue.UPDATED_NEW));

            try{
                Log.d(AppSettings.tag, "updateResult: " + Document.toJson(updateResult));
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(AppSettings.tag, "updateResult json error: " + e.getLocalizedMessage());
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
     * also adds the date and time of when pumps turn on
     * @param time time it took for pump to empty water
     * @param numPump number of pump ie "PumpTimes2"
     * @param sub user subject from idToken
     */
    public boolean updatePumpTime(String time, String numPump, String sub){

        Log.d(AppSettings.tag, "Time passed is: " + String.valueOf(time));
        //get the userItem from DynamoDB
        Document retrievedDoc = dbTable.getItem(new Primitive(sub));
        if (retrievedDoc != null){
            //update set
            Set<String> replacementSet = new HashSet<>();

            //get the desired set of pump times associated with the user
            DynamoDBEntry timeSet = retrievedDoc.get(numPump);
            //convert Set to List
            List<String> timeList = timeSet.convertToAttributeValue().getSS();
            //add existing set to replacement set
            replacementSet.addAll(timeList);

            //add new value
            Date currentTime = Calendar.getInstance().getTime();
            String newVal = currentTime.toString() + "," + time;
            replacementSet.add(newVal);
            //edit userItem with new set
            retrievedDoc.put(numPump, replacementSet);
            Log.d(AppSettings.tag, String.valueOf(replacementSet));


            //creates a document object with the updated result and updates result in Dynamo
            Document updateResult = dbTable.updateItem(retrievedDoc, new Primitive(sub), new UpdateItemOperationConfig().withReturnValues(ReturnValue.UPDATED_NEW));

            try{
                Log.d(AppSettings.tag, "updateResult: " + Document.toJson(updateResult));
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(AppSettings.tag, "updatePumpTime json error: " + e.getLocalizedMessage());
            }
            return true;
        }else{
            return false;
        }
    }


    /**
     * createUser: when new user registers, this function creates a new row item in dynamodb for new user
     * @param idToken user's idToken
     * @param username
     * @param phone user's phone number
     */
    public void createUser(String idToken, String username, String phone){
        //get user subject from idToken
        JWT jwt = new JWT(idToken);
        String subject = jwt.getSubject();

        //create new user document object
        //add attributes
        Document user = new Document();
        user.put("UserId", subject);
        user.put("phone", "1" + phone);
        user.put("username", username);
        user.put("LightStatus1", "false");
        user.put("LightStatus2", "false");
        user.put("LightStatus3", "false");
        user.put("LightStatus4", "false");
        user.put("LightStatus5", "false");
        user.put("LightStatus6", "false");
        Set<String> pumpTimes1 = new HashSet<>();
        pumpTimes1.add("0");
        Set<String> pumpTimes2 = new HashSet<>();
        pumpTimes2.add("0");
        user.put("PumpTimes1", pumpTimes1);
        user.put("PumpTimes2", pumpTimes2);

        //add new user item to dynamoDB table
        dbTable.putItem(user);

    }

}