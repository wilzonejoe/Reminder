package nz.ac.aut.dms.reminder.bt;

import android.bluetooth.BluetoothSocket;
import android.util.Base64;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import nz.ac.aut.dms.reminder.CustomDate;

/**
 * Created by wilsonjoe on 1/06/16.
 */
public class ReceivedThread extends Thread {

    BluetoothSocket socket;

    public ReceivedThread(BluetoothSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            while (socket.isConnected()) {
                byte[] buffer = new byte[255];
                ByteArrayInputStream input = new ByteArrayInputStream(buffer);
                InputStream inputStream = socket.getInputStream();
                inputStream.read(buffer);
                String s = getStringFromInputStream(input);
                String [] split = s.split("\\?");

                System.out.println(split[0]);
                JSONObject jsonObject = new JSONObject(split[0]);
                String title = jsonObject.get("title").toString();
                String startDate = jsonObject.get("startDate").toString();
                String startTime = jsonObject.get("startTime").toString();
                String endDate = jsonObject.get("endDate").toString();
                String endTime = jsonObject.get("endTime").toString();
                String description = jsonObject.get("description").toString();

                DatabaseReference fb = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Schedule").push();

                CustomDate cd = new CustomDate(startDate, endDate, startTime, endTime, title, description);
                fb.setValue(cd);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

}
