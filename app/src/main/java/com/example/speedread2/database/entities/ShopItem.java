package com.example.speedread2.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "shop_items")
public class ShopItem {
    @PrimaryKey(autoGenerate = true)
    public int id = 0;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "price")
    public int price;

    @ColumnInfo(name = "image_res")
    public int imageRes;

    @ColumnInfo(name = "type")
    public String type = "background";

    @ColumnInfo(name = "is_purchased", defaultValue = "0")
    public int isPurchased = 0;

    // Пустой конструктор для Room
    public ShopItem() {
    }

    @Ignore
    public ShopItem(String name, String description, int price, int imageRes) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageRes = imageRes;
    }

    // Геттеры
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPrice() { return price; }
    public int getImageRes() { return imageRes; }
    public String getType() { return type; }
    public int getIsPurchased() { return isPurchased; }

    // Сеттеры
    public void setIsPurchased(int isPurchased) { this.isPurchased = isPurchased; }
}