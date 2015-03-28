package com.pendragon.blueconnect;

/**
 * Created by carlos on 28/03/2015.
 */

public interface Constantes {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "nombre_dispositivo";
    public static final String TOAST = "toast";

}