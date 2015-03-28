package com.pendragon.blueconnect;

/**
 * Created by carlos on 28/03/2015.
 */


        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.bluetooth.BluetoothServerSocket;
        import android.bluetooth.BluetoothSocket;
        import android.content.Context;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.Message;
        import com.pendragon.blueconnect.logger.Log;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStream;
        import java.util.UUID;

/**
 *Esta clase tiene toda la pesca, hacetodo el trabajo de conexion y manejo con otros dispositivos
 */
public class ServicioChatBluetooth {

    private static final String TAG = "ServicioChatBluetooth";

    // Nombre para los sockets de conexion
    private static final String NAME_SECURE = "ChatBluetoothSeguro";
    private static final String NAME_INSECURE = "ChatBluetoothInseguro";

    // UUID unico para esta aplicacion (viene en internet, no tengo claro para que se usa)
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    // Member fields
    private final BluetoothAdapter miAdaptador;
    private final Handler miHandler;
    private AceptaHilo miHiloSeguro;
    private AceptaHilo miHiloInseguro;
    private HiloConexion miHiloConexion;
    private HiloConectado miHiloConectado;
    private int miEstado;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // No  estamos haciendo nada
    public static final int STATE_LISTEN = 1;     // Escuchamos conexiones entrantes
    public static final int STATE_CONNECTING = 2; // Iniciamos conexiones salientes
    public static final int STATE_CONNECTED = 3;  // Conectados a dispositivos remotos

    /**
     * Constructor del servicio de chat.
     *
     */
    public ServicioChatBluetooth(Context context, Handler handler) {
        miAdaptador = BluetoothAdapter.getDefaultAdapter();
        miEstado = STATE_NONE;
        miHandler = handler;
    }

    /**
     * Establece un estado en la sesión de chat
     *
     */
    private synchronized void estableceEstado(int estado) {
        Log.d(TAG, "estableceEstado() " + miEstado + " -> " + estado);
        miEstado = estado;

        // Le da el nuevo estado al handler
        miHandler.obtainMessage(Constantes.MESSAGE_STATE_CHANGE, estado, -1).sendToTarget();
    }

    /**
     * Devuelve el estado actual de la conexion.
     */
    public synchronized int obtieneEstado() {
        return miEstado;
    }

    /**
     * Empieza el servicio de chat.
     * Arranca el hilo de aceptacion de conexion es que se llama por la parte onResume.
     */
    public synchronized void start() {
        Log.d(TAG, "Comienzo");

        // Cancela cualquier hilo que trate de comenzar una conexion
        if (miHiloConexion != null) {
            miHiloConexion.cancel();
            miHiloConexion = null;
        }

        // Cancela cualquier hilo que esté con una conexion ahora mismo
        if (miHiloConectado != null) {
            miHiloConectado.cancela();
            miHiloConectado = null;
        }

        estableceEstado(STATE_LISTEN);

        // Inicia el hilo para escuchar en un socket
        if (miHiloSeguro == null) {
            miHiloSeguro = new AceptaHilo(true);
            miHiloSeguro.start();
        }
        if (miHiloInseguro == null) {
            miHiloInseguro = new AceptaHilo(false);
            miHiloInseguro.start();
        }
    }

    /**
     * Inicia el hilo de conexion para iniciar una conexion con un diposistivo remoto.
     *
     * recibe el dispositivo BT a conectar
     * recibe el tipo de socket, seguro o inseguro
     */
    public synchronized void conecta(BluetoothDevice dispositivo, boolean seguridad) {
        Log.d(TAG, "Conéctate a: " + dispositivo);

        // Cancela cualquier hilo tratando de iniciar una conexion
        if (miEstado == STATE_CONNECTING) {
            if (miHiloConexion != null) {
                miHiloConexion.cancel();
                miHiloConexion = null;
            }
        }

        // Cancela cualquier hilo actualmente conectado
        if (miHiloConectado != null) {
            miHiloConectado.cancela();
            miHiloConectado = null;
        }

        // Inicia el hilo para conectarte con el dispositivo dado.
        miHiloConexion = new HiloConexion(dispositivo, seguridad);
        miHiloConexion.start();
        estableceEstado(STATE_CONNECTING);
    }

