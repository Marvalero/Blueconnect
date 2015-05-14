package com.pendragon.blueconnect.utils;

import android.app.NotificationManager;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.pendragon.blueconnect.R;
import com.pendragon.blueconnect.utils.BluetoothChatService;

import com.pendragon.blueconnect.utils.MySingleton;


/**
 * Created by carlos on 14/05/2015.
 */
public class ViewNotification extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewnotification_layout);
        NotificationManager nm =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
   nm.cancel(getIntent().getExtras().getInt("notificationID"));


    }

}
