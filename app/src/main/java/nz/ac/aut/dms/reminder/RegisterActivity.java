package nz.ac.aut.dms.reminder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private Firebase firebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseRef = ((Reminder)getApplication()).getFirebaseRef();

        Button registerButton = (Button) findViewById(R.id.sign_up_register_button);

        final EditText usernameET = (EditText) findViewById(R.id.sign_up_page_username);
        final EditText passwordET = (EditText) findViewById(R.id.sign_up_page_password);
        final EditText displayNameET = (EditText) findViewById(R.id.sign_up_page_display_name);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = usernameET.getText().toString();
                String password = passwordET.getText().toString();
                final String displayName = displayNameET.getText().toString();

                firebaseRef.createUser(username, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
                    @Override
                    public void onSuccess(Map<String, Object> result) {
                        Map<String,String> data = new HashMap<String, String>();
                        data.put("displayName",displayName);
                        data.put("email", username);
                        firebaseRef.child(result.get("uid").toString()).setValue(data);
                        onBackPressed();
                    }
                    @Override
                    public void onError(FirebaseError firebaseError) {
                        Log.i("register","error");
                    }
                });
            }
        });
    }
}
