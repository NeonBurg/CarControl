package com.develop.elegant.carcontrol.DataSQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.develop.elegant.carcontrol.DataSQLite.Models.ButtonTable;
import com.develop.elegant.carcontrol.DataSQLite.Models.RaspberryVersionTable;
import com.develop.elegant.carcontrol.DataSQLite.Models.SettingsTable;

import java.util.ArrayList;

public class DataProviderSQLite {

    private static volatile SQLiteDatabase db;

    private static volatile DatabaseHelper dbHelper;

    public DataProviderSQLite(Context context) {

        if (db == null) {
            synchronized (SQLiteDatabase.class) {
                if (dbHelper == null) {
                    dbHelper = new DatabaseHelper(context);
                }

                db = dbHelper.getWritableDatabase();
            }
            //dbHelper = new TestDatabaseHelper(context);
        }

    }

    // --------------- GET METHODS ----------------------
    public SettingsTable getSettings() {

        SettingsTable settingsTable = null;

        String[] projection = {
                "id",
                SettingsTable.COLUMN_IP_ADDRESS,
                SettingsTable.COLUMN_REVISION,
                SettingsTable.COLUMN_RASPBERRY_VERSION,
                SettingsTable.COLUMN_LOGIN,
                SettingsTable.COLUMN_PASSWORD,
                SettingsTable.COLUMN_IS_SAVE_PASSWORD
        };

        Cursor cursor = db.query(SettingsTable.TABLE_NAME, projection, null, null, null, null, null);

        if(cursor.moveToNext()) {
            settingsTable = new SettingsTable(cursor);
        }

        return settingsTable;
    }

    public ArrayList<RaspberryVersionTable> getRaspberryVersionsList() {

        ArrayList<RaspberryVersionTable> raspberryVersionsList = new ArrayList<>();

        String[] projection = {
                "id",
                RaspberryVersionTable.COLUMN_NAME,
                RaspberryVersionTable.COLUMN_YEAR
        };

        Cursor cursor = db.query(RaspberryVersionTable.TABLE_NAME, projection, null, null, null, null, null);

        while(cursor.moveToNext()) {
            raspberryVersionsList.add(new RaspberryVersionTable(cursor));
        }

        return raspberryVersionsList;
    }

    public RaspberryVersionTable getRaspberryVersionTable(int raspberryVersionId) {

        RaspberryVersionTable raspberryVersionTable = null;

        String[] projection = {
                "id",
                RaspberryVersionTable.COLUMN_NAME,
                RaspberryVersionTable.COLUMN_YEAR
        };

        String selection = "id = ?";
        String[] selectionArgs = { String.valueOf(raspberryVersionId) };

        Cursor cursor = db.query(RaspberryVersionTable.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

        if(cursor.moveToNext()) {
            raspberryVersionTable = new RaspberryVersionTable(cursor);
        }

        return raspberryVersionTable;

    }

    public ButtonTable getButtonTable(String button_name) {

        ButtonTable buttonTable = null;

        String[] projection = {
                "id",
                ButtonTable.COLUMN_NAME,
                ButtonTable.COLUMN_GPIO_NUM
        };

        String selection = ButtonTable.COLUMN_NAME + " = ?";
        String[] selectionArgs = { button_name };

        Cursor cursor = db.query(ButtonTable.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

        if(cursor.moveToNext()) {
            buttonTable = new ButtonTable(cursor);
        }

        return buttonTable;
    }

    // --------------- UPDATE METHODS ------------------
    public int updateSettingsTable(SettingsTable settings_table) {

        ContentValues values = new ContentValues();
        values.put(SettingsTable.COLUMN_IP_ADDRESS, settings_table.getIpAddress());
        values.put(SettingsTable.COLUMN_REVISION, settings_table.getRevision());
        values.put(SettingsTable.COLUMN_RASPBERRY_VERSION, settings_table.getRespberryVersion());
        values.put(SettingsTable.COLUMN_LOGIN, settings_table.getLogin());
        values.put(SettingsTable.COLUMN_PASSWORD, settings_table.getPassword());
        values.put(SettingsTable.COLUMN_IS_SAVE_PASSWORD, settings_table.getIsSavePassword());

        String selection = "id = ?";
        String[] selectionArgs = { String.valueOf(settings_table.getId()) };

        return db.update(SettingsTable.TABLE_NAME, values, selection, selectionArgs);
    }

    public int updateButtonTable(ButtonTable button_table) {

        ContentValues values = new ContentValues();
        values.put(ButtonTable.COLUMN_NAME, button_table.getName());
        values.put(ButtonTable.COLUMN_GPIO_NUM, button_table.getGpioNum());

        String selection = "id = ?";
        String[] selectionArgs = { String.valueOf(button_table.getId()) };

        return db.update(ButtonTable.TABLE_NAME, values, selection, selectionArgs);
    }
}
