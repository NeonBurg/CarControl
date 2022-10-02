package com.develop.elegant.carcontrol.DataSQLite.Models;

import android.database.Cursor;

public class RaspberryVersionTable {

    public static final String TABLE_NAME = "RaspberryVersionTable";

    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_YEAR = "year";

    private int id;
    private String name;
    private String year;

    public RaspberryVersionTable(Cursor cursor) {
        setId(cursor.getInt(0));
        setName(cursor.getString(1));
        setYear(cursor.getString(2));
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) { this.name = name; }

    public void setYear(String year) { this.year = year; }

    // ------------------------------------------------------------

    public int getId() { return this.id; }

    public String getName() { return this.name; }

    public String getYear() { return this.year; }

}
