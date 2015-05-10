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
import com.pendragon.blueconnect.fragments.BluetoothChat;
import com.pendragon.blueconnect.utils.DrawerItem;
import com.pendragon.blueconnect.utils.DrawerListAdapter;

import java.util.ArrayList;


public class MainActivity extends Activity {


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

    private BluetoothAdapter mBluetoothAdapter = null;


    int actualposition;
    Fragment fragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment = null;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }


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
        items.add(new DrawerItem(tagTitles[3], R.drawable.visible));

        // Set the items with an adapter
        drawerList.setAdapter(new DrawerListAdapter(this, items));
        // We handle the event
        drawerList.setOnItemClickListener(new DrawerItemClickListener());


        // We set main fragment
        actualposition=10;
        selectItem(1);

    }

    private void selectItem(int position) {


        if (actualposition==0)
        {
            BluetoothChat actualfragment = (BluetoothChat) fragment;
            actualfragment.closeConnections();
        }

        if (position == 3) {
            actualposition=3;
            // Make bluetooth visible
        }
        else {
            if (position == 0) {

                actualposition=0;
                // Main Fragment
                fragment = new BluetoothChat();
                // We send item position
                Bundle args = new Bundle();
                args.putInt(BluetoothChat.ARG_ARTICLES_NUMBER, position);
                fragment.setArguments(args);

            } else {
                actualposition=1;
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


    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.secure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            case R.id.insecure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            case R.id.discoverable:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
        }
        return false;
    }


    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // We call the method selectItem()
            selectItem(position);
        }
    }


    private void haceVisible() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent visibleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            visibleIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(visibleIntent);
        }
    }

}

