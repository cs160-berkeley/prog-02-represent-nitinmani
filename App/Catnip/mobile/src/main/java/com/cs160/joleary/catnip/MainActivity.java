package com.cs160.joleary.catnip;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

//import org.json.JSONArray;
//import org.json.JSONObject;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    //there's not much interesting happening. when the buttons are pressed, they start
    //the PhoneToWatchService with the cat name passed in.

    private Button mFredButton;
    private Button mLexyButton;
    private EditText zipcode;
    public GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Double mLatitude;
    private Double mLongitude;
    private Intent sendIntent;
    String tweet; String twitterImageURL;
    public String[] names;
    public String[] websites;
    public String[] emails;
    public String[] termEnds;
    public String[] twitter_ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mFredButton = (Button) findViewById(R.id.fred_btn);
        mLexyButton = (Button) findViewById(R.id.lexy_btn);
        zipcode = (EditText) findViewById(R.id.editText);

        mFredButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                names = new String[4];
                emails = new String[4];
                websites = new String[4];
                termEnds = new String[4];
                twitter_ids = new String[4];
                Ion.with(MainActivity.this)
                        .load("http://congress.api.sunlightfoundation.com/legislators/locate?latitude=" + Double.toString(mLatitude) + "&longitude=" + Double.toString(mLongitude) + "&apikey=41e1deffd8124c65911ed01d6aaadc2f")
                                .asJsonObject()
                                .setCallback(new FutureCallback<JsonObject>() {
                                    @Override
                                    public void onCompleted(Exception e, JsonObject result) {
                                        final Intent activityIntent = new Intent(MainActivity.this, LocationCandidates.class);
                                        final Intent sendIntent = new Intent(getBaseContext(), PhoneToWatchService.class);
                                        JsonArray representatives = result.getAsJsonArray("results");
                                        JsonObject rep;
                                        int i = 0;
                                        for (JsonElement json_rep : representatives) {
                                            String party;
                                            rep = json_rep.getAsJsonObject();
                                            if (rep.get("party").getAsString().equalsIgnoreCase("D")) {
                                                party = "(D)";
                                            } else if (rep.get("party").getAsString().equalsIgnoreCase("R")) {
                                                party = "(R)";
                                            } else {
                                                party = "(I)";
                                            }
                                            names[i] = rep.get("first_name").getAsString() + " " + rep.get("last_name").getAsString() + " " + party;
                                            emails[i] = rep.get("oc_email").getAsString();
                                            websites[i] = rep.get("website").getAsString();
                                            termEnds[i] = rep.get("term_end").getAsString();
                                            twitter_ids[i] = rep.get("twitter_id").getAsString();
                                            i++;
                                        }
                                        activityIntent.putExtra("NAMES", names);
                                        activityIntent.putExtra("EMAILS", emails);
                                        activityIntent.putExtra("WEBSITES", websites);
                                        activityIntent.putExtra("TERM_ENDS", termEnds);
                                        activityIntent.putExtra("TWITTER_IDS", twitter_ids);
                                        sendIntent.putExtra("CAT_NAME", "Fred");
                                        sendIntent.putExtra("NAMES", names);
                                        Log.d("watchIntent", sendIntent.toString());
                                        Ion.with(MainActivity.this)
                                                .load("https://maps.googleapis.com/maps/api/geocode/json?latlng="+Double.toString(mLatitude)+","+Double.toString(mLongitude)+"&key=AIzaSyDPTAqzhB4R12A0CrxdJNiBpM0un73go1I")
                                                        .asJsonObject()
                                                        .setCallback(new FutureCallback<JsonObject>() {
                                                            @Override
                                                            public void onCompleted(Exception e, JsonObject result) {
                                                                JsonArray locations = result.getAsJsonArray("results");
                                                                JsonElement address = locations.get(0);
                                                                JsonObject addrObj = address.getAsJsonObject();
                                                                JsonArray addressComponents = addrObj.getAsJsonArray("address_components");
                                                                JsonElement county = addressComponents.get(4);
                                                                JsonElement zipcodeElem = addressComponents.get(7);
                                                                JsonObject countyObj = county.getAsJsonObject();
                                                                String countyName = countyObj.get("long_name").getAsString();
                                                                JsonObject zipcodeObj = zipcodeElem.getAsJsonObject();
                                                                String zipcode = zipcodeObj.get("long_name").getAsString();
                                                                Log.d("countyName", countyName);
                                                                Log.d("zipcode", zipcode);
                                                                sendIntent.putExtra("COUNTY", countyName);
                                                                activityIntent.putExtra("ZIPCODE", zipcode);
                                                                Log.d("county", countyName);
                                                                startService(sendIntent);
                                                                startActivity(activityIntent);
                                                            }
                                                        });
                                    }
                                });
            }
        });

        zipcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zipcode.setText("");
            }

        });

        mLexyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (zipcode.getText().toString().length() != 5) {
                    Context context = getApplicationContext();
                    CharSequence text = "Please enter a 5 digit zipcode";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    return;
                }
                Log.d("zipcode", zipcode.getText().toString());
                String link = url(zipcode.getText().toString(), null, null);
                names = new String[4];
                emails = new String[4];
                websites = new String[4];
                termEnds = new String[4];
                twitter_ids = new String[4];

                Ion.with(MainActivity.this)
                        .load("http://congress.api.sunlightfoundation.com/legislators/locate?zip=" + zipcode.getText().toString() + "&apikey=41e1deffd8124c65911ed01d6aaadc2f")
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                final Intent activityIntent = new Intent(MainActivity.this, LocationCandidates.class);
                                final Intent sendIntent = new Intent(getBaseContext(), PhoneToWatchService.class);
                                JsonArray representatives = result.getAsJsonArray("results");
                                JsonObject rep;
                                int i = 0;
                                for (JsonElement json_rep : representatives) {
                                    String party;
                                    rep = json_rep.getAsJsonObject();
                                    if (rep.get("party").getAsString().equalsIgnoreCase("D")) {
                                        party = "(D)";
                                    } else if (rep.get("party").getAsString().equalsIgnoreCase("R")) {
                                        party = "(R)";
                                    } else {
                                        party = "(I)";
                                    }
                                    names[i] = rep.get("first_name").getAsString() + " " + rep.get("last_name").getAsString() + " " + party;
                                    emails[i] = rep.get("oc_email").getAsString();
                                    websites[i] = rep.get("website").getAsString();
                                    termEnds[i] = rep.get("term_end").getAsString();
                                    twitter_ids[i] = rep.get("twitter_id").getAsString();
                                    i++;
                                }
                                activityIntent.putExtra("NAMES", names);
                                activityIntent.putExtra("EMAILS", emails);
                                activityIntent.putExtra("WEBSITES", websites);
                                activityIntent.putExtra("TERM_ENDS", termEnds);
                                activityIntent.putExtra("TWITTER_IDS", twitter_ids);
                                activityIntent.putExtra("ZIPCODE", zipcode.getText().toString());
                                sendIntent.putExtra("CAT_NAME", "Lexy");
                                sendIntent.putExtra("NAMES", names);
                                Ion.with(MainActivity.this)
                                        .load("https://maps.googleapis.com/maps/api/geocode/json?address="+zipcode.getText().toString()+"&key=AIzaSyDPTAqzhB4R12A0CrxdJNiBpM0un73go1I")
                                        .asJsonObject()
                                        .setCallback(new FutureCallback<JsonObject>() {
                                            @Override
                                            public void onCompleted(Exception e, JsonObject result) {
                                                JsonArray locations = result.getAsJsonArray("results");
                                                JsonElement address = locations.get(0);
                                                JsonObject addrObj = address.getAsJsonObject();
                                                JsonObject extraInfo = addrObj.getAsJsonObject("geometry");
                                                Log.d("a", extraInfo.toString());
                                                JsonObject location = extraInfo.getAsJsonObject("location");
                                                String latitude = location.get("lat").getAsString();
                                                String longitude = location.get("lng").getAsString();
                                                Log.d("b", latitude);
                                                Log.d("c", longitude);
                                                Ion.with(MainActivity.this)
                                                        .load("https://maps.googleapis.com/maps/api/geocode/json?latlng="+latitude+","+longitude+"&key=AIzaSyDPTAqzhB4R12A0CrxdJNiBpM0un73go1I")
                                                        .asJsonObject()
                                                        .setCallback(new FutureCallback<JsonObject>() {
                                                            @Override
                                                            public void onCompleted(Exception e, JsonObject result) {
                                                                JsonArray locations = result.getAsJsonArray("results");
                                                                JsonElement address = locations.get(0);
                                                                JsonObject addrObj = address.getAsJsonObject();
                                                                JsonArray addressComponents = addrObj.getAsJsonArray("address_components");
                                                                JsonElement county = addressComponents.get(3);
                                                                JsonObject countyObj = county.getAsJsonObject();
                                                                String countyName = countyObj.get("long_name").getAsString();
                                                                Log.d("countyName", countyName);
                                                                sendIntent.putExtra("COUNTY", countyName);
                                                                Log.d("ZIPCODE_COUNTY", countyName);
                                                                startService(sendIntent);
                                                                startActivity(activityIntent);
                                                            }
                                                        });
                                            }
                                        });

                            }
                        });
            }
        });

    }


    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch(SecurityException e) {
            e.printStackTrace();
        }
        if (mLastLocation != null) {
            mLatitude = mLastLocation.getLatitude();
            mLongitude = mLastLocation.getLongitude();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    private String url(String zipcode, String longitude, String latitude){
        if(zipcode != null){
            return "http://congress.api.sunlightfoundation.com/legislators/locate?zip="+zipcode+"&apikey=41e1deffd8124c65911ed01d6aaadc2f";
        }
        return "http://congress.api.sunlightfoundation.com/legislators/locate?latitude="+latitude+"&longitude="+longitude+"&apikey=41e1deffd8124c65911ed01d6aaadc2f";
    }

}