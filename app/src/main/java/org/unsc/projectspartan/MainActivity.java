package org.unsc.projectspartan;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;


public class MainActivity extends Activity {
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    private TextView tv1, tv2;
    private boolean isLed0on, isLed1on;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        isLed0on = false;
        isLed1on = false;
        tv1 = (TextView) findViewById(R.id.textview1);
        tv2 = (TextView) findViewById(R.id.textview2);
        mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        new connectToHost().start();
    }

    private class connectToHost extends Thread {
        @Override
        public void run() {
            try {
                Method m = mDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                mSocket = (BluetoothSocket) m.invoke(mDevice, 1);
                mSocket.connect();
                //Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
                new receivingThread().start();
            } catch (Exception e) {

            }
        }
    }

    private class receivingThread extends Thread {
        private byte[] bytes;
        private double temp0, temp1;
        @Override
        public void run() {
            while (true) {
                try {

                    bytes = new byte[100];
                    int i = mSocket.getInputStream().read(bytes);
                    JSONObject j = new JSONObject(new String(bytes));
                    j = j.getJSONObject("temperature");
                    temp0 = j.getDouble("temp0");
                    temp1 = j.getDouble("temp1");
                    if (i != -1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv1.setText("" + temp0);
                                tv2.setText("" + temp1);
                            }
                        });
                    }

                } catch (Exception e) {

                }

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mSocket.close();
        } catch (Exception e) {

        }
    }

    public void toggleLed0(View view){
        try {
            JSONObject j = new JSONObject();
            if (isLed0on) {
                j.put("value67", 0);
                isLed0on = false;
            } else {
                j.put("value67", 1);
                isLed0on = true;
            }

            if (isLed1on) {
                j.put("value68", 1);
            } else {
                j.put("value68", 0);
            }
            mSocket.getOutputStream().write(j.toString().getBytes());
        } catch (Exception e) {

        }
    }

    public void toggleLed1(View view){
        try {
            JSONObject j = new JSONObject();
            if (isLed0on) {
                j.put("value67", 1);
            } else {
                j.put("value67", 0);
            }

            if (isLed1on) {
                j.put("value68", 0);
                isLed1on = false;
            } else {
                j.put("value68", 1);
                isLed1on = true;
            }
            mSocket.getOutputStream().write(j.toString().getBytes());
        } catch (Exception e) {

        }
    }
}
