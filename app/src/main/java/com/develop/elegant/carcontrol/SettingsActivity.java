package com.develop.elegant.carcontrol;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.develop.elegant.carcontrol.DataSQLite.DataProviderSQLite;
import com.develop.elegant.carcontrol.DataSQLite.Models.ButtonTable;
import com.develop.elegant.carcontrol.DataSQLite.Models.SettingsTable;

/**
 * Created by User on 09.12.2017.
 */

public class SettingsActivity extends Activity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    ImageView backImageView;
    EditText ipAddressEditText, loginEditText, passwordEditText;
    EditText goForwardEditText, goBackEditText, goLeftEditText, goRightEditText, servoTurnEditText, motorPWMEditText;

    /*ExpandableListView raspberryVersionsExpandView;
    RaspberryVersionAdapter raspberryVersionAdapter;*/

    DataProviderSQLite dataProviderSQLite;

    SettingsTable currentSettingsTable;

    ButtonTable goForwardButton, goBackwardButton, goLeftButton, goRightButton, servoButton, motorPWMButton;

    CheckBox showPasswordChechBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupUI(findViewById(R.id.settingsScrollView));

        dataProviderSQLite = new DataProviderSQLite(this);

        backImageView = findViewById(R.id.imageView7);
        ipAddressEditText = findViewById(R.id.ipAddressEditText);
        loginEditText = findViewById(R.id.loginEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        goForwardEditText = findViewById(R.id.goForwardEditText);
        goBackEditText = findViewById(R.id.goBackwardEditText);
        goLeftEditText = findViewById(R.id.goLeftEditText);
        goRightEditText = findViewById(R.id.goRightEditText);
        servoTurnEditText = findViewById(R.id.servoTurnEditText);
        motorPWMEditText = findViewById(R.id.motorPWMEditText);

        showPasswordChechBox = findViewById(R.id.showPasswordCheckbox);
        showPasswordChechBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else {
                    passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                passwordEditText.setSelection(passwordEditText.getText().length());
            }
        });

        /*raspberryVersionsExpandView = findViewById(R.id.raspberryVersionExpandView);
        raspberryVersionAdapter = new RaspberryVersionAdapter(this, dataProviderSQLite.getRaspberryVersionsList());
        raspberryVersionsExpandView.setAdapter(raspberryVersionAdapter);
        raspberryVersionsExpandView.setOnGroupClickListener(raspberryVersionAdapter);
        raspberryVersionsExpandView.setOnChildClickListener(versionClickListener);*/

        backImageView.setOnClickListener(backImageClickListener);

        setDataFromModel();
    }

    View.OnClickListener backImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    /*ExpandableListView.OnChildClickListener versionClickListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            raspberryVersionAdapter.setSelectedOption((RaspberryVersionTable) raspberryVersionAdapter.getChild(0, groupPosition));
            raspberryVersionAdapter.collapseList(parent, groupPosition);
            return true;
        }
    };*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) < 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
        }

        return super.onKeyDown(keyCode, event);
    }

    public void onBackPressed() {
        if(saveModel()) finish();
    }

    // ------------ Скрытие клавиатуры при нажатии не по EditText ------------
    public void setupUI(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard();
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }
    }

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if(view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void setDataFromModel() {
        currentSettingsTable = dataProviderSQLite.getSettings();

        if(currentSettingsTable == null) currentSettingsTable = new SettingsTable();

        ipAddressEditText.setText(currentSettingsTable.getIpAddress());
        loginEditText.setText(currentSettingsTable.getLogin());
        passwordEditText.setText(currentSettingsTable.getPassword());

        goForwardButton = dataProviderSQLite.getButtonTable("forward");
        goBackwardButton = dataProviderSQLite.getButtonTable("backward");
        goLeftButton = dataProviderSQLite.getButtonTable("left");
        goRightButton = dataProviderSQLite.getButtonTable("right");
        servoButton = dataProviderSQLite.getButtonTable("servo");
        motorPWMButton = dataProviderSQLite.getButtonTable("motorPWM");

        goForwardEditText.setText(String.valueOf(goForwardButton.getGpioNum()));
        goBackEditText.setText(String.valueOf(goBackwardButton.getGpioNum()));
        goLeftEditText.setText(String.valueOf(goLeftButton.getGpioNum()));
        goRightEditText.setText(String.valueOf(goRightButton.getGpioNum()));
        servoTurnEditText.setText(String.valueOf(servoButton.getGpioNum()));
        motorPWMEditText.setText(String.valueOf(motorPWMButton.getGpioNum()));

        //raspberryVersionAdapter.setSelectedOption(dataProviderSQLite.getRaspberryVersionTable(currentSettingsTable.getRespberryVersion()));
        //raspberryVersionsExpandView.invalidateViews();
    }

    public boolean saveModel() {

        String ipAddressText = ipAddressEditText.getText().toString();
        String loginText = loginEditText.getText().toString();
        String passwordText = passwordEditText.getText().toString();
        int goForwardGpio = Integer.parseInt(goForwardEditText.getText().toString());
        int goBackwardGpio = Integer.parseInt(goBackEditText.getText().toString());
        int goLeftGpio = Integer.parseInt(goLeftEditText.getText().toString());
        int goRightGpio = Integer.parseInt(goRightEditText.getText().toString());
        int servoTurnGpio = Integer.parseInt(servoTurnEditText.getText().toString());
        int motorPWMGpio = Integer.parseInt(motorPWMEditText.getText().toString());

        //int raspberryVersionId = raspberryVersionAdapter.getSelectedOption().getId();

        currentSettingsTable.setIpAddress(ipAddressText);
        currentSettingsTable.setLogin(loginText);
        currentSettingsTable.setPassword(passwordText);
        //currentSettingsTable.setRaspberryVersion(raspberryVersionId);
        goForwardButton.setGpioNum(goForwardGpio);
        goBackwardButton.setGpioNum(goBackwardGpio);
        goLeftButton.setGpioNum(goLeftGpio);
        goRightButton.setGpioNum(goRightGpio);
        servoButton.setGpioNum(servoTurnGpio);
        motorPWMButton.setGpioNum(motorPWMGpio);

        Log.d(TAG, "--------> settings: ipAddress = " + currentSettingsTable.getIpAddress() + " | login = " + currentSettingsTable.getLogin() + " | pass = " + currentSettingsTable.getPassword() + " | raspberryVer = " + currentSettingsTable.getRespberryVersion());
        Log.d(TAG, "--------> forwardButton: id = " + goForwardButton.getId() + " | name = " + goForwardButton.getName() + " | gpio = " + goForwardButton.getGpioNum());
        Log.d(TAG, "--------> backwardButton: id = " + goBackwardButton.getId() + " | name = " + goBackwardButton.getName() + " | gpio = " + goBackwardButton.getGpioNum());
        Log.d(TAG, "--------> leftButton: id = " + goLeftButton.getId() + " | name = " + goLeftButton.getName() + " | gpio = " + goLeftButton.getGpioNum());
        Log.d(TAG, "--------> rightButton: id = " + goRightButton.getId() + " | name = " + goRightButton.getName() + " | gpio = " + goRightButton.getGpioNum());

        if(dataProviderSQLite.updateSettingsTable(currentSettingsTable) > 0
                && dataProviderSQLite.updateButtonTable(goForwardButton) > 0
                && dataProviderSQLite.updateButtonTable(goBackwardButton) > 0
                && dataProviderSQLite.updateButtonTable(goLeftButton) > 0
                && dataProviderSQLite.updateButtonTable(goRightButton) > 0
                && dataProviderSQLite.updateButtonTable(servoButton) > 0
                && dataProviderSQLite.updateButtonTable(motorPWMButton) > 0) {
            Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
            return true;
        }
        else {
            Toast.makeText(this, "Error saving settings", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
