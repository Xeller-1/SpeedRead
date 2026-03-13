package com.example.speedread2.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "questions",
    foreignKeys = @ForeignKey(
        entity = Text.class,
        parentColumns = "id",
        childColumns = "text_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index(value = {"text_id"}, unique = false)}
)
public class Question {
    @PrimaryKey(autoGenerate = true)
    public int id = 0;

    @ColumnInfo(name = "text_id")
    public int textId;

    @ColumnInfo(name = "question_text")
    public String questionText;

    @ColumnInfo(name = "correct_answer")
    public String correctAnswer;

    @ColumnInfo(name = "option1")
    public String option1;

    @ColumnInfo(name = "option2")
    public String option2;

    @ColumnInfo(name = "option3")
    public String option3;

    // Пустой конструктор для Room
    public Question() {
    }

    @Ignore
    public Question(int textId, String questionText, String correctAnswer, 
                   String option1, String option2, String option3) {
        this.textId = textId;
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
    }

    // Геттеры
    public int getId() { return id; }
    public int getTextId() { return textId; }
    public String getQuestionText() { return questionText; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getOption1() { return option1; }
    public String getOption2() { return option2; }
    public String getOption3() { return option3; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setTextId(int textId) { this.textId = textId; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public void setOption1(String option1) { this.option1 = option1; }
    public void setOption2(String option2) { this.option2 = option2; }
    public void setOption3(String option3) { this.option3 = option3; }
}

