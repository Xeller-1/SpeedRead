package com.example.speedread2.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.speedread2.R;
import com.example.speedread2.utils.BackgroundHelper;
import com.example.speedread2.dao.QuestionDao;
import com.example.speedread2.database.AppDatabase;
import com.example.speedread2.database.entities.Question;
import com.example.speedread2.database.entities.User;

import java.util.ArrayList;
import java.util.List;

public class ReadingResultsActivity extends AppCompatActivity {

    private static final String KEY_RESULT_PROCESSED = "key_result_processed";
    private static final String KEY_TEST_REWARD_PROCESSED = "key_test_reward_processed";

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
    private boolean isResultProcessed = false;
    private boolean isTestRewardProcessed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_results);

        BackgroundHelper.applyBackground(this);

        database = AppDatabase.getInstance(this);
        questionDao = database.questionDao();

        var textDao = database.textDao();

        textId = getIntent().getIntExtra("textId", -1);
        readingSpeed = getIntent().getIntExtra("readingSpeed", 0);
        clarity = getIntent().getIntExtra("clarity", 0);

        if (savedInstanceState != null) {
            isResultProcessed = savedInstanceState.getBoolean(KEY_RESULT_PROCESSED, false);
            isTestRewardProcessed = savedInstanceState.getBoolean(KEY_TEST_REWARD_PROCESSED, false);
        }

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

        tvReadingSpeed.setText(readingSpeed + " слов/мин");
        tvClarity.setText(clarity + "%");

        questions = questionDao.getQuestionsByTextId(textId);

        btnBack.setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int currentUserId = prefs.getInt("currentUserId", -1);

        if (!isResultProcessed) {
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
                    Toast.makeText(this, "За чтение начислено " + completedText.rewardCoins + " монет", Toast.LENGTH_LONG).show();
                }
            }
            isResultProcessed = true;
        }

        if (questions == null || questions.isEmpty()) {
            tvQuestionText.setVisibility(View.GONE);
            rgAnswers.setVisibility(View.GONE);
            btnNextQuestion.setVisibility(View.GONE);
            btnFinish.setVisibility(View.VISIBLE);
            btnFinish.setOnClickListener(v -> openMainScreen());
        } else {
            btnFinish.setVisibility(View.GONE);
            showQuestion(0);

            btnNextQuestion.setOnClickListener(v -> {
                if (!checkAnswer()) {
                    return;
                }
                if (currentQuestionIndex < questions.size() - 1) {
                    currentQuestionIndex++;
                    showQuestion(currentQuestionIndex);
                } else {
                    showFinalResults();
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_RESULT_PROCESSED, isResultProcessed);
        outState.putBoolean(KEY_TEST_REWARD_PROCESSED, isTestRewardProcessed);
    }

    private void showQuestion(int index) {
        if (index >= questions.size()) return;

        Question question = questions.get(index);
        tvQuestionText.setText(question.questionText);

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

        TextView tvComprehension = findViewById(R.id.tvComprehension);
        if (tvComprehension != null) {
            tvComprehension.setText(comprehension + "%");
            tvComprehension.setVisibility(View.VISIBLE);
        }

        int mistakes = questions.size() - correctAnswers;
        int testReward = 0;
        if (mistakes == 0) {
            testReward = 10;
        } else if (mistakes == 1) {
            testReward = 5;
        }

        if (!isTestRewardProcessed) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            int currentUserId = prefs.getInt("currentUserId", -1);
            User user = database.userDao().getUserById(currentUserId);
            if (user != null && testReward > 0) {
                database.userDao().updateCoins(currentUserId, user.coins + testReward);
                Toast.makeText(this, "За тест начислено " + testReward + " монет", Toast.LENGTH_LONG).show();
            } else if (user != null) {
                Toast.makeText(this, "За тест монеты не начислены", Toast.LENGTH_SHORT).show();
            }
            isTestRewardProcessed = true;
        }

        tvQuestionText.setVisibility(View.GONE);
        rgAnswers.setVisibility(View.GONE);
        btnNextQuestion.setVisibility(View.GONE);

        btnFinish.setVisibility(View.VISIBLE);
        btnFinish.setOnClickListener(v -> openMainScreen());

        Toast.makeText(this, "Правильных ответов: " + correctAnswers + " из " + questions.size(), Toast.LENGTH_SHORT).show();
    }

    private void openMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
