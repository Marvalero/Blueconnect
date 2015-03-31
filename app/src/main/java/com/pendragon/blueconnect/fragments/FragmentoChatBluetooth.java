package com.pendragon.blueconnect.fragments;

/**
 * Created by carlos on 28/03/2015.
 */
        import android.app.ActionBar;
        import android.app.Activity;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.content.Intent;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.Message;
        import android.support.annotation.Nullable;
        import android.support.v4.app.Fragment;
        import android.support.v4.app.FragmentActivity;
        import android.view.KeyEvent;
        import android.view.LayoutInflater;
        import android.view.Menu;
        import android.view.MenuInflater;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.view.inputmethod.EditorInfo;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.pendragon.blueconnect.ActividadDispositivos;
        import com.pendragon.blueconnect.bluetoothcontroller.Constants;
        import com.pendragon.blueconnect.R;
        import com.pendragon.blueconnect.bluetoothcontroller.ChatBluetoothService;
        import com.pendragon.blueconnect.logger.Log;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class FragmentoChatBluetooth extends Fragment {

    private static final String TAG = "FragmentoChatBluetooth";

    // Códigos de solicitud de intents
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout de las vistas
    private ListView VistaConversacion;
    private EditText TextoSalidaEditable;
    private Button BotonEnvio;

    /**
     * Nombre de los dispositivos conectados:
     */
    private String DispositivosConectados = null;

    /**
     * Array de las conversaciones
     */
    private ArrayAdapter<String> ArrayConversacion;

    /**
     * Buffer para los mensajes salientes
     */
    private StringBuffer BufferSalida;

    /**
     * Adaptador Bluetooth local
     */
    private BluetoothAdapter AdaptadorBluetooth = null;

    /**
     * Objetos miembros del servicio de chat:
     */
    private ChatBluetoothService ServicioChat = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Obtenemos el adaptador de BLuetooth local:
        AdaptadorBluetooth = BluetoothAdapter.getDefaultAdapter();

        // Si el adaptador es null, no se soporta bluetooth
        if (AdaptadorBluetooth == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "No se soporta Bluetooth", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // Si bluetooth no está conectado, se pide que se conecte.
        // setupChat() Se llamará mientras la actividad onActivityResult
        if (!AdaptadorBluetooth.isEnabled()) {
            Intent activaIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(activaIntent, REQUEST_ENABLE_BT);
            // Si funciona, configura el chat
        } else if (ServicioChat == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ServicioChat != null) {
            ServicioChat.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Si lanzamos esta comprobación en onResume() nos cubrimos de que BT no estuviera
        //activo en la fase onStart()
        //Se cancelará cuando nos devuelva el control ACTION_REQUEST_ENABLE.
        if (ServicioChat != null) {
            // Solo si el estado es STATE_NONE,sabremos que aun no hemos inciado el servicio
            if (ServicioChat.obtieneEstado() == ChatBluetoothService.STATE_NONE) {
                //Comienza el servicio de chat de bluetooth
                ServicioChat.start();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmento_chat_bluetooth, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        VistaConversacion = (ListView) view.findViewById(R.id.entrada);
        TextoSalidaEditable = (EditText) view.findViewById(R.id.texto_salida_editable);
        BotonEnvio = (Button) view.findViewById(R.id.boton_envio);
    }

    /**
     * Configura el chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Inicia el array de adaptadores
        ArrayConversacion = new ArrayAdapter<>(getActivity(), R.layout.mensaje);

        VistaConversacion.setAdapter(ArrayConversacion);
       TextoSalidaEditable.setOnEditorActionListener(ListenerEscritura);

        //Inicializamos el boton de envio con un listener para eventos de click.
        BotonEnvio.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Envia un mensaje usando contenido del widget de texto editable.
                View view = getView();
                if (null != view) {
                    TextView textView = (TextView) view.findViewById(R.id.texto_salida_editable);
                    String mensaje = textView.getText().toString();
                    enviaMensaje(mensaje);
                }
            }
        });

        // Inicializa el servicio de chat BT
        ServicioChat = new ChatBluetoothService(getActivity(), mHandler);

        // Inicializa el buffer para mensajes salientes.
        BufferSalida = new StringBuffer("");
    }

    /**
     * Hace el dispositivo visible.
     */
    private void haceVisible() {
        if (AdaptadorBluetooth.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent visibleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            visibleIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(visibleIntent);
        }
    }

    /**
     * Envia un mensaje.
     * Recibe el mensaje a enviar
     */
    private void enviaMensaje(String mensaje) {
        // Check that we're actually connected before trying anything
        if (ServicioChat.obtieneEstado() != ChatBluetoothService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.no_conectado, Toast.LENGTH_SHORT).show();
            return;
        }

        // Comprueba que el mensaje no está vacío
        if (mensaje.length() > 0) {
            // Obtiene los bytes a mandar y los manda.
            byte[] send = mensaje.getBytes();
            ServicioChat.write(send);

            // Reseteamos el buffer de salida y el texto editable
            BufferSalida.setLength(0);
           TextoSalidaEditable.setText(BufferSalida);
        }
    }

    /**
     * Listener del campo de texto editable
     */
    private TextView.OnEditorActionListener ListenerEscritura
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // Si se cumple que es un key event , se envia el mensaje
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String mensaje = view.getText().toString();
               enviaMensaje(mensaje);
            }
            return true;
        }
    };

    /**
     * Actualiza el estado.
     *
     *  recibe un ID como parámetro
     */
    private void estableceEstado(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Igual que el método anterior pero cambia el argumento que recibe.
     *
     */
    private void estableceEstado(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     *  Handler que obtiene info del chat BT
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatBluetoothService.STATE_CONNECTED:
                            estableceEstado(getString(R.string.titulo_conectado_a,DispositivosConectados));
                            ArrayConversacion.clear();
                            break;
                        case ChatBluetoothService.STATE_CONNECTING:
                            estableceEstado(R.string.titulo_conectando);
                            break;
                        case ChatBluetoothService.STATE_LISTEN:
                        case ChatBluetoothService.STATE_NONE:
                            estableceEstado(R.string.titulo_no_conectado);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construye una string desde el buffer
                    String escribeMensaje = new String(writeBuf);
                    ArrayConversacion.add("Me:  " + escribeMensaje);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construye una string a partir de lo válido del buffer
                    String leeMensaje = new String(readBuf, 0, msg.arg1);
                    ArrayConversacion.add(DispositivosConectados + ":  " + leeMensaje);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // guarda el nombre del dispositivo conectado
                    DispositivosConectados = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Conectado a "
                                + DispositivosConectados, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // Cuando La actividadDispositivos devuelve un dispositivo al que conectarse.
                if (resultCode == Activity.RESULT_OK) {
                    conectaDispositivos(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // Cuando La actividadDispositivos devuelve un dispositivo al que conectarse.
                if (resultCode == Activity.RESULT_OK) {
                    conectaDispositivos(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // respuesta para activar BT
                if (resultCode == Activity.RESULT_OK) {
                    // BT conectado, inciamos sesion de chat
                    setupChat();
                } else {
                    // BT no activado o error.
                    Log.d(TAG, "Bluetooth no activado");
                    Toast.makeText(getActivity(), R.string.bt_no_activado,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    /**
     * Para conectarnos a otros dispositivos.
     *
     * Recibe un link con los dispositivos extra.
     * Recibe el tipo de socket, si es seguro o no
     */
    private void conectaDispositivos(Intent dispositivos, boolean seguridad) {
        //Obtenemos la dirección MAC
        String direccion = dispositivos.getExtras().getString(ActividadDispositivos.DIRECCIONES_DIPOSITIVOS_EXTRA);
        // Obtenemos el objeto BT
        BluetoothDevice dispositivo = AdaptadorBluetooth.getRemoteDevice(direccion);
        // Lanzamos para conectarnos a un dispositivo
        ServicioChat.conecta(dispositivo, seguridad);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chat_bluetooth, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.escaneo_conexion_segura: {
                // Lanzamos ActividadDispositivos para ver los dispositivos y escanear
                Intent serverIntent = new Intent(getActivity(), ActividadDispositivos.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.escaneo_conexion_insegura: {
                // Lanzamos ActividadDispositivos para ver los dispositivos y escanear
                Intent serverIntent = new Intent(getActivity(), ActividadDispositivos.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.visible: {
                // Ensure this device is discoverable by others
                haceVisible();
                return true;
            }
        }
        return false;
    }

}