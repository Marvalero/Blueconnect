package com.pendragon.blueconnect;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ViewAnimator;
import android.support.v4.app.FragmentTransaction;

import com.pendragon.blueconnect.fragments.FragmentoChatBluetooth;
import com.pendragon.blueconnect.logger.Log;
import com.pendragon.blueconnect.logger.LogFragment;
import com.pendragon.blueconnect.logger.LogWrapper;
import com.pendragon.blueconnect.logger.MessageOnlyLogFilter;




public class MainActivity extends FragmentActivity {

public static final String TAG = "ActividadPrincipal";

    //Lo siguiente si el fragmento de los logs se mostrará o no:
    private boolean MuestraLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);
        if(savedInstanceState==null){
            FragmentTransaction transaccion = getSupportFragmentManager().beginTransaction();
            FragmentoChatBluetooth fragmento = new FragmentoChatBluetooth();
            transaccion.replace(R.id.contenido_fragmento,fragmento);
            transaccion.commit();
        }

    }

    @Override
    protected  void onStart() {
        super.onStart();
        initializeLogging();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        MenuItem desvaneceLog =menu.findItem(R.id.menu_desvanece_log);
        desvaneceLog.setVisible(findViewById(R.id.salida_ejemplo)instanceof ViewAnimator);
        desvaneceLog.setTitle(MuestraLog ? R.string.esconde_log : R.string.muestra_log);

        return super.onPrepareOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_desvanece_log:
                MuestraLog = !MuestraLog;
                ViewAnimator salida = (ViewAnimator) findViewById(R.id.salida_ejemplo);
                if (MuestraLog) {
                    salida.setDisplayedChild(1);
                } else {
                    salida.setDisplayedChild(0);
                }
                supportInvalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
 //Ahora creamos los objetivos para recibir los logs.


public void initializeLogging(){
//usamos el log nativo de android
    LogWrapper WrapperLog =new LogWrapper();
    Log.setLogNode(WrapperLog);

    //Filtramos  la salida excepto el mensaje de texto
    MessageOnlyLogFilter FiltroMensaje = new MessageOnlyLogFilter();
    WrapperLog.setNext(FiltroMensaje);

    //Plasmamos el log con un textView
    LogFragment FragmentoLog = (LogFragment) getSupportFragmentManager().findFragmentById(R.id.fragmento_log);
    FiltroMensaje.setNext(FragmentoLog.getLogView());

    Log.i(TAG,"Listo");
}
}


