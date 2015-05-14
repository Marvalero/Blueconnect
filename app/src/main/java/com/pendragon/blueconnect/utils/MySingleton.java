package com.pendragon.blueconnect.utils;

/**
 * Created by postigo on 13/5/15.
 */
public class MySingleton {

        private static MySingleton instance;

        public String customVar;

        public int mainFragment;


        public static void initInstance()
        {
            if (instance == null)
            {
                // Create the instance
                instance = new MySingleton();
            }
        }

        //Crea instancia Singleton
        public static MySingleton getInstance()
        {
            if (instance == null)
            {
                // Create the instance
                instance = new MySingleton( );
            }
            // Return the instance
            return instance;
        }

        //Constructor del Singleton
        private MySingleton(){
            this.mainFragment = 0;
        }

        //Guardamos el valor del nombre del dispositivo con el que hablamos
        public void setString(String ndevice)
        {
            // Custom methodç
            customVar=ndevice;
        }
        //Conseguimos el valor del nombre del dispositivo con el que hablamos
        public String getString(){
            return customVar;
        }

        public void setMainFragment(int option){
            this.mainFragment = option;
        }

        public int getMainFragment(){

            return mainFragment;
        }

}
