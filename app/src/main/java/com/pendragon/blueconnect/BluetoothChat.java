/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pendragon.blueconnect;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.OutputStreamWriter;

import com.pendragon.blueconnect.utils.BluetoothChatService;

import com.pendragon.blueconnect.utils.MySingleton;

import java.io.OutputStreamWriter;

/**
 * Main principal de los chat.
 */
public class BluetoothChat extends Activity {
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // tipos de mensajes del  Handler del chat
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // nombres recibidos del servicio de chat BT Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Codigos de los intent
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layouts
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    // nombre disp conectados
    private String mConnectedDeviceName = null;
    // Array de adaptador para las conversaciones
    private ArrayAdapter<String> mConversationArrayAdapter;
    // buffer para mensajes salientes
    private StringBuffer mOutStringBuffer;
    // adaptador BT propio
    private BluetoothAdapter mBluetoothAdapter = null;
    // Objeto del servicio de chat
    private BluetoothChatService mChatService = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Le ponemos un layout
        setContentView(R.layout.main);

        // obtenemos el adaptador propio
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Si el adaptador es null, no tenemos BT en el movil
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        Intent serverIntent = null;
        //Lanza DeviceListActivity para ver los dispositivos y escanear.
        serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);

    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // Pedimos activar BT si no está activo
        // Si ya estaba activo, lanzamos el chat
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

        } else {
            if (mChatService == null) setupChat();
        }

    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Este estado se mantendrá mientras el BT no esté activo.

        if (mChatService != null) {
            // Si el estado es STATE_NONE , de acuerdo a los códigos, sabremos que aun no esta BT activo
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Esto lanza el servicio BT
              mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Inicializa el array de adaptadores para la conversación.
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Inicializa el campo de texto editable.
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Hacemos lo mismo con el boton de envio.
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //Enviamos un mensaje usando el contenido del texto editable.
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });

        // Iniciamos el  BluetoothChatService
        mChatService = new BluetoothChatService(this, mHandler);

        // Iniciamos el buffer de mensajes salientes, por si quisieramos guardar mensajes o para guardarlos
        //en la cola.
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // para el servicio de BT
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }


    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);

    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle){
        super.onRestoreInstanceState(bundle);

    }



    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Manda un mensaje
     * El parametro message es un string con el que mandamos el mensaje.
     */
    private void sendMessage(String message) {
        // Primero asegurarnos que estamos conectados
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Comprueba que hay algo que enviar
        if (message.length() > 0) {
            // obtenemos los bytes y escribimos con el servicio de chat
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Limpiamos el campo de texto para volver a enviar
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    //Listener del campo de texto editable.
    private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // Si el código coincide con la actualizacion de una accion (ACTION UP)
            //Mandamos el mensaje
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            if(D) Log.i(TAG, "END onEditorAction");
            return true;
        }
    };

    private final void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(subTitle);
    }

    // Handler que devuelve la información del servicio de chat de BT
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                    setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                    mConversationArrayAdapter.clear();
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    setStatus(R.string.title_connecting);
                    break;
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                    setStatus(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // Este es el caso opuesto al de antes, construimos el string a partir de bytes
                String writeMessage = new String(writeBuf);
                mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // leemos el string del flujo de bytes.
                String readMessage = new String(readBuf, 0, msg.arg1);
                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // Aqui guardamos el nombre del dispositivo conectado para el historial
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);

                // save the device name
                MySingleton.getInstance().setString(mConnectedDeviceName);



                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_SECURE:
            // Devuelve un dipositivo al que conectarse( OK)
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, true);
            }
            break;
        case REQUEST_CONNECT_DEVICE_INSECURE:
            // Devuelve un dispositivo al que conectarse(pero de forma insegura)
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, false);
            }
            break;
        case REQUEST_ENABLE_BT:
            //Resultado de activar BT
            if (resultCode == Activity.RESULT_OK) {
                // Bt activo, iniciamos chat
                setupChat();
            } else {
                // No se ha activado BT, o ha dado error.
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Obtenemos la MAC del dispositivo al que nos conectamos
        String address = data.getExtras()
            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Obtenemos el objeto de  dispositivo BT.
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // lanzamos para conectarnos al servicio de forma segura.
        mChatService.connect(device, secure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.secure_connect_scan:
            //Lanza DeviceListActivity para ver los dispositivos y escanear.
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            return true;
        case R.id.insecure_connect_scan:
            // Lo mismo pero de forma insegura
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
            return true;
        case R.id.discoverable:
            // hace nuestro dispositivo visible durante 300segundos (por defecto)
            ensureDiscoverable();
            return true;
        }
        return false;
    }

    public String getmConnectedDeviceName(){
        return mConnectedDeviceName;
    }

}
