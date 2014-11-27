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
    private TextView tv0, tv1, tv2, tv3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        tv0 = (TextView) findViewById(R.id.temp0);
        tv1 = (TextView) findViewById(R.id.temp1);
        tv2 = (TextView) findViewById(R.id.temp2);
        tv3 = (TextView) findViewById(R.id.temp3);
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
        private double temp0, temp1, temp2, temp3;
        @Override
        public void run() {
            while (true) {
                try {

                    bytes = new byte[1024];
                    int i = mSocket.getInputStream().read(bytes);
                    JSONObject j = new JSONObject(new String(bytes));
                    temp0 = j.getDouble("head temperature");
                    temp1 = j.getDouble("armpits temperature");
                    temp2 = j.getDouble("crotch temperature");
                    temp3 = j.getDouble("water temperature");
                    if (i != -1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv0.setText(String.format(" %.1f", temp0));
                                tv1.setText(String.format(" %.1f", temp1));
                                tv2.setText(String.format(" %.1f", temp2));
                                tv3.setText(String.format(" %.1f", temp3));
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
}
