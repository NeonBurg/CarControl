package com.develop.elegant.carcontrol;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.develop.elegant.carcontrol.DataSQLite.DataProviderSQLite;
import com.develop.elegant.carcontrol.DataSQLite.Models.ButtonTable;
import com.develop.elegant.carcontrol.DataSQLite.Models.SettingsTable;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServoControlActivity extends AppCompatActivity {

    private static final String TAG = ServoControlActivity.class.getSimpleName();

    private final String API_MAKE_CONNECTION = "/make-connection";
    private final String API_SET_GO_FORWARD = "/set-go-forward?go_forward=";
    private final String API_SET_THROTTLE = "/set-throttle?throttle_val=";
    private final String API_SET_SERVO_ANGLE = "/set-servo-angle?servo_angle=";

    TextView textViewThrottle, textViewServo;
    TextView ip_textView, status_textView;
    SeekBar sliderThrottle, sliderServo;
    CheckBox goForwardCheckBox;

    int throttle_current = 0;

    int servo_angle_max = 30;
    int servo_angle_current = 60;
    int servo_angle_default = 60;

    DataProviderSQLite dataProviderSQLite;
    SettingsTable currentSettingsTable;
    ButtonTable goForwardButton, goBackwardButton, servoButton, motorPWMButton;

    private String conn_status;
    private boolean isConnected = false;

    private Handler handler;

    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servo_control);

        dataProviderSQLite = new DataProviderSQLite(this);
        executorService = Executors.newFixedThreadPool(4);

        ip_textView = (TextView) this.findViewById(R.id.ip_textView);
        status_textView = (TextView) this.findViewById(R.id.status_textView);

        textViewThrottle = findViewById(R.id.throttleTextView);
        textViewServo = findViewById(R.id.servoTextView);
        sliderThrottle = findViewById(R.id.sliderThrottle);
        sliderServo = findViewById(R.id.sliderServo);
        goForwardCheckBox = findViewById(R.id.goForwardCheckBox);
        goForwardCheckBox.setChecked(true);

        handler = new Handler();

        sliderThrottle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if(progress != throttle_current) {
                    throttle_current = progress;
                    setThrottle(progress);
                    textViewThrottle.setText(String.valueOf(progress));
                    if(throttle_current > 0) goForwardCheckBox.setEnabled(false);
                    else goForwardCheckBox.setEnabled(true);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sliderServo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //textViewServo.setText(String.valueOf(progress));

                if(progress < 50) {
                    float progress_percent = progress / 50.0f;
                    int angle_turn = Math.round (servo_angle_max - (servo_angle_max * progress_percent));
                    servo_angle_current = servo_angle_default - angle_turn;
                    textViewServo.setText("- " + String.valueOf(angle_turn));
                    Log.d(TAG, "angle_turn = " + angle_turn + " | servo_angle_current = " + servo_angle_current);
                }
                else if(progress > 50) {
                    progress -= 50;
                    float progress_percent = progress / 50.0f;
                    int angle_turn = Math.round(servo_angle_max * progress_percent);
                    servo_angle_current = servo_angle_default + angle_turn;
                    textViewServo.setText(String.valueOf(angle_turn));
                    Log.d(TAG, "angle_turn = " + angle_turn + " | servo_angle_current = " + servo_angle_current);
                }
                else {
                    float angle_turn = 0;
                    textViewServo.setText(String.valueOf(angle_turn));
                    servo_angle_current = servo_angle_default;
                    textViewServo.setText(String.valueOf(angle_turn));
                    Log.d(TAG, "angle_turn = " + angle_turn + " | servo_angle_current = " + servo_angle_current);
                }
                setServoAngle(servo_angle_current);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        goForwardCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) setGoForward(1);
                else setGoForward(0);
            }
        });

        sliderServo.setProgress(50);
        textViewServo.setText(String.valueOf(0));
        textViewThrottle.setText(String.valueOf(0));

        synchronizeBD();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

                String parameters = "?p_servo="+servoButton.getGpioNum()+"&p_motor="+motorPWMButton.getGpioNum()+"&in1="+goForwardButton.getGpioNum()+"&in2="+goBackwardButton.getGpioNum();

                try
                {
                    URL url = new URL("http://" + currentSettingsTable.getIpAddress() + API_MAKE_CONNECTION + parameters);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("GET");
                    //connection.setRequestProperty("Authorization", basicAuth);

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
                        isConnected = true;
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

    private void setServoAngle(final int servoAngle) {
        Log.d(TAG, "setServoAngle = " + servoAngle);
        if(isConnected) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    sendSetServoAngle(servoAngle);
                }
            });
        }
    }

    private void sendSetServoAngle(int servoAngle) {
        try {
            URL url = new URL("http://" + currentSettingsTable.getIpAddress() + API_SET_SERVO_ANGLE + servoAngle);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            System.out.println("---------------------------------------\n");
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            System.out.println("---------------------------------------\n");

            // read the output from the server
            /*reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            Log.d(TAG, "stringBuilder = " + stringBuilder.toString());*/

        } catch (Exception e) {
            e.printStackTrace();
            conn_status = e.getMessage();
            Log.d(TAG, "Connection status: " + e.getMessage());
        }
    }

    private void setThrottle(final int throttle) {
        Log.d(TAG, "setThrottle = " + throttle);
        if(isConnected) {
            /*final Thread networkThread = new Thread() {
                @Override
                public void run() {
                    sendSetThrottle(throttle);
                }
            };
            networkThread.start();*/
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    sendSetThrottle(throttle);
                }
            });
        }
    }

    private void sendSetThrottle(int throttle) {

        //BufferedReader reader = null;

        try {
            URL url = new URL("http://" + currentSettingsTable.getIpAddress() + API_SET_THROTTLE + throttle);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            System.out.println("---------------------------------------\n");
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            System.out.println("---------------------------------------\n");

            // read the output from the server
            /*reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            Log.d(TAG, "stringBuilder = " + stringBuilder.toString());*/

        } catch (Exception e) {
            e.printStackTrace();
            conn_status = e.getMessage();
            Log.d(TAG, "Connection status: " + e.getMessage());
        }

    }

    private void setGoForward(final int goForward) {
        Log.d(TAG, "setGoForward = " + goForward);
        if(isConnected) {
            final Thread networkThread = new Thread() {
                @Override
                public void run() {
                    sendSetGoForward(goForward);
                }
            };
            networkThread.start();
        }
    }

    private void sendSetGoForward(int goForward) {

        BufferedReader reader = null;

        try {
            URL url = new URL("http://" + currentSettingsTable.getIpAddress() + API_SET_GO_FORWARD + goForward);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            System.out.println("---------------------------------------\n");
            System.out.println("\nSending 'GET' request to URL : " + url);
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

    private void showLoadingWheel() {
        status_textView.setText("Connecting");
        this.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    private void hideLoadingWheel() {
        this.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
    }

    private void synchronizeBD() {

        currentSettingsTable = dataProviderSQLite.getSettings();
        goForwardButton = dataProviderSQLite.getButtonTable("forward");
        goBackwardButton = dataProviderSQLite.getButtonTable("backward");
        servoButton = dataProviderSQLite.getButtonTable("servo");
        motorPWMButton = dataProviderSQLite.getButtonTable("motorPWM");

        ip_textView.setText(currentSettingsTable.getIpAddress());

        startConnection();
    }

}
