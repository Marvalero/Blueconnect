package com.pendragon.blueconnect;

import android.app.Application;

import com.pendragon.blueconnect.utils.MySingleton;

/**
 * Created by libelula on 14/05/15.
 */
public class ApplicationControl extends Application
    {
        @Override
        public void onCreate()
        {
            super.onCreate();

            // Initialize the singletons so their instances
            // are bound to the application process.
            initSingletons();
        }

    protected void initSingletons()
    {
        // Initialize the instance of MySingleton
        MySingleton.initInstance();
    }

    public void customAppMethod()
    {
        // Custom application method
    }
}
