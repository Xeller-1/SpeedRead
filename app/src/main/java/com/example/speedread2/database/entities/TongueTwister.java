package com.example.speedread2.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "tongue_twisters")
public class TongueTwister {
    @PrimaryKey(autoGenerate = true)
    public int id = 0;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "sounds")
    public String sounds;

    @ColumnInfo(name = "difficulty", defaultValue = "1")
    public int difficulty = 1;

    // Пустой конструктор для Room
    public TongueTwister() {
    }

    @Ignore
    public TongueTwister(String title, String content, String sounds, int difficulty) {
        this.title = title;
        this.content = content;
        this.sounds = sounds;
        this.difficulty = difficulty;
    }

    // Геттеры
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getSounds() { return sounds; }
    public int getDifficulty() { return difficulty; }
}