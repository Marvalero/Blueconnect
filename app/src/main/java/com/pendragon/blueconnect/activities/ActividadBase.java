package com.pendragon.blueconnect.activities;



import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.pendragon.blueconnect.logger.Log;
import com.pendragon.blueconnect.logger.LogWrapper;

public class ActividadBase extends FragmentActivity {

    public static final String TAG = "ActividadBase";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected  void onStart() {
        super.onStart();
        initializeLogging();
    }

    /** Establecemos el objetivo a recibir los logs */
    public void initializeLogging() {

        LogWrapper logWrapper = new LogWrapper();
        Log.setLogNode(logWrapper);

        Log.i(TAG, "Listo");
    }
}