package nz.ac.aut.dms.reminder;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import nz.ac.aut.dms.reminder.bt.ConnectThread;

public class ShareBT extends AppCompatActivity {

    public static UUID UUID = java.util.UUID.fromString("a18f193e-a6bf-416b-8822-26511265b067");
    private static final int DISCOVER_DURATION = 300;
    private static final int REQUEST_BLU = 1;
    private BluetoothAdapter ba;

    private Set<BluetoothDevice> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_bt);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SEND);
        registerReceiver(br,intentFilter);

        Button sendButton = (Button) findViewById(R.id.send_via_bluetooth);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ba = BluetoothAdapter.getDefaultAdapter();
                if (ba == null) {
                    Toast.makeText(ShareBT.this, "bt is not available", Toast.LENGTH_SHORT).show();
                } else {
                    enableBt();
                }
            }
        });
    }

    public void enableBt() {
        Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, ShareBT.DISCOVER_DURATION);
        startActivityForResult(discoveryIntent, REQUEST_BLU);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == DISCOVER_DURATION && requestCode == REQUEST_BLU) {
            devices = ba.getBondedDevices();


            AlertDialog.Builder builderSingle = new AlertDialog.Builder(ShareBT.this);
            builderSingle.setTitle("Select One Name:-");

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    ShareBT.this,
                    android.R.layout.select_dialog_singlechoice);

            for (BluetoothDevice bd : devices){
                arrayAdapter.add(bd.getName());
            }

            builderSingle.setNegativeButton(
                    "cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            builderSingle.setAdapter(
                    arrayAdapter,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String strName = arrayAdapter.getItem(which);
                            AlertDialog.Builder builderInner = new AlertDialog.Builder(
                                    ShareBT.this);
                            builderInner.setMessage(strName);
                            builderInner.setTitle("Send to ");
                            builderInner.setPositiveButton(
                                    "Ok",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ConnectThread ct = new ConnectThread();
                                            BluetoothDevice bd = null;
                                            for (BluetoothDevice b :devices){
                                                if(b.getName().equals(strName)){
                                                    bd = b;
                                                    System.out.println("lol");
                                                    break;
                                                }
                                            }

                                            if(bd!= null){
                                                ct.connect(bd,UUID);
                                            }
                                            dialog.dismiss();
                                        }
                                    });
                            builderInner.show();
                        }
                    });
            builderSingle.show();

        } else {
            Toast.makeText(ShareBT.this, "bluetooth cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(ShareBT.this,"received something",Toast.LENGTH_SHORT).show();
        }
    };

}
