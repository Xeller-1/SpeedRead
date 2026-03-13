package com.example.speedread2.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "user_stats",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index(value = "user_id")
)
public class UserStats {
    @PrimaryKey(autoGenerate = true)
    public int id = 0;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "reading_speed", defaultValue = "0")
    public int readingSpeed = 0; // слов в минуту

    @ColumnInfo(name = "clarity", defaultValue = "0")
    public int clarity = 0; // четкость произношения (0-100)

    @ColumnInfo(name = "expression", defaultValue = "0")
    public int expression = 0; // выразительность (0-100)

    @ColumnInfo(name = "total_texts_read", defaultValue = "0")
    public int totalTextsRead = 0;

    @ColumnInfo(name = "total_practice_time", defaultValue = "0")
    public int totalPracticeTime = 0; // в минутах

    @ColumnInfo(name = "last_updated")
    public String lastUpdated = "";

    // Пустой конструктор для Room
    public UserStats() {
    }

    @Ignore
    public UserStats(int userId) {
        this.userId = userId;
    }

    // Геттеры
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getReadingSpeed() { return readingSpeed; }
    public int getClarity() { return clarity; }
    public int getExpression() { return expression; }
    public int getTotalTextsRead() { return totalTextsRead; }
    public int getTotalPracticeTime() { return totalPracticeTime; }
    public String getLastUpdated() { return lastUpdated; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setReadingSpeed(int readingSpeed) { this.readingSpeed = readingSpeed; }
    public void setClarity(int clarity) { this.clarity = clarity; }
    public void setExpression(int expression) { this.expression = expression; }
    public void setTotalTextsRead(int totalTextsRead) { this.totalTextsRead = totalTextsRead; }
    public void setTotalPracticeTime(int totalPracticeTime) { this.totalPracticeTime = totalPracticeTime; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
}

