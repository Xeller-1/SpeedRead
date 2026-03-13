package com.example.speedread2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.speedread2.R;
import com.example.speedread2.database.AppDatabase;
import com.example.speedread2.dao.QuestionDao;
import com.example.speedread2.database.entities.Question;
import com.example.speedread2.database.entities.User;

import java.util.ArrayList;
import java.util.List;

public class ReadingResultsActivity extends AppCompatActivity {
    
    private AppDatabase database;
    private QuestionDao questionDao;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    
    private TextView tvReadingSpeed;
    private TextView tvClarity;
    private TextView tvQuestionText;
    private RadioGroup rgAnswers;
    private RadioButton rbOption1;
    private RadioButton rbOption2;
    private RadioButton rbOption3;
    private RadioButton rbOption4;
    private Button btnNextQuestion;
    private Button btnFinish;
    private ImageButton btnBack;
    
    private int textId;
    private int readingSpeed;
    private int clarity;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_results);
        
        database = AppDatabase.getInstance(this);
        questionDao = database.questionDao();

        var textDao = database.textDao();
        
        // Получаем данные из Intent
        textId = getIntent().getIntExtra("textId", -1);
        readingSpeed = getIntent().getIntExtra("readingSpeed", 0);
        clarity = getIntent().getIntExtra("clarity", 0);
        
        // Инициализация UI
        tvReadingSpeed = findViewById(R.id.tvReadingSpeed);
        tvClarity = findViewById(R.id.tvClarity);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        rgAnswers = findViewById(R.id.rgAnswers);
        rbOption1 = findViewById(R.id.rbOption1);
        rbOption2 = findViewById(R.id.rbOption2);
        rbOption3 = findViewById(R.id.rbOption3);
        rbOption4 = findViewById(R.id.rbOption4);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);
        btnFinish = findViewById(R.id.btnFinish);
        btnBack = findViewById(R.id.btnBack);
        
        // Отображаем результаты
        tvReadingSpeed.setText(String.valueOf(readingSpeed) + " слов/мин");
        tvClarity.setText(String.valueOf(clarity) + "%");
        
        // Загружаем вопросы
        questions = questionDao.getQuestionsByTextId(textId);
        
        btnBack.setOnClickListener(v -> finish());

        // Получаем ID текущего пользователя
        android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int currentUserId = prefs.getInt("currentUserId", -1);

        User user = database.userDao().getUserById(currentUserId);
        if (user != null) {
            var stats = database.userStatsDao().getUserStats(user.id);
            if (stats != null) {
                stats.clarity = Math.max(clarity, stats.clarity);
                stats.readingSpeed = Math.max(readingSpeed, stats.readingSpeed);
                stats.expression = Math.max(stats.expression, 0);
                database.userStatsDao().updateUserStats(stats);
            }

            var completedText = textDao.getTextById(textId);
            if (completedText != null) {
                database.userDao().updateCoins(currentUserId, user.coins + completedText.rewardCoins);
            }
        }

        
        if (questions == null || questions.isEmpty()) {
            // Нет вопросов - показываем только результаты
            tvQuestionText.setVisibility(android.view.View.GONE);
            rgAnswers.setVisibility(android.view.View.GONE);
            btnNextQuestion.setVisibility(android.view.View.GONE);
            btnFinish.setVisibility(android.view.View.VISIBLE);
            btnFinish.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        } else {
            // Есть вопросы - показываем первый
            btnFinish.setVisibility(android.view.View.GONE);
            showQuestion(0);
            
            btnNextQuestion.setOnClickListener(v -> {
                if (!checkAnswer()) {
                    return;
                }
                if (currentQuestionIndex < questions.size() - 1) {
                    currentQuestionIndex++;
                    showQuestion(currentQuestionIndex);
                } else {
                    // Все вопросы отвечены - показываем результаты
                    showFinalResults();
                }
            });
        }
    }
    
    private void showQuestion(int index) {
        if (index >= questions.size()) return;
        
        Question question = questions.get(index);
        tvQuestionText.setText(question.questionText);
        
        // Перемешиваем варианты ответов
        List<String> optionsList = new ArrayList<>();
        optionsList.add(question.correctAnswer);
        optionsList.add(question.option1);
        optionsList.add(question.option2);
        optionsList.add(question.option3);
        java.util.Collections.shuffle(optionsList);
        
        rbOption1.setText(optionsList.get(0));
        rbOption2.setText(optionsList.get(1));
        rbOption3.setText(optionsList.get(2));
        rbOption4.setText(optionsList.get(3));
        
        rgAnswers.clearCheck();
        
        if (index == questions.size() - 1) {
            btnNextQuestion.setText("Завершить");
        } else {
            btnNextQuestion.setText("Следующий вопрос");
        }
    }
    
    private boolean checkAnswer() {
        int selectedId = rgAnswers.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Выберите ответ", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        RadioButton selectedRb = findViewById(selectedId);
        Question question = questions.get(currentQuestionIndex);
        
        if (selectedRb.getText().toString().equals(question.correctAnswer)) {
            correctAnswers++;
        }

        return true;
    }
    
    private void showFinalResults() {
        int comprehension = 0;
        if (questions.size() > 0) {
            comprehension = (correctAnswers * 100) / questions.size();
        }
        
        // Обновляем TextView для понимания
        TextView tvComprehension = findViewById(R.id.tvComprehension);
        if (tvComprehension != null) {
            tvComprehension.setText(String.valueOf(comprehension) + "%");
            tvComprehension.setVisibility(android.view.View.VISIBLE);
        }
        
        // Скрываем вопросы
        tvQuestionText.setVisibility(android.view.View.GONE);
        rgAnswers.setVisibility(android.view.View.GONE);
        btnNextQuestion.setVisibility(android.view.View.GONE);
        
        // Показываем кнопку завершения - переход на главный экран
        btnFinish.setVisibility(android.view.View.VISIBLE);
        btnFinish.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        
        Toast.makeText(this, "Правильных ответов: " + correctAnswers + " из " + questions.size(), Toast.LENGTH_SHORT).show();
    }
}
