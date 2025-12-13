package com.example.speedread2.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id = 0;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "password")
    public String password;

    @ColumnInfo(name = "level", defaultValue = "1")
    public int level = 1;

    @ColumnInfo(name = "experience", defaultValue = "0")
    public int experience = 0;

    @ColumnInfo(name = "coins", defaultValue = "100")
    public int coins = 100;

    @ColumnInfo(name = "created_at")
    public String createdAt = "";

    // Пустой конструктор для Room
    public User() {
    }

    // Конструктор
    @Ignore
    public User(String username, String email, String password, String createdAt) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
    }

    // Геттеры
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getCoins() { return coins; }
    public String getCreatedAt() { return createdAt; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setLevel(int level) { this.level = level; }
    public void setExperience(int experience) { this.experience = experience; }
    public void setCoins(int coins) { this.coins = coins; }
}