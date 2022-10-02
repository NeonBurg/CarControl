package com.develop.elegant.carcontrol.DataSQLite.Models;

import android.database.Cursor;

public class SettingsTable {

    public static final String TABLE_NAME = "SettingsTable";

    public static final String COLUMN_IP_ADDRESS = "ip_address";
    public static final String COLUMN_REVISION = "revision";
    public static final String COLUMN_RASPBERRY_VERSION = "raspberry_version";
    public static final String COLUMN_LOGIN = "login";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_IS_SAVE_PASSWORD = "is_save_password";

    private int id;
    private String ip_address = "";
    private int revision = 2;
    private int raspberry_version = 1;
    private String login = "";
    private String password = "";
    private int is_save_password = 0;

    public SettingsTable(Cursor cursor) {
        setId(cursor.getInt(0));
        setIpAddress(cursor.getString(1));
        setRevision(cursor.getInt(2));
        setRaspberryVersion(cursor.getInt(3));
        setLogin(cursor.getString(4));
        setPassword(cursor.getString(5));
    }

    public SettingsTable() {

    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIpAddress(String ip_address) { this.ip_address = ip_address; }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public void setRaspberryVersion(int raspberry_version) { this.raspberry_version = raspberry_version; }

    public void setLogin(String login) { this.login = login; }

    public void setPassword(String password) { this.password = password; }

    public void setIsSavePassword(boolean is_save_password) { this.is_save_password = is_save_password ? 1 : 0; }

    // --------------------------------------------------------------

    public int getId() { return this.id; }

    public String getIpAddress() { return this.ip_address; }

    public int getRevision() { return this.revision; }

    public int getRespberryVersion() { return this.raspberry_version; }

    public String getLogin() { return this.login; }

    public String getPassword() { return this.password; }

    public boolean getIsSavePassword() { return this.is_save_password != 0; }

}
