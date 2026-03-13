package com.example.speedread2.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.speedread2.database.entities.Question;

import java.util.List;

@Dao
public interface QuestionDao {
    @Insert
    void insertQuestion(Question question);

    @Insert
    void insertQuestions(List<Question> questions);

    @Query("SELECT * FROM questions WHERE text_id = :textId LIMIT 2")
    List<Question> getQuestionsByTextId(int textId);

    @Query("SELECT * FROM questions WHERE text_id = :textId")
    List<Question> getAllQuestionsByTextId(int textId);
}