    /**
     * El hilo conectado gestiona una conexion BT.
     *
     * recibe le dipispositivo BT, el socket y su tipo
     */
    public synchronized void conectado(BluetoothSocket socket, BluetoothDevice
            dispositivo, final String socketType) {
        Log.d(TAG, "Conectado, Tipo de Socket:" + socketType);

        // Cancela el hilo que complete la conexion.
        if (miHiloConexion != null) {
            miHiloConexion.cancel();
            miHiloConexion = null;
        }

        // Cancela cualquier hilo con una conexion establecida.
        if (miHiloConectado != null) {
            miHiloConectado.cancela();
            miHiloConectado = null;
        }

        // Deja de aceptar hilos de conexion porque solo queremos conectarnos a un dispositivo.
        if (miHiloSeguro != null) {
            miHiloSeguro.cancela();
            miHiloSeguro = null;
        }
        if (miHiloInseguro != null) {
            miHiloInseguro.cancela();
            miHiloInseguro = null;
        }

        // Inicia el hilo que maneja las conexiones y su rendimiento.
        miHiloConectado = new HiloConectado(socket, socketType);
        miHiloConectado.start();

        // Manda el nombre del dipositivo conectado a la actividad UI.
        Message msg = miHandler.obtainMessage(Constantes.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constantes.DEVICE_NAME, dispositivo.getName());
        msg.setData(bundle);
        miHandler.sendMessage(msg);

        estableceEstado(STATE_CONNECTED);
    }

    /**
     * Detiene todos los hilos.
     */
    public synchronized void stop() {
        Log.d(TAG, "PARA");

        if (miHiloConexion != null) {
            miHiloConexion.cancel();
            miHiloConexion = null;
        }

        if (miHiloConectado != null) {
            miHiloConectado.cancela();
            miHiloConectado = null;
        }

        if (miHiloSeguro != null) {
            miHiloSeguro.cancela();
            miHiloSeguro = null;
        }

        if (miHiloInseguro != null) {
            miHiloInseguro.cancela();
            miHiloInseguro = null;
        }
        estableceEstado(STATE_NONE);
    }

    /**
     *  Escribe al  Hilo conectado de forma asincrona.
     *
     * recibe los bytes a escribir.
     */
    public void write(byte[] out) {
        // Creamos un objeto temporal
        HiloConectado r;
        //  Sincroniza una copia del hilo conectado.
        synchronized (this) {
            if (miEstado != STATE_CONNECTED) return;
            r = miHiloConectado;
        }
        // Pone la escritura asincrona.
        r.write(out);
    }

    /**
     * Indica que el inteot de conexion falló.
     */
    private void conexionFallida() {
        // Manda un mensaje de fallo a la actividad
        Message msg = miHandler.obtainMessage(Constantes.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constantes.TOAST, "No ha sido posible conectar con el dispositivo");
        msg.setData(bundle);
        miHandler.sendMessage(msg);

        // Inicia el servicio reseteando el modo de escucha.
       ServicioChatBluetooth.this.start();
    }

    /**
     * Indica y notifica que la conexion se ha perdido.
     */
    private void conexionPerdida() {
        // Manda un mensaje de fallo a la actividad
        Message msg = miHandler.obtainMessage(Constantes.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constantes.TOAST, "Se ha perdido la conexion con el dispositivo");
        msg.setData(bundle);
        miHandler.sendMessage(msg);

        // Inicia el servicio reseteando el modo de escucha.
        ServicioChatBluetooth.this.start();
    }

    /**
     * Este hilo corre mientras se escucha para conexiones entrantes.
     * Se comporta como el lado del servidor.
     * Funciona hasta que se acepta una conexion o se cancela.
     */
    private class AceptaHilo extends Thread {
        // El socket del servidor local
        private final BluetoothServerSocket miSocketServidor;
        private String miTipoSocket;

        public AceptaHilo(boolean seguridad) {
            BluetoothServerSocket tmp = null;
            miTipoSocket = seguridad ? "Seguro" : "Inseguro";

            // Crea un nuevo socket de escucha.(del servidor)
            try {
                if (seguridad) {
                    tmp = miAdaptador.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                            MY_UUID_SECURE);
                } else {
                    tmp = miAdaptador.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Tipo de socket: " + miTipoSocket + "escucha() fallida", e);
            }
            miSocketServidor = tmp;
        }

