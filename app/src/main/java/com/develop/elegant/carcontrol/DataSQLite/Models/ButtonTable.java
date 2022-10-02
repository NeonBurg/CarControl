package com.develop.elegant.carcontrol.DataSQLite.Models;

import android.database.Cursor;

public class ButtonTable {

    public static final String TABLE_NAME = "ButtonTable";

    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_GPIO_NUM = "gpio_num";

    private int id;
    private String name;
    private int gpio_num;

    public ButtonTable(Cursor cursor) {
        setId(cursor.getInt(0));
        setName(cursor.getString(1));
        setGpioNum(cursor.getInt(2));
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGpioNum(int gpio_num) {
        this.gpio_num = gpio_num;
    }

    // -----------------------------------------------


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getGpioNum() { return this.gpio_num; }
}
