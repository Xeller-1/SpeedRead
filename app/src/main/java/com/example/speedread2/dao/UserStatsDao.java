package com.example.speedread2.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.speedread2.database.entities.UserStats;

@Dao
public interface UserStatsDao {
    @Insert
    void insertUserStats(UserStats userStats);

    @Update
    void updateUserStats(UserStats userStats);

    @Query("SELECT * FROM user_stats WHERE user_id = :userId LIMIT 1")
    UserStats getUserStats(int userId);

    @Query("UPDATE user_stats SET reading_speed = :speed WHERE user_id = :userId")
    void updateReadingSpeed(int userId, int speed);

    @Query("UPDATE user_stats SET clarity = :clarity WHERE user_id = :userId")
    void updateClarity(int userId, int clarity);

    @Query("UPDATE user_stats SET expression = :expression WHERE user_id = :userId")
    void updateExpression(int userId, int expression);

    @Query("UPDATE user_stats SET total_texts_read = total_texts_read + 1 WHERE user_id = :userId")
    void incrementTextsRead(int userId);

    @Query("UPDATE user_stats SET total_practice_time = total_practice_time + :minutes WHERE user_id = :userId")
    void addPracticeTime(int userId, int minutes);
}

