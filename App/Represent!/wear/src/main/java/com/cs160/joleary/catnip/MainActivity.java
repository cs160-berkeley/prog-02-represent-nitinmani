package com.cs160.joleary.catnip;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends Activity {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    public String[] candidates = { "John Cornyn", "Ted Cruz", "Ken Marchant"};
    int i = 0;
    private TextView mTextView;
    private Button mFeedBtn;
    private TextView obama;
    private TextView romney;
    private TextView presidentialView;
    boolean isZipCode = false;
    String prevText;
    String obamaPercentText;
    String romneyPercentText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFeedBtn = (Button) findViewById(R.id.feed_btn);
        mTextView = (TextView) findViewById(R.id.textView);
        obama = (TextView) findViewById(R.id.textView8);
        romney = (TextView) findViewById(R.id.textView9);
        presidentialView = (TextView) findViewById(R.id.textView12);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            String catName = extras.getString("CAT_NAME");
            if(catName.equalsIgnoreCase("Lexy")) {
                isZipCode = true;
            }
            if(catName.equalsIgnoreCase("Bob")) {
                mFeedBtn.setText("BACK");
                mTextView.setText("Collin County, TX");
                obama.setVisibility(TextView.VISIBLE);
                romney.setVisibility(TextView.VISIBLE);
                presidentialView.setVisibility(TextView.VISIBLE);
            } else {
                mFeedBtn.setText("John Cornyn");
                mTextView.setText("Republican");
            }
        }
        addListenerOnImage();
        mFeedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mFeedBtn.getText().toString().equalsIgnoreCase("BACK")){
                    prevText = mFeedBtn.getText().toString();
                    Random rand = new Random();
                    int value = rand.nextInt();
                    if(value < 0) {
                        value *= -1;
                    }
                    value %= 100;
                    int romneyValue = 100 - value;
                    mFeedBtn.setText("BACK");
                    if(isZipCode) {
                        mTextView.setText("Austin County, TX");
                    } else {
                        mTextView.setText("Collin County, TX");
                    }
                    obamaPercentText = "Obama: " + value + "%";
                    romneyPercentText = "Romney: " + romneyValue+"%";
                    obama.setText(obamaPercentText);
                    romney.setText(romneyPercentText);
                    obama.setVisibility(TextView.VISIBLE);
                    romney.setVisibility(TextView.VISIBLE);
                    presidentialView.setVisibility(TextView.VISIBLE);
                    Intent sendIntent = new Intent(getBaseContext(), WatchToPhoneService.class);
                    startService(sendIntent);
                } else {
                    mFeedBtn.setText(prevText);
                    mTextView.setText("Republican");
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
        mFeedBtn.setText("BACK");
        mTextView.setText("Tarrante County, TX");
        obamaPercentText = "Obama: 43%";
        romneyPercentText = "Romney: 57%";
        obama.setText(obamaPercentText);
        romney.setText(romneyPercentText);
        obama.setVisibility(TextView.VISIBLE);
        romney.setVisibility(TextView.VISIBLE);
        presidentialView.setVisibility(TextView.VISIBLE);
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
            }

            public void onSwipeBottom() {
                Toast.makeText(MainActivity.this, "bottom", Toast.LENGTH_SHORT).show();
                i += 1;
                mFeedBtn.setText(candidates[i % 3]);
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }
}
