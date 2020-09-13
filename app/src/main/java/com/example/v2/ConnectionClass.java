package com.example.v2;

import android.bluetooth.BluetoothSocket;
import android.os.SystemClock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectionClass extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public ConnectionClass(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;
        while (true) {
            try {
                bytes = mmInStream.available();
                if(bytes != 0) {
                    SystemClock.sleep(100);
                    bytes = mmInStream.available();
                    bytes = mmInStream.read(buffer, 0, bytes);
                    BluetoothActivity.handler.obtainMessage(BluetoothActivity.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                }
            } catch (IOException e) {
                e.printStackTrace();

                break;
            }
        }
    }

    public void write(String input) {
        byte[] bytes = input.getBytes();
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}