package nz.ac.aut.dms.reminder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

public class ShareNFC extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback,
        NfcAdapter.OnNdefPushCompleteCallback {
    private NfcAdapter mNfcAdapter;
    private TextView mInfoText;
    private String data;
    private static final int MESSAGE_SENT = 1;

    private String title = "";
    private String startDate = "";
    private String startTime = "";
    private String endDate = "";
    private String endTime = "";
    private String description = "";

    private DatabaseReference ref;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_nfc);

        ref = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        data = intent.getStringExtra("data");

        mInfoText = (TextView) findViewById(R.id.textout);

        Button cancelButton = (Button) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
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
            title = jsonObject.get("title").toString();
            startDate = jsonObject.get("startDate").toString();
            startTime = jsonObject.get("startTime").toString();
            endDate = jsonObject.get("endDate").toString();
            endTime = jsonObject.get("endTime").toString();
            description = jsonObject.get("description").toString();
            result += "title  " + title + "\n";
            result += "startDate  " + startDate + "\n";
            result += "startTime  " + startTime + "\n";
            result += "endDate  " + endDate + "\n";
            result += "endTime  " + endTime + "\n";
            result += "description " + description + "\n";
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mInfoText.setText(result);
        Button button = (Button) findViewById(R.id.save_to_calendar);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddEvent().execute();
            }
        });

    }

    private class AddEvent extends AsyncTask<Void, Void, Void> {
        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(ShareNFC.this);
            pd.setMessage("loading");
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            DatabaseReference fb = ref.child(mAuth.getCurrentUser().getUid()).child("Schedule").push();

            CustomDate cd = new CustomDate(startDate, endDate, startTime, endTime, title, description);
            fb.setValue(cd);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pd.dismiss();
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
