package nz.ac.aut.dms.reminder.bt;

import android.bluetooth.BluetoothSocket;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by wilsonjoe on 1/06/16.
 */
public class SendThread extends Thread {
    private BluetoothSocket socket;
    private String data;


    @Override
    public void run() {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream(4);
            output.write(data.getBytes());
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(output.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SendThread(BluetoothSocket socket, String data) {
        this.socket = socket;
        this.data = data;
    }
}
