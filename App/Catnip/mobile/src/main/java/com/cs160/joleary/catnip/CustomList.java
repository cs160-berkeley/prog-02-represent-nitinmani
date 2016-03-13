package com.cs160.joleary.catnip;

import android.app.Activity;
import android.graphics.Paint;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class CustomList extends ArrayAdapter<String>{

    private final Activity context;
    private final String[] allConcat;
    private final String[] candidateNames;
    private final String[] candidateTweets;
    private final String[] website;
    private final String[] email;
    private final String[] candidateImages;
    public CustomList(Activity context, String[] allConcat, String[] candidateNames, String[] candidateTweets, String[] website, String[] email, String[] candidateImages) {
        super(context, R.layout.mylist, allConcat);
        this.allConcat = allConcat;
        this.context = context;
        this.candidateNames = candidateNames;
        this.candidateTweets = candidateTweets;
        this.website = website;
        this.candidateImages = candidateImages;
        this.email = email;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.mylist, null, true);

        TextView candidateName = (TextView) rowView.findViewById(R.id.candName);
        candidateName.setText(candidateNames[position]);
        TextView candidateEmail = (TextView) rowView.findViewById(R.id.candEmail);
        candidateEmail.setPaintFlags(candidateEmail.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        candidateEmail.setText(email[position]);
        //candidateEmail.setLinksClickable(true);
        //Linkify.addLinks(candidateEmail, Linkify.ALL);
        TextView candidateWebsite = (TextView) rowView.findViewById(R.id.candWebsite);
        candidateWebsite.setPaintFlags(candidateWebsite.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        candidateWebsite.setText(website[position]);
        //candidateWebsite.setLinksClickable(true);
        //Linkify.addLinks(candidateWebsite, Linkify.ALL);
        TextView candidateTweet = (TextView) rowView.findViewById(R.id.candTwitter);
        candidateTweet.setText(candidateTweets[position]);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.candImage);
        Picasso.with(context).load(candidateImages[position]).into(imageView);
        return rowView;
    }
}