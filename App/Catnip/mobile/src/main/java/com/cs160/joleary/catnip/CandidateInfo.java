package com.cs160.joleary.catnip;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Nitin on 3/4/2016.
 */
public class CandidateInfo extends Activity {
    ImageView candBtn;
    ListView committees;
    ListView bills;
    String name;
    String imageURL;
    String[] committeeElems;
    String[] billElems;
    TextView termEnd;
    TextView detailedName;
    boolean notNull = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.candidate_info);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        Log.d("EXTRAS", extras.toString());
        committeeElems = new String[4];
        billElems = new String[4];
        name = extras.getString("NAME").substring(0, extras.getString("NAME").length() - 4);
        imageURL = extras.getString("IMAGE");
        //Log.d("name", extras.getString("NAME"));
        termEnd = (TextView) findViewById(R.id.termEnd);
        detailedName = (TextView) findViewById(R.id.detailedName);
        detailedName.setText(extras.getString("NAME"));
        termEnd.setText("Term End: " + extras.getString("TERM_END"));
        populateCommittees(extras.getStringArray("COMMITTEES"));
        populateBills(extras.getStringArray("BILLS"));
        candBtn = (ImageView) findViewById(R.id.detailedImage);
        Picasso.with(this).load(imageURL).into(candBtn);
    }

    public void populateCommittees(String[] committeeElems) {
        committees = (ListView) findViewById(R.id.committees);
        ArrayList<String> fakeArray2 = new ArrayList<String>();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fakeArray2);
        committees.setAdapter(arrayAdapter);
        for(int i = 0; i < committeeElems.length; i++) {
            arrayAdapter.add(committeeElems[i]);
        }
    }

    public void populateBills(String[] billElems) {
        bills = (ListView) findViewById(R.id.bills);
        ArrayList<String> fakeArray2 = new ArrayList<String>();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fakeArray2);
        bills.setAdapter(arrayAdapter);

        for(int i = 0; i < billElems.length; i++) {
            arrayAdapter.add(billElems[i]);
        }
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
}
