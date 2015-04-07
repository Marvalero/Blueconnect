package com.pendragon.blueconnect;

import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.pendragon.blueconnect.bluetoothcontroller.DevicesActivity;
import com.pendragon.blueconnect.fragments.ArticleFragment;
import com.pendragon.blueconnect.fragments.FragmentoChatBluetooth;
import com.pendragon.blueconnect.fragments.MainFragment;
import com.pendragon.blueconnect.utils.DrawerItem;
import com.pendragon.blueconnect.utils.DrawerListAdapter;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {


    // Códigos de solicitud de intents
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Drawer menu
    private ListView     drawerList;
    // Items on menu Navigation Drawer
    private String[] tagTitles;
    // drawer_layout
    private DrawerLayout drawerLayout;

    private BluetoothAdapter adaptadorBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            FragmentoChatBluetooth fragment = new FragmentoChatBluetooth();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

        adaptadorBluetooth = BluetoothAdapter.getDefaultAdapter();

        // Setting the Navigation Drawer Menu
        tagTitles = getResources().getStringArray(R.array.menu_items);
        drawerList = (ListView) findViewById(R.id.drawer_list);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // We set "drawer_list" to drawer_list_item layout
        drawerList.setAdapter(new ArrayAdapter<String>(
                this,
                R.layout.drawer_list_item,
                getResources().getStringArray(R.array.menu_items)));

        // Create a list of drawer items
        ArrayList<DrawerItem> items = new ArrayList<DrawerItem>();
        items.add(new DrawerItem(tagTitles[0],R.drawable.home));
        items.add(new DrawerItem(tagTitles[1],R.drawable.profile));
        items.add(new DrawerItem(tagTitles[2],R.drawable.search));
        items.add(new DrawerItem(tagTitles[3],R.drawable.visible));

        // Set the items with an adapter
        drawerList.setAdapter(new DrawerListAdapter(this, items));
        // We handle the event
        drawerList.setOnItemClickListener(new DrawerItemClickListener());


        // We set main fragment
        selectItem(0);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat_bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.escaneo_conexion_segura: {
// Lanzamos ActividadDispositivos para ver los dispositivos y escanear
                Intent serverIntent = new Intent(getApplicationContext(), DevicesActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.escaneo_conexion_insegura: {
// Lanzamos ActividadDispositivos para ver los dispositivos y escanear
                Intent serverIntent = new Intent(getApplicationContext(), DevicesActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.visible: {
// Ensure this device is discoverable by others
                haceVisible();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // We call the method selectItem()
            selectItem(position);
        }
    }

    private void selectItem(int position) {

        Fragment fragment;
        if (position == 3) {
            // Make bluetooth visible
        }
        else {
            if (position == 0) {
                // Main Fragment
                fragment = new MainFragment();
                // We send item position
                Bundle args = new Bundle();
                args.putInt(MainFragment.ARG_ARTICLES_NUMBER, position);
                fragment.setArguments(args);

            } else {
                // Chat History and Profile Settings
                fragment = new ArticleFragment();
                // We send item position
                Bundle args = new Bundle();
                args.putInt(ArticleFragment.ARG_ARTICLES_NUMBER, position);
                fragment.setArguments(args);
            }
            // We change the content frame
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit();

            // update selected item and title, then close the drawer
            drawerList.setItemChecked(position, true);
            setTitle(tagTitles[position]);
            drawerLayout.closeDrawer(drawerList);

        }
    }

    private void haceVisible() {
        if (adaptadorBluetooth.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent visibleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            visibleIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(visibleIntent);
        }
    }






}

