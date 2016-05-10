package nz.ac.aut.dms.reminder;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by wilsonjoe on 9/05/16.
 */
public class Reminder extends Application {
    private Firebase firebaseRef;
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        firebaseRef = new Firebase("https://reminderaut.firebaseio.com/");
    }

    public Firebase getFirebaseRef() {
        return firebaseRef;
    }

    public void setFirebaseRef(Firebase firebaseRef) {
        this.firebaseRef = firebaseRef;
    }
}
