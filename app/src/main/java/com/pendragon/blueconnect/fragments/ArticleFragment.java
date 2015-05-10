package com.pendragon.blueconnect.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pendragon.blueconnect.R;

public class ArticleFragment extends Fragment {
    public static final String ARG_ARTICLES_NUMBER = "articles_number";

    public ArticleFragment() {
        // Constructor vacío obligatorio
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // We call fragment_article layout
        View rootView = inflater.inflate(R.layout.fragment_article, container, false);
        int i = getArguments().getInt(ARG_ARTICLES_NUMBER);
        String article = getResources().getStringArray(R.array.menu_items)[i];

        getActivity().setTitle(article);
        TextView headline = (TextView)rootView.findViewById(R.id.headline);
        headline.append(" "+article);


        TextView body = (TextView)rootView.findViewById(R.id.body);

        if (i==1) {
            // It's Profile
            body.append("Name: " + " Bluetooth method");

        }
        else
        {
            // It's History
            body.append("Chats");

        }

        return rootView;
    }
}

