package com.cs160.joleary.catnip;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.app.ListActivity;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Nitin on 3/4/2016.
 */
public class LocationCandidates extends Activity implements ThreadCompleteListener{
    ListView listView;

    String[] name;
    String bsSTring;
    String[] website;
    String[] committees;
    String[] committeeElems;
    ArrayList<String> billEls;
    ArrayList<String> committeeEls;
    String[] billElems;
    String[] bills;
    String zipcode;
    String[] email;
    String[] termEnds;
    String[] urls;
    String[] twitter_ids;
    String[] tweets;
    String[] twitterUrls;
    String tweet; String twitterImageURL;

    Integer[] image = {R.drawable.lexy_160, R.drawable.lexy_160, R.drawable.lexy_160, R.drawable.lexy_160};
    String[] concatenated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        name = extras.getStringArray("NAMES");
        website= extras.getStringArray("WEBSITES");
        email = extras.getStringArray("EMAILS");
        termEnds = extras.getStringArray("TERM_ENDS");
        committees = extras.getStringArray("COMMITTEES");
        bills = extras.getStringArray("BILLS");
        zipcode = extras.getString("ZIPCODE");
        twitter_ids = extras.getStringArray("TWITTER_IDS");
        tweets = new String[twitter_ids.length];
        twitterUrls = new String[twitter_ids.length];
        NotifyingThread t = new NotifyingThread() {
            @Override
            public void doRun() {
                for(int i = 0; i < twitter_ids.length; i++) {
                    tweets[i] = getTweet(twitter_ids[i]);
                    twitterUrls[i] = getImage(twitter_ids[i]);
                }
            }
        };
        t.addListener(this);
        t.start();
        //Log.d("TWEET SIZE", twitter_ids[0]);
        //Log.d("URL SIZE", Integer.toString(urls.length));
        concatenated = concatenateEverything(name, website, email, tweets, twitterUrls);
        setContentView(R.layout.candidates);
        CustomList adapter = new CustomList(LocationCandidates.this, concatenated, name, tweets, website, email, twitterUrls);
        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d("Clicked", "a");
                Object item = listView.getItemAtPosition(position);
                final String itemString = String.valueOf(item);
                Ion.with(LocationCandidates.this)
                        .load("http://congress.api.sunlightfoundation.com/legislators/locate?zip=" + zipcode+ "&apikey=41e1deffd8124c65911ed01d6aaadc2f")
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            String bioGuideId;
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                String[] splitted = itemString.split("\n");
                                String email = splitted[2];
                                final Intent activityIntent = new Intent(LocationCandidates.this, CandidateInfo.class);
                                JsonArray representatives = result.getAsJsonArray("results");
                                JsonObject rep;
                                for (JsonElement json_rep : representatives) {
                                    String party;
                                    rep = json_rep.getAsJsonObject();
                                    if(rep.get("oc_email").getAsString().equalsIgnoreCase(email)) {
                                        activityIntent.putExtra("REP_ID", rep.get("bioguide_id").getAsString());
                                        bioGuideId = rep.get("bioguide_id").getAsString();
                                        activityIntent.putExtra("TERM_END", rep.get("term_end").getAsString());
                                        for(int i = 0; i < twitter_ids.length; i++) {
                                            Log.d("LocationTWitter", twitter_ids[i]);
                                            Log.d("Actual Twitter", rep.get("twitter_id").getAsString());
                                            if(twitter_ids[i].equalsIgnoreCase(rep.get("twitter_id").getAsString())) {
                                                activityIntent.putExtra("IMAGE", twitterUrls[i]);
                                                break;
                                            }
                                        }
                                    }
                                }
                                activityIntent.putExtra("NAME", splitted[0]);
                                Ion.with(LocationCandidates.this)
                                        .load("http://congress.api.sunlightfoundation.com/committees?member_ids=" + bioGuideId + "&apikey=41e1deffd8124c65911ed01d6aaadc2f")
                                        .asJsonObject()
                                        .setCallback(new FutureCallback<JsonObject>() {
                                            @Override
                                            public void onCompleted(Exception e, JsonObject result) {
                                                committeeEls = new ArrayList<String>();
                                                JsonArray representatives = result.getAsJsonArray("results");
                                                JsonObject rep;
                                                int i = 0;
                                                for (JsonElement json_rep : representatives) {
                                                    if (i >= 4) {
                                                        break;
                                                    }
                                                    rep = json_rep.getAsJsonObject();
                                                    if(rep.get("name").getAsString() != null) {
                                                        committeeEls.add(rep.get("name").getAsString());
                                                    }
                                                    i++;
                                                }
                                                committeeElems = new String[committeeEls.size()];
                                                for(int n = 0; n < committeeElems.length; n++) {
                                                    committeeElems[n] = committeeEls.get(n);
                                                }
                                                activityIntent.putExtra("COMMITTEES", committeeElems);
                                                Log.d("BIOGUIDE", bioGuideId);
                                                Ion.with(LocationCandidates.this)
                                                        .load("http://congress.api.sunlightfoundation.com/bills?sponsor_id=" + bioGuideId + "&apikey=41e1deffd8124c65911ed01d6aaadc2f")
                                                        .asJsonObject()
                                                        .setCallback(new FutureCallback<JsonObject>() {
                                                            @Override
                                                            public void onCompleted(Exception e, JsonObject result) {
                                                                JsonArray representatives = result.getAsJsonArray("results");
                                                                JsonObject rep;
                                                                billEls = new ArrayList<String>();
                                                                int i = 0;
                                                                for (JsonElement json_rep : representatives) {
                                                                    if (i >= 4) {
                                                                        break;
                                                                    }
                                                                    rep = json_rep.getAsJsonObject();
                                                                    Log.d("REP", rep.toString());
                                                                    if((rep != null) && (rep.get("short_title") != null) &&(!rep.get("short_title").toString().equals(null)) && (!rep.get("short_title").toString().equals("null"))) {
                                                                        Log.d("BLAH", rep.get("short_title").toString());
                                                                        billEls.add(rep.get("short_title").toString());
                                                                    }
                                                                    i++;
                                                                }
                                                                billElems = new String[billEls.size()];
                                                                for(int y = 0; y < billElems.length; y++) {
                                                                    billElems[y] = billEls.get(y);
                                                                }
                                                                activityIntent.putExtra("BILLS", billElems);
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

    private static String[] concatenateEverything(String[] name, String[] website, String[] email, String[] tweet, String[] image) {
        String[] concatenatedInfo = new String[name.length];
        for(int i = 0; i < name.length; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(name[i]);
            sb.append("\n");
            sb.append(website[i]);
            sb.append("\n");
            sb.append(email[i]);
            sb.append("\n");
            sb.append(image[i]);
            concatenatedInfo[i] = sb.toString();
        }
        return concatenatedInfo;
    }

    public String getTweet(String candidateID) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey("sb52UFykkwSwbQihDEVDPCdPV");
        cb.setOAuthConsumerSecret("Me35mCXKfS67VFK9kUFL9aJNBn5xbDSL7M5GrZxx0it0PqDom7");
        cb.setOAuthAccessToken("708221428512862208-NHmx62uqUkvNmkzAvLQuP0IE5aFaaTA");
        cb.setOAuthAccessTokenSecret("1RhgBQLcw2evfQfW0WiohaueFwEOxuRnY4uLdLFPb5FQt");
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        try {
            User user = twitter.showUser(candidateID);
            Status status = user.getStatus();
            tweet = status.getText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tweet;
    }

    public String getImage(String candidateID) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey("sb52UFykkwSwbQihDEVDPCdPV");
        cb.setOAuthConsumerSecret("Me35mCXKfS67VFK9kUFL9aJNBn5xbDSL7M5GrZxx0it0PqDom7");
        cb.setOAuthAccessToken("708221428512862208-NHmx62uqUkvNmkzAvLQuP0IE5aFaaTA");
        cb.setOAuthAccessTokenSecret("1RhgBQLcw2evfQfW0WiohaueFwEOxuRnY4uLdLFPb5FQt");
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        try {
            User user = twitter.showUser(candidateID);
            Status status = user.getStatus();
            tweet = status.getText();
            twitterImageURL = user.getOriginalProfileImageURL();
            Log.d("URL", twitterImageURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return twitterImageURL;
    }

    public void notifyOfThreadComplete(Thread t) {
        bsSTring = "nitin";
    }
}
