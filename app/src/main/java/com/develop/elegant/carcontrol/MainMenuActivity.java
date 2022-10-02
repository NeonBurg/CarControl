package com.develop.elegant.carcontrol;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.develop.elegant.carcontrol.DataSQLite.DataProviderSQLite;
import com.google.android.gms.ads.MobileAds;

public class MainMenuActivity extends AppCompatActivity {

    private static final String TAG = MainMenuActivity.class.getSimpleName();

    TextView ipTextView;

    Context context;

    DataProviderSQLite dataProviderSQLite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        context = this;

        MobileAds.initialize(this, "ca-app-pub-1747383877486799~8266917383");

        ipTextView = findViewById(R.id.ipTextView);

        dataProviderSQLite = new DataProviderSQLite(this);
        synchBD();
    }

    @Override
    protected void onResume() {
        super.onResume();
        synchBD();
    }


    public void carControlPressed(View view) {
        Intent carControlActivity = new Intent(context, MainActivity.class);
        startActivity(carControlActivity);
    }

    public void servoControlPressed(View view) {
        Intent servoControlActivity = new Intent(context, ServoControlActivity.class);
        startActivity(servoControlActivity);
    }

    public void settingsPressed(View view) {
        Intent settingsActivity = new Intent(context, SettingsActivity.class);
        startActivity(settingsActivity);
    }

    private void synchBD() {

        ipTextView.setText(dataProviderSQLite.getSettings().getIpAddress());

    }

}
