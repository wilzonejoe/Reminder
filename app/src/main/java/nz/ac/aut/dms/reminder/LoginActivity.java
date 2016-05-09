package nz.ac.aut.dms.reminder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class LoginActivity extends AppCompatActivity {

    private Firebase firebaseRef;
    private Firebase.AuthResultHandler authResultHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseRef = ((Reminder)getApplication()).getFirebaseRef();
        // Create a handler to handle the result of the authentication
        authResultHandler = new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                // Authenticated successfully with payload authData
                Log.i("login","login success");
                Intent i = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(i);
            }
            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                // Authenticated failed with error firebaseError
                Log.i("login","login error");
            }
        };

        final EditText usernameET = (EditText) findViewById(R.id.username_edit_text);
        final EditText passwordET = (EditText) findViewById(R.id.password_edit_text);
         Button signInButton = (Button) findViewById(R.id.login_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameET.getText().toString();
                String password = passwordET.getText().toString();
                firebaseRef.authWithPassword(username,password, authResultHandler);
            }
        });
        Button registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(i);
            }
        });


    }
}
