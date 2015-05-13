package com.pendragon.blueconnect.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pendragon.blueconnect.R;
import com.pendragon.blueconnect.utils.MySingleton;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Date;

public class ArticleFragment extends Fragment {
    public static final String ARG_ARTICLES_NUMBER = "articles_number";
    public static final String ARG_ARTICLES_DATE = "articles_date";

    public ArticleFragment() {
        // Constructor vacío obligatorio
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // We call fragment_article layout
        View rootView = inflater.inflate(R.layout.fragment_article, container, false);
        int i = getArguments().getInt(ARG_ARTICLES_NUMBER);
        String dateConnection=getArguments().getString(ARG_ARTICLES_DATE);
        String article = getResources().getStringArray(R.array.menu_items)[i];

        getActivity().setTitle(article);
        TextView headline = (TextView)rootView.findViewById(R.id.headline);
        //headline.append(" "+article);

        TextView body = (TextView)rootView.findViewById(R.id.body);


        //Print the last connection date
            body.append("\n"+dateConnection.toString());
        //Print MAC of connected device
        TextView mac= (TextView)rootView.findViewById(R.id.mac);
        //TODO:Sacar la MAC del fichero
           mac.append("\n"+ MySingleton.getInstance().getString());
       /* try
        {
            BufferedReader fin =
                    new BufferedReader(
                            new InputStreamReader(new FileInputStream("nombreDevice.txt")));
            //Aqui se almacena el nombre del dispositivo:
            String texto = fin.readLine();
           mac.append("texto: "+texto);
            fin.close();
        }
        catch (Exception ex)
        {
            Log.e("Ficheros", "Error al leer fichero desde memoria interna");
        }
*/
        return rootView;
    }
}

