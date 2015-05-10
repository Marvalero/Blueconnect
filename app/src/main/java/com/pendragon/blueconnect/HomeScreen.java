package com.pendragon.blueconnect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.pendragon.blueconnect.fragments.BluetoothChat;


public class HomeScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // Wereate a new thread to sleep
        Thread timer = new Thread(){
            public void run(){
                try{
                    sleep(5000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    //Llamo a la nueva actividad
                    //startActivity recibe por parametro un objeto del tipo Intent
                    //El Intent recibibe por parametro el NAME de la actividad que vamos a invocar
                    //Es el mismo que colocamos en el manifiesto
                    Intent mainActivity;
                    mainActivity = new Intent("com.pendragon.blueconnect");
                    startActivity(mainActivity);
                    finish();
                }
            }
        };
        //ejecuto el thread
        timer.start();
    }

}
