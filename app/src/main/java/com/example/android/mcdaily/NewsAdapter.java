package com.example.android.mcdaily;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class NewsAdapter extends ArrayAdapter<NewsModel> {
    private int bgColorResourceId;
    Context context;

    public NewsAdapter(Activity context, ArrayList<NewsModel> newsModel, int bgColorResourceId) {
        super(context, 0, newsModel);
        this.bgColorResourceId = bgColorResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.news_items, parent, false);
        }

        final NewsModel newsElement = getItem(position);

        TextView titleTextView = listItemView.findViewById(R.id.title);
        String title = newsElement.getTitle();
        if (title.length() > 107)
            title = title.substring(0, 107) + "...";
        titleTextView.setText(title);

        TextView authorTextView = listItemView.findViewById(R.id.author);
        authorTextView.setText(newsElement.getAuthor());

        TextView pubDateTextView = listItemView.findViewById(R.id.pubDate);
        pubDateTextView.setText(newsElement.getPubDate().substring(0, 10));

        TextView sectionTextView = listItemView.findViewById(R.id.section);
        sectionTextView.setText(newsElement.getSection());

        View textContianer = listItemView.findViewById(R.id.contianer);
        int color = ContextCompat.getColor(getContext(), bgColorResourceId);
        textContianer.setBackgroundColor(color);

        return listItemView;
    }
}
