package com.pendragon.blueconnect;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.pendragon.blueconnect.fragments.ArticleFragment;
import com.pendragon.blueconnect.fragments.MainFragment;
import com.pendragon.blueconnect.utils.DrawerItem;
import com.pendragon.blueconnect.utils.DrawerListAdapter;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends Activity {


    // Códigos de solicitud de intents
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // adaptador de BT local
    private BluetoothAdapter mBluetoothAdapter = null;
    // Lista de Drawer
    private ListView     drawerList;
    // Items del menu drawer
    private String[] tagTitles;
    // layout del drawer
    private DrawerLayout drawerLayout;
    Date dateConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

           //This is the last connection date
        dateConnection=new Date();

        // Obtiene el adaptador local BT
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Establece el menu drawer
        tagTitles = getResources().getStringArray(R.array.menu_items);
        drawerList = (ListView) findViewById(R.id.drawer_list);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Asignamos el layout al listado de items del drawer
        drawerList.setAdapter(new ArrayAdapter<String>(
                this,
                R.layout.drawer_list_item,
                getResources().getStringArray(R.array.menu_items)));

        // Creamos la lista de items del drawer
        ArrayList<DrawerItem> items = new ArrayList<DrawerItem>();
        items.add(new DrawerItem(tagTitles[0],R.drawable.home));
        items.add(new DrawerItem(tagTitles[1],R.drawable.profile));
        items.add(new DrawerItem(tagTitles[2],R.drawable.visible));

        // Le ponemos un adaptador a los items
        drawerList.setAdapter(new DrawerListAdapter(this, items));
        // Y asignamos un handler
        drawerList.setOnItemClickListener(new DrawerItemClickListener());


        // Ahora establecemos el fragmento principal
        selectItem(0);

    }

    @Override
    public void onStart() {
        super.onStart();

        // Si BT no esta activo, pedimos activarlo
        //  Y llamamos a setupChat cuando se resuelva la accion/actividad
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Si estaba activo, se lanza el chat
        }
    }
    public void onClickScanDevices(View view) {

        // Le asignamos al click
        Intent serverIntent = null;
        serverIntent = new Intent(this, BluetoothChat.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
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
                //Lanzamos DeviceListActivity para ver los dispositivos y escanear.
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            case R.id.insecure_connect_scan:
                // Lo mismo pero de forma insegura
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            case R.id.discoverable:
                // Nos hacemos visibles
                ensureDiscoverable();
                return true;
        }
        return false;
    }


    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /* Este es el listener del listview del drawer. */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // llamamos al metodo selectItem()
            selectItem(position);
        }
    }

    private void selectItem(int position) {

        Fragment fragment;
        if (position == 2) {
            // Nos hacemos visibles
            ensureDiscoverable();
        }
        else {
            if (position == 0) {
                // fragmento main
                fragment = new MainFragment();
                // mandamos la posicion del item
                Bundle args = new Bundle();
                args.putInt(MainFragment.ARG_ARTICLES_NUMBER, position);
                fragment.setArguments(args);

            } else {
                // Pasamos al historial del chat y otra info.
                fragment = new ArticleFragment();
                // Mandamos la posicion del item
                Bundle args = new Bundle();

                args.putString(ArticleFragment.ARG_ARTICLES_DATE,dateConnection.toString());
                fragment.setArguments(args);
            }
            // cambiamos el contenido del fragment
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit();

            // actualizamos el item seleccionado, el titulo y cerramos el drawer.
            drawerList.setItemChecked(position, true);
            setTitle(tagTitles[position]);
            drawerLayout.closeDrawer(drawerList);

        }
    }



}

