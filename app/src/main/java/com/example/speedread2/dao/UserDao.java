package com.example.speedread2.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.speedread2.database.entities.User;

@Dao
public interface UserDao {
    @Insert
    long insertUser(User user);

    @Update
    void updateUser(User user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getUserById(int id);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User loginUser(String email, String password);

    @Query("UPDATE users SET coins = :coins WHERE id = :userId")
    void updateCoins(int userId, int coins);

    @Query("UPDATE users SET experience = :experience WHERE id = :userId")
    void updateExperience(int userId, int experience);

    @Query("UPDATE users SET level = :level WHERE id = :userId")
    void updateLevel(int userId, int level);
}