        public void run() {
            Log.d(TAG, "Socket de tipo: " + miTipoSocket +
                    "BEGIN AceptaHilo" + this);
            setName("AceptaHilo" + miTipoSocket);

            BluetoothSocket socket = null;

            // Escucha al servidor socker si no estamos conectados.
            while (miEstado != STATE_CONNECTED) {
                try {
                    // Esto es una llamada bloqueante y solo devuelve el control con éxito o excepcion.
                    socket = miSocketServidor.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket de tipo: " + miTipoSocket + "acepta() fallido", e);
                    break;
                }

                // Si se aceptó una conexion
                if (socket != null) {
                    synchronized (ServicioChatBluetooth.this) {
                        switch (miEstado) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situacion normal.Inicia el hilo conectado.
                                conectado(socket, socket.getRemoteDevice(),
                                        miTipoSocket);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Ya no este listo o estemos ya conectados,finalizamos el nuevo socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "No se pudo cerrar el socket no deseado", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END miHiloAceptado, socket de tipo: " + miTipoSocket);

        }

        public void cancela() {
            Log.d(TAG, "Tipo de Socket" + miTipoSocket + "cancela " + this);
            try {
                miSocketServidor.close();
            } catch (IOException e) {
                Log.e(TAG, "Tipo de Socket" + miTipoSocket + "close() del servidor fallido", e);
            }
        }
    }


    /**
     * Este hilo se lanza cuando se intenta hacer una conexion saliente
     * con un dispositivo.Se lanza ya falle o tenga exito la conexion.
     */
    private class HiloConexion extends Thread {
        private final BluetoothSocket miSocket;
        private final BluetoothDevice miDispositivo;
        private String miTipoSocket;

        public HiloConexion(BluetoothDevice dispositivo, boolean seguridad) {
            miDispositivo = dispositivo;
            BluetoothSocket tmp = null;
            miTipoSocket = seguridad ? "Seguro" : "Inseguro";

            // Obtiene un socket BT para el dispositivo BT dado.

            try {
                if (seguridad) {
                    tmp = dispositivo.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = dispositivo.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Tipo de Socket: " + miTipoSocket + "create() fallido", e);
            }
            miSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN miHiloConexion  TipoSocket:" + miTipoSocket);
            setName("HiloConexion" + miTipoSocket);

            // Cancelamos la búsqueda para que no relentice la conexion
            miAdaptador.cancelDiscovery();

            // Hace una conexion al socket BT
            try {
                // Esto es una llamada bloqueante que solo devolvera el control en caso de exito
                // o excepcion.
                miSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    miSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "incapaz de close() " + miTipoSocket +
                            " fallo durante la conexion del socket ", e2);
                }
                conexionFallida();
                return;
            }

            // Resetea el HiloConexion porque ya hemos acabado.
            synchronized (ServicioChatBluetooth.this) {
                miHiloConexion = null;
            }

            // Inicia el hilo conectado
            conectado(miSocket, miDispositivo, miTipoSocket);
        }

        public void cancel() {
            try {
                miSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() de conexión " + miTipoSocket + " socket fallida", e);
            }
        }
    }

    /**
     * Este hilo corre durante una conexion con un dispositivo remoto.
     * maneja todas las las transmisiones entrantes y salientes.
     */
    private class HiloConectado extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public HiloConectado(BluetoothSocket socket, String tipoSocket) {
            Log.d(TAG, "crea HiloConectado: " + tipoSocket);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Obtiene el socket de BT  de flujos input y output
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "No se han creado los sockets temp", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN miHiloConectado");
            byte[] buffer = new byte[1024];
            int bytes;

            //  Sigue escuchando mientras el flujo de entrada siga conectado.
            while (true) {
                try {
                    // Leemos del flujo de entrada
                    bytes = mmInStream.read(buffer);

                    // Envia los bytes leiedos  a la actividad UI.
                    miHandler.obtainMessage(Constantes.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "desconectado", e);
                    conexionPerdida();
                    // Inicia el servicio mediante el reseteo del modo de escucha.
                    ServicioChatBluetooth.this.start();
                    break;
                }
            }
        }

        /**
         *
         * Escribe en los flujos de salida conectados.
         *
         * Recibe los bytes a escribir.
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Comparte el mensaje enviado a la actividad UI.
                miHandler.obtainMessage(Constantes.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Excepcion durante la escritura", e);
            }
        }

        public void cancela() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() del socket de conexion fallido", e);
            }
        }
    }
}