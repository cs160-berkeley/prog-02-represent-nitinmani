package com.cs160.joleary.catnip;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import io.fabric.sdk.android.Fabric;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Random;

public class MainActivity extends Activity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "YY0Fjne0ZbHBEKVOdMxZ0RgKF";
    private static final String TWITTER_SECRET = "i2owhs6upzICsOLGI2OgHWhqAlXSpugx76nWUpDh7JMoeeOIfb";


    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    public String[] candidates;
    public String[] names;
    public String[] parties;
    public String obamaPercent;
    public String romneyPercent;
    public String state;
    String county;
    int i = 0;
    private TextView mTextView;
    private Button mFeedBtn;
    private TextView obama;
    private TextView romney;
    private TextView presidentialView;
    boolean isZipCode = false;
    String prevText; String prevParty;
    String obamaPercentText;
    String romneyPercentText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);

        mFeedBtn = (Button) findViewById(R.id.feed_btn);
        mTextView = (TextView) findViewById(R.id.textView);
        obama = (TextView) findViewById(R.id.textView8);
        romney = (TextView) findViewById(R.id.textView9);
        presidentialView = (TextView) findViewById(R.id.textView12);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        Log.d("Checking", "the extras");
        if (extras != null) {
            String catName = extras.getString("CAT_NAME");
            if(catName.equalsIgnoreCase("Fred") ||catName.equalsIgnoreCase("Lexy")) {
                Log.d("names", extras.getString("NAMES"));
                county = extras.getString("COUNTY");
                Log.d("phonecounty", county);
                String bunchOfInfo = extras.getString("NAMES");
                Log.d("names", bunchOfInfo);
                names = bunchOfInfo.split("\n");
                candidates = Arrays.copyOfRange(names, 1, names.length);
            }
            parties = new String[candidates.length];
            for(int i = 0; i < candidates.length; i++) {
                char c = candidates[i].charAt(candidates[i].length() - 2);
                if(c == 'R') {
                    parties[i] = "Republican";
                } else if(c == 'D') {
                    parties[i] = "Democrat";
                } else {
                    parties[i] = "Independent";
                }
                candidates[i] = candidates[i].substring(0, candidates[i].length() - 4);
            }
            mFeedBtn.setText(candidates[0]);
            mTextView.setText(parties[0]);
        }
        addListenerOnImage();
        getVoteData(this, county);
        mFeedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(getBaseContext(), WatchToPhoneService.class);
                if (!mFeedBtn.getText().toString().equalsIgnoreCase("BACK")) {
                    prevText = mFeedBtn.getText().toString();
                    prevParty = mTextView.getText().toString();
                    if (prevParty.equalsIgnoreCase("Republican")) {
                        sendIntent.putExtra("NAME", prevText + " (R)");
                    } else if (prevParty.equalsIgnoreCase("Democrat")) {
                        sendIntent.putExtra("NAME", prevText + " (D)");
                    } else {
                        sendIntent.putExtra("NAME", prevText + " (I)");
                    }
                    mFeedBtn.setText("BACK");
                    mTextView.setText(county);
                    obamaPercentText = "Obama: " + obamaPercent + "%";
                    romneyPercentText = "Romney: " + romneyPercent + "%";
                    presidentialView.setText("2012 " + state + " Vote");
                    obama.setText(obamaPercentText);
                    romney.setText(romneyPercentText);
                    obama.setVisibility(TextView.VISIBLE);
                    romney.setVisibility(TextView.VISIBLE);
                    presidentialView.setVisibility(TextView.VISIBLE);
                    //startService(sendIntent);
                } else {
                    mFeedBtn.setText(prevText);
                    mTextView.setText(prevParty);
                    obama.setVisibility(TextView.INVISIBLE);
                    romney.setVisibility(TextView.INVISIBLE);
                    presidentialView.setVisibility(TextView.INVISIBLE);
                }
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
				/*
				 * The following method, "handleShakeEvent(count):" is a stub //
				 * method you would use to setup whatever you want done once the
				 * device has been shook.
				 */
                handleShakeEvent(count);
            }
        });

    }

    public void handleShakeEvent(int count) {
        try {
            InputStream in = this.getResources().openRawResource(R.raw.election_results_2012_better);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder builder = new StringBuilder();

            for (String line = null; (line = reader.readLine()) != null; ) {
                builder.append(line).append("\n");
            }

            String resultStr = builder.toString();
            JSONTokener tokener = new JSONTokener(resultStr);
            JSONArray voteData = new JSONArray(tokener);
            Random r = new Random();
            int i = r.nextInt();
            if(i < 0) {
                i *= -1;
            }
            i = i%voteData.length();
            JSONObject countyData = (JSONObject) voteData.get(i);
            StringBuilder fullCountyName = new StringBuilder();
            StringBuilder stateName = new StringBuilder();
            fullCountyName.append(countyData.get("county-name"));
            mFeedBtn.setText("BACK");
            fullCountyName.append(" County");
            mTextView.setText(fullCountyName);
            stateName.append(countyData.get("state-postal"));
            presidentialView.setText("2012 " + stateName.toString() + " Vote");
            obamaPercent = countyData.get("obama-percentage").toString();
            obama.setText("Obama: " + obamaPercent);
            romneyPercent = countyData.get("romney-percentage").toString();
            romney.setText("Romney: " +romneyPercent);
            obama.setVisibility(TextView.VISIBLE);
            romney.setVisibility(TextView.VISIBLE);;
            presidentialView.setVisibility(TextView.VISIBLE);
            Log.d("obama", obamaPercent);
            Log.d("romney", romneyPercent);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void addListenerOnImage() {
        mTextView.setOnTouchListener(new OnSwipeListener(MainActivity.this) {
            @Override
            public void onSwipeTop() {
                Toast.makeText(MainActivity.this, "top", Toast.LENGTH_SHORT).show();
            }

            public void onSwipeRight() {
                Toast.makeText(MainActivity.this, "right", Toast.LENGTH_SHORT).show();
            }

            public void onSwipeLeft() {
                Toast.makeText(MainActivity.this, "left", Toast.LENGTH_SHORT).show();
                i += 1;
                mFeedBtn.setText(candidates[i % 3]);
                mTextView.setText(parties[i%3]);
            }

            public void onSwipeBottom() {
                Toast.makeText(MainActivity.this, "bottom", Toast.LENGTH_SHORT).show();
                i += 1;
                mFeedBtn.setText(candidates[i % 3]);
                mTextView.setText(parties[i%3]);
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    public void getVoteData(Context context, String county) {
        try {
            InputStream in = context.getResources().openRawResource(R.raw.election_results_2012_better);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder builder = new StringBuilder();

            for (String line = null; (line = reader.readLine()) != null;) {
                builder.append(line).append("\n");
            }

            String resultStr = builder.toString();
            JSONTokener tokener = new JSONTokener(resultStr);
            JSONArray voteData = new JSONArray(tokener);
            for (int i = 0; i < voteData.length(); i++) {
                JSONObject countyData = (JSONObject) voteData.get(i);
                StringBuilder fullCountyName = new StringBuilder();
                StringBuilder stateName = new StringBuilder();
                fullCountyName.append(countyData.get("county-name"));
                fullCountyName.append(" County");
                if (fullCountyName.toString().contains(county)) {
                    Log.d("voteTest", "yes");
                    stateName.append(countyData.get("state-postal"));
                    state = stateName.toString();
                    obamaPercent = countyData.get("obama-percentage").toString();
                    romneyPercent = countyData.get("romney-percentage").toString();
                    Log.d("obama", obamaPercent);
                    Log.d("romney", romneyPercent);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
