package nz.ac.aut.dms.reminder;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class ShareNFC extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback,
        NfcAdapter.OnNdefPushCompleteCallback {
    private NfcAdapter mNfcAdapter;
    private TextView mInfoText;
    private String data;
    private static final int MESSAGE_SENT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_nfc);

        Intent intent = getIntent();
        data = intent.getStringExtra("data");

        mInfoText = (TextView) findViewById(R.id.textout);
        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            mInfoText = (TextView) findViewById(R.id.info);
            mInfoText.setText("NFC is not available on this device.");
        } else {
            // Register callback to set NDEF message
            mInfoText = (TextView) findViewById(R.id.info);
            mInfoText.setText("NFC connect");
            mNfcAdapter.setNdefPushMessageCallback(this, this);
            // Register callback to listen for message-sent success
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String text = this.data;
        NdefMessage msg = new NdefMessage(NdefRecord.createMime(
                "text/plain", text.getBytes())
        );
        return msg;
    }

    @Override
    public void onNdefPushComplete(NfcEvent arg0) {
        // A handler is needed to send messages to the activity when this
        // callback occurs, because it happens from a binder thread
        mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SENT:
                    Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        String dataReceived = new String(msg.getRecords()[0].getPayload());
        JSONObject jsonObject;
        String result = "";
        try {
            jsonObject = new JSONObject(dataReceived);
            result += "title  " + jsonObject.get("title") +"\n";
            result += "startDate  " + jsonObject.get("startDate") +"\n";
            result += "startTime  " + jsonObject.get("startTime") +"\n";
            result += "endDate  " + jsonObject.get("endDate") +"\n";
            result += "endTime  " + jsonObject.get("endTime") +"\n";
            result += "description " + jsonObject.get("description") +"\n";
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mInfoText.setText(result);
    }

}
