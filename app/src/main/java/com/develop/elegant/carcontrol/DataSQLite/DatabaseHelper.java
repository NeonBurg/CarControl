package com.develop.elegant.carcontrol.DataSQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.develop.elegant.carcontrol.AppConstants;
import com.develop.elegant.carcontrol.DataSQLite.Models.ButtonTable;
import com.develop.elegant.carcontrol.DataSQLite.Models.RaspberryVersionTable;
import com.develop.elegant.carcontrol.DataSQLite.Models.SettingsTable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "RaspberryCar.db";

    private static final String SQL_CREATE_SETTINGS_TABLE = "CREATE TABLE IF NOT EXISTS " + SettingsTable.TABLE_NAME + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            SettingsTable.COLUMN_IP_ADDRESS + " TEXT, " +
            SettingsTable.COLUMN_REVISION + " INTEGER, " +
            SettingsTable.COLUMN_RASPBERRY_VERSION + " INTEGER, " +
            SettingsTable.COLUMN_LOGIN + " TEXT, " +
            SettingsTable.COLUMN_PASSWORD + " TEXT, " +
            SettingsTable.COLUMN_IS_SAVE_PASSWORD + " INTEGER)";

    private static final String SQL_CREATE_RASPBERRY_VERSION_TABLE = "CREATE TABLE IF NOT EXISTS " + RaspberryVersionTable.TABLE_NAME + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            RaspberryVersionTable.COLUMN_NAME + " TEXT," +
            RaspberryVersionTable.COLUMN_YEAR + " TEXT)";

    private static final String SQL_CREATE_BUTTON_TABLE = "CREATE TABLE IF NOT EXISTS " + ButtonTable.TABLE_NAME + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ButtonTable.COLUMN_NAME + " TEXT, " +
            ButtonTable.COLUMN_GPIO_NUM + " INTEGER)";

    private static final String SQL_INSERT_SETTINGS = "INSERT INTO " + SettingsTable.TABLE_NAME + " (" + SettingsTable.COLUMN_IP_ADDRESS + ", " + SettingsTable.COLUMN_REVISION + ", "
                        + SettingsTable.COLUMN_RASPBERRY_VERSION + ", " + SettingsTable.COLUMN_LOGIN + ", " + SettingsTable.COLUMN_PASSWORD + ", " + SettingsTable.COLUMN_IS_SAVE_PASSWORD
                        + ") VALUES ('192.168.0.108:8000', 1,  1, 'pi', 'raspberry', 0)";

    private static final String SQL_INSERT_RASPBERRY_VERSIONS = "INSERT INTO " + RaspberryVersionTable.TABLE_NAME +
            "(" + RaspberryVersionTable.COLUMN_NAME + ", "+ RaspberryVersionTable.COLUMN_YEAR + ") VALUES" +
            "('" + AppConstants.RASPBERRY_VER_1 + "' , '" + AppConstants.RASPBERRY_VER_1_YEAR + "'), " +
            "('" + AppConstants.RASPBERRY_VER_2 + "', '" + AppConstants.RASPBERRY_VER_2_YEAR + "'), " +
            "('" + AppConstants.RASPBERRY_VER_3 + "', '" + AppConstants.RASPBERRY_VER_3_YEAR + "'), " +
            "('" + AppConstants.RASPBERRY_VER_4 + "', '" + AppConstants.RASPBERRY_VER_4_YEAR + "'), " +
            "('" + AppConstants.RASPBERRY_VER_5 + "', '" + AppConstants.RASPBERRY_VER_5_YEAR + "'), " +
            "('" + AppConstants.RASPBERRY_VER_6 + "', '" + AppConstants.RASPBERRY_VER_6_YEAR + "'), " +
            "('" + AppConstants.RASPBERRY_VER_7 + "', '" + AppConstants.RASPBERRY_VER_7_YEAR + "'), " +
            "('" + AppConstants.RASPBERRY_VER_8 + "', '" + AppConstants.RASPBERRY_VER_8_YEAR + "')";

    private static final String SQL_INSERT_BUTTONS = "INSERT INTO " + ButtonTable.TABLE_NAME +
            "(" + ButtonTable.COLUMN_NAME + ", "+ ButtonTable.COLUMN_GPIO_NUM +") VALUES" +
            "('forward', 20), " +
            "('backward', 21), " +
            "('left', 10), " +
            "('right', 9), " +
            "('servo', 17)," +
            "('motorPWM', 16)";

    private static final String SQL_INSERT_BUTTONS_v2 = "INSERT INTO " + ButtonTable.TABLE_NAME +
            "(" + ButtonTable.COLUMN_NAME + ", "+ ButtonTable.COLUMN_GPIO_NUM +") VALUES" +
            "('servo', 17)," +
            "('motorPWM', 16)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_SETTINGS_TABLE);
        db.execSQL(SQL_CREATE_RASPBERRY_VERSION_TABLE);
        db.execSQL(SQL_CREATE_BUTTON_TABLE);
        db.execSQL(SQL_INSERT_RASPBERRY_VERSIONS);
        db.execSQL(SQL_INSERT_BUTTONS);
        db.execSQL(SQL_INSERT_SETTINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "-----------> onUpgrade: oldVersion = " + oldVersion + " | newVersion = " + newVersion);
        if(oldVersion == 1) {
            upgradeVersion1(db);
        }
    }

    private void upgradeVersion1(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_INSERT_BUTTONS_v2);
    }
}
