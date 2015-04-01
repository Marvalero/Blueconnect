package com.pendragon.blueconnect.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pendragon.blueconnect.R;

public class MainFragment extends Fragment {
    public static final String ARG_ARTICLES_NUMBER = "articles_number";

    public MainFragment() {
        // Constructor vacío obligatorio
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // We call fragment_article layout
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        int i = getArguments().getInt(ARG_ARTICLES_NUMBER);

        return rootView;
    }

}
