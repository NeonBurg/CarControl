package com.develop.elegant.carcontrol;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.develop.elegant.carcontrol.DataSQLite.DataProviderSQLite;
import com.develop.elegant.carcontrol.DataSQLite.Models.ButtonTable;
import com.develop.elegant.carcontrol.DataSQLite.Models.SettingsTable;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private String host = "192.168.0.108:8000";

    Button forward_btn, back_btn, left_btn, right_btn;
    ImageView settings_btn;
    TextView ip_textView, status_textView;

    private String login = "webiopi";
    private String pass = "raspberry";
    final String basicAuth = "Basic " + Base64.encodeToString((login+":"+pass).getBytes(), Base64.NO_WRAP);

    // Номера портов для управления двигателем
    private int   gpio_forward = 11,
                        gpio_back = 2,
                        gpio_left = 10,
                        gpio_right = 9;

    private String conn_status;
    private boolean isConnected = false;

    private Handler handler;

    Context context;

    DataProviderSQLite dataProviderSQLite;

    SettingsTable currentSettingsTable;
    ButtonTable goForwardButton, goBackwardButton, goLeftButton, goRightButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        forward_btn = (Button) this.findViewById(R.id.forward_btn);
        back_btn = (Button) this.findViewById(R.id.back_btn);
        left_btn = (Button) this.findViewById(R.id.left_btn);
        right_btn = (Button) this.findViewById(R.id.right_btn);

        forward_btn.setOnTouchListener(btn_touch_listener);
        back_btn.setOnTouchListener(btn_touch_listener);
        left_btn.setOnTouchListener(btn_touch_listener);
        right_btn.setOnTouchListener(btn_touch_listener);

        settings_btn = (ImageView) this.findViewById(R.id.settings_btn);
        settings_btn.setOnClickListener(settingsClickListener);

        ip_textView = (TextView) this.findViewById(R.id.ip_textView);
        status_textView = (TextView) this.findViewById(R.id.status_textView);

        ip_textView.setText(host);

        handler = new Handler();

        // Открываем подключение к Raspberry Pi
        startConnection();

        setAllGpioOut();

        dataProviderSQLite = new DataProviderSQLite(this);
        synchronizeBD();
    }

    @Override
    protected void onResume() {
        super.onResume();
        synchronizeBD();
    }

    @Override
    protected void onPause() {
        super.onPause();
        clearGPIO();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearGPIO();
    }

    View.OnClickListener settingsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent settingsActivity = new Intent(context, MainMenuActivity.class);
            startActivity(settingsActivity);
        }
    };

    View.OnTouchListener btn_touch_listener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                switch (view.getId()) {
                    case R.id.forward_btn:
                        //gpio.digitalWrite(gpio_forward, true);
                        goForward(true);
                        Log.d(TAG, "forward_btn touched");
                        break;
                    case R.id.back_btn:
                        goBack(true);
                        Log.d(TAG, "back_btn touched");
                        break;
                    case R.id.left_btn:
                        goLeft(true);
                        Log.d(TAG, "left_btn touched");
                        break;
                    case R.id.right_btn:
                        goRight(true);
                        Log.d(TAG, "right_btn touched");
                        break;
                    case R.id.settings_btn:
                        Log.d(TAG, "settings_btn touched");
                        break;
                }
            }
            else if(motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == MotionEvent.ACTION_UP) {
                switch (view.getId()) {
                    case R.id.forward_btn:
                        //gpio.digitalWrite(gpio_forward, false);
                        goForward(false);
                        Log.d(TAG, "forward_btn untouched");
                        break;
                    case R.id.back_btn:
                        goBack(false);
                        Log.d(TAG, "back_btn untouched");
                        break;
                    case R.id.left_btn:
                        goLeft(false);
                        Log.d(TAG, "left_btn untouched");
                        break;
                    case R.id.right_btn:
                        goRight(false);
                        Log.d(TAG, "right_btn untouched");
                        break;
                    case R.id.settings_btn:
                        Log.d(TAG, "settings_btn untouched");
                        break;
                }
            }
            return false;
        }
    };

    private void goForward(final boolean signal) {
        final Thread networkThread = new Thread() {
            @Override
            public void run() {
                writeGpio(gpio_forward, signal);
            }
        };
        networkThread.start();
    }

    private void goBack(final boolean signal) {
        final Thread networkThread = new Thread() {
            @Override
            public void run() {
                writeGpio(gpio_back, signal);
            }
        };
        networkThread.start();
    }

    private void goLeft(final boolean signal) {
        final Thread networkThread = new Thread() {
            @Override
            public void run() {
                writeGpio(gpio_left, signal);
            }
        };
        networkThread.start();
    }

    private void goRight(final boolean signal) {
        final Thread networkThread = new Thread() {
            @Override
            public void run() {
                writeGpio(gpio_right, signal);
            }
        };
        networkThread.start();
    }

    private void clearGPIO() {

    }

    final Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String mesage = bundle.getString("myKey");
            switch (mesage) {
                case "DONE":
                    hideLoadingWheel();
                    status_textView.setText(String.valueOf(conn_status));
                    break;
            }
        }
    };

    private void startConnection() {
        // Пытаемся подключиться

        final Thread networkThread = new Thread() {
            BufferedReader reader = null;
            @Override
            public void run() {

                Bundle bundle = new Bundle();
                Message msg = new Message();
                bundle.putString("myKey", "DONE");
                msg.setData(bundle);

                handler.post(new Runnable() { // This thread runs in the UI
                    @Override
                    public void run() {
                        showLoadingWheel();
                    }

                });

                try
                {
                    URL url = new URL("http://" + host + "/GPIO/1/value");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Authorization", basicAuth);

                    int responseCode = connection.getResponseCode();
                    System.out.println("---------------------------------------\n");
                    System.out.println("\nSending 'GET' request to URL : " + url);
                    System.out.println("Response Code : " + responseCode);
                    System.out.println("---------------------------------------\n");

                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append('\n');
                    }

                    if (stringBuilder.length() > 0) {
                        Log.d(TAG, "stringBuilder = " + stringBuilder.toString());
                        conn_status = "Connected";
                    }

                    handler2.sendMessage(msg);

                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    conn_status = e.getMessage();
                    handler2.sendMessage(msg);
                    Log.d(TAG, "Connection status: " + e.getMessage());
                }
            }
        };
        networkThread.start();
    }

    private void writeGpio(int gpio_num, boolean value) {
        BufferedReader reader = null;
        int intval = 0;
        if(value) intval = 1;

        try {
            String urlParameters = "value="+intval;
            URL url = new URL("http://" + host + "/GPIO/"+gpio_num+"/value/"+intval);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", basicAuth);

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = connection.getResponseCode();
            System.out.println("---------------------------------------\n");
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            System.out.println("---------------------------------------\n");

            // read the output from the server
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            Log.d(TAG, "stringBuilder = " + stringBuilder.toString());

        } catch (Exception e) {
            e.printStackTrace();
            conn_status = e.getMessage();
            Log.d(TAG, "Connection status: " + e.getMessage());
        }
    }

    private void setGpioOut(final int gpio_num) {
        // Пытаемся подключиться

        final Thread networkThread = new Thread() {
            BufferedReader reader = null;

            @Override
            public void run() {
                try {
                    URL url = new URL("http://" + host + "/GPIO/" + gpio_num + "/function/out");

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Authorization", basicAuth);

                    int responseCode = connection.getResponseCode();
                    System.out.println("---------------------------------------\n");
                    System.out.println("\nSending 'POST' request to URL : " + url);
                    System.out.println("Response Code : " + responseCode);
                    System.out.println("---------------------------------------\n");

                    // read the output from the server
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append('\n');
                    }
                    Log.d(TAG, "stringBuilder = " + stringBuilder.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                    conn_status = e.getMessage();
                    Log.d(TAG, "Connection status: " + e.getMessage());
                }
            }
        };
        networkThread.start();
    }

    private void showLoadingWheel() {
        status_textView.setText("Connecting");
        this.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    private void hideLoadingWheel() {
        this.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
    }

    private void setAllGpioOut() {

        setGpioOut(gpio_forward);
        setGpioOut(gpio_back);
        setGpioOut(gpio_left);
        setGpioOut(gpio_right);

    }

    private void synchronizeBD() {

        currentSettingsTable = dataProviderSQLite.getSettings();
        goForwardButton = dataProviderSQLite.getButtonTable("forward");
        goBackwardButton = dataProviderSQLite.getButtonTable("backward");
        goLeftButton = dataProviderSQLite.getButtonTable("left");
        goRightButton = dataProviderSQLite.getButtonTable("right");

        host = currentSettingsTable.getIpAddress();
        login = currentSettingsTable.getLogin();
        pass = currentSettingsTable.getPassword();
        gpio_forward = goForwardButton.getGpioNum();
        gpio_back = goBackwardButton.getGpioNum();
        gpio_left = goLeftButton.getGpioNum();
        gpio_right = goRightButton.getGpioNum();

        ip_textView.setText(host);

        startConnection();
    }
}
