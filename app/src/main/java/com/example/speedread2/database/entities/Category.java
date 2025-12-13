package com.example.speedread2.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {
    @PrimaryKey(autoGenerate = true)
    public int id = 0;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "icon_res")
    public int iconRes;

    // Пустой конструктор для Room
    public Category() {
    }

    @Ignore
    public Category(String name, String description, int iconRes) {
        this.name = name;
        this.description = description;
        this.iconRes = iconRes;
    }

    // Геттеры
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getIconRes() { return iconRes; }
}