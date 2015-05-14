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

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Esta actividad lista los dispositivos emparejados y detectados, cuando
 * seleccionamos un dipositivo se nos devuelve su MAC, al resolverse el intent
 * el nombre del dispositivo se mostrará en el fragmento de informacion.
 *
 */
public class DeviceListActivity extends Activity {

    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;

    // Intent de dispositivos extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Adaptador local,emparejados y nuevos.
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Establece una ventana nueva con los dispositivos
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);

        //Se cancela si el usuario vuelve o sale
        setResult(Activity.RESULT_CANCELED);

        // Prepara el boton que lanza el escaneo.
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        // Inicializamos los array de adaptadores, tanto emparejados como
        // los recien descubiertos.
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Mete en el nuevo listView los dispositivos emparejados.
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Lo mismo pero con los dispositivos nuevos.
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // anuncio de un nuevo dispositivo encontrado
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // aviso de finalizacion del escaneo o busqueda de dispositivos
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Obtenemos el adaptador BT local.
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // prepara los dispositivos ya emparejados.
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // Si hay dispositivos emparejados los añadimos al array de dispositivos emparejados.
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cancelamos la búsqueda de nuevos dispositivos
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // nos des-suscribimos de recibir nuevos dispositivos
        this.unregisterReceiver(mReceiver);
    }

    /*
     //////////////////     BEFORE       /////////////////
    @Override
    public void onBackPressed() {
        Intent myIntent = new Intent(getBaseContext(), MainActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(myIntent);
    }
    */


    /**
     * Esto realiza el descubrimiento con el adaptador BT.
     */
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");

        // Ponemos que estamos escaneando.
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        // Ponemos sub titulos para nuevos dispositivos(en el layout)
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // Si seguimos buscando, cancelamos
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Solicitamos una búsqueda al adaptador BT local.
        mBtAdapter.startDiscovery();
    }

    // Ponemos los listener para los dispositivos del listview.
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // dejamos de buscar, porque ya estamos conectandonos a un dispositivo
            mBtAdapter.cancelDiscovery();
            //NOTA: Esto es imporante porque es donde mas bateria gasta BT.

            // Obtenemos la MAC, que son los 17 ultimos caracteres de la View.
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Hacemos un nuevo intent incluyendo ahora la MAC.
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Ponemos le resultado a OK y finalizamos la actividad.
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    // El receptor que escucha los dispositivos que se descubren y se encarga de
    // cambiar el titulo cuando se acaba la búsqueda.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // Si encontramos un dispositivo
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Obtenemos el objeto BT del intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Si ya estaba emparejado, saltamos este paso.
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            //Cambiamos el titulo cuando se acaba la búsqueda.
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

}
