package com.pendragon.blueconnect;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


//Guardamos los logs en:
import com.pendragon.blueconnect.logger.Log;
import  java.util.Set;
/**
 * Created by carlos on 25/03/2015.
 */
public class ActividadDispositivos extends Activity {

    /**Hacemos una etiqueta para el log:
     *
     */

    private static final String TAG = "ActividadListaDispositivos";

            //El valor de retorno del intent extra:
    public static String DIRECCIONES_DIPOSITIVOS_EXTRA = "Direccion_dispositivo";
/**
 * Creamos una variable para guardar un adaptador Bluetooth.
 *
 */
private BluetoothAdapter miAdaptadorBluetooth;

    private ArrayAdapter<String> AdaptadorDispositivosNuevos;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Ponemos una ventana para la lista de dispositivos(María pone en layout que le parezca)

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.actividad_dispositivos);
        //Si falla poner el resultado fallido
        setResult(Activity.RESULT_CANCELED);

       //Ponemos un botón para que inicie el scanner:
        Button BotonDeEscaneo=(Button)findViewById(R.id.boton_escaneo);
        BotonDeEscaneo.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        //Una vez en este punto hay que inicializar los nuevos adaptadores
        //así como los ya emparejados(antoriormente)
        ArrayAdapter<String> AdaptadorDispositivosEmparejados=
                new ArrayAdapter<>(this,R.layout.nombre_dispositivo);
        AdaptadorDispositivosNuevos =new ArrayAdapter<String>(this,R.layout.nombre_dispositivo);

        //Buscamos y establecemos la vista de los dispositivos emparejados:
        ListView ListViewEmparejados = (ListView) findViewById(R.id.nuevos_dispositivos);
        ListViewEmparejados.setAdapter(AdaptadorDispositivosEmparejados);
        ListViewEmparejados.setOnItemClickListener(miDispositivoClickListener);

        //Buscamos y establecemos el listview de los dispositivos nuevos:
        ListView ListViewNuevosDispositivos = (ListView) findViewById(R.id.nuevos_dispositivos);
        ListViewNuevosDispositivos.setAdapter(AdaptadorDispositivosNuevos);
        ListViewNuevosDispositivos.setOnItemClickListener(miDispositivoClickListener);

        //registro para anunciar cuando encontramos un dispositivo
        IntentFilter filtro =new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(miReceptor,filtro);

        //registro para anunciar cuando la búsqueda ha acabado:
        filtro=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(miReceptor,filtro);

        //Obtenemos el adaptador local Bluetooth(el nuestro)
        miAdaptadorBluetooth = BluetoothAdapter.getDefaultAdapter();

        //Obtenemos la configuración de los dispositivos emparejados
        Set<BluetoothDevice> dispositivosEmparejados = miAdaptadorBluetooth.getBondedDevices();

        //Si tenemos emparejados dispositivos,lo añadimos a la lista de dispositivos
        if(dispositivosEmparejados.size()> 0) {
            findViewById(R.id.dispositivos_emparejados).setVisibility(View.VISIBLE);
            for (BluetoothDevice dispositivo : dispositivosEmparejados) {
                AdaptadorDispositivosEmparejados.add(dispositivo.getName() + "\n" + dispositivo.getAddress());
            }
        }else{
            String noHayDispositivos = getResources().getText(R.string.ninguno_emparejado).toString();
            AdaptadorDispositivosEmparejados.add(noHayDispositivos);
            }
        }

        @Override
    protected void onDestroy(){
            super.onDestroy();
//Cancelamos la búsqueda,porque puede buggearse y consume mucho
            if(miAdaptadorBluetooth != null){
                miAdaptadorBluetooth.cancelDiscovery();
            }

            //nos des-subscribimos de la recepcion
            this.unregisterReceiver(miReceptor);
    }

    /**
     * Ahora empezamos la búsqueda de dispositivos con nuestro adaptador Bluetooth
     *
     */

    private void doDiscovery(){
        Log.d(TAG,"doDiscovery()");
        //Ponemos escaneando en el título:
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.escaneando);

        //Activamos los subtitulos para los nuevo dispositivos
        findViewById(R.id.titulo_nuevos_dispositivos).setVisibility(View.VISIBLE);

       //Dejamos de descubrir si seguimos haciéndolo(bugs y eso)
        if(miAdaptadorBluetooth.isDiscovering()){
            miAdaptadorBluetooth.cancelDiscovery();
        }

        //Hacemos una peticion para descubrir del Adaptador Bluetooth
        miAdaptadorBluetooth.startDiscovery();

    }

    /**
     * Ponemos los on-click listeners de todos los dispositivos en las listview
     *
     */


    private AdapterView.OnItemClickListener miDispositivoClickListener
            =new AdapterView.OnItemClickListener(){
        public void onItemClick(AdapterView<?> av,View v,int arg2,long arg3){
            //cancelamos el descubrimiento por consumo(además ya lo tiene)
            miAdaptadorBluetooth.cancelDiscovery();

            //Obtenemos la dirección MAC del dispositivo,al parecer son los últimos 17 chars en la vista...
            String informacion=((TextView)v).getText().toString();
            String direccion =informacion.substring(informacion.length() - 17);

            //Creamos el intent del resultado e incluimos la direccion MAC
            Intent intent =new Intent();
            intent.putExtra(DIRECCIONES_DIPOSITIVOS_EXTRA,direccion);

            //Configuramos el resultado y finalizams la actividad:

            setResult(Activity.RESULT_OK,intent);
            finish();


        }
    };

    /** El receptor de difusión que escucha los dispositivos descubiertos y cambia
     * el título cuando la búsqueda finaliza
      */

    private final BroadcastReceiver miReceptor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String accion =intent.getAction();
            //Cuando encntramos  un dispositivo
            if(BluetoothDevice.ACTION_FOUND.equals(accion)) {
                //Obtenemos el Dispositivo BLuetooth devuelto por el intent:
                BluetoothDevice dispositivo = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Si ya está emparejado,nos saltamos el paso,porque ya se realizó antes
                if (dispositivo.getBondState() != BluetoothDevice.BOND_BONDED) {
                    AdaptadorDispositivosNuevos.add(dispositivo.getName() + "\n" + dispositivo.getAddress());
                }
                //Cuando acabe la búsqueda, cambaimos el título de la actividad:
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(accion)){
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.selecciona_dispositivo);
                if(AdaptadorDispositivosNuevos.getCount() == 0){
                    String noHayDispositivos =getResources().getText(R.string.no_encontrados).toString();
                    AdaptadorDispositivosNuevos.add(noHayDispositivos);
                }
            }



        }
    };

}
