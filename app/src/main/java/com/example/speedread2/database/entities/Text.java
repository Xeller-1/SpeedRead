package com.example.speedread2.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "texts")
public class Text {
    @PrimaryKey(autoGenerate = true)
    public int id = 0;

    @ColumnInfo(name = "category_id")
    public int categoryId;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "lines")
    public String lines;

    @ColumnInfo(name = "author")
    public String author;

    @ColumnInfo(name = "difficulty_level", defaultValue = "1")
    public int difficultyLevel = 1;

    @ColumnInfo(name = "word_count")
    public int wordCount;

    @ColumnInfo(name = "reward_coins", defaultValue = "10")
    public int rewardCoins = 10;

    // Пустой конструктор для Room
    public Text() {
    }

    @Ignore
    public Text(int categoryId, String title, String content, String lines,
                String author, int difficultyLevel, int wordCount, int rewardCoins) {
        this.categoryId = categoryId;
        this.title = title;
        this.content = content;
        this.lines = lines;
        this.author = author;
        this.difficultyLevel = difficultyLevel;
        this.wordCount = wordCount;
        this.rewardCoins = rewardCoins;
    }

    // Геттеры
    public int getId() { return id; }
    public int getCategoryId() { return categoryId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getLines() { return lines; }
    public String getAuthor() { return author; }
    public int getDifficultyLevel() { return difficultyLevel; }
    public int getWordCount() { return wordCount; }
    public int getRewardCoins() { return rewardCoins; }
}