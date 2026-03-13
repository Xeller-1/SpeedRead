package com.example.speedread2.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.speedread2.database.entities.Text;

import java.util.List;

@Dao
public interface TextDao {
    @Insert
    void insertText(Text text);

    @Insert
    void insertTexts(List<Text> texts);

    @Query("SELECT * FROM texts")
    List<Text> getAllTexts();

    @Query("SELECT * FROM texts WHERE id = :id LIMIT 1")
    Text getTextById(int id);

    @Query("SELECT * FROM texts WHERE category_id = :categoryId")
    List<Text> getTextsByCategory(int categoryId);

    @Query("SELECT * FROM texts WHERE category_id = :categoryId AND difficulty_level = :difficulty")
    List<Text> getTextsByCategoryAndDifficulty(int categoryId, int difficulty);
}

